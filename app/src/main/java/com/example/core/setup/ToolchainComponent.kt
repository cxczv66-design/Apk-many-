package com.example.core.setup

import java.io.File

/**
 * فئة مكون الأدوات في بيئة تطوير MobileForge IDE
 */
enum class ToolCategory(val title: String, val isCritical: Boolean) {
    CRITICAL_CORE("أدوات البناء الأساسية (مدمجة واحتياطية)", true),
    COMPILER("مترجمات اللغات البرمجية", true),
    SDK_PLATFORM("مكتبات Android SDK", true),
    FULL_EMULATOR("محرك المحاكي الكامل (QEMU + AOSP)", false)
}

enum class ToolStatus {
    NOT_INSTALLED,
    DOWNLOADING,
    VERIFYING_SHA256,
    INSTALLED,
    EMBEDDED_FALLBACK_ACTIVE,
    FAILED
}

data class ToolchainItem(
    val id: String,
    val name: String,
    val description: String,
    val category: ToolCategory,
    val sizeBytes: Long,
    val expectedSha256: String,
    val downloadUrl: String,
    val isEmbeddedInApp: Boolean = false,
    val status: ToolStatus = if (isEmbeddedInApp) ToolStatus.EMBEDDED_FALLBACK_ACTIVE else ToolStatus.NOT_INSTALLED,
    val progressPercent: Float = if (isEmbeddedInApp) 1f else 0f,
    val errorMessage: String? = null
) {
    val sizeFormatted: String
        get() = when {
            sizeBytes >= 1024 * 1024 * 1024 -> String.format("%.1f GB", sizeBytes / (1024.0 * 1024 * 1024))
            sizeBytes >= 1024 * 1024 -> String.format("%.1f MB", sizeBytes / (1024.0 * 1024))
            sizeBytes >= 1024 -> String.format("%.1f KB", sizeBytes / 1024.0)
            else -> "$sizeBytes B"
        }
}
