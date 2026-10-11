package com.pillsense.app.core.util

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface

object ExifRotation {
    fun getRotationDegrees(exif: ExifInterface): Int {
        return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> 0   // Espejado horizontal (no rotado en este caso)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> 0     // Espejado vertical
            ExifInterface.ORIENTATION_TRANSPOSE -> 90
            ExifInterface.ORIENTATION_TRANSVERSE -> 270
            else -> 0
        }
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
