package com.example.wallpaper.data.repositories

import android.content.Context
import java.io.File
import java.time.Instant

class LogsRepository(
    context: Context,
) {

    private val filesDir = File(context.filesDir, "logs").apply {
        if (!exists()) mkdirs()
    }

    fun saveLogs(message: String) {
        File(filesDir, Instant.now().toString() + ".txt").writeText(message)
    }
}
