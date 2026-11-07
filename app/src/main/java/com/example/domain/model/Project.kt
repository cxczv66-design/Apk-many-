package com.example.domain.model

/**
 * يمثل مشروعاً كاملاً في MobileForge IDE
 */
data class Project(
    val id: Long = 0,
    val name: String,
    val packageName: String,
    val type: ProjectType,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val fileCount: Int = 0,
    val minSdk: Int = 26,
    val targetSdk: Int = 34,
    val lastBuildSuccess: Boolean? = null
)
