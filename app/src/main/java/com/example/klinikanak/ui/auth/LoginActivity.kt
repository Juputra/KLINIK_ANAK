package com.example.klinikanak.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityLoginBinding
import com.example.klinikanak.model.LoginResponse
import com.example.klinikanak.ui.admin.AdminDashboardActivity
import com.example.klinikanak.ui.dokter.DokterDashboardActivity
import com.example.klinikanak.ui.pasien.PasienDashboardActivity
import com.example.klinikanak.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi SessionManager
        sessionManager = SessionManager(this)

        // CEK SESI: Jika sudah login sebelumnya, langsung lempar ke Dashboard sesuai Role
        if (sessionManager.isLoggedIn()) {
            arahkankeDashboard(sessionManager.getRole())
        }

        // Aksi Tombol Login
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tampilkan loading manual (bisa ganti progress bar nanti)
            binding.btnLogin.text = "Memuat..."
            binding.btnLogin.isEnabled = false

            // Panggil fungsi userLogin dari ApiService.kt
            ApiClient.instance.userLogin(username, password).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    binding.btnLogin.text = "Masuk"
                    binding.btnLogin.isEnabled = true

                    if (response.isSuccessful && response.body()?.status == "success") {
                        val userResponse = response.body()
                        val role = userResponse?.role
                        val id = userResponse?.data?.id ?: 0
                        val nama = userResponse?.data?.nama ?: ""

                        // 1. Simpan sesi ke perangkat Android
                        sessionManager.saveSession(id, nama, role ?: "")

                        // 2. Arahkan ke halaman masing-masing (tanpa dropdown role)
                        Toast.makeText(this@LoginActivity, "Selamat datang, $nama", Toast.LENGTH_SHORT).show()
                        arahkankeDashboard(role)

                    } else {
                        // Jika status "error" dari PHP (password salah / akun tidak ada)
                        val errorMessage = response.body()?.message ?: "Gagal memuat data"
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    binding.btnLogin.text = "Masuk"
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this@LoginActivity, "Koneksi ke Server Ngrok Gagal: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        // Aksi Teks Daftar Pasien
        binding.tvDaftarPasien.setOnClickListener {
            // Uncomment baris di bawah setelah RegisterActivity dibuat
            // startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Fungsi tambahan untuk memisahkan navigasi halaman secara otomatis
    private fun arahkankeDashboard(role: String?) {
        val intent = when (role) {
            "admin" -> Intent(this, AdminDashboardActivity::class.java)
            "dokter" -> Intent(this, DokterDashboardActivity::class.java)
            "pasien" -> Intent(this, PasienDashboardActivity::class.java)
            else -> null
        }

        if (intent != null) {
            startActivity(intent)
            finish() // Agar user tidak bisa kembali ke halaman login jika menekan tombol 'Back' di HP
        }
    }
}