package com.example.klinikanak.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.klinikanak.Kunjungan
import com.example.klinikanak.R
import com.example.klinikanak.databinding.ItemRiwayatBinding
import com.example.klinikanak.utils.FormatHelper

class RiwayatAdapter(private var listRiwayat: List<Kunjungan>) :
    RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRiwayatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val riwayat = listRiwayat[position]

        // ✅ FIX 8: Format tanggal "2025-06-08" → "08 Jun 2025"
        val tanggalFormatted = FormatHelper.formatTanggal(riwayat.tanggalKunjungan)
        holder.binding.tvTitle.text = (riwayat.namaAnak ?: "Pasien") + "  •  " + tanggalFormatted

        holder.binding.tvSubtitle1.text = "Diagnosa: " + (riwayat.diagnosa ?: "-")
        holder.binding.tvSubtitle2.text = "Resep: " + (riwayat.resepObat ?: "-")

        holder.binding.tvStatus.text = "Selesai"
        holder.binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_green)
    }

    override fun getItemCount(): Int = listRiwayat.size

    fun updateData(newList: List<Kunjungan>) {
        listRiwayat = newList
        notifyDataSetChanged()
    }
}