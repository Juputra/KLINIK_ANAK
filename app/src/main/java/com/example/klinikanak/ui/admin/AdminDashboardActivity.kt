package com.example.klinikanak.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
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
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi Keluar")
                .setMessage("Apakah Anda yakin ingin keluar dari akun ini?")
                .setPositiveButton("Ya, Keluar") { _, _ ->
                    sessionManager.logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
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
        binding.menuMonitorLayanan.setOnClickListener {
            startActivity(Intent(this, MonitorLayananActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadStatistik()
    }

    private fun loadStatistik() {
        ApiClient.instance.getLayanan("admin", null, null)
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val data = response.body()!!.data

                        // Pasien baru yang menunggu konfirmasi admin (status 0)
                        binding.tvStatPendaftaran.text = data.count { it.statusLayanan == 0 }.toString()

                        // ✅ FIX 2: Ganti dari == 2 ke == 3
                        // status 3 = "Menunggu Pembayaran" (bukan 2 = "Sedang Diperiksa")
                        binding.tvStatPembayaran.text = data.count { it.statusLayanan == 3 }.toString()
                    }
                }
                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    // Statistik opsional, abaikan jika gagal
                }
            })
    }
}