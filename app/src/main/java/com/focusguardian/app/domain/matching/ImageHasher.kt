package com.focusguardian.app.domain.matching

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.roundToInt

object ImageHasher {

    /**
     * Computes a 64-bit difference hash (dHash) of a Bitmap:
     * 1. Downscales image to 9x8 grayscale bitmap (72 pixels)
     * 2. Compares adjacent pixels horizontally (pixel[x] > pixel[x+1])
     * 3. Yields exactly 8x8 = 64 boolean bits
     * Returns 16-character hex string representing the 64 bits.
     */
    fun computeDHash(source: Bitmap): String {
        // Downsample to 9x8 to compute 8 horizontal differences per row
        val scaled = Bitmap.createScaledBitmap(source, 9, 8, true)
        var hashBits = 0L

        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val leftPixel = scaled.getPixel(x, y)
                val rightPixel = scaled.getPixel(x + 1, y)

                // Convert to luminance
                val leftLum = (Color.red(leftPixel) * 0.299 + Color.green(leftPixel) * 0.587 + Color.blue(leftPixel) * 0.114)
                val rightLum = (Color.red(rightPixel) * 0.299 + Color.green(rightPixel) * 0.587 + Color.blue(rightPixel) * 0.114)

                val bitIndex = (y * 8) + x
                if (leftLum > rightLum) {
                    hashBits = hashBits or (1L shl bitIndex)
                }
            }
        }

        // Clean up scaled bitmap
        if (scaled != source) {
            scaled.recycle()
        }

        return String.format("%016x", hashBits)
    }

    /**
     * Calculates percentage similarity (0% - 100%) between two 64-bit dHashes
     * using Hamming Distance (bit difference count).
     */
    fun calculateSimilarity(hash1Hex: String, hash2Hex: String): Int {
        if (hash1Hex.length != 16 || hash2Hex.length != 16) return 0
        try {
            val h1 = hash1Hex.toULong(16)
            val h2 = hash2Hex.toULong(16)
            val diffBits = java.lang.Long.bitCount((h1 xor h2).toLong())
            val similarityFraction = 1.0 - (diffBits.toDouble() / 64.0)
            return (similarityFraction * 100.0).roundToInt().coerceIn(0, 100)
        } catch (e: Exception) {
            return 0
        }
    }
}
