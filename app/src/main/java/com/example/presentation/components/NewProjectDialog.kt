package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.ProjectType
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ForgeAmber

/**
 * نافذة حوارية لإنشاء مشروع جديد مع تخصيص القالب وحزمة التطبيق
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectDialog(
    initialTemplate: ProjectType,
    onDismiss: () -> Unit,
    onCreate: (name: String, packageName: String, type: ProjectType, description: String, minSdk: Int) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("com.example.myapp") }
    var autoSyncPackage by remember { mutableStateOf(true) }
    var selectedType by remember { mutableStateOf(initialTemplate) }
    var description by remember { mutableStateOf("") }
    var minSdk by remember { mutableIntStateOf(26) }
    var nameError by remember { mutableStateOf<String?>(null) }

    fun updateName(newName: String) {
        projectName = newName
        nameError = null
        if (autoSyncPackage) {
            val sanitized = newName.lowercase().filter { it.isLetterOrDigit() }
            packageName = if (sanitized.isNotBlank()) "com.example.$sanitized" else "com.example.myapp"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(8.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("new_project_dialog"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // شريط العنوان
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إنشاء مشروع جديد",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        )
                        Text(
                            text = "MobileForge IDE Studio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                // محتوى التكوين القابل للتمرير
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // اسم المشروع
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { updateName(it) },
                        label = { Text("اسم المشروع (Project Name)") },
                        placeholder = { Text("مثال: MyAwesomeApp") },
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_name_input")
                    )

                    // حزمة المشروع (Package Name)
                    OutlinedTextField(
                        value = packageName,
                        onValueChange = {
                            packageName = it
                            autoSyncPackage = false
                        },
                        label = { Text("اسم الحزمة (Package ID)") },
                        placeholder = { Text("com.example.app") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_package_input")
                    )

                    // اختيار نوع القالب
                    Text(
                        text = "اختر قالب المشروع الأساسي:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ProjectType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedType = type }
                                .testTag("template_${type.name.lowercase()}"),
                            color = if (isSelected) ElectricCyan.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when (type) {
                                    ProjectType.COMPOSE -> Icons.Default.Widgets
                                    ProjectType.XML_KOTLIN -> Icons.Default.PhoneAndroid
                                    ProjectType.XML_JAVA -> Icons.Default.Folder
                                    ProjectType.WEB_APP -> Icons.Default.Language
                                    ProjectType.BLANK -> Icons.Default.Folder
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = type.title,
                                    tint = if (isSelected) ElectricCyan else ForgeAmber,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = type.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = type.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedType = type },
                                    colors = RadioButtonDefaults.colors(selectedColor = ElectricCyan)
                                )
                            }
                        }
                    }

                    // إصدار الحد الأدنى لـ SDK
                    Text(
                        text = "الحد الأدنى للنظام (Min SDK):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            26 to "Android 8.0 (API 26)",
                            28 to "Android 9.0 (API 28)",
                            33 to "Android 13 (API 33)"
                        ).forEach { (sdk, label) ->
                            FilterChip(
                                selected = minSdk == sdk,
                                onClick = { minSdk = sdk },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }

                    // وصف اختياري
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("وصف المشروع (اختياري)") },
                        placeholder = { Text("أدخل نبذة مختصرة عن فكرة التطبيق...") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                // أزرار التحكم السفلية
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (projectName.isBlank()) {
                                nameError = "يرجى إدخال اسم المشروع أولاً"
                                return@Button
                            }
                            onCreate(
                                projectName.trim(),
                                packageName.trim(),
                                selectedType,
                                description.trim(),
                                minSdk
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF0B0F17)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("create_project_confirm_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إنشاء المشروع فوراً", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
