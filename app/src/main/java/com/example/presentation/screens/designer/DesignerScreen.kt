package com.example.presentation.screens.designer

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.domain.model.Project
import com.example.domain.model.ProjectType
import com.example.ui.theme.*

enum class DesignerComponentType(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    BUTTON("Button", Icons.Default.SmartButton),
    TEXT("TextView", Icons.Default.TextFields),
    INPUT("EditText", Icons.Default.Edit),
    IMAGE("ImageView", Icons.Default.Image),
    CARD("CardView", Icons.Default.WebAsset),
    SWITCH("Switch", Icons.Default.ToggleOn),
    CHECKBOX("CheckBox", Icons.Default.CheckBox),
    PROGRESS("ProgressBar", Icons.Default.HourglassTop),
    FAB("FAB", Icons.Default.AddCircle)
}

enum class ContainerLayoutType(val title: String, val xmlTag: String) {
    LINEAR_VERTICAL("LinearLayout (Vertical)", "LinearLayout"),
    LINEAR_HORIZONTAL("LinearLayout (Horizontal)", "LinearLayout"),
    CONSTRAINT("ConstraintLayout", "androidx.constraintlayout.widget.ConstraintLayout"),
    FRAME("FrameLayout", "FrameLayout")
}

data class DesignerElement(
    val id: String,
    val type: DesignerComponentType,
    val text: String = "",
    val colorHex: String = "#00E5FF",
    val fontSize: Int = 16,
    val paddingDp: Int = 8,
    val cornerRadiusDp: Int = 12,
    val isChecked: Boolean = false
)

