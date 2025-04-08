package com.example.wallpaper.service

import android.media.MediaPlayer
import android.os.ParcelFileDescriptor
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.wallpaper.data.repositories.FileSystemRepository
import org.koin.android.ext.android.getKoin
import java.io.File

class VideoWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = VideoWallpaperEngine()

    inner class VideoWallpaperEngine : Engine() {

        private val videos by lazy {
            getKoin().get<FileSystemRepository>().getDataFromAppFileSystem()
        }

        private var mediaPlayer: MediaPlayer? = null
        private var surfaceHolder: SurfaceHolder? = null
        private var isVisible = false

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
            if (videos.isEmpty()) return

            val newIndex = videos.indices.random()
            mediaPlayer?.reset()
            val openFileDescriptor = ParcelFileDescriptor.open(videos[newIndex], ParcelFileDescriptor.MODE_READ_ONLY)
            mediaPlayer?.setDataSource(openFileDescriptor?.fileDescriptor)
            openFileDescriptor?.close()
            mediaPlayer?.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            mediaPlayer?.prepareAsync()
        }

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            surfaceHolder = holder

            mediaPlayer = MediaPlayer().apply {
                val openFileDescriptor = ParcelFileDescriptor.open(videos[0], ParcelFileDescriptor.MODE_READ_ONLY)
                setDataSource(openFileDescriptor?.fileDescriptor)
                openFileDescriptor?.close()

                setOnCompletionListener { playNext() }
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
        }

        override fun onVisibilityChanged(visible: Boolean) {
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
            super.onDestroy()
        }

    }
}