package com.example.klinikanak.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // PENTING: Ganti dengan link Ngrok kamu yang sedang aktif.
    // Wajib diakhiri dengan tanda garis miring (slash /) dan menunjuk ke folder api di XAMPP.
    // PASTIKAN TIDAK ADA SPASI di depan URL!
    private const val BASE_URL ="https://nape-charm-freight.ngrok-free.dev/api/"

    val instance: ApiService by lazy {
        // OkHttpClient standar tanpa logging interceptor untuk menghindari error build
        val client = OkHttpClient.Builder()
            .build()

        val gson = GsonBuilder()
            .setLenient() // Tambahkan ini agar Retrofit lebih toleran terhadap karakter aneh
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson)) // Gunakan gson yang lenient
            .client(client)
            .build()

        retrofit.create(ApiService::class.java)
    }
}
