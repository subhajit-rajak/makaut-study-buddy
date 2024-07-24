package com.subhajitrajak.makautstudybuddy.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.subhajitrajak.makautstudybuddy.DetailsActivity
import com.subhajitrajak.makautstudybuddy.databinding.ActivityHomeBinding
import com.subhajitrajak.makautstudybuddy.models.Books

class BooksAdapter(val list:ArrayList<Books>, val context: Context): RecyclerView.Adapter<BooksAdapter.ViewHolder>() {
    class ViewHolder (val binding: ActivityHomeBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ActivityHomeBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.binding.apply {
            textView.text = model.title
            card.setOnClickListener {
                val intent = Intent(context, DetailsActivity::class.java)
                intent.putExtra("book_title", model.title)
                intent.putExtra("book_desc", model.desc)
                intent.putExtra("book_pdf", model.bookPdf)
                intent.putExtra("book_image", model.image)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity, textView, "NameTrans")
                context.startActivity(intent, options.toBundle())
            }
        }
    }

}