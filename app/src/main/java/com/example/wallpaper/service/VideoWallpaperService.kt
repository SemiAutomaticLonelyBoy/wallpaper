package com.example.wallpaper.service

import android.media.MediaPlayer
import android.os.ParcelFileDescriptor
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.wallpaper.data.repositories.FileSystemRepository
import com.example.wallpaper.data.repositories.LogsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import java.io.File

class VideoWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = VideoWallpaperEngine()

    inner class VideoWallpaperEngine : Engine() {

        private var scope = CoroutineScope(Dispatchers.IO)
        private var coroutineJob: Job? = null
        private val mutableVideos = MutableStateFlow<List<File>>(
            try {
                getKoin().get<FileSystemRepository>().getDataFromAppFileSystem()
            } catch (exception: Throwable) {
                getKoin().get<LogsRepository>().saveLogs(exception.message ?: "Ошибка получения видео")
                emptyList<File>()
            }
        )
        private val videos: StateFlow<List<File>> = mutableVideos.asStateFlow()

        private var mediaPlayer: MediaPlayer? = null
        private var surfaceHolder: SurfaceHolder? = null
        private var isVisible = false

        private fun save(message: String) {
            getKoin().get<LogsRepository>().saveLogs(message)
        }

        private val surfaceCallback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                mediaPlayer?.setSurface(holder.surface)
                if (isVisible) mediaPlayer?.start()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                mediaPlayer?.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mediaPlayer?.setSurface(null)
            }
        }

        private fun playNext() {
            runCatching {
                if (videos.value.isEmpty()) return

                val newIndex = videos.value.indices.random()
                mediaPlayer?.reset()
                val openFileDescriptor = ParcelFileDescriptor.open(videos.value[newIndex], ParcelFileDescriptor.MODE_READ_ONLY)
                mediaPlayer?.setDataSource(openFileDescriptor?.fileDescriptor)
                openFileDescriptor?.close()
                mediaPlayer?.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                mediaPlayer?.prepareAsync()
            }.onFailure { exception: Throwable ->
                save(exception.message ?: "Функция playNext")
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            runCatching {
                super.onCreate(holder)
                surfaceHolder = holder

                coroutineJob?.cancel()

                coroutineJob = scope.launch {
                    getKoin().get<FileSystemRepository>().files.collect { videos ->
                        mutableVideos.update { videos }
                    }
                }

                mediaPlayer = MediaPlayer().apply {
                    val openFileDescriptor = ParcelFileDescriptor.open(videos.value[0], ParcelFileDescriptor.MODE_READ_ONLY)
                    setDataSource(openFileDescriptor?.fileDescriptor)
                    openFileDescriptor?.close()

                    setOnCompletionListener {
                        if (isVisible) {
                            playNext()
                        }
                    }
                    setOnPreparedListener {
                        start()
                    }
                    setOnPreparedListener { mp ->
                        mp.start()
                    }
                    setVolume(0f, 0f)

                    isLooping = false
                    prepareAsync()
                }

                holder.addCallback(surfaceCallback)
            }.onFailure { exception: Throwable ->
                save(exception.message ?: "Функция onCreate")
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            isVisible = visible
            if (visible) {
                mediaPlayer?.start()
            } else {
                playNext()
                mediaPlayer?.pause()
            }
        }

        override fun onDestroy() {
            mediaPlayer?.release()
            surfaceHolder?.removeCallback(surfaceCallback)
            coroutineJob?.cancel()
            scope.cancel()
            super.onDestroy()
        }

    }
}