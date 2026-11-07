package com.example.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class DependencyItem(
    val group: String,
    val name: String,
    val version: String,
    val isCore: Boolean = false
)

/**
 * شاشة إعدادات بيئة التطوير وإدارة المكتبات والاعتماديات
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackToProjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editorFontSize by remember { mutableIntStateOf(13) }
    var enableWordWrap by remember { mutableStateOf(false) }
    var enableLineNumbers by remember { mutableStateOf(true) }
    var autoSaveEnabled by remember { mutableStateOf(true) }
    var selectedTheme by remember { mutableStateOf("MobileForge Cyber Dark") }

    val dependencies = remember {
        mutableStateListOf(
            DependencyItem("androidx.compose", "compose-bom", "2024.09.00", isCore = true),
            DependencyItem("androidx.room", "room-runtime", "2.7.0", isCore = true),
            DependencyItem("androidx.navigation", "navigation-compose", "2.8.9", isCore = true),
            DependencyItem("org.jetbrains.kotlinx", "kotlinx-coroutines-android", "1.10.2", isCore = true),
            DependencyItem("io.coil-kt", "coil-compose", "2.7.0"),
            DependencyItem("com.squareup.retrofit2", "retrofit", "2.12.0")
        )
    }

    var showAddDepDialog by remember { mutableStateOf(false) }
    var newDepArtifact by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "الإعدادات والمكتبات",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)
                        )
                        Text(
                            text = "تخصيص محرر الكود وبيئة التطوير",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToProjects) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = ElectricCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // إعدادات المحرر (Editor Settings)
            item {
                Text("إعدادات محرر الأكواد (Editor Settings):", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ForgeAmber)
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // حجم الخط
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("حجم خط المحرر", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("$editorFontSize sp", fontSize = 11.sp, color = TextMuted)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(11, 13, 15, 18).forEach { size ->
                                    FilterChip(
                                        selected = editorFontSize == size,
                                        onClick = { editorFontSize = size },
                                        label = { Text("${size}sp") }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = DarkNavy700)

                        // خيارات التبديل
                        SettingToggleRow(
                            title = "ترقيم الأسطر (Line Numbers)",
                            subtitle = "عرض أرقام الأسطر على يسار محرر الكود",
                            checked = enableLineNumbers,
                            onCheckedChange = { enableLineNumbers = it }
                        )

                        SettingToggleRow(
                            title = "الحفظ التلقائي (Auto Save)",
                            subtitle = "حفظ التعديلات تلقائياً عند مغادرة المحرر",
                            checked = autoSaveEnabled,
                            onCheckedChange = { autoSaveEnabled = it }
                        )

                        SettingToggleRow(
                            title = "التفاف الأسطر (Word Wrap)",
                            subtitle = "التفاف الأسطر الطويلة برمجياً لمنع التمرير الأفقي",
                            checked = enableWordWrap,
                            onCheckedChange = { enableWordWrap = it }
                        )
                    }
                }
            }

            // مدير المكتبات والاعتماديات (Dependencies Manager)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مدير المكتبات (Dependencies Manager):", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ElectricCyan)
                    Button(
                        onClick = { showAddDepDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DarkNavy900),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("add_dep_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة مكتبة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(dependencies) { dep ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${dep.group}:${dep.name}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "version: ${dep.version}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = ElectricCyan
                            )
                        }

                        if (!dep.isCore) {
                            IconButton(onClick = { dependencies.remove(dep) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف المكتبة", tint = ErrorRed)
                            }
                        } else {
                            Text(
                                text = "أساسية",
                                fontSize = 10.sp,
                                color = DarkNavy900,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ForgeAmber)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // معلومات البيئة
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = DarkNavy800)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("حول MobileForge IDE", fontWeight = FontWeight.Bold, color = ElectricCyan)
                        Text("الإصدار: 1.0.0 Stable (AArch64 / Mobile Studio)", fontSize = 11.sp, color = TextSecondary)
                        Text("البيئة: Android 14 API 34 | Kotlin 2.2 | Jetpack Compose", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }
    }

    if (showAddDepDialog) {
        AlertDialog(
            onDismissRequest = { showAddDepDialog = false },
            title = { Text("إضافة مكتبة Gradle جديدة", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أدخل معرف المكتبة (مثل: com.google.code.gson:gson:2.10.1):", fontSize = 12.sp)
                    OutlinedTextField(
                        value = newDepArtifact,
                        onValueChange = { newDepArtifact = it },
                        placeholder = { Text("group:name:version") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parts = newDepArtifact.trim().split(":")
                        if (parts.size >= 3) {
                            dependencies.add(DependencyItem(parts[0], parts[1], parts[2]))
                            showAddDepDialog = false
                            newDepArtifact = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DarkNavy900)
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDepDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
