package com.yyaman.libraryapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yyaman.libraryapp.R
import com.yyaman.libraryapp.data.DigitalResource
import com.yyaman.libraryapp.databinding.ItemDigitalResourceBinding

/**
 * Simple adapter: tapping the card calls onClick(resource).
 */
class DigitalAdapter(
    private val items: List<DigitalResource>,
    private val onClick: (DigitalResource) -> Unit
) : RecyclerView.Adapter<DigitalAdapter.VH>() {

    inner class VH(val binding: ItemDigitalResourceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(res: DigitalResource) {
            binding.tvTitle.text = res.title
            binding.tvAuthor.text = res.author
            binding.ivThumb.setImageResource(R.drawable.ic_resource_placeholder)
            binding.root.setOnClickListener { onClick(res) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemDigitalResourceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
