package com.subhajitrajak.makautstudybuddy.presentation.subjects

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhajitrajak.makautstudybuddy.data.models.BooksModel
import com.subhajitrajak.makautstudybuddy.databinding.ItemSemsBinding
import com.subhajitrajak.makautstudybuddy.presentation.details.DetailsActivity
import com.subhajitrajak.makautstudybuddy.utils.Constants.NOTES

class CategoryAdapter(
    private val list: ArrayList<BooksModel>,
    val context: Context
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(val binding: ItemSemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: BooksModel, context: Context) {
            binding.apply {
                organizerName.text = model.bookName
                semesterNumber.text = model.semester

                if (model.type == NOTES) {
                    topicName.text = model.topicName
                    topicName.visibility = View.VISIBLE
                }

                binding.root.setOnClickListener {
                    Intent().apply {
                        putExtra("book_model", model)
                        setClass(context, DetailsActivity::class.java)
                        context.startActivity(this)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            ItemSemsBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(
            model = list[position], context = context
        )
    }
}