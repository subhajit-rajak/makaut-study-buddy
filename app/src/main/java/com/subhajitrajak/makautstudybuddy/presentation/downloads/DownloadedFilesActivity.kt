package com.subhajitrajak.makautstudybuddy.presentation.downloads

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.databinding.ActivityDownloadedFilesBinding
import com.subhajitrajak.makautstudybuddy.presentation.pdf.PdfActivity
import com.subhajitrajak.makautstudybuddy.utils.removeWithAnim
import com.subhajitrajak.makautstudybuddy.utils.showDeleteConfirmationDialog
import com.subhajitrajak.makautstudybuddy.utils.showToast
import com.subhajitrajak.makautstudybuddy.utils.showWithAnim
import java.io.File

class DownloadedFilesActivity : AppCompatActivity() {

    private val activity = this
    private val binding: ActivityDownloadedFilesBinding by lazy {
        ActivityDownloadedFilesBinding.inflate(layoutInflater)
    }
    private lateinit var filesAdapter: DownloadedFilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val files = downloadDir?.listFiles()?.filter { it.extension == "pdf" } ?: emptyList()

        if (files.isEmpty()) {
            binding.mErrorHolder.showWithAnim()
            binding.recyclerViewFiles.removeWithAnim()
        } else {
            binding.mErrorHolder.removeWithAnim()
            binding.recyclerViewFiles.showWithAnim()
        }

        binding.apply {
            backButton.setOnClickListener {
                finish()
            }

            pullToRefresh.setOnRefreshListener {
                recreate()
                pullToRefresh.isRefreshing = false
            }

            recyclerViewFiles.layoutManager = LinearLayoutManager(activity)
            filesAdapter = DownloadedFilesAdapter(
                activity,
                files,
                onFileClick = { file -> openPdfFile(file) },
                onDeleteClick = { file -> deletePdfFile(file) }
            )
            recyclerViewFiles.adapter = filesAdapter
        }
    }

    private fun deletePdfFile(file: File) {
        showDeleteConfirmationDialog (
            context = activity,
            onConfirm = {
                val deleted = file.delete()
                if (deleted) {
                    showToast(activity, "Delete successful")
                    recreate()
                } else {
                    showToast(activity, "Delete failed")
                }
            }
        )
    }


    private fun openPdfFile(file: File) {
        Intent().apply {
            putExtra("book_pdf", file.toURI().toString())
            putExtra("location", "local")
            setClass(activity, PdfActivity::class.java)
            startActivity(this)
        }
    }
}