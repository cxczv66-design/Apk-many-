package com.example.presentation.screens.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Project
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class TerminalLine(val text: String, val color: Color = TextPrimary)

/**
 * شاشة الطرفية المدمجة (Integrated Terminal Shell)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    project: Project?,
    onBackToProjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputCommand by remember { mutableStateOf("") }
    val terminalLines = remember {
        mutableStateListOf(
            TerminalLine("MobileForge Shell v1.0.0 (Android Linux aarch64)", ElectricCyan),
            TerminalLine("Type 'help' for available commands or 'gradle assembleDebug' to build.", TextSecondary),
            TerminalLine("Workspace: /sdcard/MobileForge/${project?.name ?: "default"}", ForgeAmber)
        )
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    fun executeCommand(raw: String) {
        val cmd = raw.trim()
        if (cmd.isBlank()) return

        terminalLines.add(TerminalLine("$ $cmd", ElectricCyan))
        inputCommand = ""

        when (cmd.lowercase()) {
            "help" -> {
                terminalLines.add(TerminalLine("الأوامر المتاحة في الطرفية:", ForgeAmber))
                terminalLines.add(TerminalLine("  help               - عرض هذه المساعدة"))
                terminalLines.add(TerminalLine("  ls                 - سرد ملفات المشروع الحالي"))
                terminalLines.add(TerminalLine("  gradle tasks       - عرض مهام Gradle المتاحة"))
                terminalLines.add(TerminalLine("  gradle assembleDebug - بناء نسخة APK تجريبية"))
                terminalLines.add(TerminalLine("  aapt2 version      - عرض إصدار مجمّع موارد أندرويد"))
                terminalLines.add(TerminalLine("  kotlinc -version   - عرض إصدار مترجم لغة كوتلن"))
                terminalLines.add(TerminalLine("  clear              - مسح شاشة الطرفية"))
                terminalLines.add(TerminalLine("  git status         - فحص حالة المستودع"))
                terminalLines.add(TerminalLine("  uname -a           - معلومات النظام والنواة"))
            }
            "clear" -> {
                terminalLines.clear()
            }
            "ls" -> {
                terminalLines.add(TerminalLine("app/  build.gradle.kts  settings.gradle.kts  gradle.properties", TextPrimary))
                terminalLines.add(TerminalLine("src/main/java/  src/main/res/  src/main/AndroidManifest.xml", TextSecondary))
            }
            "gradle tasks" -> {
                terminalLines.add(TerminalLine("Tasks runnable from root project 'MobileForge':", ForgeAmber))
                terminalLines.add(TerminalLine("Build Tasks: assemble, assembleDebug, assembleRelease, check, build"))
                terminalLines.add(TerminalLine("Verification Tasks: test, testDebugUnitTest, lint"))
            }
            "gradle assembledebug", "assembledebug" -> {
                terminalLines.add(TerminalLine("> Task :app:preBuild UP-TO-DATE", TextSecondary))
                terminalLines.add(TerminalLine("> Task :app:compileDebugAapt2Resources", ElectricCyan))
                terminalLines.add(TerminalLine("> Task :app:compileDebugKotlin", ElectricCyan))
                terminalLines.add(TerminalLine("> Task :app:dexBuilderDebug [D8]", ForgeAmber))
                terminalLines.add(TerminalLine("> Task :app:packageDebug -> APK Created", CodeGreen))
                terminalLines.add(TerminalLine("BUILD SUCCESSFUL in 1.4s", CodeGreen))
            }
            "aapt2 version" -> {
                terminalLines.add(TerminalLine("Android Asset Packaging Tool (AAPT) 2.19-11300958", TextPrimary))
            }
            "kotlinc -version" -> {
                terminalLines.add(TerminalLine("info: kotlinc-jvm 2.2.10 (JRE 17.0.9+9)", TextPrimary))
            }
            "git status" -> {
                terminalLines.add(TerminalLine("On branch main. Nothing to commit, working tree clean.", CodeGreen))
            }
            "uname -a" -> {
                terminalLines.add(TerminalLine("Linux localhost 5.15.0-mobileforge #1 SMP PREEMPT Android aarch64", TextSecondary))
            }
            else -> {
                if (cmd.startsWith("echo ")) {
                    terminalLines.add(TerminalLine(cmd.removePrefix("echo "), TextPrimary))
                } else {
                    terminalLines.add(TerminalLine("bash: $cmd: command not found. Type 'help' for command list.", ErrorRed))
                }
            }
        }

        coroutineScope.launch {
            if (terminalLines.isNotEmpty()) {
                listState.animateScrollToItem(terminalLines.size - 1)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "الطرفية (Embedded Terminal)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)
                        )
                        Text(
                            text = "sh /bash interpreter",
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
                actions = {
                    IconButton(onClick = { terminalLines.clear() }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح الشاشة", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy900)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkNavy900)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(terminalLines) { line ->
                    Text(
                        text = line.text,
                        color = line.color,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // شريط الأوامر السريعة
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy900)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val quickCmds = listOf("help", "ls", "gradle assembleDebug", "gradle tasks", "aapt2 version", "kotlinc -version", "git status", "uname -a", "clear")
                items(quickCmds) { cmd ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { executeCommand(cmd) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        color = DarkNavy800
                    ) {
                        Text(text = cmd, color = ForgeAmber, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // سطر إدخال الأوامر
            Surface(
                color = DarkNavy800,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$ ",
                        color = ElectricCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TextField(
                        value = inputCommand,
                        onValueChange = { inputCommand = it },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        placeholder = { Text("أدخل أمراً (مثل: help, ls, gradle tasks)...", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { executeCommand(inputCommand) }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("terminal_command_input")
                    )
                    IconButton(
                        onClick = { executeCommand(inputCommand) },
                        modifier = Modifier.testTag("terminal_send_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "تنفيذ الأمر", tint = ElectricCyan)
                    }
                }
            }
        }
    }
}
