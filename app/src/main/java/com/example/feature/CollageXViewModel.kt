package com.example.feature

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.datastore.PreferencesDataStore
import com.example.core.designsystem.SampleArtwork
import com.example.core.model.BackgroundSpec
import com.example.core.model.BorderSpec
import com.example.core.model.CanvasMath
import com.example.core.model.CanvasSpec
import com.example.core.model.CollageLayer
import com.example.core.model.CollageProject
import com.example.core.model.ExportJobConfig
import com.example.core.model.ExportRecord
import com.example.core.model.LayerType
import com.example.core.model.LayoutType
import com.example.core.model.PhotoAdjustments
import com.example.core.model.PhotoFitMode
import com.example.core.model.ResizeMode
import com.example.core.model.StickerKind
import com.example.core.model.StylePreset
import com.example.core.model.TextStyleSpec
import com.example.core.model.UiState
import com.example.core.model.UserPreferences
import com.example.data.repository.CollageRepository
import com.example.engine.AutoFitEngine
import com.example.engine.AutoFitPreset
import com.example.engine.CollageExportEngine
import com.example.engine.ExportOutputResult
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface ExportUiState {
    data object Idle : ExportUiState
    data class Running(val progress: Float, val statusMessage: String) : ExportUiState
    data class Success(val output: ExportOutputResult) : ExportUiState
    data class Error(val message: String) : ExportUiState
}

data class EditorUiState(
    val project: CollageProject? = null,
    val selectedLayerId: String? = null,
    val swapSourceLayerId: String? = null,
    val showGuides: Boolean = false,
    val autoFitEnabled: Boolean = true,
    val autoFitPreset: AutoFitPreset = AutoFitPreset.BALANCED,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val saveStatus: String = "Saved",
)

data class ToastMessage(
    val text: String,
    val isError: Boolean = false,
)

