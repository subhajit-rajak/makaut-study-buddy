package com.subhajitrajak.makautstudybuddy.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhajitrajak.makautstudybuddy.SemActivity
import com.subhajitrajak.makautstudybuddy.databinding.ItemBranchesBinding
import com.subhajitrajak.makautstudybuddy.models.HomeModel

class HomeAdapter(val list: ArrayList<HomeModel>, val context: Context) :
    RecyclerView.Adapter<HomeAdapter.HomeItemViewHolder>() {


    class HomeItemViewHolder(val binding: ItemBranchesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: HomeModel, context: Context) {
            binding.apply {
                model.apply {
                    branchname.text = branch

                    card.setOnClickListener {
                        // handle here
                        val intent = Intent()
                        intent.putExtra("book_list",booksList)
                        intent.setClass(context,SemActivity::class.java)
                        context.startActivity(intent)
                    }
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemViewHolder {
        return HomeItemViewHolder(ItemBranchesBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: HomeItemViewHolder, position: Int) {
        holder.bind(
            model = list[position], context = context
        )
    }
}