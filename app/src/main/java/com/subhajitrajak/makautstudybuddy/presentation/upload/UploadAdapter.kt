package com.subhajitrajak.makautstudybuddy.presentation.upload

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.models.BooksModel
import com.subhajitrajak.makautstudybuddy.databinding.ItemUploadRequestsBinding
import com.subhajitrajak.makautstudybuddy.utils.Constants
import com.subhajitrajak.makautstudybuddy.utils.Constants.UPLOAD_REQUESTS
import com.subhajitrajak.makautstudybuddy.utils.getTypeCode
import com.subhajitrajak.makautstudybuddy.utils.showDeleteConfirmationDialog
import com.subhajitrajak.makautstudybuddy.utils.showToast

class UploadAdapter(
    private val context: Context,
    private val list: ArrayList<BooksModel>
) : RecyclerView.Adapter<UploadAdapter.UploadViewHolder>() {
    inner class UploadViewHolder(val binding: ItemUploadRequestsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: BooksModel, context: Context) {
            binding.apply {
                subjectName.text = model.bookName
                (model.topicName ?: model.authorName)?.also {
                    topicOrAuthorName.text = it
                    topicOrAuthorName.visibility = View.VISIBLE
                }
                details.text = context.getString(
                    R.string.upload_requests_details_text,
                    model.type,
                    model.semester,
                    model.branch
                )
                status.text = context.getString(R.string.upload_requests_status_text, model.status)

                when (model.status) {
                    Constants.PENDING -> {
                        status.setTextColor(context.getColor(R.color.yellow))
                    }

                    Constants.ACCEPTED -> {
                        status.setTextColor(context.getColor(R.color.highlight))
                    }

                    else -> {
                        status.setTextColor(context.getColor(R.color.red))
                    }
                }

                card.setOnLongClickListener {
                    if (model.status == Constants.PENDING) {
                        showDeleteDialog(adapterPosition)
                        true
                    } else false
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UploadViewHolder {
        return UploadViewHolder(
            ItemUploadRequestsBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: UploadViewHolder, position: Int) {
        holder.bind(
            model = list[position], context = context
        )
    }

    private fun showDeleteDialog(position: Int) {
        if (position < 0 || position >= list.size) return

        showDeleteConfirmationDialog (
            context = context,
            onConfirm = {
                deleteBookRequest(
                    type = list[position].type!!,
                    bookId = list[position].id,
                    bookName = list[position].bookName,
                    topicName = list[position].topicName,
                    authorName = list[position].authorName,
                    onComplete = { success, message ->
                        if (success) {
                            list.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        showToast(context, message)
                    }
                )
            }
        )
    }

    private fun deleteBookRequest(
        type: String, // NOTES_DATA or ORGANIZERS_DATA
        bookId: String,
        bookName: String,
        topicName: String?,
        authorName: String?,
        onComplete: (Boolean, String) -> Unit
    ) {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val dataRef = firebaseDatabase.getReference(UPLOAD_REQUESTS)
            .child(getTypeCode(type))
            .child(bookId)

        dataRef.removeValue()
            .addOnSuccessListener {
                val bookRef = if (topicName != null) {
                    "$bookName-$topicName.pdf"
                } else if (authorName != null) {
                    "$bookName-$authorName.pdf"
                } else {
                    "$bookName.pdf"
                }
                val fileRef = FirebaseStorage.getInstance().reference.child(bookRef)
                fileRef.delete()
                    .addOnSuccessListener {
                        showToast(context, "Storage: Delete successful")
                    }
                    .addOnFailureListener { exception ->
                        showToast(context, "Error: ${exception.message}")
                    }
                onComplete(true, "Delete successful")
            }
            .addOnFailureListener { exception ->
                onComplete(false, "Error: ${exception.message}")
            }
    }
}