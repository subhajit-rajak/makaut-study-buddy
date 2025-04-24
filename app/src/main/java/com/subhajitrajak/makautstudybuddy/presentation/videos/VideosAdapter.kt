package com.subhajitrajak.makautstudybuddy.presentation.videos

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.subhajitrajak.makautstudybuddy.data.models.VideosModel
import com.subhajitrajak.makautstudybuddy.databinding.ItemVideosBinding

class VideosAdapter(
    private val list: ArrayList<VideosModel>,
    private val context: Context
) : RecyclerView.Adapter<VideosAdapter.VideoItemViewHolder>() {

    class VideoItemViewHolder(private val binding: ItemVideosBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: VideosModel, context: Context) {
            binding.apply {
                videoTitle.text = model.title
                channelName.text = model.channelTitle
                Glide.with(context).load(model.thumbnailUrl).into(thumbnail)

                card.setOnClickListener {
                    val playlistId = model.playlistId
                    val url = "https://www.youtube.com/playlist?list=$playlistId"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.setPackage("com.google.android.youtube")

                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        // Fallback to browser
                        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(fallbackIntent)
                    }
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemViewHolder {
        return VideoItemViewHolder(
            ItemVideosBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VideoItemViewHolder, position: Int) {
        holder.bind(
            model = list[position], context = context
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<VideosModel>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}