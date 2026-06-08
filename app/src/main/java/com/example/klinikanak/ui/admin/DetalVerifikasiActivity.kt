package com.example.klinikanak.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.klinikanak.AppConstants
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityDetailVerifikasiBinding
import com.example.klinikanak.utils.FormatHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailVerifikasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailVerifikasiBinding
    private var idKunjungan: String = ""
    private var statusLayanan: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailVerifikasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        tangkapDataIntent()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Tindakan Admin"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun tangkapDataIntent() {
        idKunjungan = intent.getStringExtra("id_kunjungan") ?: ""
        statusLayanan = intent.getIntExtra("status_layanan", 0)

        binding.tvNamaAnak.text = "Pasien: ${intent.getStringExtra("nama_anak")}"
        binding.tvDiagnosa.text = "Diagnosa: ${intent.getStringExtra("diagnosa")}"
        binding.tvResep.text = "Resep: ${intent.getStringExtra("resep_obat")}"

        val totalBiaya = intent.getDoubleExtra("total_biaya", 0.0)
        val metode = intent.getStringExtra("metode_pembayaran") ?: "-"
        val keterangan = intent.getStringExtra("keterangan_pembayaran") ?: "-"
        val fileBukti = intent.getStringExtra("bukti_pembayaran")

        // Logika Tampilan Berdasarkan Status Baru
        when (statusLayanan) {
            3 -> {
                // Menunggu Input Harga dari Admin
                binding.layoutInputHarga.visibility = View.VISIBLE
                binding.btnAksiAdmin.text = "Tetapkan Harga & Kirim Tagihan"
                binding.btnAksiAdmin.setOnClickListener { prosesSetHarga() }
            }
            5 -> {
                // Pasien sudah bayar, menunggu verifikasi Admin
                binding.layoutBukti.visibility = View.VISIBLE
                binding.tvInfoBayar.text = "Dibayar via: $metode\nKet: $keterangan\nTotal: ${FormatHelper.formatRupiah(totalBiaya)}"

                // Load Gambar
                val imageUrl = AppConstants.IMAGE_URL + fileBukti
                Glide.with(this).load(imageUrl).into(binding.ivBuktiPembayaran)

                binding.btnAksiAdmin.text = "Verifikasi & Nyatakan Lunas"
                binding.btnAksiAdmin.setOnClickListener { konfirmasiLunasDialog() }
                binding.btnTolakBukti.visibility = View.VISIBLE
                binding.btnTolakBukti.setOnClickListener { dialogTolakBukti() }
            }
            6 -> {
                // Sudah Lunas (Hanya lihat)
                binding.layoutBukti.visibility = View.VISIBLE
                binding.tvInfoBayar.text = "STATUS: LUNAS\nMetode: $metode\nKet: $keterangan"
                val imageUrl = AppConstants.IMAGE_URL + fileBukti
                Glide.with(this).load(imageUrl).into(binding.ivBuktiPembayaran)
                binding.btnAksiAdmin.visibility = View.GONE
            }
        }
    }

    private fun prosesSetHarga() {
        val hargaStr = binding.etInputHarga.text.toString().trim()
        if (hargaStr.isEmpty()) {
            Toast.makeText(this, "Harga tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnAksiAdmin.isEnabled = false
        binding.btnAksiAdmin.text = "Memproses..."

        ApiClient.instance.setHarga(idKunjungan, hargaStr).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@DetailVerifikasiActivity, "Tagihan berhasil dikirim ke Pasien!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@DetailVerifikasiActivity, "Gagal memproses", Toast.LENGTH_SHORT).show()
                    binding.btnAksiAdmin.isEnabled = true
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@DetailVerifikasiActivity, "Koneksi Gagal", Toast.LENGTH_SHORT).show()
                binding.btnAksiAdmin.isEnabled = true
            }
        })
    }

    private fun konfirmasiLunasDialog() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pembayaran")
            .setMessage("Apakah bukti transfer sudah benar dan uang sudah masuk?")
            .setPositiveButton("Ya, Lunas") { _, _ -> prosesLunas() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun prosesLunas() {
        binding.btnAksiAdmin.isEnabled = false
        ApiClient.instance.konfirmasiPembayaran(idKunjungan).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@DetailVerifikasiActivity, "Pembayaran Selesai Diverifikasi!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                binding.btnAksiAdmin.isEnabled = true
            }
        })
    }
    private fun dialogTolakBukti() {
        val input = android.widget.EditText(this)
        input.hint = "Tulis alasan penolakan (cth: Foto buram)"
        input.setPadding(40, 40, 40, 40)

        AlertDialog.Builder(this)
            .setTitle("Tolak Bukti Pembayaran")
            .setMessage("Pasien akan diminta untuk mengunggah ulang bukti pembayaran.")
            .setView(input)
            .setPositiveButton("Kirim Penolakan") { _, _ ->
                val alasan = input.text.toString().trim()
                if (alasan.isNotEmpty()) {
                    prosesTolakBukti(alasan)
                } else {
                    Toast.makeText(this, "Alasan wajib diisi!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun prosesTolakBukti(alasan: String) {
        binding.btnAksiAdmin.isEnabled = false
        binding.btnTolakBukti.isEnabled = false
        binding.btnTolakBukti.text = "Memproses..."

        ApiClient.instance.tolakPembayaran(idKunjungan, alasan).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@DetailVerifikasiActivity, "Penolakan berhasil dikirim ke Pasien!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@DetailVerifikasiActivity, "Gagal menolak", Toast.LENGTH_SHORT).show()
                    binding.btnAksiAdmin.isEnabled = true
                    binding.btnTolakBukti.isEnabled = true
                    binding.btnTolakBukti.text = "Tolak Bukti (Minta Upload Ulang)"
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@DetailVerifikasiActivity, "Koneksi Gagal", Toast.LENGTH_SHORT).show()
                binding.btnAksiAdmin.isEnabled = true
                binding.btnTolakBukti.isEnabled = true
                binding.btnTolakBukti.text = "Tolak Bukti (Minta Upload Ulang)"
            }
        })
    }
}