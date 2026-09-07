package com.storiq.feature.history

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import com.storiq.core.storage.StorageRepository
import com.storiq.feature.history.ui.HistoryScreen

fun NavGraphBuilder.historyGraph(storageRepository: StorageRepository) {
    composable("history") {
        HistoryScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = HistoryViewModelFactory(storageRepository)
            )
        )
    }
}

class HistoryViewModelFactory(
    private val repository: StorageRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel?> create(modelClass: Class<T>): T {
        return HistoryViewModel(repository) as T
    }
}