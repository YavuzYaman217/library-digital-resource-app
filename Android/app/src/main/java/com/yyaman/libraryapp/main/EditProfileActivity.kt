package com.yyaman.libraryapp.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yyaman.libraryapp.data.AuthRepository
import com.yyaman.libraryapp.databinding.ActivityEditProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val repo by lazy { AuthRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) preload current values
        lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) { repo.me() }
                binding.etName.setText(user.name)
                binding.etEmail.setText(user.email)
            } catch (_: Exception) {
                Toast.makeText(this@EditProfileActivity,
                    "Failed to load profile", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // 2) save on click
        binding.btnSave.setOnClickListener {
            val name  = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name & email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) { repo.updateProfile(name, email) }
                    Toast.makeText(this@EditProfileActivity,
                        "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@EditProfileActivity,
                        e.localizedMessage ?: "Update failed",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
