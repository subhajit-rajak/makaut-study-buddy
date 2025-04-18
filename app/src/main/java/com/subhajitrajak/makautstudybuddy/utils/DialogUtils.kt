package com.subhajitrajak.makautstudybuddy.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import com.subhajitrajak.makautstudybuddy.R

fun showDeleteConfirmationDialog(
    context: Context,
    onConfirm: () -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_delete, null)
    val dialog = AlertDialog.Builder(context)
        .setView(dialogView)
        .setCancelable(true)
        .create()

    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
    val deleteButton = dialogView.findViewById<Button>(R.id.deleteButton)

    cancelButton.setOnClickListener {
        dialog.dismiss()
        onCancel?.invoke()
    }

    deleteButton.setOnClickListener {
        deleteButton.isEnabled = false
        onConfirm()
        dialog.dismiss()
    }

    dialog.show()

    dialog.window?.setLayout(
        (context.resources.displayMetrics.widthPixels * 0.70).toInt(),
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
