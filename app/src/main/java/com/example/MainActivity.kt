package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.ProjectRepository
import com.example.domain.model.Project
import com.example.domain.model.ProjectFile
import com.example.presentation.navigation.Screen
import com.example.core.setup.ToolchainManager
import com.example.presentation.screens.build.BuilderScreen
import com.example.presentation.screens.designer.DesignerScreen
import com.example.presentation.screens.editor.EditorScreen
import com.example.presentation.screens.home.HomeScreen
import com.example.presentation.screens.home.HomeViewModel
import com.example.presentation.screens.settings.SettingsScreen
import com.example.presentation.screens.setup.SetupScreen
import com.example.presentation.screens.terminal.TerminalScreen
import com.example.ui.theme.DarkNavy800
import com.example.ui.theme.DarkNavy900
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * النشاط الرئيسي لتطبيق MobileForge IDE (Android Studio على الهاتف)
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MobileForgeApp()
            }
        }
    }
}

@Composable
fun MobileForgeApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // تهيئة قاعدة البيانات والمستودع المحلي
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ProjectRepository(database) }
    val toolchainManager = remember { ToolchainManager(context) }
    val isSetupCompleted by toolchainManager.isSetupCompleted.collectAsState()

    // نموذج عرض الشاشة الرئيسية
    val homeViewModel: HomeViewModel = viewModel { HomeViewModel(repository) }

    // حالة الشاشة الحالية
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // المشروع النشط والملفات المفتوحة
    val activeProject by homeViewModel.activeProject.collectAsState()
    var currentProjectFiles by remember { mutableStateOf<List<ProjectFile>>(emptyList()) }

    // تحديث ملفات المشروع عند فتح المحرر
    fun loadProjectFiles(projectId: Long, onDone: () -> Unit) {
        coroutineScope.launch {
            val project = repository.getProjectById(projectId)
            if (project != null) {
                homeViewModel.selectActiveProject(project)
                val files = repository.getFilesForProject(projectId).firstOrNull() ?: emptyList()
                currentProjectFiles = files
                onDone()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = DarkNavy800,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                val screens = listOf(
                    Screen.Home,
                    Screen.Editor,
                    Screen.Designer,
                    Screen.Builder,
                    Screen.Terminal
                )

                screens.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (screen == Screen.Editor || screen == Screen.Designer || screen == Screen.Builder) {
                                val targetId = activeProject?.id
                                if (targetId != null) {
                                    loadProjectFiles(targetId) { currentScreen = screen }
                                } else {
                                    currentScreen = screen
                                }
                            } else {
                                currentScreen = screen
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkNavy900,
                            selectedTextColor = ElectricCyan,
                            indicatorColor = ElectricCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // معالجة زر الرجوع الفيزيائي / الإيماءات للعودة للشاشة الرئيسية
        if (currentScreen != Screen.Home) {
            BackHandler {
                currentScreen = Screen.Home
            }
        }

        when (currentScreen) {
            Screen.Home -> {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToEditor = { projectId ->
                        loadProjectFiles(projectId) { currentScreen = Screen.Editor }
                    },
                    onNavigateToDesigner = { projectId ->
                        loadProjectFiles(projectId) { currentScreen = Screen.Designer }
                    },
                    onNavigateToBuilder = { projectId ->
                        loadProjectFiles(projectId) { currentScreen = Screen.Builder }
                    },
                    onNavigateToTerminal = { projectId ->
                        if (projectId != null) {
                            loadProjectFiles(projectId) { currentScreen = Screen.Terminal }
                        } else {
                            currentScreen = Screen.Terminal
                        }
                    },
                    onNavigateToSettings = { currentScreen = Screen.Settings },
                    onNavigateToSetup = { currentScreen = Screen.Setup },
                    onExportZip = { project ->
                        coroutineScope.launch {
                            val files = repository.getFilesForProject(project.id).firstOrNull() ?: emptyList()
                            val zipFile = com.example.core.zip.ProjectZipManager.exportProjectToZipFile(context, project, files)
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "application/zip"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "مشروع ${project.name} من MobileForge IDE")
                                putExtra(android.content.Intent.EXTRA_TEXT, "تم تصدير مشروع ${project.name} بنجاح من MobileForge IDE بصيغة ZIP.")
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "مشاركة أرشيف المشروع ZIP"))
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            Screen.Editor -> {
                EditorScreen(
                    project = activeProject,
                    files = currentProjectFiles,
                    onSaveFile = { updatedFile ->
                        coroutineScope.launch {
                            repository.updateFile(updatedFile)
                        }
                    },
                    onCreateFile = { name, path ->
                        activeProject?.let { proj ->
                            coroutineScope.launch {
                                repository.createFile(proj.id, name, path, content = "// $name\n")
                                currentProjectFiles = repository.getFilesForProject(proj.id).firstOrNull() ?: emptyList()
                            }
                        }
                    },
                    onDeleteFile = { file ->
                        activeProject?.let { proj ->
                            coroutineScope.launch {
                                repository.deleteFile(file.id)
                                currentProjectFiles = repository.getFilesForProject(proj.id).firstOrNull() ?: emptyList()
                            }
                        }
                    },
                    onBackToProjects = { currentScreen = Screen.Home },
                    onNavigateToDesigner = { currentScreen = Screen.Designer },
                    onNavigateToBuilder = { currentScreen = Screen.Builder },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            Screen.Designer -> {
                DesignerScreen(
                    project = activeProject,
                    onBackToProjects = { currentScreen = Screen.Home },
                    onNavigateToEditor = { currentScreen = Screen.Editor },
                    onNavigateToBuilder = { currentScreen = Screen.Builder },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            Screen.Builder -> {
                BuilderScreen(
                    project = activeProject,
                    files = currentProjectFiles,
                    onBackToProjects = { currentScreen = Screen.Home },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            Screen.Terminal -> {
                TerminalScreen(
                    project = activeProject,
                    onBackToProjects = { currentScreen = Screen.Home },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            Screen.Settings -> {
                SettingsScreen(
                    onBackToProjects = { currentScreen = Screen.Home },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            Screen.Setup -> {
                SetupScreen(
                    toolchainManager = toolchainManager,
                    onCompleteSetup = { currentScreen = Screen.Home },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
