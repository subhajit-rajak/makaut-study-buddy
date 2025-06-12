package com.subhajitrajak.makautstudybuddy.presentation.books

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.models.BooksModel
import com.subhajitrajak.makautstudybuddy.databinding.ItemBooksBinding
import com.subhajitrajak.makautstudybuddy.presentation.details.DetailsActivity

class BooksAdapter (
    private val list: ArrayList<BooksModel>,
    private val context: Context
) : RecyclerView.Adapter<BooksAdapter.BookViewHolder>() {

    class BookViewHolder(private val binding: ItemBooksBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: BooksModel, context: Context) {
            binding.apply {
                model.apply {
                    bookTitle.text = bookName
                    binding.authorName.text = authorName
                    Glide.with(context)
                        .load(preview.takeIf { !it.isNullOrEmpty() } ?: R.drawable.no_preview)
                        .into(thumbnail)

                    root.setOnClickListener {
                        Intent().apply {
                            putExtra("book_model", model)
                            setClass(context, DetailsActivity::class.java)
                            context.startActivity(this)
                        }
                    }
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return BookViewHolder(ItemBooksBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(
            model = list[position], context = context
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<BooksModel>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}