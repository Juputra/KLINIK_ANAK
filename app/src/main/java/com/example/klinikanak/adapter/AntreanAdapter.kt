package com.example.klinikanak.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.klinikanak.Kunjungan
import com.example.klinikanak.R
import com.example.klinikanak.databinding.ItemRiwayatBinding

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

        // CLEAN CODE: Memanggil ID yang benar
        holder.binding.tvTitle.text = antrean.namaAnak ?: "Pasien #${antrean.idPasien}"
        holder.binding.tvSubtitle1.text = "Keluhan: ${antrean.keluhanAwal}"
        holder.binding.tvSubtitle2.text = "Tanggal: ${antrean.tanggalKunjungan}"

        val statusText = when(antrean.statusLayanan) {
            0 -> "Menunggu Konfirmasi"
            1 -> "Antrean Dokter"
            2 -> "Sedang Diperiksa"
            3 -> "Menunggu Pembayaran"
            4 -> "Selesai / Lunas"
            else -> "Selesai"
        }
        holder.binding.tvStatus.text = statusText

        val bgRes = when(antrean.statusLayanan) {
            0 -> R.drawable.bg_badge_orange
            1 -> R.drawable.bg_badge_blue
            2 -> R.drawable.bg_badge_blue
            3 -> R.drawable.bg_badge_orange
            4 -> R.drawable.bg_badge_green
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