package com.storiq.feature.duplicates

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import com.storiq.core.storage.StorageRepository
import com.storiq.feature.duplicates.ui.DuplicatesScreen

fun NavGraphBuilder.duplicatesGraph(storageRepository: StorageRepository) {
    composable("duplicates") {
        DuplicatesScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = DuplicatesViewModelFactory(storageRepository)
            )
        )
    }
}

class DuplicatesViewModelFactory(
    private val repository: StorageRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel?> create(modelClass: Class<T>): T {
        return DuplicatesViewModel(repository) as T
    }
}