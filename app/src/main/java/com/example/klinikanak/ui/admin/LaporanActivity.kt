package com.example.klinikanak.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityLaporanBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LaporanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        loadLaporan()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Laporan Transaksi"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadLaporan() {
        ApiClient.instance.getLayanan("admin", null, null)
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val data = response.body()!!.data
                        val selesai = data.filter { it.statusLayanan >= 3 }
                        val pendapatan = selesai.sumOf { it.totalBiaya ?: 0.0 }
                        val umum = selesai.count { it.metodePembayaran == "umum" }
                        val asuransi = selesai.count { it.metodePembayaran == "asuransi" }

                        binding.tvTotalPendapatan.text = "Rp ${pendapatan.toLong()}"
                        binding.tvTotalKunjungan.text = data.size.toString()
                        binding.tvTotalSelesai.text = selesai.size.toString()
                        binding.tvUmum.text = umum.toString()
                        binding.tvAsuransi.text = asuransi.toString()
                    } else {
                        Toast.makeText(this@LaporanActivity, "Gagal memuat laporan", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    Toast.makeText(this@LaporanActivity, "Koneksi gagal: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}