package com.storiq.feature.duplicates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storiq.core.model.DuplicateGroup
import com.storiq.core.model.MediaRecord
import com.storiq.core.storage.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class DuplicatesViewModel(
    private val repository: StorageRepository
) : ViewModel() {

    private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateGroups = _duplicateGroups.distinctUntilChanged()

    private val _selectedGroups = MutableStateFlow<MutableSet<Long>>(mutableSetOf())
    val selectedGroups = _selectedGroups.distinctUntilChanged()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading = _isLoading.distinctUntilChanged()

    private val _showReview = MutableStateFlow<Boolean>(false)
    val showReview = _showReview.distinctUntilChanged()

    init {
        loadDuplicates()
    }

    fun loadDuplicates() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val groups = repository.getDuplicateGroups()
                _duplicateGroups.value = groups
                _selectedGroups.value = mutableSetOf()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                val groups = repository.findDuplicates()
                _duplicateGroups.value = groups
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun toggleGroupSelection(groupId: Long) {
        val newSelected = _selectedGroups.value.toMutableSet()
        if (newSelected.contains(groupId)) {
            newSelected.remove(groupId)
        } else {
            newSelected.add(groupId)
        }
        _selectedGroups.value = newSelected
    }

    fun selectAllGroups() {
        _selectedGroups.value = _duplicateGroups.value.map { it.id }.toMutableSet()
    }

    fun deselectAllGroups() {
        _selectedGroups.value.clear()
    }

    fun getSelectedGroups(): List<DuplicateGroup> {
        return _duplicateGroups.value.filter { _selectedGroups.value.contains(it.id) }
    }

    fun getSelectedCount(): Int {
        return _selectedGroups.value.size
    }

    fun getSelectedTotalSize(): Long {
        return _duplicateGroups.value
            .filter { _selectedGroups.value.contains(it.id) }
            .sumOf { it.totalWastedBytes }
    }

    fun getSelectedFiles(): List<MediaRecord> {
        return _duplicateGroups.value
            .filter { _selectedGroups.value.contains(it.id) }
            .flatMap { group ->
                group.files.minusElement(group.recommendedKeepUri)
                    .map { uri ->
                        // Would need to fetch from database - simplified
                        MediaRecord(
                            uri = uri,
                            displayName = "",
                            mimeType = "",
                            sizeBytes = group.sizeBytes,
                            modifiedDate = 0,
                            createdDate = 0,
                            relativePath = null,
                            width = null,
                            height = null,
                            durationMs = null,
                            hash = group.hash,
                            category = group.category,
                            scanSessionId = 0
                        )
                    }
            }
    }

    fun showReviewScreen() {
        _showReview.value = true
    }

    fun hideReviewScreen() {
        _showReview.value = false
    }

    fun confirmDeletion() {
        viewModelScope.launch {
            val selected = getSelectedGroups()
            val allUris = selected.flatMap { group ->
                group.files.minusElement(group.recommendedKeepUri)
            }
            if (allUris.isNotEmpty()) {
                val result = repository.deleteMediaItems(allUris)
                if (result.success) {
                    loadDuplicates()
                }
            }
            _showReview.value = false
        }
    }

    fun getGroupFiles(group: DuplicateGroup): List<String> {
        // Return all file URIs in group except recommended keep
        return group.files.minusElement(group.recommendedKeepUri)
    }

    fun getRecommendedKeepUri(group: DuplicateGroup): String? {
        return group.recommendedKeepUri
    }
}