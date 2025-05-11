package com.yyaman.libraryapp.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yyaman.libraryapp.data.DummyStore
import com.yyaman.libraryapp.databinding.ActivityResourceDetailBinding
import com.yyaman.libraryapp.model.DigitalResource

class ResourceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResourceDetailBinding
    private lateinit var res: DigitalResource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResourceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Pull all the extras, including file_path
        val id       = intent.getIntExtra("id", -1)
        val title    = intent.getStringExtra("title")  ?: ""
        val author   = intent.getStringExtra("author") ?: ""
        val type     = intent.getStringExtra("type")   ?: ""
        val filePath = intent.getStringExtra("file_path") ?: ""

        // 2) Pass filePath into your model
        res = DigitalResource(
            id       = id,
            title    = title,
            author   = author,
            type     = type,
            filePath = filePath
        )

        // 3) Populate your UI
        binding.tvTitle.text  = title
        binding.tvAuthor.text = author
        // you already have a placeholder image in XML
        // binding.ivThumb.setImageResource(R.drawable.ic_resource_placeholder)

        binding.btnBookmark.setOnClickListener {
            DummyStore.bookmarks.add(res)
            Toast.makeText(this, "Resource bookmarked!", Toast.LENGTH_SHORT).show()
        }
    }
}