class CollageXViewModel(
    private val currentUserId: String,
    val userDisplayName: String,
    val userEmail: String,
    val userPhotoUrl: String?,
    private val repository: CollageRepository,
    private val preferencesStore: PreferencesDataStore,
    private val exportEngine: CollageExportEngine,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesStore.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = UserPreferences(),
        )

    val projectsState: StateFlow<UiState<List<CollageProject>>> =
        repository.observeLocalProjects(currentUserId)
            .map<List<CollageProject>, UiState<List<CollageProject>>> { UiState.Success(it) }
            .catch { error ->
                if (error is CancellationException) throw error
                emit(UiState.Error(error.localizedMessage ?: "Failed to load projects"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading,
            )

    val exportsState: StateFlow<UiState<List<ExportRecord>>> =
        repository.observeLocalExports(currentUserId)
            .map<List<ExportRecord>, UiState<List<ExportRecord>>> { UiState.Success(it) }
            .catch { error ->
                if (error is CancellationException) throw error
                emit(UiState.Error(error.localizedMessage ?: "Failed to load export history"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading,
            )

    val stylePresets: StateFlow<List<StylePreset>> =
        repository.observeStylePresets(currentUserId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = emptyList(),
            )

    private val _cloudSyncError = MutableStateFlow<String?>(null)
    val cloudSyncError: StateFlow<String?> = _cloudSyncError.asStateFlow()

    private val _editorState = MutableStateFlow(EditorUiState())
    val editorState: StateFlow<EditorUiState> = _editorState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    private val _toast = MutableStateFlow<ToastMessage?>(null)
    val toast: StateFlow<ToastMessage?> = _toast.asStateFlow()

    private val undoStack = ArrayDeque<CollageProject>()
    private val redoStack = ArrayDeque<CollageProject>()
    private var autosaveJob: Job? = null
    private var activeExportJob: Job? = null
    private var toastJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureUserProfile(userDisplayName, userEmail, userPhotoUrl)
        }
        observeCloudSyncStreams()
    }

    private fun observeCloudSyncStreams() {
        viewModelScope.launch {
            repository.observeCloudProjects(currentUserId)
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val root = firestoreError ?: error
                    Log.w(TAG, "Cloud project sync paused", root)
                    _cloudSyncError.value = root.localizedMessage ?: "Cloud sync unavailable offline"
                }
                .collect { docs ->
                    _cloudSyncError.value = null
                    repository.mergeCloudProjects(currentUserId, docs)
                }
        }
        viewModelScope.launch {
            repository.observeCloudExports(currentUserId)
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w(TAG, "Cloud export sync paused", firestoreError ?: error)
                }
                .collect { docs ->
                    repository.mergeCloudExports(currentUserId, docs)
                }
        }
    }

    fun showToast(message: String, isError: Boolean = false) {
        toastJob?.cancel()
        _toast.value = ToastMessage(message, isError)
        toastJob = viewModelScope.launch {
            delay(2800)
            _toast.value = null
        }
    }

    fun createProjectFromPhotos(
        uris: List<Uri>,
        title: String = "New Collage",
        layoutType: LayoutType = LayoutType.BENTO,
        canvasSpec: CanvasSpec? = null,
        useSamplePack: Boolean = false,
        autoFitPreset: AutoFitPreset? = AutoFitPreset.BALANCED,
        preserveRequestedLayout: Boolean = false,
        onCreated: (CollageProject) -> Unit,
    ) {
        val prefs = preferences.value
        val spec = canvasSpec ?: CanvasSpec(
            width = 3000.0,
            height = 2000.0,
            unit = prefs.defaultUnit,
            dpi = prefs.defaultDpi,
        )
        val rawProject = repository.createNewProject(
            ownerUid = currentUserId,
            title = title,
            photoUris = uris.map { it.toString() },
            layoutType = layoutType,
            canvasSpec = spec,
            useSamplePackIfEmpty = useSamplePack,
        )
        val project = if (autoFitPreset != null) {
            AutoFitEngine.applyToProject(
                project = rawProject,
                preset = autoFitPreset,
                targetCanvas = spec,
                preserveLayout = preserveRequestedLayout,
            )
        } else {
            rawProject
        }
        openProjectInEditor(
            project = project,
            autoFitEnabled = autoFitPreset != null,
            autoFitPreset = autoFitPreset ?: AutoFitPreset.BALANCED,
        )
        viewModelScope.launch {
            repository.saveProject(project, autoSyncCloud = prefs.cloudAutoSync)
        }
        onCreated(project)
    }

    fun openProjectInEditor(
        project: CollageProject,
        autoFitEnabled: Boolean = true,
        autoFitPreset: AutoFitPreset = AutoFitPreset.BALANCED,
    ) {
        undoStack.clear()
        redoStack.clear()
        _editorState.value = EditorUiState(
            project = project,
            selectedLayerId = project.layers.firstOrNull()?.id,
            swapSourceLayerId = null,
            autoFitEnabled = autoFitEnabled,
            autoFitPreset = autoFitPreset,
            canUndo = false,
            canRedo = false,
            saveStatus = "Saved",
        )
    }

    private fun commitProjectEdit(
        pushUndo: Boolean = true,
        transform: (CollageProject) -> CollageProject,
    ) {
        val current = _editorState.value.project ?: return
        val next = transform(current)
        if (next == current) return

        if (pushUndo) {
            undoStack.addLast(current)
            if (undoStack.size > MAX_UNDO_STEPS) undoStack.removeFirst()
            redoStack.clear()
        }

        _editorState.update { state ->
            state.copy(
                project = next,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                saveStatus = "Saving…",
            )
        }
        scheduleAutosave(next)
    }

    private fun scheduleAutosave(project: CollageProject) {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(450)
            val result = repository.saveProject(project, autoSyncCloud = preferences.value.cloudAutoSync)
            val saved = result.getOrNull() ?: project
            _editorState.update { state ->
                if (state.project?.id == saved.id) {
                    state.copy(
                        project = state.project.copy(
                            revision = saved.revision,
                            syncState = saved.syncState,
                            cloudBackedUp = saved.cloudBackedUp,
                        ),
                        saveStatus = if (saved.cloudBackedUp) "Synced" else "Saved",
                    )
                } else {
                    state
                }
            }
        }
    }

    fun undo() {
        val current = _editorState.value.project ?: return
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeLast()
        redoStack.addLast(current)
        _editorState.update {
            it.copy(
                project = previous,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                saveStatus = "Saving…",
            )
        }
        scheduleAutosave(previous)
    }

    fun redo() {
        val current = _editorState.value.project ?: return
        if (redoStack.isEmpty()) return
        val restored = redoStack.removeLast()
        undoStack.addLast(current)
        _editorState.update {
            it.copy(
                project = restored,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                saveStatus = "Saving…",
            )
        }
        scheduleAutosave(restored)
    }

    fun selectLayer(layerId: String?) {
        val swapFrom = _editorState.value.swapSourceLayerId
        if (swapFrom != null && layerId != null && swapFrom != layerId) {
            swapPhotoLayers(swapFrom, layerId)
            _editorState.update { it.copy(swapSourceLayerId = null, selectedLayerId = layerId) }
            showToast("Photos swapped")
            return
        }
        _editorState.update { it.copy(selectedLayerId = layerId) }
    }

    fun toggleSwapMode() {
        val selected = _editorState.value.selectedLayerId
        if (selected == null) {
            showToast("Select a photo first to swap")
            return
        }
        _editorState.update {
            val nextSwap = if (it.swapSourceLayerId == null) selected else null
            it.copy(swapSourceLayerId = nextSwap)
        }
        if (_editorState.value.swapSourceLayerId != null) {
            showToast("Tap another photo cell to swap")
        }
    }

    fun toggleGuides() {
        _editorState.update { it.copy(showGuides = !it.showGuides) }
    }

    fun updateLayoutType(layoutType: LayoutType) {
        commitProjectEdit { it.copy(layoutType = layoutType) }
    }

    fun updateBorder(transform: (BorderSpec) -> BorderSpec) {
        commitProjectEdit { it.copy(border = transform(it.border)) }
    }

    fun updateBackground(transform: (BackgroundSpec) -> BackgroundSpec) {
        commitProjectEdit { it.copy(background = transform(it.background)) }
    }

    fun applyStylePreset(preset: StylePreset) {
        commitProjectEdit {
            it.copy(border = preset.border, background = preset.background)
        }
        showToast("Applied '${preset.name}'")
    }

    fun saveCurrentStylePreset(name: String) {
        val proj = _editorState.value.project ?: return
        viewModelScope.launch {
            repository.saveStylePreset(currentUserId, name, proj.border, proj.background)
            showToast("Saved style preset")
        }
    }

    fun addPhotosToProject(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val autoFit = _editorState.value.autoFitEnabled
        val preset = _editorState.value.autoFitPreset
        commitProjectEdit { proj ->
            val maxZ = proj.layers.maxOfOrNull { it.zIndex } ?: -1
            val added = uris.mapIndexed { idx, uri ->
                CollageLayer(
                    id = "photo_${UUID.randomUUID().toString().take(8)}",
                    type = LayerType.PHOTO,
                    zIndex = maxZ + 1 + idx,
                    localUri = uri.toString(),
                    label = "Photo ${proj.photoCount + idx + 1}",
                )
            }
            val updated = proj.copy(layers = proj.layers + added)
            if (autoFit) {
                AutoFitEngine.applyToProject(updated, preset)
            } else {
                updated
            }
        }
        showToast(
            if (autoFit) "Added ${uris.size} photo${if (uris.size == 1) "" else "s"} · Auto-fitted"
            else "Added ${uris.size} photo${if (uris.size == 1) "" else "s"}",
        )
    }

    fun addSamplePhotos(count: Int) {
        val safeCount = count.coerceIn(1, 50)
        val autoFit = _editorState.value.autoFitEnabled
        val preset = _editorState.value.autoFitPreset
        commitProjectEdit { proj ->
            val maxZ = proj.layers.maxOfOrNull { it.zIndex } ?: -1
            val added = List(safeCount) { idx ->
                val key = SampleArtwork.AllKeys[(proj.photoCount + idx) % SampleArtwork.AllKeys.size]
                CollageLayer(
                    id = "sample_${UUID.randomUUID().toString().take(8)}",
                    type = LayerType.PHOTO,
                    zIndex = maxZ + 1 + idx,
                    sampleArtKey = key,
                    label = "${SampleArtwork.labelFor(key)} ${proj.photoCount + idx + 1}",
                )
            }
            val updated = proj.copy(layers = proj.layers + added)
            if (autoFit) {
                AutoFitEngine.applyToProject(updated, preset)
            } else {
                updated
            }
        }
        showToast(
            if (autoFit) "Added $safeCount sample photo${if (safeCount == 1) "" else "s"} · Auto-fitted"
            else "Added $safeCount sample photo${if (safeCount == 1) "" else "s"}",
        )
    }

    fun setAutoFitEnabled(enabled: Boolean) {
        _editorState.update { it.copy(autoFitEnabled = enabled) }
        showToast(
            if (enabled) "Auto-Fit ON (new photos & resizes auto-organize)"
            else "Manual mode (spacing, borders & layout stay as you set)",
        )
    }

    fun applyAutoFit(preset: AutoFitPreset, targetCanvas: CanvasSpec? = null) {
        _editorState.update { it.copy(autoFitPreset = preset) }
        commitProjectEdit { proj ->
            AutoFitEngine.applyToProject(
                project = proj,
                preset = preset,
                targetCanvas = targetCanvas,
            )
        }
        val proj = _editorState.value.project
        if (proj != null) {
            val dims = CanvasMath.toPixelDimensions(proj.canvas)
            showToast("Auto-Fit (${preset.label}) applied · ${dims.widthPx}×${dims.heightPx}px")
        }
    }

    fun replaceSelectedPhoto(uri: Uri) {
        val targetId = _editorState.value.selectedLayerId ?: return
        updateLayer(targetId) {
            it.copy(localUri = uri.toString(), sampleArtKey = null)
        }
        showToast("Photo replaced")
    }

    fun updateLayer(layerId: String, pushUndo: Boolean = true, transform: (CollageLayer) -> CollageLayer) {
        commitProjectEdit(pushUndo = pushUndo) { proj ->
            proj.copy(
                layers = proj.layers.map { l ->
                    if (l.id == layerId && !l.locked) transform(l) else l
                },
            )
        }
    }

    fun toggleLayerLock(layerId: String) {
        val target = _editorState.value.project?.layers?.firstOrNull { it.id == layerId } ?: return
        val nextLocked = !target.locked
        commitProjectEdit { proj ->
            proj.copy(layers = proj.layers.map { if (it.id == layerId) it.copy(locked = nextLocked) else it })
        }
        val kindName = when (target.type) {
            LayerType.PHOTO -> "Photo"
            LayerType.TEXT -> "Text"
            LayerType.STICKER -> "Sticker"
        }
        showToast(
            if (nextLocked) "$kindName protected (Don't Delete)"
            else "$kindName unlocked for editing & deleting",
        )
    }

    fun toggleLayerVisibility(layerId: String) {
        commitProjectEdit { proj ->
            proj.copy(layers = proj.layers.map { if (it.id == layerId) it.copy(visible = !it.visible) else it })
        }
    }

    fun duplicateLayer(layerId: String) {
        commitProjectEdit { proj ->
            val src = proj.layers.firstOrNull { it.id == layerId } ?: return@commitProjectEdit proj
            val maxZ = proj.layers.maxOfOrNull { it.zIndex } ?: 0
            val copy = src.copy(
                id = "dup_${UUID.randomUUID().toString().take(8)}",
                zIndex = maxZ + 1,
                normX = (src.normX + 0.04f).coerceAtMost(0.9f),
                normY = (src.normY + 0.04f).coerceAtMost(0.9f),
                locked = false,
            )
            proj.copy(layers = proj.layers + copy)
        }
        showToast("Layer duplicated")
    }

    fun deleteLayer(layerId: String, forceUnlock: Boolean = false): Boolean {
        val target = _editorState.value.project?.layers?.firstOrNull { it.id == layerId } ?: return false
        val kindName = when (target.type) {
            LayerType.PHOTO -> "Photo"
            LayerType.TEXT -> "Text"
            LayerType.STICKER -> "Sticker"
        }
        if (target.locked && !forceUnlock) {
            showToast("$kindName is protected (Don't Delete). Unlock first to remove.")
            return false
        }
        commitProjectEdit { proj ->
            proj.copy(layers = proj.layers.filterNot { it.id == layerId })
        }
        if (_editorState.value.selectedLayerId == layerId) {
            _editorState.update { it.copy(selectedLayerId = it.project?.layers?.firstOrNull()?.id) }
        }
        showToast("$kindName deleted · Tap Undo to restore")
        return true
    }

    fun resetSelectedLayer() {
        val id = _editorState.value.selectedLayerId ?: return
        updateLayer(id) {
            it.copy(
                scale = 1f,
                panX = 0f,
                panY = 0f,
                rotationDeg = 0f,
                flipHorizontal = false,
                flipVertical = false,
                cornerRadiusOverride = null,
                opacity = 1f,
                fitMode = PhotoFitMode.COVER,
                adjustments = PhotoAdjustments(),
            )
        }
        showToast("Layer reset")
    }

    fun moveLayerOrder(layerId: String, delta: Int) {
        commitProjectEdit { proj ->
            val sorted = proj.layers.sortedBy { it.zIndex }.toMutableList()
            val idx = sorted.indexOfFirst { it.id == layerId }
            val targetIdx = (idx + delta).coerceIn(0, sorted.lastIndex)
            if (idx == -1 || idx == targetIdx) return@commitProjectEdit proj
            val item = sorted.removeAt(idx)
            sorted.add(targetIdx, item)
            proj.copy(layers = sorted.mapIndexed { i, l -> l.copy(zIndex = i) })
        }
    }

    private fun swapPhotoLayers(idA: String, idB: String) {
        commitProjectEdit { proj ->
            val layerA = proj.layers.firstOrNull { it.id == idA } ?: return@commitProjectEdit proj
            val layerB = proj.layers.firstOrNull { it.id == idB } ?: return@commitProjectEdit proj
            proj.copy(
                layers = proj.layers.map { l ->
                    when (l.id) {
                        idA -> l.copy(zIndex = layerB.zIndex)
                        idB -> l.copy(zIndex = layerA.zIndex)
                        else -> l
                    }
                },
            )
        }
    }

    fun addTextLayer(style: TextStyleSpec) {
        val newId = "text_${UUID.randomUUID().toString().take(8)}"
        commitProjectEdit { proj ->
            val maxZ = proj.layers.maxOfOrNull { it.zIndex } ?: 0
            val layer = CollageLayer(
                id = newId,
                type = LayerType.TEXT,
                zIndex = maxZ + 1,
                normX = 0.5f,
                normY = 0.5f,
                label = style.text.take(24),
                textStyle = style,
            )
            proj.copy(layers = proj.layers + layer)
        }
        _editorState.update { it.copy(selectedLayerId = newId) }
    }

    fun addStickerLayer(kind: StickerKind, color: Int) {
        val newId = "sticker_${UUID.randomUUID().toString().take(8)}"
        commitProjectEdit { proj ->
            val maxZ = proj.layers.maxOfOrNull { it.zIndex } ?: 0
            val layer = CollageLayer(
                id = newId,
                type = LayerType.STICKER,
                zIndex = maxZ + 1,
                normX = 0.5f,
                normY = 0.45f,
                label = kind.label,
                stickerKind = kind,
                stickerColor = color,
            )
            proj.copy(layers = proj.layers + layer)
        }
        _editorState.update { it.copy(selectedLayerId = newId) }
    }

    /**
     * Core Differentiator: Post-creation custom canvas resize applied non-destructively
     * to the active project with optional Auto-Fit spacing/border/corner organization and Undo/Redo.
     */
    fun applyCustomCanvasSize(newSpec: CanvasSpec, autoFit: Boolean = true) {
        val preset = _editorState.value.autoFitPreset
        commitProjectEdit { proj ->
            if (autoFit) {
                AutoFitEngine.applyToProject(
                    project = proj,
                    preset = preset,
                    targetCanvas = newSpec,
                )
            } else {
                val adaptedLayers = CanvasMath.adaptLayersForResize(
                    layers = proj.layers,
                    oldSpec = proj.canvas,
                    newSpec = newSpec,
                    mode = newSpec.resizeMode,
                )
                proj.copy(
                    canvas = newSpec,
                    layers = adaptedLayers,
                )
            }
        }
        val dims = CanvasMath.toPixelDimensions(newSpec)
        showToast(
            if (autoFit) "Auto-fitted to ${dims.widthPx} × ${dims.heightPx}px"
            else "Canvas resized to ${dims.widthPx} × ${dims.heightPx}px",
        )
    }

    // --- Project Management Actions ---

    fun renameProject(project: CollageProject, newTitle: String) {
        val clean = newTitle.trim().take(120)
        if (clean.isBlank()) return
        viewModelScope.launch {
            val saved = repository.saveProject(project.copy(title = clean), preferences.value.cloudAutoSync)
            if (_editorState.value.project?.id == project.id) {
                _editorState.update { it.copy(project = it.project?.copy(title = clean)) }
            }
            if (saved.isSuccess) showToast("Renamed to \"$clean\"")
        }
    }

    fun duplicateProject(project: CollageProject) {
        val now = System.currentTimeMillis()
        val copy = project.copy(
            id = "proj_${UUID.randomUUID().toString().replace("-", "").take(16)}",
            title = "${project.title} (Copy)".take(120),
            createdAtMs = now,
            updatedAtMs = now,
            revision = 1,
            cloudBackedUp = false,
        )
        viewModelScope.launch {
            repository.saveProject(copy, preferences.value.cloudAutoSync)
            showToast("Project duplicated")
        }
    }

    fun toggleFavorite(project: CollageProject) {
        viewModelScope.launch {
            repository.saveProject(project.copy(favorite = !project.favorite), preferences.value.cloudAutoSync)
        }
    }

    fun toggleArchive(project: CollageProject) {
        viewModelScope.launch {
            repository.saveProject(project.copy(archived = !project.archived), preferences.value.cloudAutoSync)
            showToast(if (!project.archived) "Archived project" else "Restored from archive")
        }
    }

    fun deleteLocalProjectOnly(projectId: String) {
        viewModelScope.launch {
            repository.deleteLocalProject(projectId)
            showToast("Removed local copy")
        }
    }

    fun deleteCloudBackupOnly(projectId: String) {
        viewModelScope.launch {
            val res = repository.deleteCloudBackup(projectId)
            if (res.isSuccess) showToast("Deleted cloud backup")
            else showToast("Could not delete cloud backup", isError = true)
        }
    }

    fun deleteProjectEverywhere(projectId: String) {
        viewModelScope.launch {
            repository.deleteProjectEverywhere(projectId)
            showToast("Deleted project locally and in cloud")
        }
    }

    // --- Export Pipeline ---

    fun startExport(config: ExportJobConfig, customTargetUri: Uri? = null) {
        val project = _editorState.value.project ?: return
        activeExportJob?.cancel()
        activeExportJob = viewModelScope.launch {
            _exportState.value = ExportUiState.Running(0.02f, "Starting export…")
            try {
                val output = exportEngine.exportProject(
                    project = project,
                    config = config,
                    customTargetUri = customTargetUri,
                ) { progress, msg ->
                    _exportState.value = ExportUiState.Running(progress, msg)
                }
                val record = ExportRecord(
                    id = "exp_${UUID.randomUUID().toString().replace("-", "").take(16)}",
                    projectId = project.id,
                    projectTitle = project.title,
                    format = config.format,
                    widthPx = output.widthPx,
                    heightPx = output.heightPx,
                    dpi = project.canvas.dpi,
                    fileSizeBytes = output.fileSizeBytes,
                    transparentBg = config.transparentBg && config.format.supportsAlpha,
                    status = "COMPLETED",
                    localUri = output.uriString,
                    createdAtMs = System.currentTimeMillis(),
                )
                repository.recordExport(currentUserId, record, syncCloud = preferences.value.cloudAutoSync)
                _exportState.value = ExportUiState.Success(output)
            } catch (e: CancellationException) {
                _exportState.value = ExportUiState.Idle
                showToast("Export cancelled")
                throw e
            } catch (e: Throwable) {
                Log.e(TAG, "Export failed", e)
                _exportState.value = ExportUiState.Error(e.localizedMessage ?: "Export failed")
            }
        }
    }

    fun cancelExport() {
        activeExportJob?.cancel()
        activeExportJob = null
        _exportState.value = ExportUiState.Idle
    }

    fun resetExportState() {
        _exportState.value = ExportUiState.Idle
    }

    fun deleteExportRecord(exportId: String) {
        viewModelScope.launch {
            repository.deleteExportRecord(exportId)
            showToast("Export entry removed")
        }
    }

    // --- Preferences & Account ---

    fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        val next = transform(preferences.value)
        viewModelScope.launch {
            preferencesStore.update(next)
            if (next.cloudAutoSync) {
                repository.syncPreferences(next)
            }
        }
    }

    fun deleteAllUserData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAllLocalData(currentUserId)
            val cloudRes = repository.deleteAllCloudData()
            if (cloudRes.isSuccess) {
                showToast("All local and cloud account data deleted")
            } else {
                showToast("Local data cleared; cloud deletion failed", isError = true)
            }
            onComplete()
        }
    }

    private companion object {
        const val TAG = "CollageXVM"
        const val STOP_TIMEOUT_MS = 5000L
        const val MAX_UNDO_STEPS = 40
    }
}
