package com.example.klinikanak.ui.dokter

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityPemeriksaanDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PemeriksaanDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPemeriksaanDetailBinding
    private var idKunjungan: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPemeriksaanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idKunjungan = intent.getStringExtra("id_kunjungan")
        val keluhan = intent.getStringExtra("keluhan")

        binding.tvKeluhan.text = "Keluhan: $keluhan"
        binding.tvNamaAnak.text = "ID Kunjungan: $idKunjungan"

        setupToolbar()

        binding.btnSimpanPemeriksaan.setOnClickListener {
            simpanPemeriksaan()
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

    private fun simpanPemeriksaan() {
        val diagnosa = binding.etDiagnosa.text.toString().trim()
        val resep = binding.etResep.text.toString().trim()

        if (diagnosa.isEmpty() || resep.isEmpty()) {
            Toast.makeText(this, "Harap isi diagnosa dan resep", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSimpanPemeriksaan.isEnabled = false
        binding.btnSimpanPemeriksaan.text = "Menyimpan..."

        idKunjungan?.let { id ->
            ApiClient.instance.simpanPemeriksaan(id, diagnosa, resep)
                .enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        binding.btnSimpanPemeriksaan.isEnabled = true
                        binding.btnSimpanPemeriksaan.text = "Simpan Pemeriksaan"

                        if (response.isSuccessful && response.body()?.status == "success") {
                            Toast.makeText(this@PemeriksaanDetailActivity, "Pemeriksaan berhasil disimpan", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@PemeriksaanDetailActivity, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        binding.btnSimpanPemeriksaan.isEnabled = true
                        binding.btnSimpanPemeriksaan.text = "Simpan Pemeriksaan"
                        Toast.makeText(this@PemeriksaanDetailActivity, "Koneksi Gagal", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
