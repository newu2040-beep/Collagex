package com.example.core.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "collage_projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val ownerUid: String,
    val title: String,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val favorite: Boolean,
    val archived: Boolean,
    val revision: Int,
    val syncState: String,
    val cloudBackedUp: Boolean,
    val payloadJson: String,
)

@Entity(tableName = "export_records")
data class ExportEntity(
    @PrimaryKey val id: String,
    val ownerUid: String,
    val projectId: String,
    val projectTitle: String,
    val format: String,
    val widthPx: Int,
    val heightPx: Int,
    val dpi: Int,
    val fileSizeBytes: Long,
    val transparentBg: Boolean,
    val status: String,
    val localUri: String?,
    val createdAtMs: Long,
)

@Entity(tableName = "style_presets")
data class StylePresetEntity(
    @PrimaryKey val id: String,
    val ownerUid: String,
    val name: String,
    val cornerRadius: Float,
    val spacing: Float,
    val padding: Float,
    val borderColor: Int,
    val shadowEnabled: Boolean,
    val bgType: String,
    val bgPrimaryColor: Int,
    val bgSecondaryColor: Int,
    val bgTextureIntensity: Float,
    val createdAtMs: Long,
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM collage_projects WHERE ownerUid = :ownerUid ORDER BY updatedAtMs DESC")
    fun observeProjects(ownerUid: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM collage_projects WHERE id = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProject(entity: ProjectEntity)

    @Query("DELETE FROM collage_projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: String)

    @Query("DELETE FROM collage_projects WHERE ownerUid = :ownerUid")
    suspend fun deleteAllForUser(ownerUid: String)
}

@Dao
interface ExportDao {
    @Query("SELECT * FROM export_records WHERE ownerUid = :ownerUid ORDER BY createdAtMs DESC")
    fun observeExports(ownerUid: String): Flow<List<ExportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExport(entity: ExportEntity)

    @Query("DELETE FROM export_records WHERE id = :exportId")
    suspend fun deleteExportById(exportId: String)

    @Query("DELETE FROM export_records WHERE ownerUid = :ownerUid")
    suspend fun deleteAllForUser(ownerUid: String)
}

@Dao
interface StylePresetDao {
    @Query("SELECT * FROM style_presets WHERE ownerUid = :ownerUid ORDER BY createdAtMs DESC")
    fun observePresets(ownerUid: String): Flow<List<StylePresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreset(entity: StylePresetEntity)

    @Query("DELETE FROM style_presets WHERE id = :presetId")
    suspend fun deletePresetById(presetId: String)
}

@Database(
    entities = [ProjectEntity::class, ExportEntity::class, StylePresetEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CollageDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun exportDao(): ExportDao
    abstract fun stylePresetDao(): StylePresetDao

    companion object {
        @Volatile
        private var INSTANCE: CollageDatabase? = null

        fun getInstance(context: Context): CollageDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CollageDatabase::class.java,
                    "collagex_local.db",
                ).fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
