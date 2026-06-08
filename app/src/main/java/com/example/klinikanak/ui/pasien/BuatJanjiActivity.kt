package com.example.klinikanak.ui.pasien

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.Dokter
import com.example.klinikanak.DokterResponse
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityBuatJanjiBinding
import com.example.klinikanak.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class BuatJanjiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuatJanjiBinding
    private lateinit var sessionManager: SessionManager
    private val calendar = Calendar.getInstance()

    // Simpan daftar dokter agar bisa ambil id_user sesuai pilihan spinner
    private var listDokter: List<Dokter> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuatJanjiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()
        loadDokter()

        binding.etTanggal.setOnClickListener { showDatePicker() }
        binding.btnSubmit.setOnClickListener { submitJanjiMedis() }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Buat Janji"
        binding.toolbar.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadDokter() {
        ApiClient.instance.getDokter().enqueue(object : Callback<DokterResponse> {
            override fun onResponse(call: Call<DokterResponse>, response: Response<DokterResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    listDokter = response.body()!!.data
                    val namaDokter = listDokter.map { "${it.namaLengkap} (${it.spesialisasi ?: "-"})" }
                    val adapter = ArrayAdapter(
                        this@BuatJanjiActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        namaDokter
                    )
                    binding.spinnerDokter.adapter = adapter
                } else {
                    Toast.makeText(this@BuatJanjiActivity, "Gagal memuat daftar dokter", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<DokterResponse>, t: Throwable) {
                Toast.makeText(this@BuatJanjiActivity, "Koneksi gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.etTanggal.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun submitJanjiMedis() {
        val idPasien = sessionManager.getUserId().toString()
        val tanggal = binding.etTanggal.text.toString()
        val keluhan = binding.etKeluhan.text.toString()

        if (listDokter.isEmpty()) {
            Toast.makeText(this, "Daftar dokter belum termuat, coba lagi", Toast.LENGTH_SHORT).show()
            return
        }
        // Ambil id_user dokter sesuai yang dipilih di spinner
        val posisi = binding.spinnerDokter.selectedItemPosition
        val idDokter = listDokter[posisi].idUser

        if (tanggal.isEmpty() || keluhan.isEmpty()) {
            Toast.makeText(this, "Harap isi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.text = "Mengirim..."

        ApiClient.instance.buatJanji(idPasien, idDokter, tanggal, keluhan)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "Kirim Janji"
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@BuatJanjiActivity, "Antrean berhasil dibuat!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val message = response.body()?.message ?: "Gagal membuat janji"
                        Toast.makeText(this@BuatJanjiActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "Kirim Janji"
                    Toast.makeText(this@BuatJanjiActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}