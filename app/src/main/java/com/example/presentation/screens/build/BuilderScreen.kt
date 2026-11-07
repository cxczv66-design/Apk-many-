package com.example.presentation.screens.build

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.compiler.BuildResult
import com.example.core.compiler.GradleRunner
import com.example.core.compiler.GradleTask
import com.example.core.compiler.LogType
import com.example.domain.model.Project
import com.example.domain.model.ProjectFile
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

/**
 * شاشة باني تطبيقات الـ APK المتصلة بمحرك GradleRunner الحقيقي
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderScreen(
    project: Project?,
    files: List<ProjectFile>,
    onBackToProjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gradleRunner = remember { GradleRunner(context) }
    val logs by gradleRunner.logs.collectAsState()
    val currentTask by gradleRunner.currentTask.collectAsState()
    val progressPercent by gradleRunner.progressPercent.collectAsState()

    var isBuilding by remember { mutableStateOf(false) }
    var isCleanBuild by remember { mutableStateOf(false) }
    var isReleaseMode by remember { mutableStateOf(false) }
    var buildResult by remember { mutableStateOf<BuildResult?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // تمرير تلقائي لأسفل السجل عند إضافة سطور جديدة
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    fun shareApk(file: File) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_SUBJECT, "تطبيق APK من MobileForge: ${project?.name}")
                putExtra(Intent.EXTRA_TEXT, "تم بناء هذا التطبيق أوفلاين مباشرة على الهاتف بواسطة MobileForge IDE!")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(sendIntent, "تثبيت أو مشاركة ملف الـ APK"))
        } catch (e: Exception) {
            // fallback
        }
    }

    fun startBuild() {
        val currentProject = project ?: return
        isBuilding = true
        buildResult = null

        coroutineScope.launch {
            val result = gradleRunner.executeBuild(
                project = currentProject,
                files = files,
                isCleanBuild = isCleanBuild,
                isRelease = isReleaseMode
            )
            buildResult = result
            isBuilding = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "محرك البناء (Gradle Runner)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Offline Engine",
                                fontSize = 10.sp,
                                color = DarkNavy900,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ForgeAmber)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = project?.name ?: "بناء الحزمة البرمجية",
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
                    IconButton(onClick = { gradleRunner.clearLogs() }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "مسح السجل", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // بطاقة خيارات البناء (Build Configuration Card)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = project?.name ?: "مشروع مخصص",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${project?.packageName} (minSdk=${project?.minSdk ?: 26})",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = { startBuild() },
                            enabled = !isBuilding && project != null,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DarkNavy900),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("start_build_button")
                        ) {
                            if (isBuilding) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = DarkNavy900)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("جاري البناء...")
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("بناء APK الآن", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // خيارات المهام (Clean / Incremental / Variant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = !isReleaseMode,
                            onClick = { isReleaseMode = false },
                            label = { Text("assembleDebug", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                        )
                        FilterChip(
                            selected = isReleaseMode,
                            onClick = { isReleaseMode = true },
                            label = { Text("assembleRelease", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                        )
                        FilterChip(
                            selected = isCleanBuild,
                            onClick = { isCleanBuild = !isCleanBuild },
                            label = { Text("--clean", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                            leadingIcon = {
                                if (isCleanBuild) Icon(Icons.Default.CleaningServices, null, modifier = Modifier.size(14.dp))
                            }
                        )
                    }

                    if (isBuilding) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp)),
                            color = ElectricCyan
                        )
                    }
                }
            }

            // بطاقة النتيجة عند اكتمال البناء بنجاح
            AnimatedVisibility(visible = buildResult?.isSuccess == true) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CodeGreen.copy(alpha = 0.15f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CodeGreen))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CodeGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تم توليد وتوقيع APK بنجاح!", fontWeight = FontWeight.Bold, color = CodeGreen)
                            }
                            Text(
                                "${buildResult?.apkFile?.name} • ${buildResult?.durationMs}ms (${buildResult?.executedCount} منفذة، ${buildResult?.upToDateCount} تزايدية)",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = { buildResult?.apkFile?.let { shareApk(it) } },
                            colors = ButtonDefaults.buttonColors(containerColor = CodeGreen, contentColor = DarkNavy900),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تثبيت / حفظ")
                        }
                    }
                }
            }

            // شاشة سجلات الـ Console المباشرة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجلات الـ Console المباشرة (Build Output):",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary
                )
                if (currentTask != null) {
                    Text(
                        text = currentTask?.taskName ?: "",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ForgeAmber
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DarkNavy700, RoundedCornerShape(12.dp)),
                color = DarkNavy900
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (logs.isEmpty()) {
                        item {
                            Text(
                                text = "اضغط على 'بناء APK الآن' لبدء خط إنتاج الـ Gradle الكامل...",
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        items(logs) { entry ->
                            val color = when (entry.type) {
                                LogType.SUCCESS -> CodeGreen
                                LogType.TASK -> ElectricCyan
                                LogType.TASK_UP_TO_DATE -> ForgeAmber
                                LogType.WARNING -> ForgeOrange
                                LogType.ERROR -> ErrorRed
                                LogType.INFO -> TextPrimary
                            }
                            Text(
                                text = entry.text,
                                color = color,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
