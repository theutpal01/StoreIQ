package com.storiq.feature.largefiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storiq.core.model.MediaCategory
import com.storiq.core.model.MediaRecord
import com.storiq.core.storage.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class LargeFilesViewModel(
    private val repository: StorageRepository
) : ViewModel() {

    private val _allFiles = MutableStateFlow<List<MediaRecord>>(emptyList())
    val allFiles = _allFiles.distinctUntilChanged()

    private val _filteredFiles = MutableStateFlow<List<MediaRecord>>(emptyList())
    val filteredFiles = _filteredFiles.distinctUntilChanged()

    private val _sizeGroups = MutableStateFlow<List<SizeGroup>>(emptyList())
    val sizeGroups = _sizeGroups.distinctUntilChanged()

    private val _selectedSort = MutableStateFlow<SortOption>(SortOption.SIZE_DESC)
    val selectedSort = _selectedSort.distinctUntilChanged()

    private val _selectedGroup = MutableStateFlow<SizeGroupFilter?>(null)
    val selectedGroup = _selectedGroup.distinctUntilChanged()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading = _isLoading.distinctUntilChanged()

    init {
        loadFiles()
    }

    fun loadFiles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val media = repository.getMediaByCategory(MediaCategory.IMAGES) +
                        repository.getMediaByCategory(MediaCategory.VIDEOS) +
                        repository.getMediaByCategory(MediaCategory.AUDIO) +
                        repository.getMediaByCategory(MediaCategory.DOCUMENTS) +
                        repository.getMediaByCategory(MediaCategory.DOWNLOADS) +
                        repository.getMediaByCategory(MediaCategory.ARCHIVES) +
                        repository.getMediaByCategory(MediaCategory.APKS) +
                        repository.getMediaByCategory(MediaCategory.OTHER)

                val files = media.sortedByDescending { it.sizeBytes }
                _allFiles.value = files
                applyFilters()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSort(sort: SortOption) {
        _selectedSort.value = sort
        applyFilters()
    }

    fun setGroupFilter(group: SizeGroupFilter?) {
        _selectedGroup.value = group
        applyFilters()
    }

    private fun applyFilters() {
        var files = _allFiles.value

        // Filter by group
        _selectedGroup.value?.let { group ->
            files = files.filter { it.sizeBytes in group.range }
        }

        // Sort
        files = when (_selectedSort.value) {
            SortOption.SIZE_DESC -> files.sortedByDescending { it.sizeBytes }
            SortOption.SIZE_ASC -> files.sortedBy { it.sizeBytes }
            SortOption.DATE_DESC -> files.sortedByDescending { it.modifiedDate }
            SortOption.DATE_ASC -> files.sortedBy { it.modifiedDate }
            SortOption.TYPE -> files.sortedBy { it.mimeType }
            SortOption.NAME -> files.sortedBy { it.displayName.lowercase() }
        }

        _filteredFiles.value = files
        updateSizeGroups(_allFiles.value)
    }

    private fun updateSizeGroups(files: List<MediaRecord>) {
        val groups = mutableListOf<SizeGroup>()

        val ranges = listOf(
            SizeGroupFilter("> 5 GB", 5L * 1024 * 1024 * 1024, Long.MAX_VALUE),
            SizeGroupFilter("1–5 GB", 1024L * 1024 * 1024, 5L * 1024 * 1024 * 1024 - 1),
            SizeGroupFilter("500 MB–1 GB", 500L * 1024 * 1024, 1024L * 1024 * 1024 - 1),
            SizeGroupFilter("100–500 MB", 100L * 1024 * 1024, 500L * 1024 * 1024 - 1)
        )

        for (range in ranges) {
            val groupFiles = files.filter { it.sizeBytes in range.range }
            if (groupFiles.isNotEmpty()) {
                val totalSize = groupFiles.sumOf { it.sizeBytes }
                groups.add(SizeGroup(
                    filter = range,
                    count = groupFiles.size,
                    totalSize = totalSize,
                    files = groupFiles
                ))
            }
        }

        _sizeGroups.value = groups
    }

    fun selectFile(uri: String, selected: Boolean) {
        // Selection handled by UI state
    }

    fun getSelectedFiles(): List<MediaRecord> {
        // Return selected files - would need selection state
        return emptyList()
    }

    enum class SortOption {
        SIZE_DESC("Size ↓"),
        SIZE_ASC("Size ↑"),
        DATE_DESC("Date ↓"),
        DATE_ASC("Date ↑"),
        TYPE("Type"),
        NAME("Name")

        val label: String
        SortOption(label: String) {
            this.label = label
        }
    }

    data class SizeGroupFilter(
        val label: String,
        val minSize: Long,
        val maxSize: Long
    ) {
        val range: ClosedRange<Long>
            get() = minSize..maxSize
    }

    data class SizeGroup(
        val filter: SizeGroupFilter,
        val count: Int,
        val totalSize: Long,
        val files: List<MediaRecord>
    )
}