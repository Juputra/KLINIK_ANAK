package com.example.klinikanak.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.klinikanak.AppConstants
import com.example.klinikanak.Kunjungan
import com.example.klinikanak.R
import com.example.klinikanak.databinding.ItemRiwayatBinding
import com.example.klinikanak.utils.FormatHelper

class VerifikasiAdapter(
    private var list: List<Kunjungan>,
    private val onItemClick: (Kunjungan) -> Unit
) : RecyclerView.Adapter<VerifikasiAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRiwayatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val k = list[position]

        // ✅ FIX 8: Format tanggal "2025-06-08" → "08 Jun 2025"
        val tanggalFormatted = FormatHelper.formatTanggal(k.tanggalKunjungan)
        holder.binding.tvTitle.text = (k.namaAnak ?: "Pasien") + "  •  " + tanggalFormatted

        // ✅ FIX 8: Format rupiah "Rp 150000" → "Rp 150.000"
        // Gunakan total_biaya dari server jika ada, fallback ke AppConstants
        val biayaFormatted = if (k.totalBiaya != null && k.totalBiaya > 0) {
            FormatHelper.formatRupiah(k.totalBiaya)
        } else {
            FormatHelper.formatRupiah(AppConstants.BIAYA_KONSULTASI)
        }
        holder.binding.tvSubtitle1.text = "Tagihan: $biayaFormatted"
        holder.binding.tvSubtitle2.text = "Metode: " + (k.metodePembayaran ?: "Menunggu Pasien")

        val lunas = k.statusLayanan == 6

        if (lunas) {
            holder.binding.tvStatus.text = "Lunas"
            holder.binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_green)
        } else if (k.statusLayanan == 3) {
            holder.binding.tvStatus.text = "Set Tagihan"
            holder.binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_orange)
        } else if (k.statusLayanan == 5) {
            holder.binding.tvStatus.text = "Cek Bukti"
            holder.binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_blue)
        }
        holder.itemView.setOnClickListener { onItemClick(k) }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Kunjungan>) {
        list = newList
        notifyDataSetChanged()
    }
}