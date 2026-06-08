package com.example.klinikanak.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.klinikanak.AppConstants
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
        val biaya = intent.getDoubleExtra("biaya", 0.0)
        val fileBukti = intent.getStringExtra("bukti_pembayaran") // Tangkap nama file gambar

        // 2. Terapkan Format
        binding.tvNamaPasien.text = "Nama Pasien: $namaAnak"
        binding.tvTanggal.text = "Tanggal Kunjungan: ${FormatHelper.formatTanggal(tanggal)}"
        binding.tvKeluhan.text = "Keluhan: $keluhan"
        binding.tvDiagnosa.text = "Diagnosa: $diagnosa"
        binding.tvResep.text = "Resep Obat: $resep"
        binding.tvTotalBiaya.text = FormatHelper.formatRupiah(biaya)

        // 3. Logika Menampilkan Bukti Gambar
        if (!fileBukti.isNullOrEmpty() && fileBukti != "null") {
            binding.tvLabelBukti.visibility = View.VISIBLE
            binding.cardBukti.visibility = View.VISIBLE

            // URL Gambar (Gunakan URL Ngrok yang ada di AppConstants)
            val imageUrl = AppConstants.IMAGE_URL + fileBukti

            Glide.with(this)
                .load(imageUrl)
                .into(binding.ivBuktiRiwayat)
        } else {
            // Sembunyikan kotak jika ternyata tidak ada bukti gambar (misal data lama)
            binding.tvLabelBukti.visibility = View.GONE
            binding.cardBukti.visibility = View.GONE
        }
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