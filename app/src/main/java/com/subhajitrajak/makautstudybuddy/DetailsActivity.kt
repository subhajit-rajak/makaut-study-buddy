package com.subhajitrajak.makautstudybuddy

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.makautstudybuddy.databinding.ActivityDetailsBinding
import com.subhajitrajak.makautstudybuddy.databinding.LayoutProgressBinding
import com.subhajitrajak.makautstudybuddy.models.BooksModel
import com.subhajitrajak.makautstudybuddy.repository.BookRepo
import com.subhajitrajak.makautstudybuddy.utils.MyResponses
import com.subhajitrajak.makautstudybuddy.viewModels.BookViewModel
import com.subhajitrajak.makautstudybuddy.viewModels.BookViewModelFactory

class DetailsActivity : AppCompatActivity() {
    private val activity = this
    private lateinit var binding: ActivityDetailsBinding

    private val repo = BookRepo(activity)
    private val viewModel by lazy {
        ViewModelProvider(
            activity,
            BookViewModelFactory(repo)
        )[BookViewModel::class.java]
    }

    @Suppress("DEPRECATION")
    @SuppressLint("SetTextI18n", "ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bookModel = intent.getSerializableExtra("book_model") as BooksModel

        binding.apply {

            mReadBookBtn.setOnClickListener {
                viewModel.downloadFile(bookModel.bookPDF, "${bookModel.bookName}.pdf")
            }
            val dialogBinding = LayoutProgressBinding.inflate(layoutInflater)
            val dialog = Dialog(activity).apply {
                setCancelable(false)
                setContentView(dialogBinding.root)
                this.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                this.window!!.setLayout(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT
                )
            }

            viewModel.downloadLiveData.observe(activity) {
                when (it) {
                    is MyResponses.Error -> {
                        dialog.dismiss()
                    }

                    is MyResponses.Loading -> {
                        dialogBinding.mProgress.text = "${it.progress}%"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            dialogBinding.progressBar.setProgress(it.progress, true)
                        } else {
                            dialogBinding.progressBar.progress = it.progress
                        }
                        dialog.show()
                    }

                    is MyResponses.Success -> {
                        dialog.dismiss()
                        Intent().apply {
                            putExtra("book_pdf", it.data?.filePath)
                            setClass(activity, PdfActivity::class.java)
                            startActivity(this)
                        }
                    }
                }
            }
        }
    }
}