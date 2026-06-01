package com.example.klinikanak.api

import com.example.klinikanak.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    // Endpoint untuk Login (Akan memanggil login.php)
    @FormUrlEncoded
    @POST("login.php")
    fun userLogin(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    // Endpoint untuk Register Pasien (Contoh)
    /*
    @FormUrlEncoded
    @POST("register_pasien.php")
    fun registerPasien(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("nama_ortu") namaOrtu: String,
        @Field("nama_anak") namaAnak: String,
        @Field("tanggal_lahir_anak") tglLahir: String,
        @Field("jenis_kelamin_anak") jk: String,
        @Field("no_hp") noHp: String
    ): Call<GeneralResponse> // Kamu butuh membuat Data Class GeneralResponse nanti
    */

    // Kamu bisa terus menambahkan fungsi lain di bawah sini (seperti getLayanan, updateStatus)
    // seiring berjalannya proyek.
}