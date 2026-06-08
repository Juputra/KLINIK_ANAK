package com.example.klinikanak.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.ApiResponse
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

        // Tombol Back
        binding.btnBack.setOnClickListener {
            finish()
        }

        // CEK SESI: Jika sudah login sebelumnya, langsung lempar ke Dashboard sesuai Role
        if (sessionManager.isLoggedIn()) {
            arahkankeDashboard(sessionManager.getRole())
        }

        // Aksi Tombol Login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tampilkan loading manual
            binding.btnLogin.text = "Memuat..."
            binding.btnLogin.isEnabled = false

            // Panggil fungsi userLogin dari ApiService.kt
            ApiClient.instance.userLogin(email, password).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    binding.btnLogin.text = "Masuk"
                    binding.btnLogin.isEnabled = true

                    if (response.isSuccessful) {
                        val loginBody = response.body()
                        if (loginBody?.status == "success") {
                            val role = loginBody.role
                            val data = loginBody.data

                            val idStr = if (role == "pasien") data?.idPasien else data?.idUser
                            val id = idStr?.toIntOrNull() ?: 0
                            val nama = if (role == "pasien") data?.namaAnak ?: "Pasien" else data?.namaLengkap ?: "User"

                            // 1. Simpan sesi ke perangkat Android
                            sessionManager.saveSession(id, nama, role ?: "")

                            // 2. Arahkan ke halaman masing-masing
                            Toast.makeText(this@LoginActivity, "Selamat datang, $nama", Toast.LENGTH_SHORT).show()
                            arahkankeDashboard(role)
                        } else {
                            val errorMessage = loginBody?.message ?: "Username atau Password Salah"
                            Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Error Server: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    binding.btnLogin.text = "Masuk"
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this@LoginActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        // Aksi Teks Daftar Pasien
        binding.tvDaftarPasien.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Aksi Teks Lupa Password (reset password berfungsi)
        binding.tvLupaPassword.setOnClickListener {
            val ctx = this

            val wadah = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(60, 20, 60, 0)
            }
            val inputId = EditText(ctx).apply {
                hint = "Username / Email akun"
                inputType = InputType.TYPE_CLASS_TEXT
            }
            val inputPass = EditText(ctx).apply {
                hint = "Password baru"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            wadah.addView(inputId)
            wadah.addView(inputPass)

            AlertDialog.Builder(ctx)
                .setTitle("Reset Password")
                .setMessage("Masukkan username/email akunmu dan password baru.")
                .setView(wadah)
                .setPositiveButton("Simpan") { _, _ ->
                    val identitas = inputId.text.toString().trim()
                    val passBaru = inputPass.text.toString().trim()

                    if (identitas.isEmpty() || passBaru.isEmpty()) {
                        Toast.makeText(ctx, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (passBaru.length < 5) {
                        Toast.makeText(ctx, "Password minimal 5 karakter", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    ApiClient.instance.resetPassword(identitas, passBaru)
                        .enqueue(object : Callback<ApiResponse> {
                            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                                val body = response.body()
                                if (body?.status == "success") {
                                    Toast.makeText(ctx, "Password berhasil diubah. Silakan login.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(ctx, body?.message ?: "Gagal reset password", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                                Toast.makeText(ctx, "Koneksi gagal: ${t.message}", Toast.LENGTH_LONG).show()
                            }
                        })
                }
                .setNegativeButton("Batal", null)
                .show()
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
            finish()
        }
    }
}