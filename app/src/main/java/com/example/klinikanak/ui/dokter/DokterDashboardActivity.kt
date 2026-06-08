package com.example.klinikanak.ui.dokter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.databinding.ActivityDokterDashboardBinding
import com.example.klinikanak.ui.auth.LoginActivity
import com.example.klinikanak.utils.SessionManager

class DokterDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDokterDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDokterDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        binding.tvNamaDokter.text = sessionManager.getNama()

        binding.btnLogoutDokter.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.menuAntrean.setOnClickListener {
            // Arahkan ke activity yang menampilkan daftar antrean untuk dokter
            // Misalnya DokterAntreanActivity atau gunakan PemeriksaanActivity dengan list
            startActivity(Intent(this, PemeriksaanActivity::class.java))
        }

        binding.menuRiwayatPasien.setOnClickListener {
            startActivity(Intent(this, RiwayatPasienActivity::class.java))
        }
    }
}
