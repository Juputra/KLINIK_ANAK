package com.example.klinikanak.ui.pasien

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.adapter.AntreanAdapter
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityStatusLayananBinding
import com.example.klinikanak.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.view.isVisible

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
        adapter = AntreanAdapter(emptyList())
        binding.rvAntrean.layoutManager = LinearLayoutManager(this)
        binding.rvAntrean.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchData()
        }
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
                            // Filter hanya yang belum selesai (0 atau 1)
                            val listAntrean = body.data.filter { it.statusLayanan < 3 }
                            adapter.updateData(listAntrean)
                            binding.tvEmpty.isVisible = adapter.itemCount == 0
                            
                            if (listAntrean.isEmpty()) {
                                Toast.makeText(this@StatusLayananActivity, "Tidak ada antrean aktif", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@StatusLayananActivity, body?.status ?: "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@StatusLayananActivity, "Error Server: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@StatusLayananActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
