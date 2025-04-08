package com.example.wallpaper.ui.presentation.WallpaperList

import android.net.Uri
import androidx.compose.runtime.Immutable
import java.io.File

@Immutable
sealed interface WallPapersListScreenState {
    data object EmptyState : WallPapersListScreenState

    data object LoadingState : WallPapersListScreenState

    data class WallPapers(
        val videos: List<File>,
    ) : WallPapersListScreenState
}
