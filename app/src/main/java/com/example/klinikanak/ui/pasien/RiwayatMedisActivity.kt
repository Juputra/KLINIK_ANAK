package com.example.klinikanak.ui.pasien

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.adapter.RiwayatAdapter
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityRiwayatMedisBinding
import com.example.klinikanak.ui.DetailRiwayatActivity
import com.example.klinikanak.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RiwayatMedisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRiwayatMedisBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: RiwayatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatMedisBinding.inflate(layoutInflater)
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
        supportActionBar?.title = "Riwayat Medis"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        // Tambahkan intent saat diklik
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

        binding.rvRiwayat.layoutManager = LinearLayoutManager(this)
        binding.rvRiwayat.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener { fetchData() }
    }

    private fun fetchData() {
        binding.swipeRefresh.isRefreshing = true
        val idUser = sessionManager.getUserId().toString()

        // BUG FIX: Ubah dari "3" menjadi "4" agar hanya yang LUNAS yang masuk rekam medis!
        ApiClient.instance.getLayanan("pasien", idUser, "6")
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            adapter.updateData(body.data)
                            binding.tvEmpty.isVisible = adapter.itemCount == 0
                            if (body.data.isEmpty()) {
                                Toast.makeText(this@RiwayatMedisActivity, "Belum ada riwayat medis (Atau belum lunas)", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            adapter.updateData(emptyList())
                            binding.tvEmpty.isVisible = true
                        }
                    } else {
                        Toast.makeText(this@RiwayatMedisActivity, "Error Server", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@RiwayatMedisActivity, "Koneksi Gagal", Toast.LENGTH_SHORT).show()
                }
            })
    }
}