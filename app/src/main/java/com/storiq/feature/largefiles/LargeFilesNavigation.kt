package com.storiq.feature.largefiles

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import com.storiq.core.storage.StorageRepository
import com.storiq.feature.largefiles.ui.LargeFilesScreen

fun NavGraphBuilder.largeFilesGraph(storageRepository: StorageRepository) {
    composable("large_files") {
        LargeFilesScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = LargeFilesViewModel.Factory(storageRepository)
            )
        )
    }
}

class LargeFilesViewModel.Factory(
    private val repository: StorageRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel?> create(modelClass: Class<T>): T {
        return LargeFilesViewModel(repository) as T
    }
}