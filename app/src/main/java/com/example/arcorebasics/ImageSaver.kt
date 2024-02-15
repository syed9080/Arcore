package com.example.arcorebasics

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class ImageSaver(private val context: Context) {

    fun saveImage(bitmap: Bitmap) {
        // Save the bitmap to a file or perform other actions based on your requirements
        // Example: Save to internal storage
        val fileName = "ARImage_${System.currentTimeMillis()}.png"
        val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.close()
        Log.d(TAG, "Image saved: $fileName")
    }
}
