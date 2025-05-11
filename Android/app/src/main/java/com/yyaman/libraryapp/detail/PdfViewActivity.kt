package com.yyaman.libraryapp.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.yyaman.libraryapp.R
import java.io.File

class PdfViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)

        val filePath = intent.getStringExtra("pdf_path")
        if (filePath == null) {
            finish()
            return
        }

        findViewById<PDFView>(R.id.pdfView)
            .fromFile(File(filePath))
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .load()
    }
}