/**
 * مصمم الواجهات المرئي المتقدم مع المعاينة التفاعلية ومعاينة الويب المباشرة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignerScreen(
    project: Project?,
    onBackToProjects: () -> Unit,
    onNavigateToEditor: () -> Unit,
    onNavigateToBuilder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elements = remember {
        mutableStateListOf(
            DesignerElement("el_1", DesignerComponentType.TEXT, text = "تطبيقي الرائع عبر الهاتف 🚀", fontSize = 22, colorHex = "#00E5FF"),
            DesignerElement("el_2", DesignerComponentType.BUTTON, text = "انقر للتفاعل الفوري", colorHex = "#00E5FF"),
            DesignerElement("el_3", DesignerComponentType.INPUT, text = "أدخل بياناتك هنا..."),
            DesignerElement("el_4", DesignerComponentType.CARD, text = "بطاقة معلومات مع ظل وتصميم جذاب"),
            DesignerElement("el_5", DesignerComponentType.SWITCH, text = "تفعيل الإشعارات الفورية", isChecked = true)
        )
    }

    var selectedElementId by remember { mutableStateOf<String?>("el_1") }
    var layoutType by remember { mutableStateOf(ContainerLayoutType.LINEAR_VERTICAL) }
    var isLiveInteractiveMode by remember { mutableStateOf(false) }
    var isWebPreviewMode by remember { mutableStateOf(project?.type == ProjectType.WEB_APP) }
    var showCodeDialog by remember { mutableStateOf(false) }
    var generatedMode by remember { mutableStateOf(if (project?.type == ProjectType.COMPOSE) "Compose" else "XML") }
    var clickCounter by remember { mutableIntStateOf(0) }

    val selectedElement = elements.find { it.id == selectedElementId }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مصمم الواجهات",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ForgeAmber)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isLiveInteractiveMode) {
                                Text(
                                    text = "تفاعلي مباشر",
                                    fontSize = 10.sp,
                                    color = DarkNavy900,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CodeGreen)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = project?.name ?: "تصميم واجهات أندرويد",
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
                    // تبديل المعاينة التفاعلية
                    IconButton(
                        onClick = { isLiveInteractiveMode = !isLiveInteractiveMode },
                        modifier = Modifier.testTag("toggle_live_preview_btn")
                    ) {
                        Icon(
                            imageVector = if (isLiveInteractiveMode) Icons.Default.TouchApp else Icons.Default.PanTool,
                            contentDescription = "وضع التجربة",
                            tint = if (isLiveInteractiveMode) CodeGreen else TextSecondary
                        )
                    }
                    // تبديل معاينة الويب
                    IconButton(onClick = { isWebPreviewMode = !isWebPreviewMode }) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "معاينة ويب",
                            tint = if (isWebPreviewMode) ForgeOrange else TextSecondary
                        )
                    }
                    // عرض الكود البرمجي
                    IconButton(onClick = { showCodeDialog = true }) {
                        Icon(Icons.Default.Code, contentDescription = "الكود المتولد", tint = ElectricCyan)
                    }
                    // بناء التطبيق
                    IconButton(onClick = onNavigateToBuilder) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "بناء التطبيق", tint = CodeGreen)
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
        ) {
            // شريط اختيار نوع التخطيط ومكتبة المكونات
            Surface(
                color = DarkNavy800,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "مكتبة المكونات (اضغط لإضافة عنصر):", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = layoutType.title,
                            fontSize = 10.sp,
                            color = ForgeAmber,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ForgeAmber.copy(alpha = 0.15f))
                                .clickable {
                                    val nextIdx = (layoutType.ordinal + 1) % ContainerLayoutType.values().size
                                    layoutType = ContainerLayoutType.values()[nextIdx]
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(DesignerComponentType.values()) { type ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val newEl = DesignerElement(
                                            id = "el_${System.currentTimeMillis()}",
                                            type = type,
                                            text = "${type.label} جديد"
                                        )
                                        elements.add(newEl)
                                        selectedElementId = newEl.id
                                    }
                                    .border(1.dp, DarkNavy600, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                color = DarkNavy700
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = type.icon, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = type.label, color = TextPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // منطقة المعاينة (إما معاينة شاشة الهاتف أو معاينة WebView لتطبيقات الويب)
            if (isWebPreviewMode) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                val sampleHtml = """
                                    <!DOCTYPE html>
                                    <html dir="rtl">
                                    <head>
                                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                        <style>
                                            body { font-family: sans-serif; background: #0f172a; color: white; text-align: center; padding: 2rem; }
                                            button { background: #00e5ff; color: #0b0f17; border: none; padding: 12px 24px; font-weight: bold; border-radius: 8px; font-size: 1rem; cursor: pointer; }
                                            .card { background: #1e293b; padding: 1.5rem; border-radius: 12px; margin-top: 1rem; }
                                        </style>
                                    </head>
                                    <body>
                                        <h2>🌐 معاينة تطبيق الويب (HTML5 Live)</h2>
                                        <p>معاينة فورية مع دعم JavaScript و CSS كامل داخل MobileForge!</p>
                                        <button onclick="document.getElementById('res').innerText = 'تم التفاعل بنجاح في: ' + new Date().toLocaleTimeString()">انقر لتجربة الـ JS</button>
                                        <div id="res" class="card">جاهز للتفاعل...</div>
                                    </body>
                                    </html>
                                """.trimIndent()
                                loadDataWithBaseURL(null, sampleHtml, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // شاشة هاتف وهمية (Phone Canvas Container)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkNavy900)
                        .border(2.dp, DarkNavy700, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // محاكاة نوتش الكاميرا العلوية
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkNavy700)
                        )

                        if (elements.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("انقر على أي مكون في الشريط العلوي لإضافته هنا", color = TextMuted, fontSize = 13.sp)
                            }
                        }

                        elements.forEachIndexed { index, el ->
                            val isSelected = el.id == selectedElementId
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(el.cornerRadiusDp.dp))
                                    .border(
                                        width = if (isSelected && !isLiveInteractiveMode) 2.dp else 0.dp,
                                        color = if (isSelected && !isLiveInteractiveMode) ElectricCyan else Color.Transparent,
                                        shape = RoundedCornerShape(el.cornerRadiusDp.dp)
                                    )
                                    .clickable {
                                        if (!isLiveInteractiveMode) selectedElementId = el.id
                                    }
                                    .padding(el.paddingDp.dp)
                            ) {
                                when (el.type) {
                                    DesignerComponentType.BUTTON -> {
                                        Button(
                                            onClick = {
                                                if (isLiveInteractiveMode) clickCounter++
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DarkNavy900),
                                            shape = RoundedCornerShape(el.cornerRadiusDp.dp)
                                        ) {
                                            Text(
                                                text = if (isLiveInteractiveMode && clickCounter > 0) "${el.text} ($clickCounter)" else el.text,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = el.fontSize.sp
                                            )
                                        }
                                    }
                                    DesignerComponentType.TEXT -> {
                                        Text(
                                            text = el.text,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = el.fontSize.sp),
                                            color = TextPrimary
                                        )
                                    }
                                    DesignerComponentType.INPUT -> {
                                        var inputText by remember { mutableStateOf(el.text) }
                                        OutlinedTextField(
                                            value = inputText,
                                            onValueChange = { if (isLiveInteractiveMode) inputText = it },
                                            readOnly = !isLiveInteractiveMode,
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("أدخل النص...") }
                                        )
                                    }
                                    DesignerComponentType.CARD -> {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = DarkNavy800),
                                            shape = RoundedCornerShape(el.cornerRadiusDp.dp)
                                        ) {
                                            Text(text = el.text, modifier = Modifier.padding(16.dp), color = TextPrimary)
                                        }
                                    }
                                    DesignerComponentType.IMAGE -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                                .clip(RoundedCornerShape(el.cornerRadiusDp.dp))
                                                .background(DarkNavy800),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Image, contentDescription = null, tint = ForgeAmber, modifier = Modifier.size(40.dp))
                                        }
                                    }
                                    DesignerComponentType.SWITCH -> {
                                        var switched by remember { mutableStateOf(el.isChecked) }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = el.text, color = TextPrimary, fontSize = el.fontSize.sp)
                                            Switch(
                                                checked = switched,
                                                onCheckedChange = { if (isLiveInteractiveMode) switched = it },
                                                enabled = isLiveInteractiveMode
                                            )
                                        }
                                    }
                                    DesignerComponentType.CHECKBOX -> {
                                        var checked by remember { mutableStateOf(el.isChecked) }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = checked,
                                                onCheckedChange = { if (isLiveInteractiveMode) checked = it },
                                                enabled = isLiveInteractiveMode
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = el.text, color = TextPrimary, fontSize = el.fontSize.sp)
                                        }
                                    }
                                    DesignerComponentType.PROGRESS -> {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = ForgeAmber)
                                    }
                                    DesignerComponentType.FAB -> {
                                        FloatingActionButton(
                                            onClick = { if (isLiveInteractiveMode) clickCounter++ },
                                            containerColor = ForgeAmber,
                                            contentColor = DarkNavy900
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // لوحة تعديل الخصائص وإعادة الترتيب (Attributes & Reorder Inspector)
            selectedElement?.let { el ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "خصائص: ${el.type.label}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan
                            )
                            Row {
                                // زر تحريك لأعلى
                                val idx = elements.indexOfFirst { it.id == el.id }
                                IconButton(
                                    onClick = {
                                        if (idx > 0) {
                                            elements.removeAt(idx)
                                            elements.add(idx - 1, el)
                                        }
                                    },
                                    enabled = idx > 0
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "أعلى", tint = if (idx > 0) TextPrimary else TextMuted)
                                }
                                // زر تحريك لأسفل
                                IconButton(
                                    onClick = {
                                        if (idx < elements.size - 1) {
                                            elements.removeAt(idx)
                                            elements.add(idx + 1, el)
                                        }
                                    },
                                    enabled = idx < elements.size - 1
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "أسفل", tint = if (idx < elements.size - 1) TextPrimary else TextMuted)
                                }
                                // زر الحذف
                                IconButton(onClick = { elements.remove(el); selectedElementId = null }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف العنصر", tint = ErrorRed)
                                }
                            }
                        }

                        // تعديل النص
                        OutlinedTextField(
                            value = el.text,
                            onValueChange = { newTxt ->
                                val i = elements.indexOfFirst { it.id == el.id }
                                if (i != -1) elements[i] = el.copy(text = newTxt)
                            },
                            label = { Text("النص (Text / Content)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // ضبط حجم الخط والحشو
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = "${el.fontSize}",
                                onValueChange = {
                                    val size = it.toIntOrNull() ?: el.fontSize
                                    val i = elements.indexOfFirst { item -> item.id == el.id }
                                    if (i != -1) elements[i] = el.copy(fontSize = size)
                                },
                                label = { Text("حجم الخط (sp)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = "${el.paddingDp}",
                                onValueChange = {
                                    val p = it.toIntOrNull() ?: el.paddingDp
                                    val i = elements.indexOfFirst { item -> item.id == el.id }
                                    if (i != -1) elements[i] = el.copy(paddingDp = p)
                                },
                                label = { Text("الحشو (Padding dp)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    // نافذة عرض الكود المتولد تلقائياً
    if (showCodeDialog) {
        val generatedCode = if (generatedMode == "Compose") {
            buildString {
                appendLine("@Composable\nfun GeneratedScreen() {")
                appendLine("    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {")
                elements.forEach { el ->
                    when (el.type) {
                        DesignerComponentType.BUTTON -> appendLine("        Button(onClick = { /* action */ }, modifier = Modifier.fillMaxWidth()) { Text(\"${el.text}\", fontSize = ${el.fontSize}.sp) }")
                        DesignerComponentType.TEXT -> appendLine("        Text(text = \"${el.text}\", fontSize = ${el.fontSize}.sp, modifier = Modifier.padding(${el.paddingDp}.dp))")
                        DesignerComponentType.INPUT -> appendLine("        OutlinedTextField(value = \"\", onValueChange = {}, label = { Text(\"${el.text}\") }, modifier = Modifier.fillMaxWidth())")
                        DesignerComponentType.CARD -> appendLine("        Card(modifier = Modifier.fillMaxWidth()) { Text(\"${el.text}\", modifier = Modifier.padding(16.dp)) }")
                        DesignerComponentType.SWITCH -> appendLine("        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(\"${el.text}\"); Switch(checked = true, onCheckedChange = {}) }")
                        DesignerComponentType.CHECKBOX -> appendLine("        Row { Checkbox(checked = true, onCheckedChange = {}); Text(\"${el.text}\") }")
                        DesignerComponentType.IMAGE -> appendLine("        Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.fillMaxWidth().height(100.dp))")
                        DesignerComponentType.PROGRESS -> appendLine("        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())")
                        DesignerComponentType.FAB -> appendLine("        FloatingActionButton(onClick = {}) { Icon(Icons.Default.Add, null) }")
                    }
                    appendLine("        Spacer(modifier = Modifier.height(8.dp))")
                }
                appendLine("    }\n}")
            }
        } else {
            buildString {
                appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                appendLine("<${layoutType.xmlTag} xmlns:android=\"http://schemas.android.com/apk/res/android\"")
                appendLine("    android:layout_width=\"match_parent\"")
                appendLine("    android:layout_height=\"match_parent\"")
                if (layoutType.xmlTag == "LinearLayout") {
                    val orient = if (layoutType == ContainerLayoutType.LINEAR_HORIZONTAL) "horizontal" else "vertical"
                    appendLine("    android:orientation=\"$orient\"")
                }
                appendLine("    android:padding=\"16dp\">")
                elements.forEachIndexed { i, el ->
                    when (el.type) {
                        DesignerComponentType.BUTTON -> appendLine("    <Button android:id=\"@+id/btn_${i}\" android:layout_width=\"match_parent\" android:layout_height=\"wrap_content\" android:text=\"${el.text}\" />")
                        DesignerComponentType.TEXT -> appendLine("    <TextView android:id=\"@+id/txt_${i}\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:text=\"${el.text}\" android:textSize=\"${el.fontSize}sp\" />")
                        DesignerComponentType.INPUT -> appendLine("    <EditText android:id=\"@+id/edt_${i}\" android:layout_width=\"match_parent\" android:layout_height=\"wrap_content\" android:hint=\"${el.text}\" />")
                        DesignerComponentType.CARD -> appendLine("    <androidx.cardview.widget.CardView android:layout_width=\"match_parent\" android:layout_height=\"wrap_content\" />")
                        DesignerComponentType.SWITCH -> appendLine("    <androidx.appcompat.widget.SwitchCompat android:layout_width=\"match_parent\" android:layout_height=\"wrap_content\" android:text=\"${el.text}\" />")
                        DesignerComponentType.CHECKBOX -> appendLine("    <CheckBox android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:text=\"${el.text}\" />")
                        DesignerComponentType.IMAGE -> appendLine("    <ImageView android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" />")
                        DesignerComponentType.PROGRESS -> appendLine("    <ProgressBar style=\"?android:attr/progressBarStyleHorizontal\" android:layout_width=\"match_parent\" android:layout_height=\"wrap_content\" />")
                        DesignerComponentType.FAB -> appendLine("    <com.google.android.material.floatingactionbutton.FloatingActionButton android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" />")
                    }
                }
                appendLine("</${layoutType.xmlTag}>")
            }
        }

        AlertDialog(
            onDismissRequest = { showCodeDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الكود المتولد", fontWeight = FontWeight.Bold)
                    Row {
                        FilterChip(
                            selected = generatedMode == "Compose",
                            onClick = { generatedMode = "Compose" },
                            label = { Text("Compose") }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = generatedMode == "XML",
                            onClick = { generatedMode = "XML" },
                            label = { Text("XML") }
                        )
                    }
                }
            },
            text = {
                Surface(
                    color = DarkNavy900,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = generatedCode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = ElectricCyan,
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showCodeDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}
