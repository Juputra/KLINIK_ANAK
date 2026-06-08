package com.example.klinikanak.ui.dokter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.adapter.AntreanAdapter
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityPemeriksaanBinding
import com.example.klinikanak.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.view.isVisible
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.Kunjungan

class PemeriksaanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPemeriksaanBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: AntreanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPemeriksaanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        fetchData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Daftar Antrean"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = AntreanAdapter(emptyList()) { kunjungan ->
            if (kunjungan.statusLayanan == 1) {
                // Pasien masih di ruang tunggu, konfirmasi panggilan
                AlertDialog.Builder(this)
                    .setTitle("Panggil Pasien")
                    .setMessage("Ubah status ${kunjungan.namaAnak} menjadi 'Sedang Diperiksa' dan arahkan ke ruangan?")
                    .setPositiveButton("Ya, Panggil") { _, _ ->
                        panggilPasien(kunjungan)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            } else if (kunjungan.statusLayanan == 2) {
                // Pasien sudah berstatus sedang diperiksa, langsung buka form
                bukaFormPemeriksaan(kunjungan)
            }
        }
        binding.rvPemeriksaan.layoutManager = LinearLayoutManager(this)
        binding.rvPemeriksaan.adapter = adapter
    }

    private fun panggilPasien(kunjungan: Kunjungan) {
        // Tampilkan loading di SwipeRefresh
        binding.swipeRefresh.isRefreshing = true

        ApiClient.instance.updateStatus(kunjungan.idKunjungan.toString(), "2")
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@PemeriksaanActivity, "Pasien dipanggil ke ruangan", Toast.LENGTH_SHORT).show()
                        bukaFormPemeriksaan(kunjungan)
                    } else {
                        Toast.makeText(this@PemeriksaanActivity, "Gagal mengubah status", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@PemeriksaanActivity, "Koneksi gagal saat memanggil pasien", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun bukaFormPemeriksaan(kunjungan: Kunjungan) {
        val intent = Intent(this, PemeriksaanDetailActivity::class.java).apply {
            putExtra("id_kunjungan", kunjungan.idKunjungan.toString())
            putExtra("keluhan", kunjungan.keluhanAwal)
            putExtra("nama_anak", kunjungan.namaAnak ?: "Pasien #${kunjungan.idPasien}")
            putExtra("nama_ortu", kunjungan.namaOrtu ?: "-")
        }
        startActivity(intent)
    }
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener { fetchData() }
    }

    private fun fetchData() {
        binding.swipeRefresh.isRefreshing = true
        val idUser = sessionManager.getUserId().toString()

        // Pass 'null' pada parameter status agar mengambil semua data pasien dokter ini
        ApiClient.instance.getLayanan("dokter", idUser, null)
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful && response.body()?.status == "success") {

                        // Filter lokal: Hanya tampilkan pasien dengan status 1 (Antre) & 2 (Sedang Diperiksa)
                        val listAktif = response.body()!!.data.filter {
                            it.statusLayanan == 1 || it.statusLayanan == 2
                        }

                        adapter.updateData(listAktif)
                        binding.tvEmpty.isVisible = adapter.itemCount == 0
                    } else {
                        Toast.makeText(this@PemeriksaanActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@PemeriksaanActivity, "Koneksi Gagal", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }
}