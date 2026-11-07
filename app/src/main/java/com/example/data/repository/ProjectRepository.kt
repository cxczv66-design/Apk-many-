package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.FileEntity
import com.example.data.local.ProjectEntity
import com.example.domain.model.Project
import com.example.domain.model.ProjectFile
import com.example.domain.model.ProjectType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * مستودع إدارة المشاريع والملفات (Clean Architecture Repository)
 */
class ProjectRepository(private val database: AppDatabase) {

    private val projectDao = database.projectDao()
    private val fileDao = database.fileDao()

    /**
     * جلب جميع المشاريع المتوفرة
     */
    fun getAllProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects().map { entities ->
            entities.map { entity ->
                val count = fileDao.getFileCount(entity.id)
                entity.toDomain(fileCount = count)
            }
        }
    }

    /**
     * جلب تفاصيل مشروع معين
     */
    suspend fun getProjectById(id: Long): Project? {
        val entity = projectDao.getProjectById(id) ?: return null
        val count = fileDao.getFileCount(entity.id)
        return entity.toDomain(fileCount = count)
    }

    /**
     * إنشاء مشروع جديد وتوليد ملفاته من القالب المختار
     */
    suspend fun createProject(
        name: String,
        packageName: String,
        type: ProjectType,
        description: String = "",
        minSdk: Int = 26,
        targetSdk: Int = 34
    ): Long {
        val newProject = ProjectEntity(
            name = name.trim(),
            packageName = packageName.trim(),
            type = type.name,
            description = description.trim(),
            minSdk = minSdk,
            targetSdk = targetSdk,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val projectId = projectDao.insertProject(newProject)

        // توليد ملفات القالب
        val templateFiles = TemplateProvider.generateInitialFiles(
            projectId = projectId,
            projectName = name.trim(),
            packageName = packageName.trim(),
            type = type
        )

        fileDao.insertFiles(templateFiles.map { FileEntity.fromDomain(it) })
        return projectId
    }

    /**
     * حذف مشروع وجميع ملفاته المرتبطة
     */
    suspend fun deleteProject(projectId: Long) {
        val entity = projectDao.getProjectById(projectId) ?: return
        fileDao.deleteFilesByProject(projectId)
        projectDao.deleteProject(entity)
    }

    /**
     * جلب ملفات مشروع معين
     */
    fun getFilesForProject(projectId: Long): Flow<List<ProjectFile>> {
        return fileDao.getFilesByProject(projectId).map { list ->
            list.map { it.toDomain() }
        }
    }

    /**
     * حفظ تعديلات ملف
     */
    suspend fun updateFile(file: ProjectFile) {
        fileDao.updateFile(FileEntity.fromDomain(file))
        // تحديث توقيت المشروع
        val project = projectDao.getProjectById(file.projectId)
        if (project != null) {
            projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    /**
     * إنشاء ملف جديد في المشروع
     */
    suspend fun createFile(
        projectId: Long,
        name: String,
        path: String,
        content: String = "",
        isDirectory: Boolean = false
    ): Long {
        val extension = if (name.contains('.')) name.substringAfterLast('.') else ""
        val fileEntity = FileEntity(
            projectId = projectId,
            name = name,
            path = path,
            content = content,
            extension = extension,
            isDirectory = isDirectory,
            lastModified = System.currentTimeMillis()
        )
        val fileId = fileDao.insertFile(fileEntity)
        val project = projectDao.getProjectById(projectId)
        if (project != null) {
            projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
        }
        return fileId
    }

    /**
     * حذف ملف معين
     */
    suspend fun deleteFile(fileId: Long) {
        val file = fileDao.getFileById(fileId) ?: return
        fileDao.deleteFile(file)
    }

    /**
     * تهيئة مشاريع تجريبية أولية إذا كانت قاعدة البيانات فارغة تماماً
     */
    suspend fun seedInitialProjectsIfEmpty() {
        val existing = projectDao.getProjectById(1L)
        if (existing == null) {
            createProject(
                name = "MyForgeCounter",
                packageName = "com.forge.counter",
                type = ProjectType.COMPOSE,
                description = "تطبيق Jetpack Compose تفاعلي مع عداد ذكي وزر تشغيل متطور"
            )
            createProject(
                name = "QuickNoteApp",
                packageName = "com.forge.quicknotes",
                type = ProjectType.XML_KOTLIN,
                description = "تطبيق مذكرات كلاسيكي بـ XML و ConstraintLayout"
            )
            createProject(
                name = "CyberWebApp",
                packageName = "com.forge.cyberweb",
                type = ProjectType.WEB_APP,
                description = "واجهة ويب مستقبلية تفاعلية بـ HTML5 Canvas و CSS3"
            )
        }
    }
}
