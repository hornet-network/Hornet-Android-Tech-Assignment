package com.hornet.movies.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

fun String?.pathToUrl(): String {
    return if (this.isNullOrBlank()) "" else "https://image.tmdb.org/t/p/w500$this"
}

fun RectF.normalize(width: Int, height: Int): RectF {
    return RectF(this.left/width.toFloat(),this.top/height.toFloat(),this.right/width.toFloat(),this.bottom/height.toFloat())
}

private suspend fun urlToBitmap(imageUrl: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}

fun loadBitmapFromUrl(imageUrl: String, onResult: (Bitmap) -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
        val bitmap = urlToBitmap(imageUrl)
        withContext(Dispatchers.Main) {
            onResult(bitmap!!)
        }
    }
}