package com.example.data.repository

import com.example.core.database.CollageDatabase
import com.example.core.database.ExportEntity
import com.example.core.database.ProjectEntity
import com.example.core.database.StylePresetEntity
import com.example.core.designsystem.SampleArtwork
import com.example.core.model.BackgroundSpec
import com.example.core.model.BackgroundType
import com.example.core.model.BorderSpec
import com.example.core.model.CanvasOrientation
import com.example.core.model.CanvasSpec
import com.example.core.model.CollageLayer
import com.example.core.model.CollageProject
import com.example.core.model.DimensionUnit
import com.example.core.model.ExportFormat
import com.example.core.model.ExportRecord
import com.example.core.model.LayerType
import com.example.core.model.LayoutType
import com.example.core.model.ProjectJsonCodec
import com.example.core.model.ResizeMode
import com.example.core.model.StylePreset
import com.example.core.model.SyncState
import com.example.core.model.UserPreferences
import com.example.data.firebase.CloudSyncRepository
import com.example.data.firebase.CollageProjectDoc
import com.example.data.firebase.ExportRecordDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Local-first Repository coordinating Room (instant responsive source of truth) with
 * Firestore CloudSyncRepository (asynchronous cloud backup & cross-device metadata sync).
 */
class CollageRepository(
    private val database: CollageDatabase,
    private val cloudSync: CloudSyncRepository,
) {
    private val projectDao = database.projectDao()
    private val exportDao = database.exportDao()
    private val stylePresetDao = database.stylePresetDao()

    fun observeLocalProjects(ownerUid: String): Flow<List<CollageProject>> =
        projectDao.observeProjects(ownerUid).map { entities ->
            entities.map { entity ->
                ProjectJsonCodec.decodeProject(
                    id = entity.id,
                    ownerUid = entity.ownerUid,
                    title = entity.title,
                    createdAtMs = entity.createdAtMs,
                    updatedAtMs = entity.updatedAtMs,
                    favorite = entity.favorite,
                    archived = entity.archived,
                    revision = entity.revision,
                    syncState = SyncState.entries.firstOrNull { it.name == entity.syncState } ?: SyncState.LOCAL_ONLY,
                    cloudBackedUp = entity.cloudBackedUp,
                    payloadJson = entity.payloadJson,
                )
            }
        }

    fun observeCloudProjects(ownerUid: String): Flow<List<CollageProjectDoc>> =
        cloudSync.observeCloudProjects(ownerUid)

    fun observeLocalExports(ownerUid: String): Flow<List<ExportRecord>> =
        exportDao.observeExports(ownerUid).map { list ->
            list.map { e ->
                ExportRecord(
                    id = e.id,
                    projectId = e.projectId,
                    projectTitle = e.projectTitle,
                    format = ExportFormat.fromRaw(e.format),
                    widthPx = e.widthPx,
                    heightPx = e.heightPx,
                    dpi = e.dpi,
                    fileSizeBytes = e.fileSizeBytes,
                    transparentBg = e.transparentBg,
                    status = e.status,
                    localUri = e.localUri,
                    createdAtMs = e.createdAtMs,
                )
            }
        }

    fun observeCloudExports(ownerUid: String): Flow<List<ExportRecordDoc>> =
        cloudSync.observeCloudExports(ownerUid)

    fun observeStylePresets(ownerUid: String): Flow<List<StylePreset>> =
        stylePresetDao.observePresets(ownerUid).map { list ->
            list.map { e ->
                StylePreset(
                    id = e.id,
                    name = e.name,
                    border = BorderSpec(
                        cornerRadius = e.cornerRadius,
                        spacing = e.spacing,
                        padding = e.padding,
                        color = e.borderColor,
                        shadowEnabled = e.shadowEnabled,
                    ),
                    background = BackgroundSpec(
                        type = BackgroundType.fromRaw(e.bgType),
                        primaryColor = e.bgPrimaryColor,
                        secondaryColor = e.bgSecondaryColor,
                        textureIntensity = e.bgTextureIntensity,
                    ),
                )
            }
        }

    suspend fun ensureUserProfile(displayName: String, email: String, photoUrl: String?): Result<Unit> =
        cloudSync.upsertUserProfile(displayName, email, photoUrl)

    suspend fun saveProject(
        project: CollageProject,
        autoSyncCloud: Boolean,
    ): Result<CollageProject> {
        val updated = project.copy(
            updatedAtMs = System.currentTimeMillis(),
            revision = project.revision + 1,
            syncState = if (autoSyncCloud) SyncState.PENDING else SyncState.LOCAL_ONLY,
        )
        projectDao.upsertProject(updated.toEntity())

        if (!autoSyncCloud) {
            return Result.success(updated)
        }

        val syncResult = cloudSync.syncProject(updated)
        val finalProject = if (syncResult.isSuccess) {
            updated.copy(syncState = SyncState.SYNCED, cloudBackedUp = true)
        } else {
            updated.copy(syncState = SyncState.ERROR)
        }
        projectDao.upsertProject(finalProject.toEntity())
        return if (syncResult.isSuccess) Result.success(finalProject) else Result.success(finalProject)
    }

    /**
     * Merges remote cloud metadata into Room without overwriting newer local revisions.
     */
    suspend fun mergeCloudProjects(ownerUid: String, cloudDocs: List<CollageProjectDoc>) {
        for (doc in cloudDocs) {
            if (doc.id.isBlank()) continue
            val local = projectDao.getProjectById(doc.id)
            val remoteUpdatedMs = doc.updatedAt?.toDate()?.time ?: System.currentTimeMillis()
            if (local == null) {
                val restored = doc.toLocalProject(ownerUid, remoteUpdatedMs)
                projectDao.upsertProject(restored.toEntity())
            } else if (doc.revision.toInt() > local.revision && remoteUpdatedMs > local.updatedAtMs) {
                // Keep local photo URIs while updating remote metadata/canvas/border/background
                val decoded = ProjectJsonCodec.decodeProject(
                    id = local.id,
                    ownerUid = local.ownerUid,
                    title = doc.title.ifBlank { local.title },
                    createdAtMs = local.createdAtMs,
                    updatedAtMs = remoteUpdatedMs,
                    favorite = doc.favorite,
                    archived = doc.archived,
                    revision = doc.revision.toInt(),
                    syncState = SyncState.SYNCED,
                    cloudBackedUp = true,
                    payloadJson = local.payloadJson,
                )
                val merged = decoded.copy(
                    layoutType = LayoutType.fromRaw(doc.layoutType),
                    canvas = doc.extractCanvas(),
                    background = doc.extractBackground(decoded.background.imageUri),
                    border = doc.extractBorder(),
                )
                projectDao.upsertProject(merged.toEntity())
            }
        }
    }

    suspend fun deleteLocalProject(projectId: String) {
        projectDao.deleteProjectById(projectId)
    }

    suspend fun deleteCloudBackup(projectId: String): Result<Unit> {
        val result = cloudSync.deleteCloudProject(projectId)
        val local = projectDao.getProjectById(projectId)
        if (result.isSuccess && local != null) {
            projectDao.upsertProject(
                local.copy(cloudBackedUp = false, syncState = SyncState.LOCAL_ONLY.name),
            )
        }
        return result
    }

    suspend fun deleteProjectEverywhere(projectId: String): Result<Unit> {
        projectDao.deleteProjectById(projectId)
        return cloudSync.deleteCloudProject(projectId)
    }

    suspend fun recordExport(ownerUid: String, record: ExportRecord, syncCloud: Boolean): Result<Unit> {
        exportDao.upsertExport(
            ExportEntity(
                id = record.id,
                ownerUid = ownerUid,
                projectId = record.projectId,
                projectTitle = record.projectTitle,
                format = record.format.name,
                widthPx = record.widthPx,
                heightPx = record.heightPx,
                dpi = record.dpi,
                fileSizeBytes = record.fileSizeBytes,
                transparentBg = record.transparentBg,
                status = record.status,
                localUri = record.localUri,
                createdAtMs = record.createdAtMs,
            ),
        )
        return if (syncCloud) cloudSync.recordCloudExport(record) else Result.success(Unit)
    }

    suspend fun mergeCloudExports(ownerUid: String, cloudExports: List<ExportRecordDoc>) {
        for (doc in cloudExports) {
            if (doc.id.isBlank()) continue
            exportDao.upsertExport(
                ExportEntity(
                    id = doc.id,
                    ownerUid = ownerUid,
                    projectId = doc.projectId,
                    projectTitle = doc.projectTitle,
                    format = doc.format,
                    widthPx = doc.widthPx.toInt(),
                    heightPx = doc.heightPx.toInt(),
                    dpi = doc.dpi.toInt(),
                    fileSizeBytes = doc.fileSizeBytes,
                    transparentBg = doc.transparentBg,
                    status = doc.status,
                    localUri = null,
                    createdAtMs = doc.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun deleteExportRecord(exportId: String): Result<Unit> {
        exportDao.deleteExportById(exportId)
        return cloudSync.deleteCloudExport(exportId)
    }

    suspend fun saveStylePreset(ownerUid: String, name: String, border: BorderSpec, background: BackgroundSpec) {
        val id = "preset_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        stylePresetDao.upsertPreset(
            StylePresetEntity(
                id = id,
                ownerUid = ownerUid,
                name = name.trim().ifBlank { "Custom Style" }.take(40),
                cornerRadius = border.cornerRadius,
                spacing = border.spacing,
                padding = border.padding,
                borderColor = border.color,
                shadowEnabled = border.shadowEnabled,
                bgType = background.type.name,
                bgPrimaryColor = background.primaryColor,
                bgSecondaryColor = background.secondaryColor,
                bgTextureIntensity = background.textureIntensity,
                createdAtMs = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun syncPreferences(prefs: UserPreferences): Result<Unit> =
        cloudSync.syncPreferences(prefs)

    suspend fun deleteAllLocalData(ownerUid: String) {
        projectDao.deleteAllForUser(ownerUid)
        exportDao.deleteAllForUser(ownerUid)
    }

    suspend fun deleteAllCloudData(): Result<Unit> =
        cloudSync.deleteAllUserCloudData()

    fun createNewProject(
        ownerUid: String,
        title: String,
        photoUris: List<String>,
        layoutType: LayoutType = LayoutType.BENTO,
        canvasSpec: CanvasSpec = CanvasSpec(),
        useSamplePackIfEmpty: Boolean = false,
    ): CollageProject {
        val now = System.currentTimeMillis()
        val id = "proj_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val layers = if (photoUris.isNotEmpty()) {
            photoUris.mapIndexed { idx, uri ->
                CollageLayer(
                    id = "layer_${idx}_${UUID.randomUUID().toString().take(6)}",
                    type = LayerType.PHOTO,
                    zIndex = idx,
                    localUri = uri,
                    label = "Photo ${idx + 1}",
                )
            }
        } else if (useSamplePackIfEmpty) {
            listOf(
                SampleArtwork.PAGODA,
                SampleArtwork.ALPINE,
                SampleArtwork.BLOSSOMS,
                SampleArtwork.SUNSET,
                SampleArtwork.SAKURA_VALLEY,
                SampleArtwork.LAKE,
            ).take(6).mapIndexed { idx, key ->
                CollageLayer(
                    id = "sample_${idx}_${UUID.randomUUID().toString().take(6)}",
                    type = LayerType.PHOTO,
                    zIndex = idx,
                    sampleArtKey = key,
                    label = SampleArtwork.labelFor(key),
                )
            }
        } else {
            emptyList()
        }

        return CollageProject(
            id = id,
            ownerUid = ownerUid,
            title = title.ifBlank { "Spring Memories" },
            createdAtMs = now,
            updatedAtMs = now,
            layoutType = layoutType,
            canvas = canvasSpec,
            layers = layers,
            revision = 1,
            syncState = SyncState.LOCAL_ONLY,
        )
    }

    private fun CollageProject.toEntity(): ProjectEntity = ProjectEntity(
        id = id,
        ownerUid = ownerUid,
        title = title,
        createdAtMs = createdAtMs,
        updatedAtMs = updatedAtMs,
        favorite = favorite,
        archived = archived,
        revision = revision,
        syncState = syncState.name,
        cloudBackedUp = cloudBackedUp,
        payloadJson = ProjectJsonCodec.encodeProject(this),
    )

    private fun CollageProjectDoc.extractCanvas(): CanvasSpec = CanvasSpec(
        width = (canvas["width"] as? Number)?.toDouble() ?: 3000.0,
        height = (canvas["height"] as? Number)?.toDouble() ?: 2000.0,
        unit = DimensionUnit.fromRaw(canvas["unit"] as? String),
        dpi = (canvas["dpi"] as? Number)?.toInt() ?: 300,
        aspectLocked = (canvas["aspectLocked"] as? Boolean) ?: true,
        resizeMode = ResizeMode.fromRaw(canvas["resizeMode"] as? String),
        orientation = CanvasOrientation.fromRaw(canvas["orientation"] as? String),
    )

    private fun CollageProjectDoc.extractBackground(existingUri: String?): BackgroundSpec = BackgroundSpec(
        type = BackgroundType.fromRaw(background["type"] as? String),
        primaryColor = (background["primaryColor"] as? Number)?.toInt() ?: 0xFFFFFFFF.toInt(),
        secondaryColor = (background["secondaryColor"] as? Number)?.toInt() ?: 0xFFFDE6EC.toInt(),
        textureIntensity = (background["textureIntensity"] as? Number)?.toFloat() ?: 0f,
        imageUri = existingUri,
    )

    private fun CollageProjectDoc.extractBorder(): BorderSpec = BorderSpec(
        cornerRadius = (border["cornerRadius"] as? Number)?.toFloat() ?: 16f,
        spacing = (border["spacing"] as? Number)?.toFloat() ?: 8f,
        padding = (border["padding"] as? Number)?.toFloat() ?: 12f,
        color = (border["color"] as? Number)?.toInt() ?: 0xFFFFFFFF.toInt(),
        shadowEnabled = (border["shadowEnabled"] as? Boolean) ?: false,
    )

    private fun CollageProjectDoc.toLocalProject(ownerUid: String, updatedMs: Long): CollageProject {
        val restoredLayers = layerSummaries.mapIndexed { idx, summary ->
            val key = summary["sampleKey"] as? String
            val fallbackSample = key ?: SampleArtwork.AllKeys[idx % SampleArtwork.AllKeys.size]
            CollageLayer(
                id = (summary["layerId"] as? String) ?: "restored_$idx",
                type = LayerType.fromRaw(summary["layerType"] as? String),
                zIndex = (summary["zIndex"] as? Number)?.toInt() ?: idx,
                label = (summary["label"] as? String) ?: "Layer ${idx + 1}",
                sampleArtKey = if (LayerType.fromRaw(summary["layerType"] as? String) == LayerType.PHOTO) fallbackSample else null,
            )
        }
        return CollageProject(
            id = id,
            ownerUid = ownerUid,
            title = title.ifBlank { "Cloud Collage" },
            schemaVersion = schemaVersion.toInt(),
            createdAtMs = createdAt?.toDate()?.time ?: updatedMs,
            updatedAtMs = updatedMs,
            layoutType = LayoutType.fromRaw(layoutType),
            canvas = extractCanvas(),
            background = extractBackground(null),
            border = extractBorder(),
            layers = restoredLayers,
            favorite = favorite,
            archived = archived,
            revision = revision.toInt(),
            syncState = SyncState.SYNCED,
            cloudBackedUp = true,
        )
    }
}
