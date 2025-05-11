package com.yyaman.libraryapp.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.yyaman.libraryapp.R
import com.yyaman.libraryapp.adapter.BookmarkAdapter
import com.yyaman.libraryapp.databinding.FragmentBookmarksBinding
import com.yyaman.libraryapp.ui.BookmarkState
import com.yyaman.libraryapp.ui.BookmarkViewModel

class BookmarksFragment : Fragment() {
    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!
    private val vm: BookmarkViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentBookmarksBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 1) Adapter & RecyclerView
        val adapter = BookmarkAdapter { bm ->
            vm.delete(bm.id) { success ->
                val msg = if (success)
                    getString(R.string.bookmark_deleted)
                else
                    getString(R.string.error_occurred)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                // refresh list
                vm.loadAll()
            }
        }
        binding.rvBookmarks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        // 2) Observe state
        vm.state.observe(viewLifecycleOwner) { st ->
            when (st) {
                is BookmarkState.Loading -> {
                    binding.rvBookmarks.visibility = View.GONE
                    binding.tvEmpty.visibility     = View.GONE
                }
                is BookmarkState.Success -> {
                    if (st.items.isEmpty()) {
                        binding.rvBookmarks.visibility = View.GONE
                        binding.tvEmpty.visibility     = View.VISIBLE
                    } else {
                        binding.tvEmpty.visibility     = View.GONE
                        binding.rvBookmarks.visibility = View.VISIBLE
                        adapter.submitList(st.items)
                    }
                }
                is BookmarkState.Error -> {
                    binding.rvBookmarks.visibility = View.GONE
                    binding.tvEmpty.visibility     = View.VISIBLE
                    Toast.makeText(requireContext(), st.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // 3) Load your bookmarks
        vm.loadAll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
