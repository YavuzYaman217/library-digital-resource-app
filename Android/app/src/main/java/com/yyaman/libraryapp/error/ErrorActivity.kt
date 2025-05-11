package com.yyaman.libraryapp.error

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yyaman.libraryapp.databinding.ActivityErrorBinding

class ErrorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityErrorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnRetry.setOnClickListener { finish() }
    }
}
