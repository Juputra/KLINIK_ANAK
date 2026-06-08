package com.example.klinikanak.ui.pasien

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.AppConstants
import com.example.klinikanak.R
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityPembayaranDetailBinding
import com.example.klinikanak.utils.FormatHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PembayaranDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPembayaranDetailBinding
    private var idKunjungan: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPembayaranDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idKunjungan = intent.getStringExtra("id_kunjungan")

        // FIX 9: Ambil data diagnosa dan resep dari Intent
        val diagnosa   = intent.getStringExtra("diagnosa") ?: "-"
        val resep      = intent.getStringExtra("resep") ?: "-"
        val namaAnak   = intent.getStringExtra("nama_anak") ?: "Pasien"

        // FIX 9: Tampilkan ringkasan medis sebelum bayar
        // Pasien tahu mereka membayar untuk layanan apa
        binding.tvNamaPasien.text  = "Pasien: $namaAnak"
        binding.tvDiagnosa.text    = "Diagnosa: $diagnosa"
        binding.tvResepObat.text   = "Resep: $resep"

        // FIX 3 + FIX 8: Pakai konstanta terpusat + format rupiah yang benar
        binding.tvTotalBiaya.text = FormatHelper.formatRupiah(AppConstants.BIAYA_KONSULTASI)

        setupToolbar()
        binding.btnProsesBayar.setOnClickListener { konfirmasiPembayaran() }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Konfirmasi Bayar"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun konfirmasiPembayaran() {
        val selectedId = binding.rgMetode.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Pilih metode pembayaran", Toast.LENGTH_SHORT).show()
            return
        }

        val metode     = if (selectedId == R.id.rbAsuransi) "asuransi" else "umum"
        val keterangan = binding.etKeterangan.text.toString().trim()

        binding.btnProsesBayar.isEnabled = false
        binding.btnProsesBayar.text = "Memproses..."

        idKunjungan?.let { id ->
            ApiClient.instance.bayar(
                id,
                AppConstants.BIAYA_KONSULTASI.toString(), // FIX 3: dari konstanta
                metode,
                keterangan
            ).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    binding.btnProsesBayar.isEnabled = true
                    binding.btnProsesBayar.text = "Konfirmasi Pembayaran"
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(
                            this@PembayaranDetailActivity,
                            "Pembayaran berhasil dikonfirmasi!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@PembayaranDetailActivity,
                            "Gagal konfirmasi: ${response.body()?.message ?: "Error"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    binding.btnProsesBayar.isEnabled = true
                    binding.btnProsesBayar.text = "Konfirmasi Pembayaran"
                    Toast.makeText(this@PembayaranDetailActivity, "Koneksi Gagal", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}