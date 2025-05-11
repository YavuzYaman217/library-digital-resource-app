package com.yyaman.libraryapp.detail

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yyaman.libraryapp.R
import com.yyaman.libraryapp.data.Book
import com.yyaman.libraryapp.data.DummyStore
import com.yyaman.libraryapp.databinding.ActivityBookDetailBinding
import com.yyaman.libraryapp.main.MainActivity

class BookDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookDetailBinding
    private lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Read all the fields, including availability as a Boolean
        val id = intent.getIntExtra("id", -1)
        val title = intent.getStringExtra("title") ?: ""
        val author = intent.getStringExtra("author") ?: ""
        val year = intent.getIntExtra("year", 0)
        val available = intent.getBooleanExtra("available", true)

        // 2) Construct your data.Book with a Boolean, not a String
        book = Book(id, title, author, year, available)

        // 3) Populate the UI
        binding.tvBookTitle.text = title
        binding.tvAuthor.text = author
        binding.ivBookCover.setImageResource(R.drawable.ic_book_placeholder)

        // Bookmark button (unchanged)
        binding.btnBookmark.setOnClickListener {
            DummyStore.bookmarks.add(book)
            Toast.makeText(this, "Bookmarked!", Toast.LENGTH_SHORT).show()
        }

        // Reserve button: disable if not available
        binding.btnReserve.apply {
            isEnabled = book.available
            text = if (book.available)
                getString(R.string.reserve)
            else
                getString(R.string.unavailable)

            setOnClickListener {
                DummyStore.reservations.add(book)
                Toast.makeText(this@BookDetailActivity, "Reserved!", Toast.LENGTH_SHORT).show()
                // Jump back to main + Reservations tab
                startActivity(
                    Intent(this@BookDetailActivity, MainActivity::class.java)
                        .putExtra("dest", "reservations")
                )
                finish()
            }
        }
    }
}
