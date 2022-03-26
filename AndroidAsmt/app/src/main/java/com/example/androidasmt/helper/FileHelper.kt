package com.example.androidasmt.helper

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import com.example.androidasmt.static.FileStatic
import android.graphics.Bitmap
import java.io.OutputStream
import java.lang.Exception
import android.graphics.BitmapFactory
import android.util.Base64

object FileHelper {
    fun getPath(context: Context, uri: Uri): String? {
        val isKitKatorAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKatorAbove && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return context.getExternalFilesDir(null)?.absolutePath + "/" + split[1]
                    // return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "")
                    }

                    if (id.startsWith("msf:")) {
                        return createTempFile(context, uri)
                    }

                    try {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            id.toLong()
                        )
                        return getDataColumn(context, contentUri, null, null)
                    } catch (e: NumberFormatException) {
                        Log.e("FileUtils", "Downloads provider returned unexpected uri $uri", e)
                        return null
                    }
                }
//                id = id.replace("\\D+".toRegex(), "")
//                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), id.toLong())
//                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    // creates a temporary file with uri and return the absolute file path
    private fun createTempFile(context: Context, uri: Uri): String? {

        // Create Temp File here --------------------------------------------------------------
        val file = File(context.cacheDir, FileStatic.JPG)
        var filePath: String? = null
        try {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                FileOutputStream(file).use { output ->
                    // You may need to change buffer size. I use large buffer size to help loading large file , but be ware of
                    // OutOfMemory Exception
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (inputStream!!.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                    filePath = file.path
                }
            }
        } catch (e: IOException) {
            Log.e("FileUtils", "Uri id that starts with msf: have problem", e)
        }

        // Remove all the temp file -----------------------------------------------------------
//        val files = context.cacheDir.listFiles()
//        if (files != null) {
//            for (f in files) {
//                if (f.name.contains(uri.lastPathSegment!!)) {
//                    f.delete()
//                }
//            }
//        }

        return filePath
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs,null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    // creates a temporary file with base64 string and return the absolute file path
    fun createTempFile(context: Context, base64: String?): String? {

        // if dont have base64 url, return null
        if (base64.isNullOrEmpty()) {
            return null
        }

        // convert base64 url to bitmap
        val byteArray = Base64.decode(base64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

        // store the file in cache directory
        val outputDir = context.cacheDir
        val imageFile = File(outputDir, FileStatic.JPG)  // File name is inside FileStatic.JPG

        // store the file
        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Log.e(context.javaClass.simpleName, "Error writing file", e)
            return null
        }

        // return file path
        return imageFile.absolutePath
    }
}