package com.subhajitrajak.makautstudybuddy.presentation.syllabus

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhajitrajak.makautstudybuddy.data.models.SyllabusModel
import com.subhajitrajak.makautstudybuddy.databinding.ItemBranchesBinding
import java.util.ArrayList

class SyllabusAdapter(private val list: ArrayList<SyllabusModel>, private val context: Context) :
    RecyclerView.Adapter<SyllabusAdapter.HomeItemViewHolder>() {

    class HomeItemViewHolder(private val binding: ItemBranchesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: SyllabusModel, context: Context) {
            binding.apply {
                model.apply {
                    branchname.text = branch

                    card.setOnClickListener {
                        val intent = Intent()
                        intent.putExtra("sem_list",semList)
                        intent.setClass(context, SyllabusSemActivity::class.java)
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