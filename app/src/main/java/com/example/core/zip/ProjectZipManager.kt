package com.example.core.zip

import android.content.Context
import com.example.domain.model.Project
import com.example.domain.model.ProjectFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * مدير حزم وتصدير واستيراد مشاريع MobileForge بصيغة ZIP
 */
object ProjectZipManager {

    /**
     * تصدير جميع ملفات المشروع إلى ملف ZIP صالح للتنزيل والمشاركة
     */
    fun exportProjectToZipFile(context: Context, project: Project, files: List<ProjectFile>): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val zipFile = File(exportDir, "${project.name.replace(" ", "_")}-MobileForge.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            files.forEach { file ->
                if (!file.isDirectory) {
                    val entryName = file.path.ifBlank { file.name }
                    val entry = ZipEntry(entryName)
                    zos.putNextEntry(entry)
                    zos.write(file.content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }
        }
        return zipFile
    }

    /**
     * تحويل المشروع إلى مصفوفة بايتات بصيغة ZIP
     */
    fun exportProjectToBytes(project: Project, files: List<ProjectFile>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            files.forEach { file ->
                if (!file.isDirectory) {
                    val entryName = file.path.ifBlank { file.name }
                    val entry = ZipEntry(entryName)
                    zos.putNextEntry(entry)
                    zos.write(file.content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }
        }
        return baos.toByteArray()
    }
}
