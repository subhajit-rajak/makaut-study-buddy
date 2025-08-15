package com.subhajitrajak.makautstudybuddy.presentation.pdf

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.subhajitrajak.makautstudybuddy.R

class ModelSpinnerAdapter(
    context: Context,
    private val displayNames: Array<String>,
    private val apiIdentifiers: Array<String>,
    private val isPremium: Boolean
) : ArrayAdapter<String>(context, R.layout.spinner_item_with_icon, displayNames) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent, false)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent, true)
    }

    private fun createItemView(position: Int, recycledView: View?, parent: ViewGroup, isDropdown: Boolean): View {
        val view = recycledView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_item_with_icon, parent, false)

        val displayName = displayNames[position]
        val apiIdentifier = apiIdentifiers[position]
        val isDeepSeek = apiIdentifier.contains("deepseek", ignoreCase = true)
        val isPremiumModel = !isDeepSeek
        val shouldDisable = isPremiumModel && !isPremium

        val textView = view.findViewById<TextView>(R.id.spinnerText)
        val premiumIcon = view.findViewById<ImageView>(R.id.premiumIcon)

        textView.text = displayName

        if (shouldDisable) {
            // Grey out the text and show premium icon
            textView.alpha = 0.5f
            premiumIcon.visibility = View.VISIBLE
        } else {
            // Normal appearance
            textView.alpha = 1.0f
            premiumIcon.visibility = View.GONE
        }

        return view
    }

    override fun isEnabled(position: Int): Boolean {
        val apiIdentifier = apiIdentifiers[position]
        val isDeepSeek = apiIdentifier.contains("deepseek", ignoreCase = true)
        val isPremiumModel = !isDeepSeek
        return !isPremiumModel || isPremium
    }
}
