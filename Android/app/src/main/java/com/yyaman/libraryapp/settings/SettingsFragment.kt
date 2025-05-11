package com.yyaman.libraryapp.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.yyaman.libraryapp.auth.LoginActivity
import com.yyaman.libraryapp.data.AuthRepository
import com.yyaman.libraryapp.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // for persisting switches
    private val prefs by lazy {
        requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    // your repository which has deleteAccount() & clearToken()
    private val authRepo by lazy { AuthRepository(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ─── Notifications toggle ───────────────────────
        binding.switchNotifications.isChecked = prefs.getBoolean("notify", true)
        binding.switchNotifications.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("notify", checked).apply()
        }

        // ─── Dark mode toggle ────────────────────────────
        val darkSaved = prefs.getBoolean("dark", false)
        binding.switchDark.isChecked = darkSaved
        binding.switchDark.setOnCheckedChangeListener { _, dark ->
            val mode = if (dark)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
            prefs.edit().putBoolean("dark", dark).apply()
        }

        // ─── Delete Account button ───────────────────────
        binding.btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("This will permanently delete your account and all associated data. Are you sure?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            // call your new endpoint
                            val msg = authRepo.deleteAccount()
                            // clear stored JWT
                            authRepo.clearToken()
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()

                            // navigate back to Login, clearing back‐stack
                            startActivity(
                                Intent(requireContext(), LoginActivity::class.java)
                                    .addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    )
                            )
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Failed to delete account: ${e.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
