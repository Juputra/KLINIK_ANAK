package com.example.klinikanak.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityAdminDashboardBinding
import com.example.klinikanak.ui.auth.LoginActivity
import com.example.klinikanak.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        binding.tvNamaAdmin.text = sessionManager.getNama()

        binding.btnLogoutAdmin.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.menuKonfirmasiPendaftaran.setOnClickListener {
            startActivity(Intent(this, KonfirmasiPendaftaranActivity::class.java))
        }
        binding.menuKelolaDokter.setOnClickListener {
            startActivity(Intent(this, KelolaDokterActivity::class.java))
        }
        binding.menuKelolaAdmin.setOnClickListener {
            startActivity(Intent(this, KelolaAdminActivity::class.java))
        }
        binding.menuVerifikasiPembayaran.setOnClickListener {
            startActivity(Intent(this, VerifikasiPembayaranActivity::class.java))
        }
        binding.menuLaporan.setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadStatistik() // refresh angka tiap kembali ke dashboard
    }

    private fun loadStatistik() {
        ApiClient.instance.getLayanan("admin", null, null)
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val data = response.body()!!.data
                        binding.tvStatPendaftaran.text = data.count { it.statusLayanan == 0 }.toString()
                        binding.tvStatPembayaran.text = data.count { it.statusLayanan == 2 }.toString()
                    }
                }
                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    // statistik opsional; abaikan jika gagal
                }
            })
    }
}