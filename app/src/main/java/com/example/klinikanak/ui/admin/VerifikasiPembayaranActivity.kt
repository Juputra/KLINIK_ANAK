package com.example.klinikanak.ui.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.Kunjungan
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.adapter.VerifikasiAdapter
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityVerifikasiPembayaranBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifikasiPembayaranActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifikasiPembayaranBinding
    private lateinit var adapter: VerifikasiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifikasiPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        binding.swipeRefresh.setOnRefreshListener { fetchData() }
        fetchData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Verifikasi Pembayaran"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = VerifikasiAdapter(emptyList()) { kunjungan ->
            val intent = Intent(this, DetailVerifikasiActivity::class.java).apply {
                putExtra("id_kunjungan", kunjungan.idKunjungan.toString())
                putExtra("status_layanan", kunjungan.statusLayanan)
                putExtra("nama_anak", kunjungan.namaAnak)
                putExtra("diagnosa", kunjungan.diagnosa)
                putExtra("resep_obat", kunjungan.resepObat)
                putExtra("total_biaya", kunjungan.totalBiaya ?: 0.0)
                putExtra("metode_pembayaran", kunjungan.metodePembayaran)
                putExtra("keterangan_pembayaran", kunjungan.keteranganPembayaran)
                putExtra("bukti_pembayaran", kunjungan.buktiPembayaran)
            }
            startActivity(intent)
        }
        binding.rvVerifikasi.layoutManager = LinearLayoutManager(this)
        binding.rvVerifikasi.adapter = adapter
    }

    private fun fetchData() {
        binding.swipeRefresh.isRefreshing = true
        // Ambil semua kunjungan
        ApiClient.instance.getLayanan("admin", null, null)
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful && response.body()?.status == "success") {
                        // Tampilkan status 3 (Input Harga), 5 (Cek Bukti), 6 (Lunas)
                        val list = response.body()!!.data.filter { it.statusLayanan == 3 || it.statusLayanan >= 5 }
                        adapter.updateData(list)
                        binding.tvEmpty.isVisible = adapter.itemCount == 0

                        if (list.isEmpty()) {
                            Toast.makeText(this@VerifikasiPembayaranActivity, "Belum ada transaksi", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@VerifikasiPembayaranActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@VerifikasiPembayaranActivity, "Koneksi gagal: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun tampilkanDialogKonfirmasi(kunjungan: Kunjungan) {
        val metode = kunjungan.metodePembayaran ?: "Belum diinput pasien"
        val ket = kunjungan.keteranganPembayaran ?: "Tidak ada keterangan"

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pembayaran")
            .setMessage("Pasien: ${kunjungan.namaAnak}\nMetode: $metode\nKeterangan: $ket\n\nTerima pembayaran ini dan nyatakan Lunas?")
            .setPositiveButton("Ya, Lunas") { _, _ -> konfirmasiLunas(kunjungan.idKunjungan.toString()) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun konfirmasiLunas(idKunjungan: String) {
        // Ubah status menjadi 4 (Selesai/Lunas)
        ApiClient.instance.updateStatus(idKunjungan, "4")
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@VerifikasiPembayaranActivity, "Pembayaran berhasil diverifikasi!", Toast.LENGTH_SHORT).show()
                        fetchData() // Refresh layar setelah lunas
                    } else {
                        Toast.makeText(this@VerifikasiPembayaranActivity, "Gagal verifikasi", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@VerifikasiPembayaranActivity, "Koneksi gagal", Toast.LENGTH_SHORT).show()
                }
            })
    }
}