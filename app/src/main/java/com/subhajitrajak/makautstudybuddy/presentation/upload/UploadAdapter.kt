package com.subhajitrajak.makautstudybuddy.presentation.upload

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.models.BooksModel
import com.subhajitrajak.makautstudybuddy.databinding.ItemUploadRequestsBinding
import com.subhajitrajak.makautstudybuddy.utils.Constants
import com.subhajitrajak.makautstudybuddy.utils.Constants.BOOK_LIST
import com.subhajitrajak.makautstudybuddy.utils.Constants.UPLOAD_REQUESTS
import com.subhajitrajak.makautstudybuddy.utils.getBranchCode
import com.subhajitrajak.makautstudybuddy.utils.getTypeCode
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
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UploadAdapter.UploadViewHolder {
        return UploadViewHolder(
            ItemUploadRequestsBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: UploadAdapter.UploadViewHolder, position: Int) {
        holder.bind(
            model = list[position], context = context
        )

        holder.binding.card.setOnLongClickListener {
            showDeleteDialog(position)
            true
        }
    }

    private fun showDeleteDialog(position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_delete, null)
        val dialog = android.app.AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val deleteButton = dialogView.findViewById<Button>(R.id.deleteButton)

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        deleteButton.setOnClickListener {
            // Perform delete
            deleteBookRequest(
                type = list[position].type!!,
                branch = list[position].branch!!,
                bookId = list[position].id,
                bookName = list[position].bookName,
                onComplete = { success, message ->
                    if (success) {
                        list.removeAt(position)
                        notifyItemRemoved(position)
                        dialog.dismiss()
                    }
                    showToast(context, message)
                }
            )
        }

        dialog.show()

        // Force width to wrap content and apply margins manually
        val window = dialog.window
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.70).toInt(),  // 70% of screen width
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun deleteBookRequest(
        type: String, // NOTES_DATA or ORGANIZERS_DATA
        branch: String,
        bookId: String,
        bookName: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val dataRef = firebaseDatabase.getReference(UPLOAD_REQUESTS)
            .child(getTypeCode(type))
            .child(getBranchCode(branch))
            .child(BOOK_LIST)
            .child(bookId)

        dataRef.removeValue()
            .addOnSuccessListener {
                val fileRef = FirebaseStorage.getInstance().reference.child("$bookName.pdf")
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