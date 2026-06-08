package com.example.klinikanak.ui.pasien

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.databinding.ActivityPasienDashboardBinding
import com.example.klinikanak.ui.auth.LoginActivity
import com.example.klinikanak.utils.SessionManager

class PasienDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasienDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasienDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        
        // Tampilkan Nama User
        binding.tvNamaUser.text = sessionManager.getNama()

        // Menu Logout
        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Navigasi Menu
        binding.menuBuatJanji.setOnClickListener {
            startActivity(Intent(this, BuatJanjiActivity::class.java))
        }

        binding.menuStatusLayanan.setOnClickListener {
            startActivity(Intent(this, StatusLayananActivity::class.java))
        }

        binding.menuPembayaran.setOnClickListener {
            startActivity(Intent(this, PembayaranActivity::class.java))
        }

        binding.menuRiwayat.setOnClickListener {
            startActivity(Intent(this, RiwayatMedisActivity::class.java))
        }
    }
}
