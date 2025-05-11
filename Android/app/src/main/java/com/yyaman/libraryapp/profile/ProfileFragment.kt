package com.yyaman.libraryapp.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yyaman.libraryapp.adapter.ReservationAdapter
import com.yyaman.libraryapp.adapter.ReservationAdapter.ReservationItem
import com.yyaman.libraryapp.data.AuthRepository
import com.yyaman.libraryapp.databinding.FragmentProfileBinding
import com.yyaman.libraryapp.main.EditProfileActivity
import com.yyaman.libraryapp.reservations.ReservationState
import com.yyaman.libraryapp.reservations.ReservationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // for fetching real user info
    private val authRepo by lazy { AuthRepository(requireContext()) }

    // reuse your existing Reservations VM
    private val vm: ReservationViewModel by viewModels()
    private lateinit var adapter: ReservationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Load header
        loadProfileHeader()

        // 2) Edit‐profile button
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        // 3) Setup reservations list (no-op cancel + hide button)
        adapter = ReservationAdapter(onCancel = { /*nothing*/ }, showCancel = false)
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        // 4) Observe reservation state
        vm.state.observe(viewLifecycleOwner) { st ->
            binding.progressBar.visibility =
                if (st is ReservationState.Loading) View.VISIBLE else View.GONE

            when (st) {
                is ReservationState.Success -> {
                    val items = st.reservations.map { resp ->
                        ReservationItem(
                            id         = resp.id,
                            book       = resp.book,
                            reservedAt = resp.createdAt,
                            dueDate    = resp.dueDate
                        )
                    }
                    adapter.submitList(items)
                    binding.tvEmpty.visibility =
                        if (items.isEmpty()) View.VISIBLE else View.GONE
                }
                is ReservationState.Error -> {
                    Toast.makeText(requireContext(), st.error, Toast.LENGTH_LONG).show()
                }
                // Loading already handled by the progressBar toggle
                else -> {}
            }
        }

        vm.loadReservations()
    }

    override fun onResume() {
        super.onResume()
        // refresh header after possible edit
        loadProfileHeader()
    }

    private fun loadProfileHeader() {
        lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) { authRepo.me() }
                binding.tvName.text  = user.name
                binding.tvEmail.text = user.email
            } catch (e: Exception) {
                Toast
                    .makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
