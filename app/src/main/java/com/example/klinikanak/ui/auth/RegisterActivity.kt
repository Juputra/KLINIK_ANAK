package com.example.klinikanak.ui.auth

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.R
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tombol Back
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Tampilkan pop-up kalender jika form Tanggal Lahir diklik
        binding.etTanggalLahir.setOnClickListener {
            showDatePicker()
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            val ortu = binding.etNamaOrtu.text.toString().trim()
            val anak = binding.etNamaAnak.text.toString().trim()
            val tglLahir = binding.etTanggalLahir.text.toString().trim()
            val hp = binding.etNoHp.text.toString().trim()

            // Ambil ID dari pilihan Jenis Kelamin
            val selectedJkId = binding.rgJenisKelamin.checkedRadioButtonId
            val jk = when (selectedJkId) {
                R.id.rbLakiLaki -> "L"
                R.id.rbPerempuan -> "P"
                else -> ""
            }

            // Validasi Data Kosong
            if (email.isEmpty() || pass.isEmpty() || ortu.isEmpty() || anak.isEmpty() || tglLahir.isEmpty() || jk.isEmpty() || hp.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom termasuk Tanggal Lahir & Jenis Kelamin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Matikan tombol sementara agar tidak dobel klik
            binding.btnRegister.isEnabled = false
            binding.btnRegister.text = "Memproses..."

            // Panggil API Register dengan data dinamis dari UI
            ApiClient.instance.registerPasien(email, pass, ortu, anak, tglLahir, jk, hp)
                .enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.text = "Daftar Sekarang"

                        if (response.isSuccessful) {
                            val registerBody = response.body()
                            if (registerBody?.status == "success") {
                                Toast.makeText(this@RegisterActivity, "Registrasi Berhasil! Silakan Login", Toast.LENGTH_LONG).show()
                                finish() // Kembali ke halaman Login
                            } else {
                                val msg = registerBody?.message ?: "Registrasi Gagal (Status Error)"
                                Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@RegisterActivity, "Error Server: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.text = "Daftar Sekarang"
                        Toast.makeText(this@RegisterActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Format wajib ke Database MariaDB: YYYY-MM-DD
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.etTanggalLahir.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Logika agar pengguna tidak bisa memasukkan tanggal lahir dari masa depan
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
}