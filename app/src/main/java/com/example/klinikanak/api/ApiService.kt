package com.example.klinikanak.api

import com.example.klinikanak.ApiResponse
import com.example.klinikanak.LayananResponse
import com.example.klinikanak.model.LoginResponse
import com.example.klinikanak.DokterResponse
import com.example.klinikanak.PasienResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded
    @POST("login.php")
    fun userLogin(
        @Field("username") email: String,
        @Field("password") pass: String
    ): Call<LoginResponse>


    @FormUrlEncoded
    @POST("buat_janji.php")
    fun buatJanji(
        @Field("id_pasien") idPasien: String,
        @Field("id_dokter") idDokter: String,
        @Field("tanggal_kunjungan") tanggal: String,
        @Field("keluhan_awal") keluhan: String
    ): Call<ApiResponse>

    @GET("get_layanan.php")
    fun getLayanan(
        @Query("role") role: String,
        @Query("id_user") idUser: String?,
        @Query("status") status: String?
    ): Call<LayananResponse>

    @FormUrlEncoded
    @POST("update_status.php")
    fun updateStatus(
        @Field("id_kunjungan") idKunjungan: String,
        @Field("status_baru") statusBaru: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("simpan_pemeriksaan.php")
    fun simpanPemeriksaan(
        @Field("id_kunjungan") idKunjungan: String,
        @Field("diagnosa") diagnosa: String,
        @Field("resep_obat") resepObat: String
    ): Call<ApiResponse>

    @GET("get_dokter.php")
    fun getDokter(): Call<DokterResponse>

    @FormUrlEncoded
    @POST("tambah_dokter.php")
    fun tambahDokter(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("nama_lengkap") namaLengkap: String,
        @Field("no_sip") noSip: String,
        @Field("spesialisasi") spesialisasi: String
    ): Call<ApiResponse>

    @GET("get_admin.php")
    fun getAdmin(): Call<DokterResponse>

    @FormUrlEncoded
    @POST("tambah_admin.php")
    fun tambahAdmin(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("nama_lengkap") namaLengkap: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("bayar.php")
    fun bayar(
        @Field("id_kunjungan") idKunjungan: String,
        @Field("total_biaya") totalBiaya: String,
        @Field("metode_pembayaran") metode: String,
        @Field("keterangan_pembayaran") keterangan: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("update_dokter.php")
    fun updateDokter(
        @Field("id_user") idUser: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("nama_lengkap") namaLengkap: String,
        @Field("no_sip") noSip: String,
        @Field("spesialisasi") spesialisasi: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("update_admin.php")
    fun updateAdmin(
        @Field("id_user") idUser: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("nama_lengkap") namaLengkap: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("hapus_user.php")
    fun hapusUser(@Field("id_user") idUser: String): Call<ApiResponse>

    @GET("get_pasien.php")
    fun getPasien(): Call<PasienResponse>

    @FormUrlEncoded
    @POST("register_pasien.php")
    fun registerPasien(
        @Field("email") email: String,
        @Field("password") pass: String,
        @Field("nama_ortu") ortu: String,
        @Field("nama_anak") anak: String,
        @Field("tanggal_lahir_anak") tglLahir: String,
        @Field("jenis_kelamin_anak") jk: String,
        @Field("no_hp") hp: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("update_pasien.php")
    fun updatePasien(
        @Field("id_pasien") idPasien: String,
        @Field("email") email: String,
        @Field("password") pass: String,
        @Field("nama_ortu") ortu: String,
        @Field("nama_anak") anak: String,
        @Field("tanggal_lahir_anak") tglLahir: String,
        @Field("jenis_kelamin_anak") jk: String,
        @Field("no_hp") hp: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("hapus_pasien.php")
    fun hapusPasien(@Field("id_pasien") idPasien: String): Call<ApiResponse>

    @FormUrlEncoded
    @POST("hapus_kunjungan.php")
    fun hapusKunjungan(
        @Field("id_kunjungan") idKunjungan: String
    ): Call<ApiResponse>
    @GET("get_profil.php")
    fun getProfil(
        @Query("role") role: String,
        @Query("id_user") idUser: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("update_profil.php")
    fun updateProfil(
        @Field("role") role: String,
        @Field("id_user") idUser: String,
        @Field("password") password: String,
        @Field("password_lama") passwordLama: String,
        @Field("nama") nama: String,          // Untuk Dokter
        @Field("no_sip") noSip: String,       // Untuk Dokter
        @Field("spesialisasi") spesialisasi: String, // Untuk Dokter
        @Field("nama_ortu") namaOrtu: String, // Untuk Pasien
        @Field("nama_anak") namaAnak: String, // Untuk Pasien
        @Field("no_hp") noHp: String          // Untuk Pasien
    ): Call<ApiResponse>
}

