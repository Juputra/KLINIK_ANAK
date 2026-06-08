package com.example.klinikanak.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("role") val role: String?, // 'admin', 'dokter', atau 'pasien'
    @SerializedName("data") val data: UserData?
)

data class UserData(
    // Fields untuk User (Admin/Dokter)
    @SerializedName("id_user") val idUser: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("nama_lengkap") val namaLengkap: String?,
    @SerializedName("no_sip") val noSip: String?,
    @SerializedName("spesialisasi") val spesialisasi: String?,

    // Fields untuk Pasien
    @SerializedName("id_pasien") val idPasien: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("nama_ortu") val namaOrtu: String?,
    @SerializedName("nama_anak") val namaAnak: String?,
    @SerializedName("tanggal_lahir_anak") val tanggalLahirAnak: String?,
    @SerializedName("jenis_kelamin_anak") val jenisKelaminAnak: String?,
    @SerializedName("no_hp") val noHp: String?
)
