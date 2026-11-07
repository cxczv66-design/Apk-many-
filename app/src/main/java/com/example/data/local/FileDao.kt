package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * واجهة الاستعلامات لملفات المشاريع
 */
@Dao
interface FileDao {
    @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY isDirectory DESC, name ASC")
    fun getFilesByProject(projectId: Long): Flow<List<FileEntity>>

    @Query("SELECT * FROM project_files WHERE id = :id")
    suspend fun getFileById(id: Long): FileEntity?

    @Query("SELECT * FROM project_files WHERE projectId = :projectId AND path = :path LIMIT 1")
    suspend fun getFileByPath(projectId: Long, path: String): FileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<FileEntity>)

    @Update
    suspend fun updateFile(file: FileEntity)

    @Delete
    suspend fun deleteFile(file: FileEntity)

    @Query("DELETE FROM project_files WHERE projectId = :projectId")
    suspend fun deleteFilesByProject(projectId: Long)

    @Query("SELECT COUNT(*) FROM project_files WHERE projectId = :projectId AND isDirectory = 0")
    suspend fun getFileCount(projectId: Long): Int
}
