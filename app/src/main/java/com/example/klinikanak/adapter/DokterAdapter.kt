package com.example.klinikanak.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.klinikanak.Dokter
import com.example.klinikanak.databinding.ItemDokterBinding

class DokterAdapter(
    private var listDokter: List<Dokter>,
    private val onItemClick: ((Dokter) -> Unit)? = null
) : RecyclerView.Adapter<DokterAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDokterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDokterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dokter = listDokter[position]
        holder.binding.tvNamaDokter.text = dokter.namaLengkap
        holder.binding.tvSpesialisasi.text = dokter.spesialisasi ?: "-"
        holder.binding.tvNoSip.text = "SIP: ${dokter.noSip ?: "-"}"
        holder.itemView.setOnClickListener { onItemClick?.invoke(dokter) }
    }

    override fun getItemCount(): Int = listDokter.size

    fun updateData(newList: List<Dokter>) {
        listDokter = newList
        notifyDataSetChanged()
    }
}