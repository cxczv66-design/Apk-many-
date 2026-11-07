package com.example.presentation.screens.setup

import android.os.Environment
import android.os.StatFs
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.setup.ToolCategory
import com.example.core.setup.ToolStatus
import com.example.core.setup.ToolchainItem
import com.example.core.setup.ToolchainManager
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * شاشة الإعداد الأولي وتحميل حزمة أدوات التطوير والمحاكي (Offline Initial Setup)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    toolchainManager: ToolchainManager,
    onCompleteSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val toolchain by toolchainManager.toolchainState.collectAsState()
    val isCompleted by toolchainManager.isSetupCompleted.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isDownloadingAll by remember { mutableStateOf(false) }

    // حساب المساحة الخالية على الجهاز
    val availableSpaceFormatted = remember {
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
            String.format("%.1f GB", bytesAvailable / (1024.0 * 1024 * 1024))
        } catch (e: Exception) {
            "12+ GB"
        }
    }

    val totalDownloadSize = remember(toolchain) {
        val bytes = toolchain.filter { it.status == ToolStatus.NOT_INSTALLED }.sumOf { it.sizeBytes }
        when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> "$bytes B"
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(ElectricCyan, ForgeAmber))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = DarkNavy900, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "مركز الأدوات والمحاكي",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)
                            )
                            Text(
                                text = "Offline Toolchain & Emulator Manager",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = DarkNavy800,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // زر التخطي الفوري
                    OutlinedButton(
                        onClick = {
                            toolchainManager.activateEmbeddedMode()
                            onCompleteSetup()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("skip_setup_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("البدء بالأدوات المدمجة", fontSize = 12.sp, color = TextPrimary)
                    }

                    // زر التحميل الكامل
                    Button(
                        onClick = {
                            isDownloadingAll = true
                            coroutineScope.launch {
                                toolchainManager.downloadAllTools()
                                isDownloadingAll = false
                                onCompleteSetup()
                            }
                        },
                        enabled = !isDownloadingAll,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DarkNavy900),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("download_all_tools_button")
                    ) {
                        if (isDownloadingAll) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = DarkNavy900)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("جاري التثبيت...")
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تثبيت الكل ($totalDownloadSize)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // بطاقة الترحيب والتشخيص
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavy800),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ElectricCyan, ForgeAmber)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = CodeGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "بيئة تطوير محلية 100% تعمل بدون إنترنت",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "الأدوات الحرجة (AAPT2, D8/R8, ApkSigner, ECJ) مضمّنة مسبقاً وتعمل فورياً بحجم ~80 MB. لتحميل المحاكي الكامل وصورة Android AOSP المخصصة أو مترجم kotlinc، يمكنك التثبيت الآن والعمل أوفلاين للأبد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkNavy900)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = ForgeAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("المساحة الخالية على الجهاز:", fontSize = 11.sp, color = TextSecondary)
                            }
                            Text(availableSpaceFormatted, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                        }
                    }
                }
            }

            // سرد مكونات الـ Toolchain مقسمة حسب الفئات
            ToolCategory.values().forEach { category ->
                val categoryTools = toolchain.filter { it.category == category }
                if (categoryTools.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(top = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (category.isCritical) ElectricCyan else ForgeAmber
                                )
                                if (category == ToolCategory.CRITICAL_CORE) {
                                    Text(
                                        text = "مدمجة مسبقاً",
                                        fontSize = 10.sp,
                                        color = DarkNavy900,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CodeGreen)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(categoryTools, key = { it.id }) { tool ->
                        ToolchainRowItem(
                            tool = tool,
                            onDownload = {
                                coroutineScope.launch {
                                    toolchainManager.downloadComponent(tool.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolchainRowItem(
    tool: ToolchainItem,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tool.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = tool.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // زر الحالة أو التنزيل
                when (tool.status) {
                    ToolStatus.INSTALLED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CodeGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مثبّت", fontSize = 11.sp, color = CodeGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    ToolStatus.EMBEDDED_FALLBACK_ACTIVE -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = ForgeAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مدمج محلياً", fontSize = 11.sp, color = ForgeAmber, fontWeight = FontWeight.Bold)
                        }
                    }
                    ToolStatus.DOWNLOADING -> {
                        CircularProgressIndicator(
                            progress = { tool.progressPercent },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                            color = ElectricCyan
                        )
                    }
                    ToolStatus.VERIFYING_SHA256 -> {
                        Text("فحص SHA-256...", fontSize = 10.sp, color = ForgeAmber, fontFamily = FontFamily.Monospace)
                    }
                    ToolStatus.NOT_INSTALLED, ToolStatus.FAILED -> {
                        Button(
                            onClick = onDownload,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavy700, contentColor = ElectricCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tool.sizeFormatted, fontSize = 11.sp)
                        }
                    }
                }
            }

            // شريط التقدم أثناء التنزيل
            if (tool.status == ToolStatus.DOWNLOADING) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { tool.progressPercent },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                    color = ElectricCyan
                )
            }

            // بصمة الأمان SHA-256
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SHA-256: ${tool.expectedSha256.take(16)}...",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
            }
        }
    }
}
