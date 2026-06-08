package com.example.klinikanak.ui.pasien

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.R
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityPembayaranDetailBinding
import com.example.klinikanak.utils.FormatHelper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class PembayaranDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPembayaranDetailBinding
    private var selectedImageUri: Uri? = null

    // Variabel untuk menampung data kunjungan dari intent
    private var idKunjungan: String = ""
    private var totalBiayaRaw: Double = 0.0

    // Handler Modern untuk Membuka Galeri HP Pasien
    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivPreviewBukti.setImageURI(it) // Tampilkan gambar ke preview ImageView
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPembayaranDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        tangkapDataIntent()
        setupMetodePembayaran()

        // Tombol Pilih Gambar Bukti
        binding.btnPilihFoto.setOnClickListener {
            getImage.launch("image/*")
        }

        // Tombol Konfirmasi / Proses Bayar (Kirim ke Server)
        binding.btnProsesBayar.setOnClickListener {
            prosesUploadPembayaran()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Pembayaran"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun tangkapDataIntent() {
        // Menerima data kiriman dari halaman list/adapter sebelumnya
        idKunjungan = intent.getStringExtra("id_kunjungan") ?: ""
        val namaAnak = intent.getStringExtra("nama_anak") ?: "Pasien"
        val diagnosa = intent.getStringExtra("diagnosa") ?: "-"
        val resep = intent.getStringExtra("resep_obat") ?: "-"
        totalBiayaRaw = intent.getDoubleExtra("total_biaya", 150000.0)
        // Set data ke komponen UI Ringkasan Medis
        binding.tvNamaPasien.text = "Pasien: $namaAnak"
        binding.tvDiagnosa.text = "Diagnosa: $diagnosa"
        binding.tvResepObat.text = "Resep: $resep"

        // Format tagihan menjadi Rupiah (contoh: Rp 150.000)
        binding.tvTotalBiaya.text = FormatHelper.formatRupiah(totalBiayaRaw)
    }

    private fun setupMetodePembayaran() {
        // Mengatur visibility form berdasarkan RadioButton yang dipilih pasien
        binding.rgMetode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbUmum -> {
                    binding.layoutUmum.visibility = View.VISIBLE
                    binding.layoutAsuransi.visibility = View.GONE
                }
                R.id.rbAsuransi -> {
                    binding.layoutUmum.visibility = View.GONE
                    binding.layoutAsuransi.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun prosesUploadPembayaran() {
        val checkedRadioId = binding.rgMetode.checkedRadioButtonId

        // 1. Validasi: Apakah sudah memilih metode?
        if (checkedRadioId == -1) {
            Toast.makeText(this, "Silakan pilih metode pembayaran!", Toast.LENGTH_SHORT).show()
            return
        }

        var metodeStr = ""
        var keteranganFinal = binding.etKeterangan.text.toString().trim()

        // 2. Validasi & Pengumpulan Data Spesifik Metode
        if (checkedRadioId == R.id.rbUmum) {
            metodeStr = "Umum"
            if (keteranganFinal.isEmpty()) {
                keteranganFinal = "Pembayaran Mandiri via Transfer VA BCA"
            }
        } else {
            metodeStr = "Asuransi"
            val namaAsuransi = binding.etNamaAsuransi.text.toString().trim()
            val noAsuransi = binding.etNoAsuransi.text.toString().trim()

            if (namaAsuransi.isEmpty() || noAsuransi.isEmpty()) {
                Toast.makeText(this, "Nama dan Nomor Kartu Asuransi wajib diisi!", Toast.LENGTH_SHORT).show()
                return
            }
            // Gabungkan info asuransi ke dalam keterangan untuk dibaca Admin
            keteranganFinal = "Provider: $namaAsuransi | No.Kartu: $noAsuransi | Ket: $keteranganFinal"
        }

        // 3. Validasi: Apakah sudah pilih foto bukti?
        if (selectedImageUri == null) {
            Toast.makeText(this, "Wajib mengunggah foto bukti pembayaran / kartu!", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. Konversi URI Gambar menjadi File Riil di Cache Android
        val fileBukti = uriToFile(selectedImageUri!!)
        if (fileBukti == null) {
            Toast.makeText(this, "Gagal memproses file gambar", Toast.LENGTH_SHORT).show()
            return
        }

        // 5. Bungkus Data Teks Menjadi RequestBody (Wajib untuk @Multipart)
        val idKunjunganBody = idKunjungan.toRequestBody("text/plain".toMediaTypeOrNull())
        val totalBiayaBody = totalBiayaRaw.toInt().toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val metodeBody = metodeStr.toRequestBody("text/plain".toMediaTypeOrNull())
        val keteranganBody = keteranganFinal.toRequestBody("text/plain".toMediaTypeOrNull())

        // 6. Bungkus File Menjadi MultipartBody.Part
        val requestFile = fileBukti.asRequestBody("image/*".toMediaTypeOrNull())
        val buktiMultipart = MultipartBody.Part.createFormData("bukti_pembayaran", fileBukti.name, requestFile)

        // 7. Animasi Loading Sederhana (Kunci Tombol)
        binding.btnProsesBayar.isEnabled = false
        binding.btnProsesBayar.text = "Mengunggah Bukti..."

        // 8. Eksekusi Jaringan Melalui Retrofit
        ApiClient.instance.bayarDenganBukti(
            idKunjunganBody, totalBiayaBody, metodeBody, keteranganBody, buktiMultipart
        ).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                binding.btnProsesBayar.isEnabled = true
                binding.btnProsesBayar.text = getString(R.string.payment_confirm)

                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {
                    Toast.makeText(this@PembayaranDetailActivity, body.message, Toast.LENGTH_LONG).show()
                    finish() // Tutup activity dan kembali ke halaman dashboard/antrean
                } else {
                    Toast.makeText(this@PembayaranDetailActivity, body?.message ?: "Gagal memproses pembayaran", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                binding.btnProsesBayar.isEnabled = true
                binding.btnProsesBayar.text = getString(R.string.payment_confirm)
                Toast.makeText(this@PembayaranDetailActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // --- HELPER FUNCTION ---
    // Mengubah URI Stream Galeri menjadi File Fisik Sementara di Direktori Cache Aplikasi
    private fun uriToFile(uri: Uri): File? {
        return try {
            val contentResolver = applicationContext.contentResolver
            val internalFile = File(applicationContext.cacheDir, "temp_bukti_${System.currentTimeMillis()}.jpg")
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val outputStream = FileOutputStream(internalFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.close()
            inputStream.close()
            internalFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}