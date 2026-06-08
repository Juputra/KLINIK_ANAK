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
import com.example.klinikanak.R
import com.example.klinikanak.adapter.AdminAdapter
import com.example.klinikanak.api.ApiClient
import com.example.klinikanak.databinding.ActivityKelolaAdminBinding
import com.example.klinikanak.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KelolaAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKelolaAdminBinding
    private lateinit var adapter: AdminAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKelolaAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupToolbar()
        adapter = AdminAdapter(emptyList()) { admin -> tampilkanOpsi(admin) }
        binding.rvAdmin.layoutManager = LinearLayoutManager(this)
        binding.rvAdmin.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { fetchData() }
        binding.btnTambahAdmin.setOnClickListener { tampilkanForm(null) }
        fetchData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Kelola Admin"
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun fetchData() {
        binding.swipeRefresh.isRefreshing = true
        ApiClient.instance.getAdmin().enqueue(object : Callback<DokterResponse> {
            override fun onResponse(call: Call<DokterResponse>, response: Response<DokterResponse>) {
                binding.swipeRefresh.isRefreshing = false
                if (response.isSuccessful && response.body()?.status == "success") {
                    adapter.updateData(response.body()!!.data)
                    binding.tvEmpty.isVisible = adapter.itemCount == 0
                } else {
                    Toast.makeText(this@KelolaAdminActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<DokterResponse>, t: Throwable) {
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@KelolaAdminActivity, "Koneksi gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun tampilkanOpsi(admin: Dokter) {
        AlertDialog.Builder(this)
            .setTitle(admin.namaLengkap)
            .setItems(arrayOf("Edit Data", "Hapus")) { _, which ->
                if (which == 0) tampilkanForm(admin) else konfirmasiHapus(admin)
            }
            .show()
    }

    private fun tampilkanForm(admin: Dokter?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_tambah_admin, null)
        val etNama = view.findViewById<EditText>(R.id.etNamaAdmin)
        val etUsername = view.findViewById<EditText>(R.id.etUsernameAdmin)
        val etPassword = view.findViewById<EditText>(R.id.etPasswordAdmin)

        if (admin != null) {
            etNama.setText(admin.namaLengkap)
            etUsername.setText(admin.username)
            etPassword.hint = "Password (kosongkan jika tidak diubah)"
        }

        AlertDialog.Builder(this)
            .setTitle(if (admin == null) "Tambah Admin" else "Edit Admin")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString().trim()
                val username = etUsername.text.toString().trim()
                val password = etPassword.text.toString().trim()
                if (nama.isEmpty() || username.isEmpty()) {
                    Toast.makeText(this, "Nama dan username wajib diisi", Toast.LENGTH_SHORT).show()
                } else if (admin == null) {
                    if (password.isEmpty()) {
                        Toast.makeText(this, "Password wajib diisi", Toast.LENGTH_SHORT).show()
                    } else {
                        ApiClient.instance.tambahAdmin(username, password, nama).enqueue(cb())
                    }
                } else {
                    ApiClient.instance.updateAdmin(admin.idUser, username, password, nama).enqueue(cb())
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun konfirmasiHapus(admin: Dokter) {
        // Celah 1: Admin hapus diri sendiri
        if (admin.idUser == sessionManager.getUserId().toString()) {
            Toast.makeText(this, "Tidak bisa menghapus akun yang sedang Anda gunakan", Toast.LENGTH_SHORT).show()
            return
        }

        // Celah 2: Admin menghapus admin terakhir
        if (adapter.itemCount <= 1) {
            Toast.makeText(this, "Sistem ditolak: Harus tersisa minimal 1 Admin di klinik!", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Hapus Admin")
            .setMessage("Yakin menghapus ${admin.namaLengkap}?")
            .setPositiveButton("Hapus") { _, _ ->
                ApiClient.instance.hapusUser(admin.idUser).enqueue(cb())
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun cb() = object : Callback<ApiResponse> {
        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
            val body = response.body()
            if (response.isSuccessful && body?.status == "success") {
                Toast.makeText(this@KelolaAdminActivity, body.message, Toast.LENGTH_SHORT).show()
                fetchData()
            } else {
                Toast.makeText(this@KelolaAdminActivity, body?.message ?: "Gagal", Toast.LENGTH_SHORT).show()
            }
        }
        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
            Toast.makeText(this@KelolaAdminActivity, "Koneksi gagal", Toast.LENGTH_SHORT).show()
        }
    }
}