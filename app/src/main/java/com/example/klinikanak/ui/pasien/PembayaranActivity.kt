package com.example.klinikanak.ui.pasien

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.adapter.PembayaranAdapter
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityPembayaranBinding
import com.example.klinikanak.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PembayaranActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPembayaranBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: PembayaranAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPembayaranBinding.inflate(layoutInflater)
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
        supportActionBar?.title = "Tagihan Pembayaran"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = PembayaranAdapter(emptyList()) { kunjungan ->
            // ✅ FIX 9: Kirim data medis ke halaman detail pembayaran
            val intent = Intent(this, PembayaranDetailActivity::class.java).apply {
                putExtra("id_kunjungan", kunjungan.idKunjungan.toString())
                putExtra("diagnosa", kunjungan.diagnosa ?: "-")
                putExtra("resep", kunjungan.resepObat ?: "-")
                putExtra("nama_anak", kunjungan.namaAnak ?: "Pasien")
            }
            startActivity(intent)
        }
        binding.rvPembayaran.layoutManager = LinearLayoutManager(this)
        binding.rvPembayaran.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener { fetchData() }
    }

    private fun fetchData() {
        binding.swipeRefresh.isRefreshing = true
        val idUser = sessionManager.getUserId().toString()

        ApiClient.instance.getLayanan("pasien", idUser, "3")
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            adapter.updateData(body.data)
                            binding.tvEmpty.isVisible = adapter.itemCount == 0
                        } else {
                            adapter.updateData(emptyList())
                            binding.tvEmpty.isVisible = true
                        }
                    } else {
                        Toast.makeText(this@PembayaranActivity, "Error Server", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@PembayaranActivity, "Koneksi Gagal", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }
}