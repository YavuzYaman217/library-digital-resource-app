package com.yyaman.libraryapp.adapter

import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yyaman.libraryapp.data.BookmarkResponse
import com.yyaman.libraryapp.databinding.ItemBookmarkBinding

class BookmarkAdapter(
    private val onDelete: (BookmarkResponse) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.VH>() {

    private var items: List<BookmarkResponse> = emptyList()

    fun submitList(newList: List<BookmarkResponse>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemBookmarkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])

    inner class VH(private val b: ItemBookmarkBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(resp: BookmarkResponse) {
            // 1) Title
            b.tvTitle.text = resp.item.title

            // 2) Subtitle: author + (year or type)
            b.tvSubtitle.text = when (resp.itemType) {
                "book"    -> "${resp.item.author} • ${resp.item.year}"
                "digital" -> "${resp.item.author} • ${resp.item.type}"
                else      -> resp.itemType
            }

            // 3) Availability (only for books)
            resp.item.available?.let {
                b.tvAvailability.text = if (it) "Available" else "Unavailable"
                b.tvAvailability.visibility = View.VISIBLE // <-- Here
            } ?: run {
                b.tvAvailability.visibility = View.GONE // <-- And here
            }

            // 4) Delete button
            b.btnDelete.setOnClickListener { onDelete(resp) }
        }
    }
}
