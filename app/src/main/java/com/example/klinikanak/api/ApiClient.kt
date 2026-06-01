package com.example.klinikanak.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // PENTING: Ganti dengan link Ngrok kamu yang sedang aktif.
    // Wajib diakhiri dengan tanda garis miring (slash /) dan menunjuk ke folder api di XAMPP.
    private const val BASE_URL = " https://nape-charm-freight.ngrok-free.dev/api/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Pengubah JSON otomatis
            .build()

        retrofit.create(ApiService::class.java)
    }
}
