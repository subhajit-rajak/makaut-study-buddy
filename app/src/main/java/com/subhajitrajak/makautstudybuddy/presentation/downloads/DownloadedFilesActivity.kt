package com.subhajitrajak.makautstudybuddy.presentation.downloads

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.databinding.ActivityDownloadedFilesBinding
import com.subhajitrajak.makautstudybuddy.presentation.pdf.PdfActivity
import com.subhajitrajak.makautstudybuddy.utils.removeWithAnim
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
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_confirm_delete, null)
        val dialog = android.app.AlertDialog.Builder(activity)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val deleteButton = dialogView.findViewById<Button>(R.id.deleteButton)

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        deleteButton.setOnClickListener {
            deleteButton.isEnabled = false

            // Perform delete
            val deleted = file.delete()
            if (deleted) {
                showToast(activity, "Delete successful")
                recreate()
            } else {
                showToast(activity, "Delete failed")
            }
            dialog.dismiss()
        }

        dialog.show()

        // Force width to wrap content and apply margins manually
        val window = dialog.window
        window?.setLayout(
            (this.resources.displayMetrics.widthPixels * 0.70).toInt(),  // 70% of screen width
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun openPdfFile(file: File) {
        val uri = FileProvider.getUriForFile(
            activity,
            "${packageName}.provider",
            file
        )

        Intent().apply {
            putExtra("book_pdf", uri.toString())
            putExtra("location", "local")
            setClass(activity, PdfActivity::class.java)
            startActivity(this)
        }
    }
}