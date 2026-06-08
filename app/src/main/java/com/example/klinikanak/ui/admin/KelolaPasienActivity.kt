package com.example.klinikanak.ui.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.Pasien
import com.example.klinikanak.PasienResponse
import com.example.klinikanak.R
import com.example.klinikanak.adapter.PasienAdapter
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityKelolaPasienBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class KelolaPasienActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKelolaPasienBinding
    private lateinit var adapter: PasienAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKelolaPasienBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        adapter = PasienAdapter(emptyList()) { pasien -> tampilkanOpsi(pasien) }
        binding.rvPasien.layoutManager = LinearLayoutManager(this)
        binding.rvPasien.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { fetchData() }
        binding.btnTambahPasien.setOnClickListener { tampilkanForm(null) }

        fetchData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Kelola Pasien"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun fetchData() {
        binding.swipeRefresh.isRefreshing = true
        ApiClient.instance.getPasien().enqueue(object : Callback<PasienResponse> {
            override fun onResponse(call: Call<PasienResponse>, response: Response<PasienResponse>) {
                binding.swipeRefresh.isRefreshing = false
                if (response.isSuccessful && response.body()?.status == "success") {
                    adapter.updateData(response.body()!!.data)
                    binding.tvEmpty.isVisible = adapter.itemCount == 0
                } else {
                    Toast.makeText(this@KelolaPasienActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<PasienResponse>, t: Throwable) {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@KelolaPasienActivity, "Jaringan Bermasalah", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun tampilkanOpsi(pasien: Pasien) {
        AlertDialog.Builder(this)
            .setTitle(pasien.namaAnak)
            .setItems(arrayOf("Edit Data Pasien", "Hapus Pasien")) { _, which ->
                if (which == 0) tampilkanForm(pasien) else cekKunjunganSebelumHapus(pasien)
            }.show()
    }

    private fun tampilkanForm(pasien: Pasien?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_tambah_pasien, null)
        val etAnak = view.findViewById<EditText>(R.id.etNamaAnak)
        val etOrtu = view.findViewById<EditText>(R.id.etNamaOrtu)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPass = view.findViewById<EditText>(R.id.etPassword)
        val etTgl = view.findViewById<EditText>(R.id.etTanggalLahir)
        val etHp = view.findViewById<EditText>(R.id.etNoHp)
        val rgJk = view.findViewById<RadioGroup>(R.id.rgJenisKelamin)

        etTgl.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                etTgl.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        if (pasien != null) {
            etAnak.setText(pasien.namaAnak)
            etOrtu.setText(pasien.namaOrtu)
            etEmail.setText(pasien.email)
            etTgl.setText(pasien.tanggalLahirAnak)
            etHp.setText(pasien.noHp)
            etPass.hint = "Kosongkan jika tidak ganti sandi"
            if (pasien.jenisKelaminAnak == "L") view.findViewById<RadioButton>(R.id.rbLaki).isChecked = true
            else view.findViewById<RadioButton>(R.id.rbPerempuan).isChecked = true
        }

        AlertDialog.Builder(this)
            .setTitle(if (pasien == null) "Tambah Pasien" else "Edit Pasien")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val anak = etAnak.text.toString().trim()
                val ortu = etOrtu.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val pass = etPass.text.toString().trim()
                val tgl = etTgl.text.toString().trim()
                val hp = etHp.text.toString().trim()
                val jk = if (rgJk.checkedRadioButtonId == R.id.rbLaki) "L" else "P"

                if (anak.isEmpty() || ortu.isEmpty() || email.isEmpty() || tgl.isEmpty() || hp.isEmpty()) {
                    Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
                } else if (pasien == null) {
                    if (pass.isEmpty()) Toast.makeText(this, "Sandi wajib diisi", Toast.LENGTH_SHORT).show()
                    else ApiClient.instance.registerPasien(email, pass, ortu, anak, tgl, jk, hp).enqueue(cb())
                } else {
                    ApiClient.instance.updatePasien(pasien.idPasien.toString(), email, pass, ortu, anak, tgl, jk, hp).enqueue(cb())
                }
            }.setNegativeButton("Batal", null).show()
    }

    private fun cekKunjunganSebelumHapus(pasien: Pasien) {
        Toast.makeText(this, "Memeriksa status aktif pasien...", Toast.LENGTH_SHORT).show()

        ApiClient.instance.getLayanan("pasien", pasien.idPasien.toString(), null)
            .enqueue(object : Callback<LayananResponse> {
                override fun onResponse(call: Call<LayananResponse>, response: Response<LayananResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        // Filter: Apakah ada status kunjungan < 4 (Antre/Periksa/Bayar)
                        val kunjunganAktif = response.body()!!.data.filter { it.statusLayanan < 4 }

                        if (kunjunganAktif.isNotEmpty()) {
                            AlertDialog.Builder(this@KelolaPasienActivity)
                                .setTitle("Gagal Menghapus")
                                .setMessage("Anak ${pasien.namaAnak} memiliki ${kunjunganAktif.size} rekam antrean medis yang sedang berjalan aktif di klinik.\n\nSelesaikan seluruh proses pemeriksaan/pembayaran pasien terlebih dahulu sebelum menghapus akun.")
                                .setPositiveButton("Mengerti", null).show()
                        } else {
                            konfirmasiHapusPasien(pasien)
                        }
                    } else {
                        konfirmasiHapusPasien(pasien) // Jika belum punya rekam jejak, aman langsung tawarkan hapus
                    }
                }
                override fun onFailure(call: Call<LayananResponse>, t: Throwable) {
                    Toast.makeText(this@KelolaPasienActivity, "Gagal memvalidasi keamanan data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun konfirmasiHapusPasien(pasien: Pasien) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pasien")
            .setMessage("Yakin ingin menghapus permanen data ${pasien.namaAnak}?\nTindakan ini membersihkan history jika tidak ada transaksi tertunda.")
            .setPositiveButton("Hapus") { _, _ ->
                ApiClient.instance.hapusPasien(pasien.idPasien.toString()).enqueue(cb())
            }.setNegativeButton("Batal", null).show()
    }

    private fun cb() = object : Callback<ApiResponse> {
        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
            if (response.isSuccessful && response.body()?.status == "success") {
                Toast.makeText(this@KelolaPasienActivity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                fetchData()
            } else {
                Toast.makeText(this@KelolaPasienActivity, response.body()?.message ?: "Gagal", Toast.LENGTH_SHORT).show()
            }
        }
        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
            Toast.makeText(this@KelolaPasienActivity, "Koneksi Bermasalah", Toast.LENGTH_SHORT).show()
        }
    }
}