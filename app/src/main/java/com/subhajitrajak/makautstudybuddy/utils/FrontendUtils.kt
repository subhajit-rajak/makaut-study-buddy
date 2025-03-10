package com.subhajitrajak.makautstudybuddy.utils

import android.content.Context
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Toast

fun View.fadeView(
    duration: Long = 1000,
    from: Float = 0f,
    to: Float = 1f,
) {
    val anim = AlphaAnimation(from, to)
    anim.duration = duration
    startAnimation(anim)
}

fun View.removeWithAnim(){
    fadeView(from=1f,to=0f, duration = 1000)
    visibility = View.GONE

}
fun View.showWithAnim(){
    fadeView(duration = 1000)
    visibility = View.VISIBLE
}
fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}