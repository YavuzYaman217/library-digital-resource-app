package com.yyaman.libraryapp.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yyaman.libraryapp.databinding.ActivityRegisterBinding
import com.yyaman.libraryapp.main.MainActivity
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val vm: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vm.state.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> binding.progressBar.visibility = View.VISIBLE // Show the ProgressBar
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE // Hide the ProgressBar
                    // Auto–login: go to Main
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE // Hide the ProgressBar
                    Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                }
                else -> binding.progressBar.visibility = View.GONE // Hide the ProgressBar
            }
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString()
            val name  = binding.etName.text.toString().trim()
            vm.register(email, pass, name)
        }
    }
}