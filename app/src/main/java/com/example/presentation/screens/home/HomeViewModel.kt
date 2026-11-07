package com.example.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ProjectRepository
import com.example.domain.model.Project
import com.example.domain.model.ProjectType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * نموذج العرض لشاشة المشاريع الرئيسية (Home Dashboard)
 */
class HomeViewModel(private val repository: ProjectRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<ProjectType?>(null)
    val selectedTypeFilter: StateFlow<ProjectType?> = _selectedTypeFilter.asStateFlow()

    private val _showNewProjectDialog = MutableStateFlow(false)
    val showNewProjectDialog: StateFlow<Boolean> = _showNewProjectDialog.asStateFlow()

    private val _preselectedTemplate = MutableStateFlow(ProjectType.COMPOSE)
    val preselectedTemplate: StateFlow<ProjectType> = _preselectedTemplate.asStateFlow()

    private val _projectToDelete = MutableStateFlow<Project?>(null)
    val projectToDelete: StateFlow<Project?> = _projectToDelete.asStateFlow()

    private val _activeProject = MutableStateFlow<Project?>(null)
    val activeProject: StateFlow<Project?> = _activeProject.asStateFlow()

    val projects: StateFlow<List<Project>> = combine(
        repository.getAllProjects(),
        _searchQuery,
        _selectedTypeFilter
    ) { allProjects, query, filter ->
        allProjects.filter { project ->
            val matchesQuery = query.isBlank() ||
                    project.name.contains(query, ignoreCase = true) ||
                    project.packageName.contains(query, ignoreCase = true)
            val matchesFilter = filter == null || project.type == filter
            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.seedInitialProjectsIfEmpty()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(type: ProjectType?) {
        _selectedTypeFilter.value = if (_selectedTypeFilter.value == type) null else type
    }

    fun openNewProjectDialog(template: ProjectType = ProjectType.COMPOSE) {
        _preselectedTemplate.value = template
        _showNewProjectDialog.value = true
    }

    fun closeNewProjectDialog() {
        _showNewProjectDialog.value = false
    }

    fun createProject(
        name: String,
        packageName: String,
        type: ProjectType,
        description: String = "",
        minSdk: Int = 26
    ) {
        viewModelScope.launch {
            val newId = repository.createProject(
                name = name,
                packageName = packageName,
                type = type,
                description = description,
                minSdk = minSdk
            )
            _showNewProjectDialog.value = false
            // تعيين المشروع كمشروع نشط
            val created = repository.getProjectById(newId)
            _activeProject.value = created
        }
    }

    fun requestDeleteProject(project: Project) {
        _projectToDelete.value = project
    }

    fun dismissDeleteDialog() {
        _projectToDelete.value = null
    }

    fun confirmDeleteProject() {
        val project = _projectToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteProject(project.id)
            if (_activeProject.value?.id == project.id) {
                _activeProject.value = null
            }
            _projectToDelete.value = null
        }
    }

    fun selectActiveProject(project: Project) {
        _activeProject.value = project
    }
}
