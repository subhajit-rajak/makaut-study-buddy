package com.subhajitrajak.makautstudybuddy.presentation.downloads

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhajitrajak.makautstudybuddy.databinding.ItemDownloadedFileBinding
import java.io.File

class DownloadedFilesAdapter(
    private val context: Context,
    private val files: List<File>,
    private val onFileClick: (File) -> Unit,
    private val onDeleteClick: (File) -> Unit
) : RecyclerView.Adapter<DownloadedFilesAdapter.FileViewHolder>() {

    inner class FileViewHolder(val binding: ItemDownloadedFileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File) {
            binding.apply {
                tvFileName.text = file.name
                root.setOnClickListener {
                    onFileClick(file)
                }

                deleteButton.setOnClickListener {
                    onDeleteClick(file)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        return FileViewHolder(
            ItemDownloadedFileBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(file = files[position])
    }
}
