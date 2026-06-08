package com.example.klinikanak.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.klinikanak.Dokter
import com.example.klinikanak.databinding.ItemDokterBinding

class AdminAdapter(
    private var list: List<Dokter>,
    private val onItemClick: ((Dokter) -> Unit)? = null
) : RecyclerView.Adapter<AdminAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDokterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDokterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val admin = list[position]
        holder.binding.tvNamaDokter.text = admin.namaLengkap
        holder.binding.tvSpesialisasi.text = "Administrator"
        holder.binding.tvNoSip.text = "Username: ${admin.username}"
        holder.itemView.setOnClickListener { onItemClick?.invoke(admin) }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Dokter>) {
        list = newList
        notifyDataSetChanged()
    }
}