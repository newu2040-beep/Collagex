package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.core.model.CollageProject
import com.example.core.model.ExportRecord
import com.example.core.model.UserPreferences
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CloudSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id),
        ),
    )

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        check(!uid.isNullOrBlank()) { "User must be signed in with Google before accessing Firestore." }
        return uid
    }

    suspend fun upsertUserProfile(displayName: String, email: String, photoUrl: String?): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Unauthenticated"))
        val path = "users/$uid"
        return try {
            val safeName = displayName.trim().ifBlank { "Creator" }.take(100)
            val safeEmail = email.trim().take(200)
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (existing.exists()) {
                val updateMap = mutableMapOf<String, Any>(
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                if (!photoUrl.isNullOrBlank()) {
                    updateMap["photoUrl"] = photoUrl.take(500)
                }
                docRef.update(updateMap).await()
            } else {
                val createMap = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                if (!photoUrl.isNullOrBlank()) {
                    createMap["photoUrl"] = photoUrl.take(500)
                }
                docRef.set(createMap).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeCloudProjects(userId: String): Flow<List<CollageProjectDoc>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Session user mismatch." }
        val path = "users/$uid/projects"
        emitAll(
            db.collection("users").document(uid).collection("projects")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CollageProjectDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                },
        )
    }

    suspend fun syncProject(project: CollageProject): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Unauthenticated"))
        val docId = sanitizeDocId(project.id)
        val path = "users/$uid/projects/$docId"
        return try {
            require(uid == requireUserId())
            val docRef = db.collection("users").document(uid).collection("projects").document(docId)
            val canvasMap = mapOf(
                "width" to project.canvas.width.coerceIn(1.0, 20000.0),
                "height" to project.canvas.height.coerceIn(1.0, 20000.0),
                "unit" to project.canvas.unit.name,
                "dpi" to project.canvas.dpi.coerceIn(36, 1200).toLong(),
                "aspectLocked" to project.canvas.aspectLocked,
                "resizeMode" to project.canvas.resizeMode.name,
                "orientation" to project.canvas.orientation.name,
            )
            val backgroundMap = mapOf(
                "type" to project.background.type.name,
                "primaryColor" to project.background.primaryColor.toLong(),
                "secondaryColor" to project.background.secondaryColor.toLong(),
                "textureIntensity" to project.background.textureIntensity.coerceIn(0f, 1f).toDouble(),
            )
            val borderMap = mapOf(
                "cornerRadius" to project.border.cornerRadius.coerceIn(0f, 120f).toDouble(),
                "spacing" to project.border.spacing.coerceIn(0f, 80f).toDouble(),
                "padding" to project.border.padding.coerceIn(0f, 120f).toDouble(),
                "color" to project.border.color.toLong(),
                "shadowEnabled" to project.border.shadowEnabled,
            )
            val summaries = project.layers.take(100).map { l ->
                buildMap<String, Any> {
                    put("layerId", l.id.take(64))
                    put("layerType", l.type.name)
                    put("zIndex", l.zIndex.toLong())
                    put("label", l.label.take(60))
                    if (l.sampleArtKey != null) put("sampleKey", l.sampleArtKey.take(40))
                }
            }
            val safeTitle = project.title.trim().ifBlank { "Untitled Collage" }.take(120)
            val existing = docRef.get().await()
            if (existing.exists()) {
                val remoteRev = existing.getLong("revision") ?: 0L
                val nextRev = maxOf(project.revision.toLong(), remoteRev + 1L).coerceAtMost(1_000_000L)
                docRef.update(
                    mapOf(
                        "title" to safeTitle,
                        "schemaVersion" to project.schemaVersion.coerceIn(1, 10).toLong(),
                        "layoutType" to project.layoutType.name,
                        "photoCount" to project.photoCount.coerceIn(0, 200).toLong(),
                        "favorite" to project.favorite,
                        "archived" to project.archived,
                        "revision" to nextRev,
                        "canvas" to canvasMap,
                        "background" to backgroundMap,
                        "border" to borderMap,
                        "layerSummaries" to summaries,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "title" to safeTitle,
                        "schemaVersion" to project.schemaVersion.coerceIn(1, 10).toLong(),
                        "layoutType" to project.layoutType.name,
                        "photoCount" to project.photoCount.coerceIn(0, 200).toLong(),
                        "favorite" to project.favorite,
                        "archived" to project.archived,
                        "revision" to project.revision.coerceIn(1, 1_000_000).toLong(),
                        "canvas" to canvasMap,
                        "background" to backgroundMap,
                        "border" to borderMap,
                        "layerSummaries" to summaries,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCloudProject(projectId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Unauthenticated"))
        val docId = sanitizeDocId(projectId)
        val path = "users/$uid/projects/$docId"
        return try {
            requireUserId()
            db.collection("users").document(uid).collection("projects").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeCloudExports(userId: String): Flow<List<ExportRecordDoc>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid/exports"
        emitAll(
            db.collection("users").document(uid).collection("exports")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ExportRecordDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                },
        )
    }

    suspend fun recordCloudExport(record: ExportRecord): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Unauthenticated"))
        val docId = sanitizeDocId(record.id)
        val path = "users/$uid/exports/$docId"
        return try {
            requireUserId()
            val payload = mapOf(
                "userId" to uid,
                "projectId" to sanitizeDocId(record.projectId),
                "projectTitle" to record.projectTitle.trim().ifBlank { "Collage" }.take(120),
                "format" to record.format.name,
                "widthPx" to record.widthPx.coerceIn(1, 20000).toLong(),
                "heightPx" to record.heightPx.coerceIn(1, 20000).toLong(),
                "dpi" to record.dpi.coerceIn(36, 1200).toLong(),
                "fileSizeBytes" to record.fileSizeBytes.coerceAtLeast(0L),
                "transparentBg" to record.transparentBg,
                "status" to record.status,
                "createdAt" to FieldValue.serverTimestamp(),
            )
            db.collection("users").document(uid).collection("exports").document(docId)
                .set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCloudExport(exportId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Unauthenticated"))
        val docId = sanitizeDocId(exportId)
        val path = "users/$uid/exports/$docId"
        return try {
            requireUserId()
            db.collection("users").document(uid).collection("exports").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun syncPreferences(prefs: UserPreferences): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Unauthenticated"))
        val path = "users/$uid/preferences/main"
        return try {
            requireUserId()
            val docRef = db.collection("users").document(uid).collection("preferences").document("main")
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "themePalette" to prefs.themePalette.name,
                        "darkMode" to prefs.darkMode.name,
                        "languageCode" to prefs.languageCode.take(10).ifBlank { "en" },
                        "notificationsEnabled" to prefs.notificationsEnabled,
                        "stripExifDefault" to prefs.stripExifDefault,
                        "defaultDpi" to prefs.defaultDpi.coerceIn(36, 1200).toLong(),
                        "defaultUnit" to prefs.defaultUnit.name,
                        "cloudAutoSync" to prefs.cloudAutoSync,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "themePalette" to prefs.themePalette.name,
                        "darkMode" to prefs.darkMode.name,
                        "languageCode" to prefs.languageCode.take(10).ifBlank { "en" },
                        "notificationsEnabled" to prefs.notificationsEnabled,
                        "stripExifDefault" to prefs.stripExifDefault,
                        "defaultDpi" to prefs.defaultDpi.coerceIn(36, 1200).toLong(),
                        "defaultUnit" to prefs.defaultUnit.name,
                        "cloudAutoSync" to prefs.cloudAutoSync,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteAllUserCloudData(): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Unauthenticated"))
        val rootPath = "users/$uid"
        return try {
            requireUserId()
            val userDoc = db.collection("users").document(uid)
            val projects = userDoc.collection("projects").get().await()
            projects.documents.forEach { it.reference.delete().await() }
            val exports = userDoc.collection("exports").get().await()
            exports.documents.forEach { it.reference.delete().await() }
            val prefs = userDoc.collection("preferences").get().await()
            prefs.documents.forEach { it.reference.delete().await() }
            userDoc.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, rootPath)
            Result.failure(e)
        }
    }

    private fun sanitizeDocId(raw: String): String {
        val cleaned = raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        return cleaned.ifBlank { "doc_${System.currentTimeMillis()}" }.take(128)
    }
}
