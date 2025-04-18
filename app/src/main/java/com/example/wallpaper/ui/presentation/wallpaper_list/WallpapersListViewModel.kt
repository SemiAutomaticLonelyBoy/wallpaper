package com.example.wallpaper.ui.presentation.wallpaper_list

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallpaper.data.repositories.FileSystemRepository
import com.example.wallpaper.data.repositories.LogsRepository
import com.example.wallpaper.data.repositories.WallpaperServiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class WallpapersListViewModel(
    private val fileSystemRepository: FileSystemRepository,
    private val wallpaperServiceRepository: WallpaperServiceRepository,
    private val logsRepository: LogsRepository,
) : ViewModel() {

    private val mutableState: MutableStateFlow<WallPapersListScreenState> = MutableStateFlow(
        WallPapersListScreenState.LoadingState
    )
    val state: StateFlow<WallPapersListScreenState> = mutableState.asStateFlow()

    init {
        getVideoFromApplication()
        viewModelScope.launch {
            fileSystemRepository.files.collect { videos: List<File> ->
                mutableState.update {
                    if (videos.isEmpty()) {
                        WallPapersListScreenState.EmptyState
                    } else {
                        WallPapersListScreenState.WallPapers(
                            videos = videos,
                        )
                    }
                }
                if (videos.isEmpty()) {
                    wallpaperServiceRepository.stopWallpaperService()
                }
            }
        }
    }

    fun inputAction(action: InputAction) {
        when (action) {
            is InputAction.LoadVideoFromStorage -> loadVideoToStorage(action.uris)
            is InputAction.SetEditableMode -> setEditableMode(action.isEditable)
            is InputAction.OnVideoClick -> onVideoClick(action.videoName)
            InputAction.DeleteVideos -> deleteVideos()
        }
    }

    private fun deleteVideos() {
        val success = (state.value as? WallPapersListScreenState.WallPapers) ?: return

        mutableState.update {
            WallPapersListScreenState.WallPapers(
                isEditModeActive = false,
                selectedVideos = setOf(),
                videos = success.videos
            )
        }
        viewModelScope.launch {
            runCatching {
                fileSystemRepository.deleteDataFromAppFileSystemByFileName(success.selectedVideos)
            }.onFailure { exception: Throwable ->
                logsRepository.saveLogs(exception.message ?: "Не удалось удалить видео")
            }
        }
    }

    private fun onVideoClick(videoName: String) {
        val success = (state.value as? WallPapersListScreenState.WallPapers) ?: return

        val newValue = if (success.selectedVideos.contains(videoName)) {
            success.selectedVideos.minus(videoName)
        } else {
            success.selectedVideos.plusElement(videoName)
        }

        mutableState.update {
            WallPapersListScreenState.WallPapers(
                videos = success.videos,
                isEditModeActive = newValue.isNotEmpty(),
                selectedVideos = newValue,
            )
        }
    }

    private fun setEditableMode(isEditable: Boolean) {
        val success = (state.value as? WallPapersListScreenState.WallPapers) ?: return

        mutableState.update {
            WallPapersListScreenState.WallPapers(
                videos = success.videos,
                isEditModeActive = isEditable,
                selectedVideos = emptySet(),
            )
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

                logsRepository.saveLogs(exception.message ?: "Ошибка получения видео")
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
            }.onSuccess {
                if (oldVideos.isEmpty()) wallpaperServiceRepository.startWallpaperService()
            }.onFailure { exception: Throwable ->
                logsRepository.saveLogs(exception.message ?: "Ошибка загрузки видео")
            }
        }
    }

    sealed interface InputAction {
        data class LoadVideoFromStorage(val uris: List<Uri>) : InputAction
        data class SetEditableMode(val isEditable: Boolean) : InputAction
        data class OnVideoClick(val videoName: String) : InputAction
        data object DeleteVideos : InputAction
    }

}
