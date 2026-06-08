package com.example.klinikanak.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.databinding.ActivityDetailRiwayatBinding
import com.example.klinikanak.utils.FormatHelper

class DetailRiwayatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailRiwayatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailRiwayatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // 1. Tangkap data dari Intent
        val namaAnak = intent.getStringExtra("nama_anak") ?: "-"
        val tanggal = intent.getStringExtra("tanggal") ?: "-"
        val keluhan = intent.getStringExtra("keluhan") ?: "-"
        val diagnosa = intent.getStringExtra("diagnosa") ?: "-"
        val resep = intent.getStringExtra("resep") ?: "-"
        val biaya = intent.getDoubleExtra("biaya", 0.0) // Menggunakan format Double dari database

        // 2. Terapkan FormatHelper untuk Tanggal dan Uang
        binding.tvNamaPasien.text = "Nama Pasien: $namaAnak"
        binding.tvTanggal.text = "Tanggal Kunjungan: ${FormatHelper.formatTanggal(tanggal)}"
        binding.tvKeluhan.text = "Keluhan: $keluhan"
        binding.tvDiagnosa.text = "Diagnosa: $diagnosa"
        binding.tvResep.text = "Resep Obat: $resep"

        // Render format rupiah rapi
        binding.tvTotalBiaya.text = FormatHelper.formatRupiah(biaya)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Riwayat"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}