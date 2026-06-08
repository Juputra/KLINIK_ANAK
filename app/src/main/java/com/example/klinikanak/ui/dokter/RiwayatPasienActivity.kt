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
import com.example.klinikanak.databinding.ActivityRiwayatPasienBinding
import com.example.klinikanak.ui.DetailRiwayatActivity
import com.example.klinikanak.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RiwayatPasienActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRiwayatPasienBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: RiwayatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatPasienBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupToolbar()

        adapter = RiwayatAdapter(emptyList()) { kunjungan ->
            val intent = Intent(this, DetailRiwayatActivity::class.java).apply {
                putExtra("nama_anak", kunjungan.namaAnak)
                putExtra("tanggal", kunjungan.tanggalKunjungan)
                putExtra("keluhan", kunjungan.keluhanAwal)
                putExtra("diagnosa", kunjungan.diagnosa)
                putExtra("resep", kunjungan.resepObat)
                putExtra("biaya", kunjungan.totalBiaya ?: 0.0)
            }
            startActivity(intent)
        }
        binding.rvRiwayatPasien.layoutManager = LinearLayoutManager(this)
        binding.rvRiwayatPasien.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { fetchData() }
        fetchData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Riwayat Pasien"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun fetchData() {
        binding.swipeRefresh.isRefreshing = true
        val idDokter = sessionManager.getUserId().toString()

        // Ambil semua kunjungan dokter ini
        ApiClient.instance.getLayanan("dokter", idDokter, null)
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful && response.body()?.status == "success") {

                        // BUG FIX: Filter pasien yang sudah selesai diperiksa (Status 3 ke atas)
                        val list = response.body()!!.data.filter { it.statusLayanan >= 6 || it.statusLayanan == 99 }
                        adapter.updateData(list)
                        binding.tvEmpty.isVisible = adapter.itemCount == 0

                        if (list.isEmpty()) {
                            Toast.makeText(this@RiwayatPasienActivity, "Belum ada riwayat pasien", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@RiwayatPasienActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@RiwayatPasienActivity, "Koneksi gagal", Toast.LENGTH_LONG).show()
                }
            })
    }
}