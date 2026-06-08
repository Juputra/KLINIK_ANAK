package com.example.klinikanak.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.Kunjungan
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.adapter.AntreanAdapter
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityKonfirmasiPendaftaranBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.view.isVisible

class KonfirmasiPendaftaranActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKonfirmasiPendaftaranBinding
    private lateinit var adapter: AntreanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKonfirmasiPendaftaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        binding.swipeRefresh.setOnRefreshListener { fetchData() }
        fetchData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Konfirmasi Pendaftaran"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        // Saat satu item diklik, tampilkan dialog konfirmasi
        adapter = AntreanAdapter(emptyList()) { kunjungan ->
            tampilkanDialogKonfirmasi(kunjungan)
        }
        binding.rvPendaftaran.layoutManager = LinearLayoutManager(this)
        binding.rvPendaftaran.adapter = adapter
    }

    private fun fetchData() {
        binding.swipeRefresh.isRefreshing = true
        // role "admin" => get_layanan.php tidak memfilter id_user; status "0" = baru daftar
        ApiClient.instance.getLayanan("admin", null, "0")
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val list = response.body()!!.data
                        adapter.updateData(list)
                        binding.tvEmpty.isVisible = adapter.itemCount == 0
                        if (list.isEmpty()) {
                            Toast.makeText(this@KonfirmasiPendaftaranActivity, "Tidak ada pendaftaran baru", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@KonfirmasiPendaftaranActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@KonfirmasiPendaftaranActivity, "Koneksi gagal: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun tampilkanDialogKonfirmasi(kunjungan: Kunjungan) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pendaftaran")
            .setMessage("Pendaftaran atas nama ${kunjungan.namaAnak}.\nSilakan pilih tindakan:")
            .setPositiveButton("Terima (Antre)") { _, _ -> konfirmasi(kunjungan.idKunjungan.toString()) }
            .setNegativeButton("Tolak") { _, _ -> tampilkanDialogTolakAdmin(kunjungan) }
            .setNeutralButton("Batal", null)
            .show()
    }
    private fun tampilkanDialogTolakAdmin(kunjungan: Kunjungan) {
        val input = android.widget.EditText(this).apply {
            hint = "Alasan tolak (cth: Dokter Cuti / Jadwal Penuh)"
            setPadding(40, 40, 40, 40)
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Tolak Pendaftaran")
            .setView(input)
            .setPositiveButton("Tolak Pasien") { _, _ ->
                val alasan = input.text.toString().trim()
                if (alasan.isEmpty()) {
                    Toast.makeText(this, "Alasan tolak wajib diisi!", Toast.LENGTH_SHORT).show()
                } else {
                    prosesTolak(kunjungan.idKunjungan.toString(), "admin", alasan)
                }
            }
            .setNegativeButton("Kembali", null)
            .show()
    }
    private fun prosesTolak(idKunjungan: String, role: String, alasan: String) {
        binding.swipeRefresh.isRefreshing = true
        ApiClient.instance.batalKunjungan(idKunjungan, role, alasan).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@KonfirmasiPendaftaranActivity, "Pasien ditolak", Toast.LENGTH_SHORT).show()
                    fetchData()
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@KonfirmasiPendaftaranActivity, "Koneksi Gagal", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun konfirmasi(idKunjungan: String) {
        // Ubah status 0 -> 1 (Antre) lewat endpoint update_status.php yang sudah ada
        ApiClient.instance.updateStatus(idKunjungan, "1")
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@KonfirmasiPendaftaranActivity, "Pasien masuk antrean dokter", Toast.LENGTH_SHORT).show()
                        fetchData() // refresh: item yang sudah dikonfirmasi otomatis hilang dari daftar
                    } else {
                        Toast.makeText(this@KonfirmasiPendaftaranActivity, "Gagal konfirmasi", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@KonfirmasiPendaftaranActivity, "Koneksi gagal", Toast.LENGTH_SHORT).show()
                }
            })
    }
}