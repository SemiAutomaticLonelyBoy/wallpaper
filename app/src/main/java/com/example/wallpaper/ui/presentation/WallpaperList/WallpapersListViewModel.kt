package com.example.wallpaper.ui.presentation.WallpaperList

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallpaper.data.repositories.FileSystemRepository
import com.example.wallpaper.data.repositories.WallpaperServiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class WallpapersListViewModel(
    private val fileSystemRepository: FileSystemRepository,
    private val wallpaperServiceRepository: WallpaperServiceRepository,
) : ViewModel() {

    private val mutableState: MutableStateFlow<WallPapersListScreenState> = MutableStateFlow(
        WallPapersListScreenState.LoadingState
    )
    val state: StateFlow<WallPapersListScreenState> = mutableState.asStateFlow()

    init {
        getVideoFromApplication()
    }

    private val mutableActions: Channel<OutputAction> = Channel()
    val action: Flow<OutputAction> = mutableActions.receiveAsFlow()

    fun inputAction(action: InputAction) {
        when (action) {
            is InputAction.LoadVideoFromStorage -> loadVideoToStorage(action.uris)
        }
    }

    private fun getVideoFromApplication() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                fileSystemRepository.getDataFromAppFileSystem()
            }.onSuccess { videos: List<File> ->
                mutableState.update {
                    if (videos.isEmpty()) {
                        WallPapersListScreenState.EmptyState
                    } else {
                        WallPapersListScreenState.WallPapers(
                            videos = videos,
                        )
                    }
                }
            }.onFailure { exception: Throwable ->
                mutableState.update {
                    WallPapersListScreenState.EmptyState
                }

                println(exception.message)
            }

        }
    }


    private fun loadVideoToStorage(uris: List<Uri>) {
        val oldVideos =
            (state.value as? WallPapersListScreenState.WallPapers)?.videos ?: emptyList()

        mutableState.update {
            WallPapersListScreenState.LoadingState
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                fileSystemRepository.setDataToAppFileSystem(uris)
            }.onSuccess { videos: List<File> ->
                mutableState.update {
                    if (videos.isEmpty()) {
                        WallPapersListScreenState.EmptyState
                    } else {
                        WallPapersListScreenState.WallPapers(
                            videos = oldVideos.plus(videos).distinct(),
                        )
                    }
                }
                if (oldVideos.isEmpty()) wallpaperServiceRepository.startWallpaperService()
            }.onFailure { exception: Throwable ->
                exception.message
            }
        }
    }

    sealed interface OutputAction {

    }

    sealed interface InputAction {
        data class LoadVideoFromStorage(val uris: List<Uri>) : InputAction
    }

}