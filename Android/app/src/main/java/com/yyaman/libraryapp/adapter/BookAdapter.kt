// app/src/main/java/com/yyaman/libraryapp/adapter/BookAdapter.kt

package com.yyaman.libraryapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yyaman.libraryapp.R
import com.yyaman.libraryapp.data.Book
import com.yyaman.libraryapp.databinding.ItemBookBinding

/**
 * Displays a list of books; wires the Reserve and Bookmark buttons to [onReserve] and [onBookmark].
 */
class BookAdapter(
    private val onReserve:  (Book) -> Unit,
    private val onBookmark: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private var items: List<Book> = emptyList()

    /** Update the list of books and refresh */
    fun submitList(newList: List<Book>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class BookViewHolder(
        private val binding: ItemBookBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.tvTitle.text  = book.title
            binding.tvAuthor.text = book.author

            // Reserve button
            binding.btnReserve.apply {
                if (book.available) {
                    text = context.getString(R.string.reserve)
                    isEnabled = true
                    setOnClickListener { onReserve(book) }
                } else {
                    text = context.getString(R.string.unavailable)
                    isEnabled = false
                    setOnClickListener(null)
                }
            }

            // Bookmark button
            binding.btnBookmark.apply {
                text = context.getString(R.string.bookmark)
                isEnabled = true
                setOnClickListener { onBookmark(book) }
            }
        }
    }
}
