package com.subhajitrajak.makautstudybuddy.presentation.askAi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.models.AiModel

class ModelSpinnerAdapter(
    context: Context,
    private val aiModels: List<AiModel>,
    private val isPremium: Boolean
) : ArrayAdapter<AiModel>(context, R.layout.spinner_item_with_icon, aiModels) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun isEnabled(position: Int): Boolean {
        val aiModel = aiModels[position]
        return !aiModel.isPremium || isPremium
    }

    private fun createItemView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val view = recycledView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_item_with_icon, parent, false)

        val aiModel = aiModels[position]
        val textView = view.findViewById<TextView>(R.id.spinnerText)
        val premiumIcon = view.findViewById<ImageView>(R.id.premiumIcon)

        val displayText = if (aiModel.isFaster) {
            "${aiModel.modelName} ⚡"
        } else {
            aiModel.modelName
        }
        textView.text = displayText
        premiumIcon.visibility = if (aiModel.isPremium) View.VISIBLE else View.GONE

        if (!isEnabled(position)) {
            textView.alpha = 0.5f
        } else {
            textView.alpha = 1.0f
        }

        return view
    }
}