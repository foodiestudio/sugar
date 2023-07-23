package com.github.foodiestudio.sugar.saf

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File

/**
 * 使用前需确保 [SAFHelper] 初始化过
 */
object MediaStoreHelper {

    internal const val MediaDocumentProvider_AUTHORITY = "com.android.providers.media.documents"

    fun createMediaStoreUri(
        filename: String,
        collection: Uri = MediaStore.Files.getContentUri("external"),
        directory: String?
    ): Uri? = SAFHelper.fileSystem.createMediaStoreUri(filename, collection, directory)

    suspend fun scanUri(uri: Uri, mimeType: String): Uri? =
        SAFHelper.fileSystem.scanUri(uri, mimeType)

    suspend fun scanFile(file: File, mimeType: String): Uri? =
        SAFHelper.fileSystem.scanFile(file, mimeType)


    internal fun getDisplayName(contentResolver: ContentResolver, uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        try {
            if (uri == null) return null
            cursor = contentResolver.query(
                uri, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    internal fun getFilePath(contentResolver: ContentResolver, uri: Uri): String? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]
        var contentUri: Uri? = null
        when (type) {
            "image" -> {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            "video" -> {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            "audio" -> {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        }
        val selection = "_id=?"
        val selectionArgs = arrayOf(split[1])
        return getDataColumn(contentResolver, contentUri, selection, selectionArgs)
    }
}