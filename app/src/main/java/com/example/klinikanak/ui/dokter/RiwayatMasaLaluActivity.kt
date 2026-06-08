package com.example.klinikanak.ui.dokter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.adapter.RiwayatAdapter
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityRiwayatMasaLaluBinding
import com.example.klinikanak.ui.DetailRiwayatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RiwayatMasaLaluActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatMasaLaluBinding
    private lateinit var adapter: RiwayatAdapter
    private var idPasien: String = ""
    private var namaAnak: String = "Pasien"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatMasaLaluBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idPasien = intent.getStringExtra("id_pasien") ?: ""
        namaAnak = intent.getStringExtra("nama_anak") ?: "Pasien"

        setupToolbar()
        setupRecyclerView()

        binding.swipeRefresh.setOnRefreshListener { fetchData() }
        fetchData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Set judul toolbar menggunakan nama anak
        supportActionBar?.title = "Rekam Medis: $namaAnak"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        // Menggunakan RiwayatAdapter yang sudah ada
        adapter = RiwayatAdapter(emptyList()) { kunjungan ->
            val intent = Intent(this, DetailRiwayatActivity::class.java).apply {
                putExtra("nama_anak", kunjungan.namaAnak)
                putExtra("tanggal", kunjungan.tanggalKunjungan)
                putExtra("keluhan", kunjungan.keluhanAwal)
                putExtra("diagnosa", kunjungan.diagnosa)
                putExtra("resep", kunjungan.resepObat)
                putExtra("biaya", kunjungan.totalBiaya ?: 0.0)
                putExtra("bukti_pembayaran", kunjungan.buktiPembayaran)
            }
            startActivity(intent)
        }
        binding.rvRiwayatMasaLalu.layoutManager = LinearLayoutManager(this)
        binding.rvRiwayatMasaLalu.adapter = adapter
    }

    private fun fetchData() {
        if (idPasien.isEmpty()) return

        binding.swipeRefresh.isRefreshing = true

        ApiClient.instance.getRiwayatMedisPasien(idPasien)
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val list = response.body()!!.data
                        adapter.updateData(list)
                        binding.tvEmpty.isVisible = list.isEmpty()
                    } else {
                        Toast.makeText(this@RiwayatMasaLaluActivity, "Gagal memuat rekam medis", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@RiwayatMasaLaluActivity, "Koneksi gagal", Toast.LENGTH_SHORT).show()
                }
            })
    }
}