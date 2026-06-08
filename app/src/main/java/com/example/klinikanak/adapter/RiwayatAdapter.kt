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

        holder.binding.tvStatus.text = "Selesai"
        holder.binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_green)
        holder.binding.tvStatus.text = "Dibatalkan"
        holder.binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_red)

        // Terapkan aksi klik ke seluruh area card
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