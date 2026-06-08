package com.example.klinikanak.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.Dokter
import com.example.klinikanak.DokterResponse
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.R
import com.example.klinikanak.adapter.DokterAdapter
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityKelolaDokterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KelolaDokterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKelolaDokterBinding
    private lateinit var adapter: DokterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKelolaDokterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        adapter = DokterAdapter(emptyList()) { dokter -> tampilkanOpsi(dokter) }
        binding.rvDokter.layoutManager = LinearLayoutManager(this)
        binding.rvDokter.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { fetchData() }
        binding.btnTambahDokter.setOnClickListener { tampilkanForm(null) }
        fetchData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Kelola Dokter"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun fetchData() {
        binding.swipeRefresh.isRefreshing = true
        ApiClient.instance.getDokter().enqueue(object : Callback<DokterResponse> {
            override fun onResponse(call: Call<DokterResponse>, response: Response<DokterResponse>) {
                binding.swipeRefresh.isRefreshing = false
                if (response.isSuccessful && response.body()?.status == "success") {
                    adapter.updateData(response.body()!!.data)
                    binding.tvEmpty.isVisible = adapter.itemCount == 0
                } else {
                    Toast.makeText(this@KelolaDokterActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<DokterResponse>, t: Throwable) {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@KelolaDokterActivity, "Koneksi gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun tampilkanOpsi(dokter: Dokter) {
        AlertDialog.Builder(this)
            .setTitle(dokter.namaLengkap)
            .setItems(arrayOf("Edit Data", "Hapus")) { _, which ->
                if (which == 0) tampilkanForm(dokter) else konfirmasiHapus(dokter)
            }
            .show()
    }

    private fun tampilkanForm(dokter: Dokter?) {
        val view       = LayoutInflater.from(this).inflate(R.layout.dialog_tambah_dokter, null)
        val etNama     = view.findViewById<EditText>(R.id.etNama)
        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etNoSip    = view.findViewById<EditText>(R.id.etNoSip)
        val etSpes     = view.findViewById<EditText>(R.id.etSpesialisasi)

        if (dokter != null) {
            etNama.setText(dokter.namaLengkap)
            etUsername.setText(dokter.username)
            etNoSip.setText(dokter.noSip ?: "")
            etSpes.setText(dokter.spesialisasi ?: "")
            etPassword.hint = "Password (kosongkan jika tidak diubah)"
        }

        AlertDialog.Builder(this)
            .setTitle(if (dokter == null) "Tambah Dokter" else "Edit Dokter")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nama     = etNama.text.toString().trim()
                val username = etUsername.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val noSip    = etNoSip.text.toString().trim()
                val spes     = etSpes.text.toString().trim()

                if (nama.isEmpty() || username.isEmpty()) {
                    Toast.makeText(this, "Nama dan username wajib diisi", Toast.LENGTH_SHORT).show()
                } else if (dokter == null) {
                    if (password.isEmpty()) {
                        Toast.makeText(this, "Password wajib diisi", Toast.LENGTH_SHORT).show()
                    } else {
                        ApiClient.instance.tambahDokter(username, password, nama, noSip, spes).enqueue(cb())
                    }
                } else {
                    ApiClient.instance.updateDokter(dokter.idUser, username, password, nama, noSip, spes).enqueue(cb())
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ✅ FIX 6: Cek pasien aktif sebelum hapus
    private fun konfirmasiHapus(dokter: Dokter) {
        // Tampilkan loading / nonaktifkan interaksi sementara
        Toast.makeText(this, "Memeriksa data pasien aktif...", Toast.LENGTH_SHORT).show()

        ApiClient.instance.getLayanan("dokter", dokter.idUser, null)
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val pasienAktif = response.body()!!.data.filter { it.statusLayanan < 3 }

                        if (pasienAktif.isNotEmpty()) {
                            // ❌ TOLAK: masih ada pasien yang belum selesai
                            AlertDialog.Builder(this@KelolaDokterActivity)
                                .setTitle("Tidak Bisa Dihapus")
                                .setMessage(
                                    "${dokter.namaLengkap} masih memiliki ${pasienAktif.size} pasien aktif " +
                                            "yang belum selesai ditangani.\n\n" +
                                            "Selesaikan semua kunjungan aktif terlebih dahulu sebelum menghapus dokter ini."
                                )
                                .setPositiveButton("Mengerti", null)
                                .show()
                        } else {
                            // ✅ AMAN: tidak ada pasien aktif, lanjut konfirmasi hapus
                            AlertDialog.Builder(this@KelolaDokterActivity)
                                .setTitle("Hapus Dokter")
                                .setMessage("Yakin menghapus ${dokter.namaLengkap}?\n\nTindakan ini tidak dapat dibatalkan.")
                                .setPositiveButton("Hapus") { _, _ ->
                                    ApiClient.instance.hapusUser(dokter.idUser).enqueue(cb())
                                }
                                .setNegativeButton("Batal", null)
                                .show()
                        }
                    } else {
                        // Jika tidak bisa cek, izinkan hapus dengan peringatan
                        AlertDialog.Builder(this@KelolaDokterActivity)
                            .setTitle("Hapus Dokter")
                            .setMessage("Tidak dapat memverifikasi pasien aktif. Yakin tetap menghapus ${dokter.namaLengkap}?")
                            .setPositiveButton("Hapus") { _, _ ->
                                ApiClient.instance.hapusUser(dokter.idUser).enqueue(cb())
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                }
                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    Toast.makeText(
                        this@KelolaDokterActivity,
                        "Koneksi gagal saat memeriksa pasien: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun cb() = object : Callback<ApiResponse> {
        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
            val body = response.body()
            if (response.isSuccessful && body?.status == "success") {
                Toast.makeText(this@KelolaDokterActivity, body.message, Toast.LENGTH_SHORT).show()
                fetchData()
            } else {
                Toast.makeText(this@KelolaDokterActivity, body?.message ?: "Gagal", Toast.LENGTH_SHORT).show()
            }
        }
        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
            Toast.makeText(this@KelolaDokterActivity, "Koneksi gagal", Toast.LENGTH_SHORT).show()
        }
    }
}