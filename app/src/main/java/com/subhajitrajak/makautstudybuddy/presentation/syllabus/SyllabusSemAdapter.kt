package com.subhajitrajak.makautstudybuddy.presentation.syllabus

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhajitrajak.makautstudybuddy.data.models.SyllabusSemModel
import com.subhajitrajak.makautstudybuddy.databinding.ItemSyllabusSemBinding
import com.subhajitrajak.makautstudybuddy.presentation.pdf.PdfActivity

class SyllabusSemAdapter(
    private val list: ArrayList<SyllabusSemModel>,
    val context: Context
) : RecyclerView.Adapter<SyllabusSemAdapter.SyllabusSemViewHolder>() {

    class SyllabusSemViewHolder(val binding: ItemSyllabusSemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: SyllabusSemModel, context: Context) {
            binding.apply {
                semesterNumber.text = model.semester
                lastUpdatedDate.text = model.lastUpdated

                binding.root.setOnClickListener {
                    Intent().apply {
                        putExtra("book_pdf", model.file)
                        putExtra("location", "remote")
                        setClass(context, PdfActivity::class.java)
                        context.startActivity(this)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SyllabusSemViewHolder {
        return SyllabusSemViewHolder(
            ItemSyllabusSemBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: SyllabusSemViewHolder, position: Int) {
        holder.bind(
            model = list[position], context = context
        )
    }
}