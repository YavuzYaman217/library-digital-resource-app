package com.yyaman.libraryapp.auth

import android.content.Intent
import android.os.Bundle
import android.view.View // Import the View class
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yyaman.libraryapp.databinding.ActivityLoginBinding
import com.yyaman.libraryapp.main.MainActivity
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val vm: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vm.state.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE // Use View.VISIBLE
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE // Use View.GONE
                    // Navigate to MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE // Use View.GONE
                    Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                }
                else -> binding.progressBar.visibility = View.GONE // Use View.GONE
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString()
            vm.login(email, pass)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}