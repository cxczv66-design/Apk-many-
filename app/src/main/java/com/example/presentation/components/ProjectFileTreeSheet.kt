package com.example.presentation.components

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
import com.example.domain.model.Project
import com.example.domain.model.ProjectFile
import com.example.ui.theme.*

/**
 * شجرة ملفات المشروع التفاعلية (File Tree Explorer)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFileTreeSheet(
    project: Project?,
    files: List<ProjectFile>,
    selectedFileId: Long?,
    onSelectFile: (ProjectFile) -> Unit,
    onCreateNewFile: (name: String, path: String) -> Unit,
    onDeleteFile: (ProjectFile) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var fileToDelete by remember { mutableStateOf<ProjectFile?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkNavy800,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DarkNavy600) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(16.dp)
        ) {
            // شريط رأس المستكشف
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مستكشف ملفات المشروع",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    )
                    Text(
                        text = project?.name ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { showCreateFileDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DarkNavy900),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_file_tree_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ملف جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = DarkNavy700
            )

            // قائمة ملفات المشروع
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(files, key = { it.id }) { file ->
                    val isSelected = file.id == selectedFileId
                    val icon = when (file.extension.lowercase()) {
                        "kt" -> Icons.Default.Code
                        "java" -> Icons.Default.Coffee
                        "xml" -> Icons.Default.Description
                        "json" -> Icons.Default.DataArray
                        "html", "css", "js" -> Icons.Default.Language
                        else -> Icons.Default.InsertDriveFile
                    }
                    val iconColor = when (file.extension.lowercase()) {
                        "kt" -> PurpleAccent
                        "java" -> ForgeAmber
                        "xml" -> ElectricCyan
                        "html", "css", "js" -> ForgeOrange
                        else -> TextSecondary
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onSelectFile(file)
                                onDismiss()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        color = if (isSelected) DarkNavy700 else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) ElectricCyan else TextPrimary
                                        )
                                    )
                                    Text(
                                        text = file.path,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        ),
                                        color = TextMuted
                                    )
                                }
                            }

                            if (files.size > 1) {
                                IconButton(
                                    onClick = { fileToDelete = file },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "حذف الملف",
                                        tint = ErrorRed.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // نافذة إضافة ملف جديد
    if (showCreateFileDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFileDialog = false },
            title = { Text("إنشاء ملف برمجي جديد", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("أدخل اسم الملف مع الامتداد (مثل: MyScreen.kt أو colors.xml):", fontSize = 12.sp)
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        placeholder = { Text("مثال: Utils.kt") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_file_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newFileName.trim()
                        if (name.isNotBlank()) {
                            val path = "app/src/main/java/${name}"
                            onCreateNewFile(name, path)
                            showCreateFileDialog = false
                            newFileName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DarkNavy900)
                ) {
                    Text("إنشاء الملف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFileDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // تأكيد حذف ملف
    fileToDelete?.let { file ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("حذف الملف؟") },
            text = { Text("هل أنت متأكد من حذف الملف '${file.name}' نهائياً؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFile(file)
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
