package com.yyaman.libraryapp.digital

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.yyaman.libraryapp.ui.DigitalViewModel
import com.yyaman.libraryapp.ui.DigitalState
import com.yyaman.libraryapp.data.DigitalResource
import com.yyaman.libraryapp.databinding.FragmentDigitalListBinding
import com.yyaman.libraryapp.adapter.DigitalAdapter
import com.yyaman.libraryapp.detail.PdfViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DigitalListFragment : Fragment() {
    private var _binding: FragmentDigitalListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DigitalViewModel by viewModels()
    private var isDownloading = false

    companion object {
        private const val TAG = "DigitalListFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDigitalListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvDigital.layoutManager = LinearLayoutManager(requireContext())
        binding.progressBar.visibility = View.GONE

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DigitalState.Loading -> {
                    // initial resource list loading
                    binding.progressBar.visibility = View.VISIBLE
                }
                is DigitalState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvDigital.adapter = DigitalAdapter(state.items) { resource ->
                        onResourceClicked(resource)
                    }
                }
                is DigitalState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.loadAll()
    }

    private fun onResourceClicked(resource: DigitalResource) {
        if (isDownloading) {
            Snackbar.make(binding.root, "Please wait, download in progress…", Snackbar.LENGTH_SHORT).show()
            return
        }
        isDownloading = true
        binding.progressBar.visibility = View.VISIBLE

        viewModel.download(
            resource.id,
            onFile = { body -> saveAndShowPdf(resource.id, body) },
            onError = { err ->
                isDownloading = false
                binding.progressBar.visibility = View.GONE
                Snackbar.make(binding.root, "Download failed: $err", Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun saveAndShowPdf(id: Int, body: ResponseBody) {
        lifecycleScope.launchWhenStarted {
            // 1) Save file off the UI thread
            val cacheFile = withContext(Dispatchers.IO) {
                val outFile = File(requireContext().cacheDir, "res_${id}.pdf")
                try {
                    body.byteStream().use { input ->
                        FileOutputStream(outFile).use { output ->
                            input.copyTo(output)
                            output.flush()
                        }
                    }
                } catch (e: IOException) {
                    // if anything goes wrong, treat it as a failure
                    Log.e(TAG, "Error writing PDF file", e)
                    return@withContext null
                }
                outFile
            }

            // 2) Back on main thread: check success
            if (cacheFile == null || !cacheFile.exists()) {
                isDownloading = false
                binding.progressBar.visibility = View.GONE
                Snackbar.make(binding.root, "Failed to download PDF, please retry", Snackbar.LENGTH_LONG).show()
                return@launchWhenStarted
            }

            // 3) Launch in‐app PDF viewer
            binding.progressBar.visibility = View.GONE
            isDownloading = false
            val intent = Intent(requireContext(), PdfViewActivity::class.java)
                .putExtra("pdf_path", cacheFile.absolutePath)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
