package com.example.klinikanak.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.klinikanak.Kunjungan
import com.example.klinikanak.R
import com.example.klinikanak.databinding.ItemRiwayatBinding

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

        holder.binding.tvTitle.text = (k.namaAnak ?: "Pasien") + "  •  " + k.tanggalKunjungan

        val biayaStr = k.totalBiaya?.let { "Rp ${it.toLong()}" } ?: "Rp 150000"
        holder.binding.tvSubtitle1.text = "Tagihan: $biayaStr"
        holder.binding.tvSubtitle2.text = "Metode: " + (k.metodePembayaran ?: "Menunggu Pasien")

        val lunas = k.statusLayanan >= 4

        if (lunas) {
            holder.binding.tvStatus.text = "Lunas"
            holder.binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_green)
            holder.itemView.setOnClickListener(null)
        } else {
            holder.binding.tvStatus.text = "Belum Bayar"
            holder.binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_orange)
            holder.itemView.setOnClickListener { onItemClick(k) }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Kunjungan>) {
        list = newList
        notifyDataSetChanged()
    }
}