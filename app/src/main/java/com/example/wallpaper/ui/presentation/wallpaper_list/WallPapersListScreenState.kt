package com.example.wallpaper.ui.presentation.wallpaper_list

import androidx.compose.runtime.Immutable
import java.io.File

@Immutable
sealed interface WallPapersListScreenState {
    data object EmptyState : WallPapersListScreenState

    data object LoadingState : WallPapersListScreenState

    data class WallPapers(
        val videos: List<File>,
        val isEditModeActive: Boolean = false,
        val selectedVideos: Set<String> = setOf(),
    ) : WallPapersListScreenState

}
