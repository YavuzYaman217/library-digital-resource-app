// app/src/main/java/com/yyaman/libraryapp/search/SearchFragment.kt

package com.yyaman.libraryapp.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.yyaman.libraryapp.R
import com.yyaman.libraryapp.adapter.BookAdapter
import com.yyaman.libraryapp.data.Book
import com.yyaman.libraryapp.databinding.FragmentSearchBinding
import com.yyaman.libraryapp.ui.BookState
import com.yyaman.libraryapp.ui.BookViewModel
import com.yyaman.libraryapp.ui.BookmarkViewModel

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val bookVm:    BookViewModel     by viewModels()
    private val bookmarkVm: BookmarkViewModel by viewModels()

    private lateinit var adapter: BookAdapter
    private var lastQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentSearchBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 1) Create adapter passing two lambdas that each take a Book
        adapter = BookAdapter(
            onReserve  = { book: Book ->
                // Pass the entire Book object to the reserve function
                bookVm.reserve(book) { ok, msg -> // Change book.id to book
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    if (ok) bookVm.search(lastQuery)
                }
            },
            onBookmark = { book: Book ->
                bookmarkVm.add("book", book.id) { resp ->
                    val text = if (resp != null)
                        getString(R.string.bookmark_added)
                    else
                        getString(R.string.error_occurred)
                    Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResults.adapter = adapter

        // 2) Observe search state
        bookVm.state.observe(viewLifecycleOwner) { st ->
            when (st) {
                is BookState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is BookState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.submitList(st.books)
                }
                is BookState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, st.error, Snackbar.LENGTH_LONG).show()
                }
                else -> {
                    // Handle any other unhandled states, e.g., BookState.Idle
                    binding.progressBar.visibility = View.GONE
                    // Optionally clear the list or take other actions
                }
            }
        }

        // 3) Wire search button
        binding.btnSearch.setOnClickListener {
            val q = binding.etSearch.text.toString().trim()
            if (q.isBlank()) {
                Toast.makeText(requireContext(),
                    getString(R.string.enter_search_term),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                lastQuery = q
                bookVm.search(q)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
