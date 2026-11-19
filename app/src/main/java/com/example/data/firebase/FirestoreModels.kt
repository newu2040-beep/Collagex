package com.example.data.firebase

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfileDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class CollageProjectDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val schemaVersion: Long = 1L,
    val layoutType: String = "BENTO",
    val photoCount: Long = 0L,
    val favorite: Boolean = false,
    val archived: Boolean = false,
    val revision: Long = 1L,
    val canvas: Map<String, Any> = emptyMap(),
    val background: Map<String, Any> = emptyMap(),
    val border: Map<String, Any> = emptyMap(),
    val layerSummaries: List<Map<String, Any>> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ExportRecordDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val projectId: String = "",
    val projectTitle: String = "",
    val format: String = "PNG",
    val widthPx: Long = 0L,
    val heightPx: Long = 0L,
    val dpi: Long = 300L,
    val fileSizeBytes: Long = 0L,
    val transparentBg: Boolean = false,
    val status: String = "COMPLETED",
    @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class UserPreferencesDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val themePalette: String = "PASTEL_PINK",
    val darkMode: String = "SYSTEM",
    val languageCode: String = "en",
    val notificationsEnabled: Boolean = false,
    val stripExifDefault: Boolean = true,
    val defaultDpi: Long = 300L,
    val defaultUnit: String = "PX",
    val cloudAutoSync: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)
