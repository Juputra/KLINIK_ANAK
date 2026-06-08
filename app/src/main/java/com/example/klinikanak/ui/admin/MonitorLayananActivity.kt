package com.example.klinikanak.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.Kunjungan
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.adapter.AntreanAdapter
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityMonitorLayananBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MonitorLayananActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonitorLayananBinding
    private lateinit var adapter: AntreanAdapter
    private var allKunjunganList: List<Kunjungan> = emptyList() // Simpan semua data asli

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitorLayananBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupFilter()
        setupRecyclerView()

        binding.swipeRefresh.setOnRefreshListener { fetchData() }
        fetchData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Monitor Seluruh Layanan"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupFilter() {
        val statusOptions = arrayOf(
            "Semua Status",
            "Menunggu Konfirmasi Pendaftaran", // 0
            "Antrean Dokter",      // 1
            "Sedang Diperiksa",    // 2
            "Menunggu Input Harga",// 3
            "Menunggu Pembayaran Pasien", // 4
            "Menunggu Cek Bukti",  // 5
            "Selesai / Lunas"     // 4
        )

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)
        binding.spinnerFilter.adapter = spinnerAdapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilter(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = AntreanAdapter(emptyList()) { kunjungan ->
            // Menampilkan Opsi Hapus saat item di-klik
            konfirmasiHapus(kunjungan)
        }
        binding.rvMonitor.layoutManager = LinearLayoutManager(this)
        binding.rvMonitor.adapter = adapter
    }

    private fun fetchData() {
        binding.swipeRefresh.isRefreshing = true

        // Ambil SEMUA data (role admin, idUser null, status null)
        ApiClient.instance.getLayanan("admin", null, null)
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful && response.body()?.status == "success") {
                        allKunjunganList = response.body()!!.data

                        // Terapkan filter berdasarkan pilihan spinner saat ini
                        applyFilter(binding.spinnerFilter.selectedItemPosition)
                    } else {
                        Toast.makeText(this@MonitorLayananActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@MonitorLayananActivity, "Koneksi gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun applyFilter(filterIndex: Int) {
        val filteredList = when (filterIndex) {
            1 -> allKunjunganList.filter { it.statusLayanan == 0 }
            2 -> allKunjunganList.filter { it.statusLayanan == 1 }
            3 -> allKunjunganList.filter { it.statusLayanan == 2 }
            4 -> allKunjunganList.filter { it.statusLayanan == 3 }
            5 -> allKunjunganList.filter { it.statusLayanan == 4 }
            6 -> allKunjunganList.filter { it.statusLayanan == 5 }
            7 -> allKunjunganList.filter { it.statusLayanan == 6 }
            else -> allKunjunganList
        }

        adapter.updateData(filteredList)
        binding.tvEmpty.isVisible = adapter.itemCount == 0
    }

    private fun konfirmasiHapus(kunjungan: Kunjungan) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Data Kunjungan")
            .setMessage("Anda yakin ingin menghapus riwayat/kunjungan atas nama ${kunjungan.namaAnak}?\n\nTindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                eksekusiHapus(kunjungan.idKunjungan.toString())
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun eksekusiHapus(idKunjungan: String) {
        binding.swipeRefresh.isRefreshing = true
        ApiClient.instance.hapusKunjungan(idKunjungan).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@MonitorLayananActivity, "Data kunjungan dihapus", Toast.LENGTH_SHORT).show()
                    fetchData() // Refresh list setelah dihapus
                } else {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@MonitorLayananActivity, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@MonitorLayananActivity, "Koneksi gagal", Toast.LENGTH_SHORT).show()
            }
        })
    }
}