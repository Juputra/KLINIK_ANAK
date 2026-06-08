package com.example.klinikanak.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.klinikanak.Pasien
import com.example.klinikanak.databinding.ItemDokterBinding

class PasienAdapter(
    private var listPasien: List<Pasien>,
    private val onItemClick: (Pasien) -> Unit
) : RecyclerView.Adapter<PasienAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDokterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDokterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pasien = listPasien[position]
        holder.binding.tvNamaDokter.text = pasien.namaAnak
        holder.binding.tvSpesialisasi.text = "Orang Tua: ${pasien.namaOrtu}"
        holder.binding.tvNoSip.text = "Kontak: ${pasien.noHp} | ${pasien.email}"

        holder.itemView.setOnClickListener { onItemClick(pasien) }
    }

    override fun getItemCount(): Int = listPasien.size

    fun updateData(newList: List<Pasien>) {
        listPasien = newList
        notifyDataSetChanged()
    }
}