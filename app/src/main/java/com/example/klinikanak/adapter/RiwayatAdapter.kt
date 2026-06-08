package com.example.klinikanak.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.klinikanak.Kunjungan
import com.example.klinikanak.R
import com.example.klinikanak.databinding.ItemRiwayatBinding
import com.example.klinikanak.utils.FormatHelper

class RiwayatAdapter(
    private var listRiwayat: List<Kunjungan>,
    private val onItemClick: ((Kunjungan) -> Unit)? = null // Tambahan parameter aksi klik
) : RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRiwayatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val riwayat = listRiwayat[position]

        val tanggalFormatted = FormatHelper.formatTanggal(riwayat.tanggalKunjungan)
        holder.binding.tvTitle.text = (riwayat.namaAnak ?: "Pasien") + "  •  " + tanggalFormatted

        holder.binding.tvSubtitle1.text = "Diagnosa: " + (riwayat.diagnosa ?: "-")
        holder.binding.tvSubtitle2.text = "Resep: " + (riwayat.resepObat ?: "-")

        // LOGIKA PERBAIKAN: Gunakan 'when' agar status dinamis sesuai data
        val statusText = when (riwayat.statusLayanan) {
            6 -> "Selesai"
            99 -> "Dibatalkan"
            else -> "Status: ${riwayat.statusLayanan}" // Untuk jaga-jaga kalau ada status lain
        }

        val backgroundResource = when (riwayat.statusLayanan) {
            6 -> R.drawable.bg_badge_green
            99 -> R.drawable.bg_badge_red
            else -> R.drawable.bg_badge_orange
        }

        holder.binding.tvStatus.text = statusText
        holder.binding.tvStatus.setBackgroundResource(backgroundResource)

        // Terapkan aksi klik
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(riwayat)
        }
    }

    override fun getItemCount(): Int = listRiwayat.size

    fun updateData(newList: List<Kunjungan>) {
        listRiwayat = newList
        notifyDataSetChanged()
    }
}