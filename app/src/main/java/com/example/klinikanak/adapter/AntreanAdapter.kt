package com.example.klinikanak.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.klinikanak.Kunjungan
import com.example.klinikanak.R
import com.example.klinikanak.databinding.ItemRiwayatBinding
import com.example.klinikanak.utils.FormatHelper

class AntreanAdapter(
    private var listAntrean: List<Kunjungan>,
    private val onItemClick: ((Kunjungan) -> Unit)? = null
) : RecyclerView.Adapter<AntreanAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRiwayatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val antrean = listAntrean[position]

        holder.binding.tvTitle.text   = antrean.namaAnak ?: "Pasien #${antrean.idPasien}"
        holder.binding.tvSubtitle1.text = "Keluhan: ${antrean.keluhanAwal}"

        // ✅ FIX 8: Format tanggal "2025-06-08" → "08 Jun 2025"
        holder.binding.tvSubtitle2.text = "Tanggal: ${FormatHelper.formatTanggal(antrean.tanggalKunjungan)}"

        val statusText = when (antrean.statusLayanan) {
            0 -> "Menunggu Konfirmasi"
            1 -> {
                // Jika nomor antrean ada (tidak null) dan lebih besar dari 0
                if (antrean.nomorAntrean != null && antrean.nomorAntrean > 0) {
                    "Antrean Ke-${antrean.nomorAntrean}"
                } else {
                    "Antrean Dokter"
                }
            }
            2 -> "Sedang Diperiksa"
            3 -> "Menunggu Set Harga"
            4 -> "Menunggu Pembayaran"
            5 -> "Verifikasi Pembayaran"
            6 -> "Selesai / Lunas"
            99 -> "Dibatalkan"
            else -> "Selesai"
        }
        holder.binding.tvStatus.text = statusText

        val bgRes = when (antrean.statusLayanan) {
            0, 3, 4 -> R.drawable.bg_badge_orange // Kuning/Orange untuk yg "Menunggu"
            1, 2, 5 -> R.drawable.bg_badge_blue   // Biru untuk yang sedang "Aktif diproses"
            6 -> R.drawable.bg_badge_green        // Hijau untuk Lunas
            99 -> R.drawable.bg_badge_red
            else -> R.drawable.bg_badge_green
        }
        holder.binding.tvStatus.setBackgroundResource(bgRes)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(antrean)
        }
    }

    override fun getItemCount(): Int = listAntrean.size

    fun updateData(newList: List<Kunjungan>) {
        listAntrean = newList
        notifyDataSetChanged()
    }
}