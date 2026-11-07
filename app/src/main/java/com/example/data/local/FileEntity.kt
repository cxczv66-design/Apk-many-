package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.ProjectFile

/**
 * كيان جدول ملفات ومجلدات المشاريع
 */
@Entity(
    tableName = "project_files",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"]), Index(value = ["projectId", "path"], unique = true)]
)
data class FileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val name: String,
    val path: String,
    val content: String,
    val extension: String,
    val isDirectory: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
) {
    fun toDomain(): ProjectFile {
        return ProjectFile(
            id = id,
            projectId = projectId,
            name = name,
            path = path,
            content = content,
            extension = extension,
            isDirectory = isDirectory,
            lastModified = lastModified
        )
    }

    companion object {
        fun fromDomain(file: ProjectFile): FileEntity {
            return FileEntity(
                id = file.id,
                projectId = file.projectId,
                name = file.name,
                path = file.path,
                content = file.content,
                extension = file.extension,
                isDirectory = file.isDirectory,
                lastModified = file.lastModified
            )
        }
    }
}
