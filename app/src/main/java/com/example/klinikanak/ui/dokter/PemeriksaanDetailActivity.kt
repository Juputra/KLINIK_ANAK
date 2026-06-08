package com.example.klinikanak.ui.dokter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityPemeriksaanDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.jvm.java

class PemeriksaanDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPemeriksaanDetailBinding
    private var idKunjungan: String? = null
    private var idPasien: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPemeriksaanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idKunjungan = intent.getStringExtra("id_kunjungan")
        idPasien = intent.getStringExtra("id_pasien")
        val keluhan   = intent.getStringExtra("keluhan") ?: "-"
        val namaAnak  = intent.getStringExtra("nama_anak") ?: "Pasien"
        val namaOrtu  = intent.getStringExtra("nama_ortu") ?: "-"

        // ✅ FIX 1: Tampilkan nama anak yang sebenarnya, bukan ID kunjungan
        binding.tvNamaAnak.text = namaAnak
        // Tampilkan nama orang tua dan keluhan
        binding.tvKeluhan.text  = "Keluhan: $keluhan\nOrang tua: $namaOrtu"

        setupToolbar()

        // ✅ FIX 4: Klik tombol → tampilkan dialog konfirmasi dulu
        binding.btnSimpanPemeriksaan.setOnClickListener {
            tampilkanDialogKonfirmasi()
        }
        binding.btnLihatRiwayat.setOnClickListener {
            val intent = Intent(this, RiwayatMasaLaluActivity::class.java).apply {
                putExtra("id_pasien", idPasien)
                putExtra("nama_anak", namaAnak)
            }
            startActivity(intent)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Pemeriksaan"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // ✅ FIX 4: Dialog konfirmasi sebelum menyimpan data medis
    private fun tampilkanDialogKonfirmasi() {
        val diagnosa = binding.etDiagnosa.text.toString().trim()
        val resep    = binding.etResep.text.toString().trim()

        if (diagnosa.isEmpty() || resep.isEmpty()) {
            Toast.makeText(this, "Harap isi diagnosa dan resep", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pemeriksaan")
            .setMessage(
                "Pastikan data sudah benar sebelum disimpan:\n\n" +
                        "Diagnosa: $diagnosa\n\n" +
                        "Resep: $resep\n\n" +
                        "Data yang sudah tersimpan tidak dapat diedit."
            )
            .setPositiveButton("Ya, Simpan") { _, _ ->
                simpanPemeriksaan(diagnosa, resep)
            }
            .setNegativeButton("Periksa Ulang", null)
            .show()
    }

    private fun simpanPemeriksaan(diagnosa: String, resep: String) {
        binding.btnSimpanPemeriksaan.isEnabled = false
        binding.btnSimpanPemeriksaan.text = "Menyimpan..."

        idKunjungan?.let { id ->
            ApiClient.instance.simpanPemeriksaan(id, diagnosa, resep)
                .enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        binding.btnSimpanPemeriksaan.isEnabled = true
                        binding.btnSimpanPemeriksaan.text = "Simpan Pemeriksaan"

                        if (response.isSuccessful && response.body()?.status == "success") {
                            Toast.makeText(
                                this@PemeriksaanDetailActivity,
                                "Pemeriksaan berhasil disimpan",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@PemeriksaanDetailActivity,
                                "Gagal menyimpan: ${response.body()?.message ?: "Error"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        binding.btnSimpanPemeriksaan.isEnabled = true
                        binding.btnSimpanPemeriksaan.text = "Simpan Pemeriksaan"
                        Toast.makeText(
                            this@PemeriksaanDetailActivity,
                            "Koneksi Gagal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}