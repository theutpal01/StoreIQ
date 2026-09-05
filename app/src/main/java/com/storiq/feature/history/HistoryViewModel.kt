package com.storiq.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storiq.core.model.MediaCategory
import com.storiq.core.model.StorageBreakdown
import com.storiq.core.model.StorageSnapshot
import com.storiq.core.storage.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: StorageRepository
) : ViewModel() {

    private val _snapshots = MutableStateFlow<List<StorageSnapshot>>(emptyList())
    val snapshots = _snapshots.distinctUntilChanged()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading = _isLoading.distinctUntilChanged()

    private val _timeRange = MutableStateFlow<TimeRange>(TimeRange.LAST_30_DAYS)
    val timeRange = _timeRange.distinctUntilChanged()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val endTime = System.currentTimeMillis()
                val startTime = when (_timeRange.value) {
                    TimeRange.LAST_7_DAYS -> endTime - 7 * 24 * 60 * 60 * 1000L
                    TimeRange.LAST_30_DAYS -> endTime - 30 * 24 * 60 * 60 * 1000L
                    TimeRange.LAST_90_DAYS -> endTime - 90 * 24 * 60 * 60 * 1000L
                    TimeRange.LAST_365_DAYS -> endTime - 365 * 24 * 60 * 60 * 1000L
                    TimeRange.ALL_TIME -> 0
                }
                
                val snapshots = if (startTime == 0) {
                    repository.getStorageHistory(1000)
                } else {
                    repository.getStorageHistoryRange(startTime, endTime)
                }
                
                _snapshots.value = snapshots
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
        loadHistory()
    }

    val latestSnapshot: StorageSnapshot?
        get() = _snapshots.value.firstOrNull()

    val storageGrowth: Long
        get() {
            val list = _snapshots.value
            if (list.size < 2) return 0L
            return list.first().usedBytes - list.last().usedBytes
        }

    val storageGrowthPercent: Float
        get() {
            val list = _snapshots.value
            if (list.size < 2) return 0f
            val oldest = list.last().usedBytes
            if (oldest == 0L) return 0f
            return ((list.first().usedBytes - oldest).toFloat() / oldest) * 100
        }

    val averageDailyGrowth: Long
        get() {
            val list = _snapshots.value
            if (list.size < 2) return 0L
            val days = (list.first().timestamp - list.last().timestamp) / (24 * 60 * 60 * 1000L)
            if (days == 0L) return 0L
            return (list.first().usedBytes - list.last().usedBytes) / days
        }

    val projectedFullDate: String?
        get() {
            val growth = averageDailyGrowth
            if (growth <= 0) return null
            val latest = latestSnapshot
            latest?.let { snapshot ->
                val freeSpace = snapshot.freeBytes
                val daysToFull = freeSpace / growth
                if (daysToFull > 3650) return "Never (at current rate)"
                val projectedTime = snapshot.timestamp + daysToFull * 24 * 60 * 60 * 1000L
                return java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(projectedTime))
            }
            return null
        }

    enum class TimeRange {
        LAST_7_DAYS("Last 7 days"),
        LAST_30_DAYS("Last 30 days"),
        LAST_90_DAYS("Last 90 days"),
        LAST_365_DAYS("Last year"),
        ALL_TIME("All time")

        val label: String
        TimeRange(label: String) {
            this.label = label
        }
    }

    data class ChartDataPoint(
        val timestamp: Long,
        val usedGB: Float,
        val freeGB: Float,
        val appsGB: Float,
        val mediaGB: Float,
        val docsGB: Float,
        val otherGB: Float
    )

    val chartData: List<ChartDataPoint>
        get() = _snapshots.value.map { snapshot ->
            ChartDataPoint(
                timestamp = snapshot.timestamp,
                usedGB = snapshot.usedBytes / (1024f * 1024 * 1024),
                freeGB = snapshot.freeBytes / (1024f * 1024 * 1024),
                appsGB = snapshot.appBytes / (1024f * 1024 * 1024),
                mediaGB = snapshot.mediaBytes / (1024f * 1024 * 1024),
                docsGB = snapshot.documentsBytes / (1024f * 1024 * 1024),
                otherGB = snapshot.otherBytes / (1024f * 1024 * 1024)
            )
        }.reversed() // Oldest first for chart
}