package com.example.klinikanak

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

// Struktur Tabel: kunjungan
data class Kunjungan(
    @SerializedName("id_kunjungan") val idKunjungan: Int,
    @SerializedName("id_pasien") val idPasien: Int,
    @SerializedName("id_dokter") val idDokter: Int,
    @SerializedName("tanggal_kunjungan") val tanggalKunjungan: String, // Format: YYYY-MM-DD
    @SerializedName("keluhan_awal") val keluhanAwal: String,
    @SerializedName("diagnosa") val diagnosa: String?,
    @SerializedName("resep_obat") val resepObat: String?,
    @SerializedName("total_biaya") val totalBiaya: Double?,
    @SerializedName("metode_pembayaran") val metodePembayaran: String?, // enum('umum', 'asuransi')
    @SerializedName("keterangan_pembayaran") val keteranganPembayaran: String?,
    @SerializedName("bukti_pembayaran") val buktiPembayaran: String?,
    @SerializedName("catatan_admin") val catatanAdmin: String?,
    @SerializedName("status_layanan") val statusLayanan: Int, // default 0
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("nama_anak") val namaAnak: String? = null,
    @SerializedName("nama_ortu") val namaOrtu: String? = null,
    @SerializedName("nama_dokter") val namaDokter: String? = null
)

data class LayananResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total_data") val totalData: Int,
    @SerializedName("data") val data: List<Kunjungan>
)

// Struktur Tabel: pasien
data class Pasien(
    @SerializedName("id_pasien") val idPasien: Int,
    @SerializedName("email") val email: String,
    @SerializedName("nama_ortu") val namaOrtu: String,
    @SerializedName("nama_anak") val namaAnak: String,
    @SerializedName("tanggal_lahir_anak") val tanggalLahirAnak: String,
    @SerializedName("jenis_kelamin_anak") val jenisKelaminAnak: String, // enum('L', 'P')
    @SerializedName("no_hp") val noHp: String
)

// Untuk fitur Kelola Dokter
data class Dokter(
    @SerializedName("id_user") val idUser: String,
    @SerializedName("username") val username: String,
    @SerializedName("nama_lengkap") val namaLengkap: String,
    @SerializedName("no_sip") val noSip: String?,
    @SerializedName("spesialisasi") val spesialisasi: String?
)

data class DokterResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total_data") val totalData: Int,
    @SerializedName("data") val data: List<Dokter>
)

data class PasienResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total_data") val totalData: Int,
    @SerializedName("data") val data: List<Pasien>
)