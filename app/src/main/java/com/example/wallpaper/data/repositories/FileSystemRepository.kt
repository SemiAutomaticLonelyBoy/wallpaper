package com.example.wallpaper.data.repositories

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toFile
import androidx.core.net.toUri
import java.io.File

class FileSystemRepository(
    context: Context,
) {

    private val conRes = context.contentResolver
    private val filesDir = File(context.filesDir, "my_directory")

    fun setDataToAppFileSystem(uris: List<Uri>): List<File> {
        return uris.map {
            val directory = File(filesDir, it.getFileNameFromUri() )
            conRes.openInputStream(it)?.use { input ->
                directory.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            directory
        }
    }

    private fun Uri.getFileNameFromUri(): String {
        var fileName = ""
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

        conRes.query(this, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
            }
        }

        return fileName
    }

    fun getDataFromAppFileSystem(): List<File> {

        return if (!filesDir.exists()) {
            if (filesDir.mkdir()) {
                emptyList<File>()
            } else {
                throw Error("Ошибка создания директории")
            }
        } else  {
            filesDir.listFiles()?.filter { it.isFile } ?: emptyList<File>()
        }

    }
}