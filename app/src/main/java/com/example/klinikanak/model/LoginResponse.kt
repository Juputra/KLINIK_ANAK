package com.example.klinikanak.model
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("role") val role: String?,
    @SerializedName("data") val data: UserData?
)

data class UserData(
    @SerializedName("id") val id: Int,
    @SerializedName("nama") val nama: String,
    @SerializedName("nama_anak") val namaAnak: String? // Hanya ada jika dia pasien
)