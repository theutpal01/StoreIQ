package com.storiq.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storiq.core.model.MediaCategory
import com.storiq.core.model.Recommendation
import com.storiq.core.model.StorageBreakdown
import com.storiq.core.storage.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: StorageRepository
) : ViewModel() {

    class Factory(private val repository: StorageRepository) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }

    private val _storageBreakdown = MutableStateFlow<List<StorageBreakdown>>(emptyList())
    val storageBreakdown = _storageBreakdown.distinctUntilChanged()

    private val _totalStorage = MutableStateFlow<String>("")
    val totalStorage = _totalStorage.distinctUntilChanged()

    private val _usedStorage = MutableStateFlow<String>("")
    val usedStorage = _usedStorage.distinctUntilChanged()

    private val _freeStorage = MutableStateFlow<String>("")
    val freeStorage = _freeStorage.distinctUntilChanged()

    private val _usagePercent = MutableStateFlow<Float>(0f)
    val usagePercent = _usagePercent.distinctUntilChanged()

    private val _recommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    val recommendations = _recommendations.distinctUntilChanged()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading = _isLoading.distinctUntilChanged()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load storage stats
                val totalBytes = repository.getTotalStorageBytes()
                val usedBytes = repository.getUsedStorageBytes()
                val freeBytes = repository.getFreeStorageBytes()
                val usagePercent = repository.getStorageUsagePercent()

                _totalStorage.value = formatBytes(totalBytes)
                _usedStorage.value = formatBytes(usedBytes)
                _freeStorage.value = formatBytes(freeBytes)
                _usagePercent.value = usagePercent

                // Load storage breakdown
                val breakdown = repository.getStorageBreakdown()
                _storageBreakdown.value = breakdown

                // Load recommendations
                val recommendations = repository.getActiveRecommendations()
                _recommendations.value = recommendations
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshStorageData() {
        viewModelScope.launch {
            try {
                val totalBytes = repository.getTotalStorageBytes()
                val usedBytes = repository.getUsedStorageBytes()
                val freeBytes = repository.getFreeStorageBytes()
                val usagePercent = repository.getStorageUsagePercent()

                _totalStorage.value = formatBytes(totalBytes)
                _usedStorage.value = formatBytes(usedBytes)
                _freeStorage.value = formatBytes(freeBytes)
                _usagePercent.value = usagePercent

                val breakdown = repository.getStorageBreakdown()
                _storageBreakdown.value = breakdown

                val recommendations = repository.getActiveRecommendations()
                _recommendations.value = recommendations
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun startFullScan(): StorageRepositoryImpl.FullScanResult {
        // This would be called from UI with proper coroutine context
        return StorageRepositoryImpl.FullScanResult(success = false, errorMessage = "Call from UI coroutine")
    }

    companion object {
        fun formatBytes(bytes: Long): String {
            return when {
                bytes >= 1_099_511_627_776L -> "%.2f TB".format(bytes / 1_099_511_627_776.0)
                bytes >= 1_073_741_824L -> "%.2f GB".format(bytes / 1_073_741_824.0)
                bytes >= 1_048_576L -> "%.2f MB".format(bytes / 1_048_576.0)
                bytes >= 1024L -> "%.2f KB".format(bytes / 1024.0)
                else -> "$bytes B"
            }
        }
    }
}