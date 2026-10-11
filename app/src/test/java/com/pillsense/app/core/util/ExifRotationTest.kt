package com.pillsense.app.core.util

import android.graphics.Bitmap
import androidx.exifinterface.media.ExifInterface
import org.junit.Assert.*
import org.junit.Test

class ExifRotationTest {
    @Test fun `converts EXIF orientation to rotation degrees`() {
        val mock = object : ExifInterface("") {
            override fun getAttributeInt(tag: String, defaultValue: Int): Int {
                return when (tag) {
                    ExifInterface.TAG_ORIENTATION -> ExifInterface.ORIENTATION_ROTATE_90
                    else -> defaultValue
                }
            }
        }
        assertEquals(90, ExifRotation.getRotationDegrees(mock))
    }

    @Test fun `handles 180 degree rotation`() {
        val mock = object : ExifInterface("") {
            override fun getAttributeInt(tag: String, defaultValue: Int): Int {
                return when (tag) {
                    ExifInterface.TAG_ORIENTATION -> ExifInterface.ORIENTATION_ROTATE_180
                    else -> defaultValue
                }
            }
        }
        assertEquals(180, ExifRotation.getRotationDegrees(mock))
    }

    @Test fun `handles 270 degree rotation`() {
        val mock = object : ExifInterface("") {
            override fun getAttributeInt(tag: String, defaultValue: Int): Int {
                return when (tag) {
                    ExifInterface.TAG_ORIENTATION -> ExifInterface.ORIENTATION_ROTATE_270
                    else -> defaultValue
                }
            }
        }
        assertEquals(270, ExifRotation.getRotationDegrees(mock))
    }

    @Test fun `returns 0 for normal orientation`() {
        val mock = object : ExifInterface("") {
            override fun getAttributeInt(tag: String, defaultValue: Int): Int {
                return when (tag) {
                    ExifInterface.TAG_ORIENTATION -> ExifInterface.ORIENTATION_NORMAL
                    else -> defaultValue
                }
            }
        }
        assertEquals(0, ExifRotation.getRotationDegrees(mock))
    }

    @Test fun `rotateBitmap returns same bitmap when rotation is 0`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val result = ExifRotation.rotateBitmap(bitmap, 0)
        assertEquals(bitmap, result)
        bitmap.recycle()
    }
}
