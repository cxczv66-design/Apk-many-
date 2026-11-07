package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Project
import com.example.domain.model.ProjectType

/**
 * كيان جدول المشاريع في قاعدة بيانات Room المحلية
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val packageName: String,
    val type: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val minSdk: Int = 26,
    val targetSdk: Int = 34,
    val lastBuildSuccess: Boolean? = null
) {
    fun toDomain(fileCount: Int = 0): Project {
        return Project(
            id = id,
            name = name,
            packageName = packageName,
            type = try {
                ProjectType.valueOf(type)
            } catch (e: Exception) {
                ProjectType.COMPOSE
            },
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            fileCount = fileCount,
            minSdk = minSdk,
            targetSdk = targetSdk,
            lastBuildSuccess = lastBuildSuccess
        )
    }

    companion object {
        fun fromDomain(project: Project): ProjectEntity {
            return ProjectEntity(
                id = project.id,
                name = project.name,
                packageName = project.packageName,
                type = project.type.name,
                description = project.description,
                createdAt = project.createdAt,
                updatedAt = project.updatedAt,
                minSdk = project.minSdk,
                targetSdk = project.targetSdk,
                lastBuildSuccess = project.lastBuildSuccess
            )
        }
    }
}
