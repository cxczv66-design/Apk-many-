package com.example.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Project
import com.example.domain.model.ProjectType
import com.example.presentation.components.NewProjectDialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * شاشة لوحة تحكم مشاريع MobileForge IDE الرئيسية (Home Dashboard)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToEditor: (projectId: Long) -> Unit,
    onNavigateToDesigner: (projectId: Long) -> Unit,
    onNavigateToBuilder: (projectId: Long) -> Unit,
    onNavigateToTerminal: (projectId: Long?) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSetup: () -> Unit = {},
    onExportZip: (Project) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedTypeFilter.collectAsState()
    val showNewProjectDialog by viewModel.showNewProjectDialog.collectAsState()
    val preselectedTemplate by viewModel.preselectedTemplate.collectAsState()
    val projectToDelete by viewModel.projectToDelete.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()

    var showSearchField by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(listOf(ElectricCyan, ForgeAmber))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = DarkNavy900,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MobileForge",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ElectricCyan
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "IDE",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ForgeAmber
                                    )
                                )
                            }
                            Text(
                                text = "Android Studio on Mobile v1.0",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSearchField = !showSearchField },
                        modifier = Modifier.testTag("toggle_search_button")
                    ) {
                        Icon(
                            imageVector = if (showSearchField) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = ElectricCyan
                        )
                    }
                    IconButton(
                        onClick = { onNavigateToTerminal(activeProject?.id) },
                        modifier = Modifier.testTag("top_terminal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "طرفية الأوامر",
                            tint = ForgeAmber
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("top_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات والمكتبات",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openNewProjectDialog() },
                containerColor = ElectricCyan,
                contentColor = DarkNavy900,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("مشروع جديد", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_new_project")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // شريط البحث المنسدل
            item {
                AnimatedVisibility(visible = showSearchField, enter = fadeIn(), exit = fadeOut()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("ابحث في أسماء وحزم المشاريع...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "مسح")
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_projects_input")
                        )
                    }
                }
            }

            // بطاقة تشخيص أدوات البناء والمحرك البرمجي (Toolchain Diagnostic Banner)
            item {
                SystemEngineDiagnosticCard(
                    projectCount = projects.size,
                    activeProject = activeProject,
                    onOpenTerminal = { onNavigateToTerminal(activeProject?.id) },
                    onOpenSetup = onNavigateToSetup
                )
            }

            // شبكة الوصول السريع لأدوات الاستوديو (Quick Action Tiles)
            item {
                QuickStudioActionsRow(
                    onNewProject = { viewModel.openNewProjectDialog() },
                    onOpenEditor = {
                        val target = activeProject ?: projects.firstOrNull()
                        if (target != null) onNavigateToEditor(target.id) else viewModel.openNewProjectDialog()
                    },
                    onOpenDesigner = {
                        val target = activeProject ?: projects.firstOrNull()
                        if (target != null) onNavigateToDesigner(target.id) else viewModel.openNewProjectDialog()
                    },
                    onOpenBuilder = {
                        val target = activeProject ?: projects.firstOrNull()
                        if (target != null) onNavigateToBuilder(target.id) else viewModel.openNewProjectDialog()
                    }
                )
            }

            // قسم قوالب المشاريع الجاهزة (Project Templates)
            item {
                SectionHeader(
                    title = "قوالب المشاريع الجاهزة",
                    subtitle = "ابدأ فوراً بتطبيق متكامل بنقرة واحدة"
                )
                TemplatesCarousel(
                    onSelectTemplate = { template ->
                        viewModel.openNewProjectDialog(template)
                    }
                )
            }

            // تصفية المشاريع (Filter Chips)
            item {
                SectionHeader(
                    title = "مشاريعي النشطة (${projects.size})",
                    subtitle = "إدارة الأكواد والواجهات وترجمة التطبيقات إلى APK"
                )
                FilterChipsRow(
                    selectedFilter = selectedFilter,
                    onSelect = { viewModel.onFilterChange(it) }
                )
            }

            // قائمة المشاريع أو الحالة الفارغة
            if (projects.isEmpty()) {
                item {
                    EmptyProjectsState(
                        isFiltered = searchQuery.isNotEmpty() || selectedFilter != null,
                        onCreateNew = { viewModel.openNewProjectDialog() },
                        onClearFilters = {
                            viewModel.onSearchQueryChange("")
                            viewModel.onFilterChange(null)
                        }
                    )
                }
            } else {
                items(projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        isActive = activeProject?.id == project.id,
                        onSelect = { viewModel.selectActiveProject(project) },
                        onOpenEditor = { onNavigateToEditor(project.id) },
                        onOpenDesigner = { onNavigateToDesigner(project.id) },
                        onBuild = { onNavigateToBuilder(project.id) },
                        onExportZip = { onExportZip(project) },
                        onDelete = { viewModel.requestDeleteProject(project) }
                    )
                }
            }
        }
    }

    // نافذة إنشاء مشروع جديد
    if (showNewProjectDialog) {
        NewProjectDialog(
            initialTemplate = preselectedTemplate,
            onDismiss = { viewModel.closeNewProjectDialog() },
            onCreate = { name, pkg, type, desc, sdk ->
                viewModel.createProject(name, pkg, type, desc, sdk)
            }
        )
    }

    // نافذة تأكيد الحذف
    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("حذف المشروع؟", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "هل أنت متأكد من رغبتك في حذف المشروع '${project.name}' (${project.packageName}) نهائياً؟ سيتم مسح جميع الملفات والشفرات المصدرية التابعة له."
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeleteProject() },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("حذف نهائي", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

/**
 * بطاقة إحصائيات وتشخيص محرك بيئة التطوير (Toolchain Status)
 */
@Composable
private fun SystemEngineDiagnosticCard(
    projectCount: Int,
    activeProject: Project?,
    onOpenTerminal: () -> Unit,
    onOpenSetup: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("system_diagnostic_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ElectricCyan.copy(alpha = 0.5f), ForgeAmber.copy(alpha = 0.5f))))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CodeGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "محرك بيئة التطوير جاهز للعمل (Ready)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CodeGreen
                        )
                    )
                }
                Text(
                    text = "إدارة الأدوات والمحاكي ⚙",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = DarkNavy900,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricCyan)
                        .clickable { onOpenSetup() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusPill(label = "مترجم ECJ/Kotlin", value = "مدمج v2.2", color = ElectricCyan)
                StatusPill(label = "معالج AAPT2", value = "نشط 100%", color = ForgeAmber)
                StatusPill(label = "محول D8/R8", value = "متاح Dex", color = PurpleAccent)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkNavy900)
                    .clickable { onOpenTerminal() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$ ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    )
                    Text(
                        text = if (activeProject != null) "project: ${activeProject.name} | gradlew build: ready" else "mobileforge shell: ready | $projectCount projects",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = TextMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

/**
 * شريط إجراءات الاستوديو السريعة
 */
@Composable
private fun QuickStudioActionsRow(
    onNewProject: () -> Unit,
    onOpenEditor: () -> Unit,
    onOpenDesigner: () -> Unit,
    onOpenBuilder: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            title = "محرر الكود",
            subtitle = "Sora Editor",
            icon = Icons.Default.Code,
            color = ElectricCyan,
            modifier = Modifier.weight(1f),
            onClick = onOpenEditor
        )
        QuickActionButton(
            title = "مصمم الواجهات",
            subtitle = "Drag & Drop",
            icon = Icons.Default.DesignServices,
            color = ForgeAmber,
            modifier = Modifier.weight(1f),
            onClick = onOpenDesigner
        )
        QuickActionButton(
            title = "باني الـ APK",
            subtitle = "D8 & Signer",
            icon = Icons.Default.Build,
            color = CodeGreen,
            modifier = Modifier.weight(1f),
            onClick = onOpenBuilder
        )
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

/**
 * شريط قوالب المشاريع الجاهزة
 */
@Composable
private fun TemplatesCarousel(onSelectTemplate: (ProjectType) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ProjectType.values()) { template ->
            TemplateCard(template = template, onSelect = { onSelectTemplate(template) })
        }
    }
}

