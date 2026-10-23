package com.example.eduvault.core.util

/**
 * Tiện ích tính toán số lượng credit cần thiết để mở khóa tài liệu.
 * - Dung lượng <= 2 MB: 1 credit
 * - Dung lượng 2.1 MB - 5 MB: 2 credits
 * - Dung lượng > 5 MB: 3 credits
 */
object PriceCalculator {
    fun calculatePrice(fileSizeBytes: Long, quantityLabel: String = ""): Int {
        if (fileSizeBytes > 0L) {
            val sizeInMb = fileSizeBytes / (1024.0 * 1024.0)
            return when {
                sizeInMb <= 2.0 -> 1
                sizeInMb <= 5.0 -> 2
                else -> 3
            }
        }
        val cleanStr = quantityLabel.replace(",", ".").trim()
        if (cleanStr.endsWith("MB", ignoreCase = true)) {
            val sizeInMb = cleanStr.replace("MB", "", ignoreCase = true).trim().toDoubleOrNull() ?: 0.0
            return when {
                sizeInMb <= 2.0 -> 1
                sizeInMb <= 5.0 -> 2
                else -> 3
            }
        }
        return 1
    }
}
