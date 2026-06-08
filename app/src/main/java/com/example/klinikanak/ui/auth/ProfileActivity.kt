package com.example.klinikanak.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.klinikanak.ApiResponse
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityProfileBinding
import com.example.klinikanak.model.LoginResponse
import com.example.klinikanak.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()
        aturTampilanBerdasarkanRole()
        loadDataProfil()

        binding.btnSimpanProfil.setOnClickListener { simpanPerubahan() }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Profil"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun aturTampilanBerdasarkanRole() {
        val role = sessionManager.getRole()
        if (role == "pasien") {
            binding.etNamaUtama.hint = "Nama Orang Tua"
            binding.layoutNamaAnak.visibility = View.VISIBLE
            binding.layoutNoHp.visibility = View.VISIBLE
        } else if (role == "dokter") {
            binding.etNamaUtama.hint = "Nama Lengkap Dokter"
            binding.layoutSpesialisasi.visibility = View.VISIBLE
            binding.layoutSip.visibility = View.VISIBLE
        }
    }

    private fun loadDataProfil() {
        val role = sessionManager.getRole() ?: return
        val idUser = sessionManager.getUserId().toString()

        ApiClient.instance.getProfil(role, idUser).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data

                    if (role == "pasien") {
                        binding.etNamaUtama.setText(data?.namaOrtu)
                        binding.etNamaAnak.setText(data?.namaAnak)
                        binding.etNoHp.setText(data?.noHp)
                    } else if (role == "dokter") {
                        binding.etNamaUtama.setText(data?.namaLengkap)
                        binding.etSpesialisasi.setText(data?.spesialisasi)
                        binding.etSip.setText(data?.noSip)
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun simpanPerubahan() {
        val role = sessionManager.getRole() ?: return
        val idUser = sessionManager.getUserId().toString()

        // Ambil data password
        val passwordLama = binding.etPasswordLama.text.toString().trim()
        val passwordBaru = binding.etPasswordBaru.text.toString().trim()

        val namaUtama = binding.etNamaUtama.text.toString().trim()
        var namaAnak = ""
        var noHp = ""
        var spesialisasi = ""
        var noSip = ""

        // Validasi aturan ganti password
        if (passwordBaru.isNotEmpty() && passwordLama.isEmpty()) {
            Toast.makeText(this, "Harap masukkan password lama Anda terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (role == "pasien") {
            namaAnak = binding.etNamaAnak.text.toString().trim()
            noHp = binding.etNoHp.text.toString().trim()
            if (namaUtama.isEmpty() || namaAnak.isEmpty() || noHp.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom nama dan HP", Toast.LENGTH_SHORT).show()
                return
            }
        } else if (role == "dokter") {
            spesialisasi = binding.etSpesialisasi.text.toString().trim()
            noSip = binding.etSip.text.toString().trim()
            if (namaUtama.isEmpty()) {
                Toast.makeText(this, "Nama dokter wajib diisi", Toast.LENGTH_SHORT).show()
                return
            }
        }

        binding.btnSimpanProfil.isEnabled = false
        binding.btnSimpanProfil.text = "Menyimpan..."

        ApiClient.instance.updateProfil(
            role = role,
            idUser = idUser,
            passwordLama = passwordLama, // Kirim password lama
            password = passwordBaru,     // Kirim password baru
            nama = namaUtama, noSip = noSip, spesialisasi = spesialisasi,
            namaOrtu = namaUtama, namaAnak = namaAnak, noHp = noHp
        ).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                binding.btnSimpanProfil.isEnabled = true
                binding.btnSimpanProfil.text = "Simpan Perubahan"

                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@ProfileActivity, "Profil berhasil diupdate!", Toast.LENGTH_SHORT).show()

                    val namaHeader = if (role == "pasien") namaAnak else namaUtama
                    sessionManager.saveSession(idUser.toInt(), namaHeader, role)

                    finish()
                } else {
                    // Menampilkan pesan error dari PHP (Misal: "Password lama salah!")
                    val pesanError = response.body()?.message ?: "Gagal update profil"
                    Toast.makeText(this@ProfileActivity, pesanError, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                binding.btnSimpanProfil.isEnabled = true
                binding.btnSimpanProfil.text = "Simpan Perubahan"
                Toast.makeText(this@ProfileActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}