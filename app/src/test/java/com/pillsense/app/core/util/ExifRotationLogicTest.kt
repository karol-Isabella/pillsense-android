package com.pillsense.app.core.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Pure unit tests for EXIF rotation conversion logic.
 * These tests verify the conversion from EXIF orientation values to rotation degrees
 * without requiring Android context or mocking ExifInterface.
 */
class ExifRotationLogicTest {

    companion object {
        // EXIF orientation constants (extracted from ExifInterface)
        private const val ORIENTATION_NORMAL = 1
        private const val ORIENTATION_FLIP_HORIZONTAL = 2
        private const val ORIENTATION_ROTATE_180 = 3
        private const val ORIENTATION_FLIP_VERTICAL = 4
        private const val ORIENTATION_TRANSPOSE = 5
        private const val ORIENTATION_ROTATE_90 = 6
        private const val ORIENTATION_TRANSVERSE = 7
        private const val ORIENTATION_ROTATE_270 = 8
    }

    private fun convertOrientationToDegrees(orientation: Int): Int {
        return when (orientation) {
            ORIENTATION_ROTATE_90 -> 90
            ORIENTATION_ROTATE_180 -> 180
            ORIENTATION_ROTATE_270 -> 270
            ORIENTATION_FLIP_HORIZONTAL -> 0   // Espejado horizontal (no rotado en este caso)
            ORIENTATION_FLIP_VERTICAL -> 0     // Espejado vertical
            ORIENTATION_TRANSPOSE -> 90
            ORIENTATION_TRANSVERSE -> 270
            else -> 0
        }
    }

    @Test
    fun `normal orientation returns 0 degrees`() {
        assertEquals(0, convertOrientationToDegrees(ORIENTATION_NORMAL))
    }

    @Test
    fun `rotate 90 returns 90 degrees`() {
        assertEquals(90, convertOrientationToDegrees(ORIENTATION_ROTATE_90))
    }

    @Test
    fun `rotate 180 returns 180 degrees`() {
        assertEquals(180, convertOrientationToDegrees(ORIENTATION_ROTATE_180))
    }

    @Test
    fun `rotate 270 returns 270 degrees`() {
        assertEquals(270, convertOrientationToDegrees(ORIENTATION_ROTATE_270))
    }

    @Test
    fun `flip horizontal returns 0 degrees`() {
        assertEquals(0, convertOrientationToDegrees(ORIENTATION_FLIP_HORIZONTAL))
    }

    @Test
    fun `flip vertical returns 0 degrees`() {
        assertEquals(0, convertOrientationToDegrees(ORIENTATION_FLIP_VERTICAL))
    }

    @Test
    fun `transpose orientation returns 90 degrees`() {
        assertEquals(90, convertOrientationToDegrees(ORIENTATION_TRANSPOSE))
    }

    @Test
    fun `transverse orientation returns 270 degrees`() {
        assertEquals(270, convertOrientationToDegrees(ORIENTATION_TRANSVERSE))
    }

    @Test
    fun `unknown orientation returns 0 degrees`() {
        assertEquals(0, convertOrientationToDegrees(999))
    }

    @Test
    fun `all valid orientations produce expected outputs`() {
        val expectedResults = mapOf(
            ORIENTATION_NORMAL to 0,
            ORIENTATION_FLIP_HORIZONTAL to 0,
            ORIENTATION_ROTATE_180 to 180,
            ORIENTATION_FLIP_VERTICAL to 0,
            ORIENTATION_TRANSPOSE to 90,
            ORIENTATION_ROTATE_90 to 90,
            ORIENTATION_TRANSVERSE to 270,
            ORIENTATION_ROTATE_270 to 270
        )

        expectedResults.forEach { (orientation, expectedDegrees) ->
            assertEquals(
                "Orientation $orientation should return $expectedDegrees degrees",
                expectedDegrees,
                convertOrientationToDegrees(orientation)
            )
        }
    }
}
