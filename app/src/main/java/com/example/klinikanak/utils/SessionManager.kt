package com.example.klinikanak.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("KlinikAnakPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        const val KEY_IS_LOGIN = "isLogin"
        const val KEY_ID = "id_user"
        const val KEY_NAMA = "nama_lengkap"
        const val KEY_ROLE = "role"
    }

    // Fungsi untuk menyimpan data saat login berhasil
    fun saveSession(id: Int, nama: String, role: String) {
        editor.putBoolean(KEY_IS_LOGIN, true)
        editor.putInt(KEY_ID, id)
        editor.putString(KEY_NAMA, nama)
        editor.putString(KEY_ROLE, role)
        editor.apply() // Gunakan apply() agar proses berjalan di background (lebih cepat)
    }

    // Fungsi untuk mengecek apakah user sudah login
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGIN, false)
    }

    // Fungsi untuk mengambil Role (Admin / Dokter / Pasien)
    fun getRole(): String? {
        return prefs.getString(KEY_ROLE, null)
    }

    // Fungsi untuk mengambil ID User
    fun getUserId(): Int {
        return prefs.getInt(KEY_ID, 0)
    }

    // Fungsi untuk mengambil Nama User
    fun getNama(): String {
        return prefs.getString(KEY_NAMA, "User") ?: "User"
    }

    // Fungsi untuk Logout (Menghapus seluruh sesi)
    fun logout() {
        editor.clear()
        editor.apply()
    }
}