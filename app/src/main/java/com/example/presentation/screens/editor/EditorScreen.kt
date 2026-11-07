package com.example.presentation.screens.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.editor.SyntaxHighlighter
import com.example.core.linter.CodeDiagnostic
import com.example.core.linter.CodeLinter
import com.example.core.linter.DiagnosticSeverity
import com.example.domain.model.Project
import com.example.domain.model.ProjectFile
import com.example.presentation.components.ProjectFileTreeSheet
import com.example.ui.theme.*

/**
 * شاشة محرر الأكواد المتطور (Sora/CodeView Style) مع تلوين الصيغة وفحص الأخطاء النحوية (Code Linter)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    project: Project?,
    files: List<ProjectFile>,
    onSaveFile: (ProjectFile) -> Unit,
    onCreateFile: (name: String, path: String) -> Unit,
    onDeleteFile: (ProjectFile) -> Unit,
    onBackToProjects: () -> Unit,
    onNavigateToDesigner: () -> Unit,
    onNavigateToBuilder: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFile by remember(files) { mutableStateOf(files.firstOrNull { !it.isDirectory }) }
    var codeValue by remember(selectedFile) {
        mutableStateOf(TextFieldValue(selectedFile?.content ?: ""))
    }

    // سجل التراجع والإعادة (Undo/Redo History)
    val undoStack = remember(selectedFile) { mutableStateListOf<String>() }
    val redoStack = remember(selectedFile) { mutableStateListOf<String>() }

    var isSaved by remember { mutableStateOf(true) }
    var showFileTreeSheet by remember { mutableStateOf(false) }
    var showProblemsSheet by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchKeyword by remember { mutableStateOf("") }
    var replaceKeyword by remember { mutableStateOf("") }
    var fontSize by remember { mutableIntStateOf(13) }

    // فحص الأخطاء النحوية الفوري (Real-time Linter)
    val diagnostics = remember(codeValue.text, selectedFile) {
        CodeLinter.analyze(codeValue.text, selectedFile?.language ?: "Kotlin")
    }

    val programmingSymbols = listOf(
        "{", "}", "(", ")", "[", "]", ";", "=", "<", ">",
        "/", "\"", "'", "$", ":", ".", "!", "?", "&", "|",
        "+", "-", "*", "\t"
    )

    val autocompleteKeywords = listOf(
        "fun", "val", "var", "class", "override", "Composable",
        "remember", "Modifier", "Column", "Row", "Box", "Button",
        "Text", "Spacer", "fillMaxSize()", "padding()"
    )

    fun updateCode(newValue: TextFieldValue) {
        if (codeValue.text != newValue.text) {
            undoStack.add(codeValue.text)
            redoStack.clear()
            isSaved = false
        }
        codeValue = newValue
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeLast()
            redoStack.add(codeValue.text)
            codeValue = TextFieldValue(previous, selection = TextRange(previous.length))
            isSaved = false
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeLast()
            undoStack.add(codeValue.text)
            codeValue = TextFieldValue(next, selection = TextRange(next.length))
            isSaved = false
        }
    }

    fun replaceAll() {
        if (searchKeyword.isNotEmpty()) {
            val updated = codeValue.text.replace(searchKeyword, replaceKeyword)
            updateCode(TextFieldValue(updated))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showFileTreeSheet = true }
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedFile?.name ?: (project?.name ?: "محرر الأكواد"),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "قائمة الملفات",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = selectedFile?.path ?: (project?.packageName ?: ""),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = TextMuted
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToProjects) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = ElectricCyan)
                    }
                },
                actions = {
                    // التراجع والإعادة
                    IconButton(
                        onClick = { undo() },
                        enabled = undoStack.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.Undo,
                            contentDescription = "تراجع",
                            tint = if (undoStack.isNotEmpty()) TextPrimary else TextMuted
                        )
                    }
                    IconButton(
                        onClick = { redo() },
                        enabled = redoStack.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.Redo,
                            contentDescription = "إعادة",
                            tint = if (redoStack.isNotEmpty()) TextPrimary else TextMuted
                        )
                    }
                    // البحث والاستبدال
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            imageVector = Icons.Default.FindReplace,
                            contentDescription = "بحث واستبدال",
                            tint = if (showSearchBar) ForgeAmber else TextSecondary
                        )
                    }
                    // زر حفظ الملف
                    IconButton(
                        onClick = {
                            selectedFile?.let {
                                onSaveFile(it.copy(content = codeValue.text, lastModified = System.currentTimeMillis()))
                                isSaved = true
                            }
                        },
                        modifier = Modifier.testTag("save_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "حفظ الكود",
                            tint = if (isSaved) CodeGreen else ForgeAmber
                        )
                    }
                    // شجرة الملفات
                    IconButton(onClick = { showFileTreeSheet = true }) {
                        Icon(Icons.Default.Folder, contentDescription = "شجرة الملفات", tint = ElectricCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy800)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(DarkNavy900)) {
                // شريط اقتراحات الإكمال التلقائي
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkNavy800)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(autocompleteKeywords) { kw ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    val currentText = codeValue.text
                                    val newText = currentText + kw + " "
                                    updateCode(TextFieldValue(newText, selection = TextRange(newText.length)))
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            color = DarkNavy700
                        ) {
                            Text(
                                text = kw,
                                color = ForgeAmber,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // شريط الرموز البرمجية السريعة
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(programmingSymbols) { symbol ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkNavy700)
                                .border(1.dp, DarkNavy600, RoundedCornerShape(6.dp))
                                .clickable {
                                    val current = codeValue.text
                                    val selStart = codeValue.selection.start
                                    val selEnd = codeValue.selection.end
                                    val newText = current.replaceRange(selStart, selEnd, symbol)
                                    val newPos = selStart + symbol.length
                                    updateCode(TextFieldValue(newText, selection = TextRange(newPos)))
                                }
                                .padding(horizontal = 11.dp, vertical = 7.dp),
                            color = DarkNavy700
                        ) {
                            Text(
                                text = if (symbol == "\t") "TAB" else symbol,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkNavy900)
        ) {
            // شريط البحث والاستبدال المنسدل
            AnimatedVisibility(visible = showSearchBar, enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    color = DarkNavy800,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchKeyword,
                                onValueChange = { searchKeyword = it },
                                placeholder = { Text("بحث عن...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = replaceKeyword,
                                onValueChange = { replaceKeyword = it },
                                placeholder = { Text("استبدال بـ...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { replaceAll() },
                                colors = ButtonDefaults.buttonColors(containerColor = ForgeAmber, contentColor = DarkNavy900),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("استبدال الكل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // شريط تبويبات الملفات المفتوحة
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy800)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(files.filter { !it.isDirectory }) { file ->
                    val isSelected = selectedFile?.id == file.id
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .clickable {
                                selectedFile = file
                                codeValue = TextFieldValue(file.content)
                                undoStack.clear()
                                redoStack.clear()
                                isSaved = true
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (isSelected) DarkNavy900 else DarkNavy800
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ElectricCyan else TextSecondary
                                )
                            )
                            if (isSelected && !isSaved) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(ForgeAmber)
                                )
                            }
                        }
                    }
                }
            }

            // منطقة كود المحرر مع ترقيم الأسطر
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                val lines = codeValue.text.lines()

                // عمود أرقام الأسطر
                Column(
                    modifier = Modifier
                        .background(DarkNavy800)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..maxOf(lines.size, 1)) {
                        Text(
                            text = "$i",
                            fontFamily = FontFamily.Monospace,
                            fontSize = fontSize.sp,
                            color = TextMuted,
                            lineHeight = (fontSize + 7).sp
                        )
                    }
                }

                // حقل كتابة الكود البرمجي
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .background(DarkNavy900)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState())
                        .verticalScroll(rememberScrollState())
                ) {
                    TextField(
                        value = codeValue,
                        onValueChange = { updateCode(it) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize + 7).sp
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("code_editor_textarea")
                    )
                }
            }

            // شريط الحالة السفلي (Status Bar + Problems Button)
            Surface(
                color = DarkNavy800,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // زر فحص المشاكل البرمجية
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showProblemsSheet = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            color = if (diagnostics.isEmpty()) CodeGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (diagnostics.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (diagnostics.isEmpty()) CodeGreen else ErrorRed,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (diagnostics.isEmpty()) "سليم 0" else "${diagnostics.size} مشكلة",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (diagnostics.isEmpty()) CodeGreen else ErrorRed
                                )
                            }
                        }

                        Text(
                            text = "الأسطر: ${codeValue.text.lines().size}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = selectedFile?.language ?: "Text",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        Text(
                            text = "UTF-8",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }

    // نافذة استعراض المشاكل النحوية (Problems Sheet)
    if (showProblemsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProblemsSheet = false },
            containerColor = DarkNavy800
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فحص الأخطاء النحوية (Code Problems)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)
                    )
                    IconButton(onClick = { showProblemsSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DarkNavy700)

                if (diagnostics.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CodeGreen, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("كودك نظيف وخالٍ من الأخطاء النحوية الأساسية!", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(diagnostics) { diag ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkNavy900)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (diag.severity == DiagnosticSeverity.ERROR) Icons.Default.Error else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (diag.severity == DiagnosticSeverity.ERROR) ErrorRed else ForgeAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(diag.message, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("السطر ${diag.line} • ${diag.ruleId}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // نافذة شجرة الملفات الجانبية
    if (showFileTreeSheet) {
        ProjectFileTreeSheet(
            project = project,
            files = files,
            selectedFileId = selectedFile?.id,
            onSelectFile = { file ->
                selectedFile = file
                codeValue = TextFieldValue(file.content)
                undoStack.clear()
                redoStack.clear()
                isSaved = true
            },
            onCreateNewFile = { name, path ->
                onCreateFile(name, path)
            },
            onDeleteFile = { file ->
                onDeleteFile(file)
            },
            onDismiss = { showFileTreeSheet = false }
        )
    }
}
