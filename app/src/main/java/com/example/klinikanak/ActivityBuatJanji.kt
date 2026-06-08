package com.example.klinikanak

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.klinikanak.databinding.ActivityBuatJanjiBinding
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.utils.SessionManager
import java.util.*

class ActivityBuatJanji : AppCompatActivity() {

    private lateinit var binding: ActivityBuatJanjiBinding
    private lateinit var sessionManager: SessionManager
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBuatJanjiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar Back Button
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        sessionManager = SessionManager(this)

        // Setup DatePicker
        binding.etTanggal.setOnClickListener {
            showDatePicker()
        }

        binding.btnSubmit.setOnClickListener {
            submitJanjiMedis()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Format: YYYY-MM-DD sesuai keinginan database
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.etTanggal.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Batasi agar tidak bisa pilih tanggal kemarin
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun submitJanjiMedis() {
        // Mengambil ID Pasien secara dinamis dari SessionManager
        val idPasien = sessionManager.getUserId().toString()
        val idDokter = "2" // Contoh Dokter Budi, sesuaikan dengan logika Anda

        val tanggal = binding.etTanggal.text.toString()
        val keluhan = binding.etKeluhan.text.toString()

        if (tanggal.isEmpty() || keluhan.isEmpty()) {
            Toast.makeText(this, "Harap isi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        // Memanggil API buatJanji melalui ApiClient
        ApiClient.instance.buatJanji(idPasien, idDokter, tanggal, keluhan)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@ActivityBuatJanji, "Antrean berhasil dibuat!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val message = response.body()?.message ?: "Gagal membuat janji"
                        Toast.makeText(this@ActivityBuatJanji, message, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@ActivityBuatJanji, "Koneksi Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
