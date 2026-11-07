package com.example.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * وجهات التنقل الرئيسية داخل MobileForge IDE
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "المشاريع", Icons.Default.Dashboard)
    object Editor : Screen("editor", "المحرر", Icons.Default.Code)
    object Designer : Screen("designer", "المصمم", Icons.Default.DesignServices)
    object Builder : Screen("builder", "البناء", Icons.Default.Build)
    object Terminal : Screen("terminal", "الطرفية", Icons.Default.Terminal)
    object Settings : Screen("settings", "الإعدادات", Icons.Default.Settings)
    object Setup : Screen("setup", "مركز الأدوات", Icons.Default.Build)
}
