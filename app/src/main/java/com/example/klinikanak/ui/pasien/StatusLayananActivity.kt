package com.example.klinikanak.ui.pasien

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.Kunjungan
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.adapter.AntreanAdapter
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityStatusLayananBinding
import com.example.klinikanak.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StatusLayananActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatusLayananBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: AntreanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusLayananBinding.inflate(layoutInflater)
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
        supportActionBar?.title = "Status Layanan"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        // Pasien hanya lihat, tidak perlu onItemClick
        adapter = AntreanAdapter(emptyList()) { kunjungan ->
            // Pasien hanya bisa klik jika statusnya masih 0 (Menunggu Konfirmasi)
            if (kunjungan.statusLayanan == 0) {
                tampilkanDialogBatalPasien(kunjungan)
            }
        }
        binding.rvAntrean.layoutManager = LinearLayoutManager(this)
        binding.rvAntrean.adapter = adapter
    }
    private fun tampilkanDialogBatalPasien(kunjungan: Kunjungan) {
        val input = android.widget.EditText(this).apply {
            hint = "Alasan batal (cth: Berhalangan hadir)"
            setPadding(40, 40, 40, 40)
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Batalkan Janji Medis?")
            .setMessage("Apakah Anda yakin ingin membatalkan antrean ini?")
            .setView(input)
            .setPositiveButton("Ya, Batalkan") { _, _ ->
                val alasan = input.text.toString().trim()
                prosesBatal(kunjungan.idKunjungan.toString(), "pasien", if (alasan.isEmpty()) "Dibatalkan sepihak oleh pasien" else alasan)
            }
            .setNegativeButton("Tutup", null)
            .show()
    }
    private fun prosesBatal(idKunjungan: String, role: String, alasan: String) {
        binding.swipeRefresh.isRefreshing = true
        ApiClient.instance.batalKunjungan(idKunjungan, role, alasan).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@StatusLayananActivity, "Janji berhasil dibatalkan", Toast.LENGTH_SHORT).show()
                    fetchData() // Refresh list
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@StatusLayananActivity, "Koneksi Gagal", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener { fetchData() }
    }

    private fun fetchData() {
        binding.swipeRefresh.isRefreshing = true
        val idUser = sessionManager.getUserId().toString()

        ApiClient.instance.getLayanan("pasien", idUser, null)
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            // ✅ FIX 5: Ganti < 3 menjadi < 4
                            // Sekarang status 3 (Menunggu Pembayaran) juga muncul
                            val listAntrean = body.data.filter { it.statusLayanan < 6 }
                            adapter.updateData(listAntrean)
                            binding.tvEmpty.isVisible = adapter.itemCount == 0

                            // Banner HANYA muncul kalau ada tagihan (Status 4)
                            val adaYangHarusBayar = listAntrean.any { it.statusLayanan == 4 }
                            tampilkanBannerPembayaran(adaYangHarusBayar)

                            if (listAntrean.isEmpty()) {
                                Toast.makeText(
                                    this@StatusLayananActivity,
                                    "Tidak ada antrean aktif",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@StatusLayananActivity,
                                body?.status ?: "Gagal mengambil data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@StatusLayananActivity,
                            "Error Server: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(
                        this@StatusLayananActivity,
                        "Koneksi Gagal: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // ✅ FIX 5: Banner navigasi — muncul jika ada kunjungan berstatus 3
    // Mengarahkan pasien ke menu Pembayaran secara aktif
    private fun tampilkanBannerPembayaran(tampil: Boolean) {
        // Gunakan tvBannerPembayaran yang sudah kamu tambahkan di layout
        // (lihat instruksi update layout di bawah)
        binding.tvBannerPembayaran.isVisible = tampil
        if (tampil) {
            binding.tvBannerPembayaran.setOnClickListener {
                startActivity(Intent(this, PembayaranActivity::class.java))
            }
        }
    }
}