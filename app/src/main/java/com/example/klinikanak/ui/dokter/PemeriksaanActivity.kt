package com.example.klinikanak.ui.dokter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
            // Buka Detail Pemeriksaan
            val intent = Intent(this, PemeriksaanDetailActivity::class.java)
            intent.putExtra("id_kunjungan", kunjungan.idKunjungan.toString())
            intent.putExtra("keluhan", kunjungan.keluhanAwal)
            startActivity(intent)
        }
        binding.rvPemeriksaan.layoutManager = LinearLayoutManager(this)
        binding.rvPemeriksaan.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchData()
        }
    }

    private fun fetchData() {
        binding.swipeRefresh.isRefreshing = true
        val idUser = sessionManager.getUserId().toString()
        
        ApiClient.instance.getLayanan("dokter", idUser, "1") // 0 = Menunggu
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            adapter.updateData(body.data)
                            binding.tvEmpty.isVisible = adapter.itemCount == 0
                        } else {
                            Toast.makeText(this@PemeriksaanActivity, body?.status ?: "Gagal", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@PemeriksaanActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
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
