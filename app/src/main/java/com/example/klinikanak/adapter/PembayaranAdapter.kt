package com.example.klinikanak.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.klinikanak.Kunjungan
import com.example.klinikanak.R
import com.example.klinikanak.databinding.ItemRiwayatBinding

class PembayaranAdapter(
    private var listPembayaran: List<Kunjungan>,
    private val onItemClick: (Kunjungan) -> Unit
) : RecyclerView.Adapter<PembayaranAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRiwayatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kunjungan = listPembayaran[position]

        // CLEAN CODE & BONUS FIX
        holder.binding.tvTitle.text = kunjungan.namaAnak ?: "Pasien"
        holder.binding.tvSubtitle1.text = "Diagnosa: " + (kunjungan.diagnosa ?: "Selesai")
        holder.binding.tvSubtitle2.text = "Obat diresepkan: " + (kunjungan.resepObat ?: "Tanpa obat")

        holder.binding.tvStatus.text = "Klik Untuk Bayar"
        holder.binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_orange)

        holder.itemView.setOnClickListener {
            onItemClick(kunjungan)
        }
    }

    override fun getItemCount(): Int = listPembayaran.size

    fun updateData(newList: List<Kunjungan>) {
        listPembayaran = newList
        notifyDataSetChanged()
    }
}