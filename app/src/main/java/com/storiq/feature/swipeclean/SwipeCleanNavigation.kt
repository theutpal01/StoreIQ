package com.storiq.feature.swipeclean

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import com.storiq.core.storage.StorageRepository
import com.storiq.feature.swipeclean.ui.SwipeCleanGalleryScreen
import com.storiq.feature.swipeclean.ui.SwipeCleanReviewScreen

fun NavGraphBuilder.swipeCleanGraph(storageRepository: StorageRepository) {
    composable("swipe_clean") {
        SwipeCleanGalleryScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = SwipeCleanViewModel.Factory(storageRepository)
            )
        )
    }
    composable("swipe_clean_review") {
        SwipeCleanReviewScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = SwipeCleanViewModel.Factory(storageRepository)
            )
        )
    }
}

class SwipeCleanViewModel.Factory(
    private val repository: StorageRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel?> create(modelClass: Class<T>): T {
        return SwipeCleanViewModel(repository) as T
    }
}