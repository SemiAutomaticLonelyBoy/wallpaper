package com.example.wallpaper.data.repositories

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File

class FileSystemRepository(
    context: Context,
) {

    private val conRes = context.contentResolver
    private val filesDir = File(context.filesDir, "video")
    private val mutableFiles = MutableSharedFlow<List<File>>()
    val files = mutableFiles.asSharedFlow()

    suspend fun setDataToAppFileSystem(uris: List<Uri>) {
        uris.map {
            val directory = File(filesDir, it.getFileNameFromUri())
            conRes.openInputStream(it)?.use { input ->
                directory.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            directory
        }
        mutableFiles.emit(getDataFromAppFileSystem())
    }

    suspend fun deleteDataFromAppFileSystemByFileName(videos: Set<String>) {
        videos.map {
            val file = File(filesDir, it)
            file.delete()
        }
        mutableFiles.emit(getDataFromAppFileSystem())
    }

    private fun Uri.getFileNameFromUri(): String {
        var fileName = ""
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

        conRes.query(this, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                fileName =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
            }
        }

        return fileName
    }

    fun getDataFromAppFileSystem(): List<File> {
        return if (!filesDir.exists()) {
            if (filesDir.mkdir()) {
                emptyList()
            } else {
                throw Error("Ошибка создания директории")
            }
        } else {
            val filteredFiles = filesDir.listFiles()?.filter { it.isFile } ?: emptyList<File>()
            return filteredFiles
        }
    }
}
