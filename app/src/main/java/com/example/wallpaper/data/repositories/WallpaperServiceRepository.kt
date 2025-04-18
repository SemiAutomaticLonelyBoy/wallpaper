package com.example.wallpaper.data.repositories

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.wallpaper.service.VideoWallpaperService

class WallpaperServiceRepository(
    context: Context,
) {
    private val _context = context

    fun startWallpaperService() {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(_context, VideoWallpaperService::class.java)
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        _context.startActivity(intent)
    }

    fun stopWallpaperService() {
        val wallpaperManager = WallpaperManager.getInstance(_context)
        wallpaperManager.clear(WallpaperManager.FLAG_SYSTEM)
    }
}
