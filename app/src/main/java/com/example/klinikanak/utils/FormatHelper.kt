package com.example.klinikanak.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

object FormatHelper {

    // ─── Format Rupiah ────────────────────────────────────────────────────────
    // Mengubah angka ke format "Rp X.XXX"
    // Mendukung Int, Long, Double, dan nullable Double

    fun formatRupiah(amount: Int): String {
        val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp ${nf.format(amount)}"
    }

    fun formatRupiah(amount: Long): String {
        val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp ${nf.format(amount)}"
    }

    fun formatRupiah(amount: Double?): String {
        if (amount == null) return "Rp 0"
        val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp ${nf.format(amount.toLong())}"
    }

    // ─── Format Tanggal ───────────────────────────────────────────────────────
    // Input: String "YYYY-MM-DD" (format database)
    // Output: "08 Jun 2025"
    // Jika format tidak dikenali, kembalikan string asli

    fun formatTanggal(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "-"
        return try {
            val inputFormat  = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            val date = inputFormat.parse(dateStr) ?: return dateStr
            outputFormat.format(date)
        } catch (e: Exception) {
            dateStr // Kembalikan apa adanya jika parsing gagal
        }
    }
}