@Composable
private fun TemplateCard(template: ProjectType, onSelect: () -> Unit) {
    val accentColor = when (template) {
        ProjectType.COMPOSE -> ElectricCyan
        ProjectType.XML_KOTLIN -> PurpleAccent
        ProjectType.XML_JAVA -> ForgeAmber
        ProjectType.WEB_APP -> ForgeOrange
        ProjectType.BLANK -> CodeGreen
    }

    Card(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .testTag("template_card_${template.name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.6f), Color.Transparent)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.language,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = "استخدام القالب",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = template.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = template.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * شريط تصفية المشاريع
 */
@Composable
private fun FilterChipsRow(
    selectedFilter: ProjectType?,
    onSelect: (ProjectType?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onSelect(null) },
                label = { Text("الكل") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                    selectedLabelColor = ElectricCyan
                )
            )
        }
        items(ProjectType.values()) { type ->
            FilterChip(
                selected = selectedFilter == type,
                onClick = { onSelect(type) },
                label = { Text(type.language) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                    selectedLabelColor = ElectricCyan
                )
            )
        }
    }
}

/**
 * بطاقة عرض المشروع المفصلة
 */
@Composable
private fun ProjectCard(
    project: Project,
    isActive: Boolean,
    onSelect: () -> Unit,
    onOpenEditor: () -> Unit,
    onOpenDesigner: () -> Unit,
    onBuild: () -> Unit,
    onExportZip: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val formattedDate = remember(project.updatedAt) { dateFormat.format(Date(project.updatedAt)) }

    val accentColor = when (project.type) {
        ProjectType.COMPOSE -> ElectricCyan
        ProjectType.XML_KOTLIN -> PurpleAccent
        ProjectType.XML_JAVA -> ForgeAmber
        ProjectType.WEB_APP -> ForgeOrange
        ProjectType.BLANK -> CodeGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .testTag("project_item_${project.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = if (isActive) {
            CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ElectricCyan, ForgeAmber)))
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // الصف العلوي: الاسم ونوع القالب وتاريخ التحديث
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (project.type) {
                                ProjectType.COMPOSE -> Icons.Default.Widgets
                                ProjectType.XML_KOTLIN -> Icons.Default.PhoneAndroid
                                ProjectType.XML_JAVA -> Icons.Default.Code
                                ProjectType.WEB_APP -> Icons.Default.Language
                                ProjectType.BLANK -> Icons.Default.Folder
                            },
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = project.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "نشط",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = DarkNavy900,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ElectricCyan)
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = project.packageName,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onExportZip,
                        modifier = Modifier.testTag("export_zip_${project.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = "تصدير كـ ZIP",
                            tint = ForgeAmber.copy(alpha = 0.85f)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_project_${project.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف المشروع",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (project.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // شارات المعلومات (الملفات، الإصدار، حالة البناء)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgeInfo(label = "${project.fileCount} ملفات", color = TextSecondary)
                BadgeInfo(label = "API ${project.minSdk}+", color = TextSecondary)
                BadgeInfo(label = formattedDate, color = TextMuted)
                Spacer(modifier = Modifier.weight(1f))
                // شارة حالة آخر بناء
                val (statusText, statusColor) = when (project.lastBuildSuccess) {
                    true -> "APK ناجح" to CodeGreen
                    false -> "فشل البناء" to ErrorRed
                    null -> "جاهز للبناء" to ForgeAmber
                }
                BadgeInfo(label = statusText, color = statusColor)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // شريط الإجراءات المباشرة للمشروع
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenEditor,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_editor_${project.id}"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("المحرر", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenDesigner,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_designer_${project.id}"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.DesignServices, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("المصمم", fontSize = 12.sp)
                }

                Button(
                    onClick = onBuild,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("build_apk_${project.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = DarkNavy900
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("بناء APK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun BadgeInfo(label: String, color: Color) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyProjectsState(
    isFiltered: Boolean,
    onCreateNew: () -> Unit,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isFiltered) Icons.Default.SearchOff else Icons.Default.FolderOpen,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isFiltered) "لا توجد مشاريع مطابقة للبحث" else "لا توجد مشاريع حالياً",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isFiltered) "جرب تغيير كلمات البحث أو إعادة تعيين الفلاتر" else "ابدأ رحلتك البرمجية بإنشاء أول تطبيق أندرويد على هاتفك الآن!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (isFiltered) {
                OutlinedButton(onClick = onClearFilters) {
                    Text("إعادة ضبط الفلاتر")
                }
            } else {
                Button(
                    onClick = onCreateNew,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DarkNavy900)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إنشاء مشروعي الأول", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
