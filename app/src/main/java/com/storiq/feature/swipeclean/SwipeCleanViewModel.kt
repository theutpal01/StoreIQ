package com.storiq.feature.swipeclean

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storiq.core.model.MediaCategory
import com.storiq.core.model.MediaRecord
import com.storiq.core.model.SwipeDecision
import com.storiq.core.model.SwipeMediaType
import com.storiq.core.model.SwipeSession
import com.storiq.core.storage.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class SwipeCleanViewModel(
    private val repository: StorageRepository
) : ViewModel() {

    private val _mediaItems = MutableStateFlow<List<MediaRecord>>(emptyList())
    val mediaItems = _mediaItems.distinctUntilChanged()

    private val _currentIndex = MutableStateFlow<Int>(0)
    val currentIndex = _currentIndex.distinctUntilChanged()

    private val _session = MutableStateFlow<SwipeSession?>(null)
    val session = _session.distinctUntilChanged()

    private val _decisions = MutableStateFlow<MutableMap<String, SwipeDecision>>(mutableMapOf())
    val decisions = _decisions.distinctUntilChanged()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading = _isLoading.distinctUntilChanged()

    private val _showReview = MutableStateFlow<Boolean>(false)
    val showReview = _showReview.distinctUntilChanged()

    var currentMediaType: SwipeMediaType = SwipeMediaType.PHOTOS

    fun loadMedia(mediaType: SwipeMediaType) {
        currentMediaType = mediaType
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val session = repository.createSwipeSession(mediaType)
                _session.value = session

                val items = when (mediaType) {
                    SwipeMediaType.PHOTOS -> repository.getMediaByCategory(MediaCategory.IMAGES)
                    SwipeMediaType.VIDEOS -> repository.getMediaByCategory(MediaCategory.VIDEOS)
                    SwipeMediaType.PHOTOS_AND_VIDEOS -> repository.getMediaByCategory(MediaCategory.IMAGES) + repository.getMediaByCategory(MediaCategory.VIDEOS)
                    SwipeMediaType.SMART_CLEAN -> getSmartCleanItems()
                }

                val updatedSession = session.copy(totalItems = items.size)
                repository.updateSwipeSession(updatedSession)
                _session.value = updatedSession

                _mediaItems.value = items
                _currentIndex.value = 0
                _decisions.value = mutableMapOf()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getSmartCleanItems(): List<MediaRecord> {
        // Priority order for Smart Clean:
        // 1. Duplicates, 2. Screenshots, 3. Very large media, 4. Old downloads,
        // 5. Screen recordings, 6. Older media, 7. Normal camera media
        val items = mutableListOf<MediaRecord>()
        
        // Duplicates first
        val duplicates = repository.getAllMediaWithHash()
            .groupBy { it.hash }
            .values
            .filter { it.size > 1 }
            .flatMap { it.drop(1) } // Keep first, add rest as duplicates
        items.addAll(duplicates)

        // Screenshots
        items.addAll(repository.getScreenshots())

        // Large media (>100MB)
        items.addAll(repository.getLargeMedia(100 * 1024 * 1024))

        // Screen recordings
        items.addAll(repository.getScreenRecordings())

        // Older media (sort by date, take oldest)
        val allMedia = repository.getMediaByCategory(MediaCategory.IMAGES) + repository.getMediaByCategory(MediaCategory.VIDEOS)
        val olderMedia = allMedia.sortedBy { it.modifiedDate }.take(50)
        items.addAll(olderMedia)

        return items.distinctBy { it.uri }
    }

    fun makeDecision(decision: SwipeDecision) {
        val currentIndex = _currentIndex.value
        val items = _mediaItems.value
        
        if (currentIndex < items.size) {
            val media = items[currentIndex]
            val newDecisions = _decisions.value.toMutableMap()
            newDecisions[media.uri] = decision
            _decisions.value = newDecisions

            val session = _session.value
            session?.let { s ->
                val updatedSession = when (decision) {
                    SwipeDecision.KEEP -> s.copy(keptItems = s.keptItems + 1)
                    SwipeDecision.DELETE -> s.copy(
                        deleteCandidates = s.deleteCandidates + 1,
                        totalCandidateBytes = s.totalCandidateBytes + media.sizeBytes
                    )
                    SwipeDecision.UNDECIDED -> s
                }
                repository.updateSwipeSession(updatedSession)
                _session.value = updatedSession
            }

            // Move to next item
            if (currentIndex + 1 < items.size) {
                _currentIndex.value = currentIndex + 1
            } else {
                // Session complete
                showReviewScreen()
            }
        }
    }

    fun undoLastDecision() {
        val currentIndex = _currentIndex.value
        if (currentIndex > 0) {
            val previousIndex = currentIndex - 1
            val items = _mediaItems.value
            if (previousIndex < items.size) {
                val media = items[previousIndex]
                val newDecisions = _decisions.value.toMutableMap()
                val previousDecision = newDecisions.remove(media.uri)
                
                val session = _session.value
                session?.let { s ->
                    val updatedSession = when (previousDecision) {
                        SwipeDecision.KEEP -> s.copy(keptItems = s.keptItems - 1)
                        SwipeDecision.DELETE -> s.copy(
                            deleteCandidates = s.deleteCandidates - 1,
                            totalCandidateBytes = s.totalCandidateBytes - media.sizeBytes
                        )
                        SwipeDecision.UNDECIDED, null -> s
                    }
                    repository.updateSwipeSession(updatedSession)
                    _session.value = updatedSession
                }
                
                _decisions.value = newDecisions
                _currentIndex.value = previousIndex
            }
        }
    }

    private fun showReviewScreen() {
        _showReview.value = true
    }

    fun getDeleteCandidates(): List<MediaRecord> {
        return _mediaItems.value.filter { _decisions.value[it.uri] == SwipeDecision.DELETE }
    }

    fun getKeptItems(): List<MediaRecord> {
        return _mediaItems.value.filter { _decisions.value[it.uri] == SwipeDecision.KEEP }
    }

    fun confirmDeletion() {
        viewModelScope.launch {
            val candidates = getDeleteCandidates()
            val uris = candidates.map { it.uri }
            if (uris.isNotEmpty()) {
                val result = repository.deleteMediaItems(uris)
                if (result.success) {
                    // Refresh media list
                    loadMedia(currentMediaType)
                }
            }
            _showReview.value = false
        }
    }

    fun cancelSession() {
        _showReview.value = false
        _currentIndex.value = 0
        _decisions.value = mutableMapOf()
    }

    fun selectAllForDeletion() {
        val newDecisions = _mediaItems.value.associateWith { SwipeDecision.DELETE }.toMutableMap()
        _decisions.value = newDecisions
        
        val session = _session.value
        session?.let { s ->
            val totalBytes = _mediaItems.value.sumOf { it.sizeBytes }
            val updatedSession = s.copy(
                deleteCandidates = _mediaItems.value.size,
                keptItems = 0,
                totalCandidateBytes = totalBytes
            )
            repository.updateSwipeSession(updatedSession)
            _session.value = updatedSession
        }
    }

    fun deselectAll() {
        _decisions.value.clear()
        
        val session = _session.value
        session?.let { s ->
            val updatedSession = s.copy(
                deleteCandidates = 0,
                keptItems = _mediaItems.value.size,
                totalCandidateBytes = 0
            )
            repository.updateSwipeSession(updatedSession)
            _session.value = updatedSession
        }
    }
}