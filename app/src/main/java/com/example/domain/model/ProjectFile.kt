package com.example.domain.model

/**
 * يمثل ملفاً أو مجلداً داخل شجرة مشروع MobileForge IDE
 */
data class ProjectFile(
    val id: Long = 0,
    val projectId: Long,
    val name: String,
    val path: String,
    val content: String,
    val extension: String,
    val isDirectory: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
) {
    val language: String
        get() = when (extension.lowercase()) {
            "kt" -> "Kotlin"
            "java" -> "Java"
            "xml" -> "XML"
            "json" -> "JSON"
            "gradle", "kts" -> "Gradle"
            "html" -> "HTML"
            "css" -> "CSS"
            "js" -> "JavaScript"
            "txt", "md" -> "Markdown"
            else -> "Text"
        }
}
