package com.subhajitrajak.makautstudybuddy.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import com.subhajitrajak.makautstudybuddy.R

class SwipeButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val START = 0
        private const val CENTER = 1
        private const val END = 2
    }

    private var swipeBtn: ImageView = ImageView(context)
    private var centerText: TextView = TextView(context)
    private lateinit var background: ViewGroup
    private var trail: ViewGroup? = null
    private var collapsedHeight = 0
    private var collapsedWidth = 0
    private var trailEnabled = false
    private var hasActiveStatus = false
    private var hasFinishAnimation = true
    private var isActive = false
    private var onActiveListener: OnActiveListener? = null

    init {
        initView(context, attrs, defStyleAttr, defStyleRes)
    }

    fun setOnActiveListener(listener: () -> Unit) {
        onActiveListener = object : OnActiveListener {
            override fun onActive() {
                listener()
            }
        }
    }

    fun setInnerText(text: String) {
        centerText.text = text
    }

    fun setInnerTextColor(color: Int) {
        centerText.setTextColor(color)
    }

    fun setInnerTextSize(size: Int) {
        centerText.textSize = size.toFloat()
    }

    fun setInnerTextPadding(padding: Int) {
        centerText.setPadding(padding, padding, padding, padding)
    }

    fun setInnerTextPaddings(left: Int, top: Int, right: Int, bottom: Int) {
        centerText.setPadding(left, top, right, bottom)
    }

    fun setInnerTextGravity(gravity: Int) {
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        when (gravity) {
            START -> {
                params.addRule(ALIGN_PARENT_START)
                params.leftMargin = 32
            }
            CENTER -> {
                params.addRule(CENTER_IN_PARENT)
            }
            END -> {
                params.addRule(ALIGN_PARENT_END)
                params.rightMargin = 32
            }
        }
        params.addRule(CENTER_VERTICAL)
        background.updateViewLayout(centerText, params)
    }

    fun setOuterBackgroundDrawable(drawable: Drawable?) {
        background.background = drawable ?: ContextCompat.getDrawable(context, R.drawable.swipe_rounded_background)
    }

    fun setOuterBackgroundTint(color: Int) {
        if (color != -1) {
            background.backgroundTintList = ColorStateList.valueOf(color)
        }
    }

    fun setOuterBackgroundHeight(height: Float) {
        background.layoutParams = background.layoutParams.apply {
            this.height = height.toInt()
        }
    }

    fun setButtonBackgroundDrawable(drawable: Drawable?) {
        swipeBtn.background = drawable ?: ContextCompat.getDrawable(context, R.drawable.swipe_btn_background)
    }

    fun setButtonBackgroundTint(color: Int) {
        if (color != -1) {
            swipeBtn.backgroundTintList = ColorStateList.valueOf(color)
        }
    }

    fun setButtonBackgroundImage(drawable: Drawable?) {
        swipeBtn.setImageDrawable(drawable)
    }

    fun setButtonWidth(width: Int) {
        swipeBtn.layoutParams = swipeBtn.layoutParams.apply {
            this.width = width
        }
    }

    fun setButtonHeight(height: Int) {
        swipeBtn.layoutParams = swipeBtn.layoutParams.apply {
            this.height = height
        }
    }

    fun setButtonPadding(padding: Int) {
        swipeBtn.setPadding(padding, padding, padding, padding)
    }

    fun setTrailEnabled(enabled: Boolean) {
        trailEnabled = enabled
    }

    fun setTrailBackgroundTint(color: Int) {
        trail?.backgroundTintList = ColorStateList.valueOf(color)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        background = RelativeLayout(context)
        val layoutParamsView = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutParamsView.addRule(CENTER_IN_PARENT)
        addView(background, layoutParamsView)

        centerText.gravity = Gravity.CENTER
        val textLayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        textLayoutParams.addRule(CENTER_IN_PARENT)
        background.addView(centerText, textLayoutParams)

        if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {
            context.withStyledAttributes(
                attrs,
                R.styleable.SwipeButton,
                defStyleAttr,
                R.style.default_swipe_button_style
            ) {

                setOuterBackgroundDrawable(getDrawable(R.styleable.SwipeButton_outer_background_drawable))
                setOuterBackgroundTint(getColor(R.styleable.SwipeButton_outer_background_tint, -1))
                setOuterBackgroundHeight(
                    getDimension(
                        R.styleable.SwipeButton_outer_background_height,
                        LayoutParams.WRAP_CONTENT.toFloat()
                    )
                )

                centerText.text = getText(R.styleable.SwipeButton_inner_text)
                centerText.setTextColor(
                    getColor(
                        R.styleable.SwipeButton_inner_text_color,
                        Color.WHITE
                    )
                )
                centerText.typeface = ResourcesCompat.getFont(context, R.font.montserrat_semibold)

                val padding = getDimension(R.styleable.SwipeButton_inner_text_padding, -1f)
                if (padding != -1f) {
                    setInnerTextPadding(padding.toInt())
                } else {
                    val left =
                        getDimension(R.styleable.SwipeButton_inner_text_left_padding, 0f).toInt()
                    val top =
                        getDimension(R.styleable.SwipeButton_inner_text_top_padding, 0f).toInt()
                    val right =
                        getDimension(R.styleable.SwipeButton_inner_text_right_padding, 0f).toInt()
                    val bottom =
                        getDimension(R.styleable.SwipeButton_inner_text_bottom_padding, 0f).toInt()
                    setInnerTextPaddings(left, top, right, bottom)
                }

                centerText.textSize = convertPixelsToSp(
                    getDimension(R.styleable.SwipeButton_inner_text_size, 0f), context
                ).takeIf { it != 0f } ?: 12f

                setInnerTextGravity(getInt(R.styleable.SwipeButton_inner_text_gravity, CENTER))

                collapsedWidth = getDimensionPixelSize(
                    R.styleable.SwipeButton_button_width,
                    LayoutParams.WRAP_CONTENT
                )
                collapsedHeight = getDimensionPixelSize(
                    R.styleable.SwipeButton_button_height,
                    LayoutParams.WRAP_CONTENT
                )

                setButtonBackgroundDrawable(getDrawable(R.styleable.SwipeButton_button_background_drawable))
                setButtonBackgroundTint(
                    getColor(
                        R.styleable.SwipeButton_button_background_tint,
                        -1
                    )
                )
                setButtonBackgroundImage(getDrawable(R.styleable.SwipeButton_button_background_src))

                val btnParams = LayoutParams(collapsedWidth, collapsedHeight).apply {
                    addRule(ALIGN_PARENT_START)
                    addRule(CENTER_VERTICAL)
                    leftMargin = 16
                    rightMargin = 16
                }
                addView(swipeBtn, btnParams)

                setButtonPadding(
                    getDimensionPixelSize(
                        R.styleable.SwipeButton_button_background_padding,
                        0
                    )
                )
                swipeBtn.elevation = 1f

                trailEnabled = getBoolean(R.styleable.SwipeButton_trail_enabled, false)
                val trailTint = getColor(
                    R.styleable.SwipeButton_trail_background_tint,
                    ContextCompat.getColor(context, R.color.swipe_button_gray)
                )
                val trailDrawable = getDrawable(R.styleable.SwipeButton_outer_background_drawable)

                if (trailEnabled) {
                    trail = RelativeLayout(context).apply {
                        layoutParams = LayoutParams(collapsedWidth, collapsedHeight).apply {
                            addRule(ALIGN_PARENT_START)
                            addRule(CENTER_VERTICAL)
                        }
                        background = trailDrawable ?: ContextCompat.getDrawable(
                            context,
                            R.drawable.swipe_rounded_background
                        )
                        backgroundTintList = ColorStateList.valueOf(trailTint)
                        elevation = 0f
                    }
                    addView(trail)
                }

                hasActiveStatus = getBoolean(R.styleable.SwipeButton_has_active_status, false)
                hasFinishAnimation = getBoolean(R.styleable.SwipeButton_has_finish_animation, true)

            }
        }

        setOnTouchListener(getButtonTouchListener())
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun getButtonTouchListener() = OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> !isTouchOutsideInitialPosition(event, swipeBtn)
            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val halfBtn = swipeBtn.width / 2

                when {
                    x > halfBtn && x + halfBtn < width -> swipeBtn.x = x - halfBtn
                    x + halfBtn >= width -> swipeBtn.x = (width - swipeBtn.width).toFloat()
                    x < halfBtn -> swipeBtn.x = 0f
                }

                centerText.alpha = 1 - 1.3f * (swipeBtn.x + swipeBtn.width) / width
                expandTrail()
                true
            }
            MotionEvent.ACTION_UP -> {
                if (isActive) {
                    if (hasFinishAnimation) deactivateButton()
                    onActiveListener?.onActive()
                } else {
                    if (swipeBtn.x + swipeBtn.width > background.width * 0.9f) {
                        if (hasActiveStatus) activateButton()
                        else {
                            onActiveListener?.onActive()
                            if (hasFinishAnimation) moveButtonBack()
                        }
                    } else {
                        moveButtonBack()
                    }
                }
                true
            }
            else -> false
        }
    }

    private fun activateButton() {
        val positionAnimator = ValueAnimator.ofFloat(swipeBtn.x, background.width - swipeBtn.width - 16f).apply {
            addUpdateListener { swipeBtn.x = it.animatedValue as Float }
        }

        val widthAnimator = ValueAnimator.ofInt(collapsedWidth, background.width).apply {
            addUpdateListener {
                swipeBtn.layoutParams = swipeBtn.layoutParams.apply { width = it.animatedValue as Int }
            }
        }

        AnimatorSet().apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isActive = true
                    (swipeBtn.layoutParams as LayoutParams).leftMargin = 16
                    swipeBtn.requestLayout()
                }
            })
            playTogether(positionAnimator, widthAnimator)
            start()
        }
    }

    private fun deactivateButton() {
        val widthAnimator = ValueAnimator.ofInt(swipeBtn.width, collapsedWidth).apply {
            addUpdateListener {
                swipeBtn.layoutParams = swipeBtn.layoutParams.apply { width = it.animatedValue as Int }
                expandTrail()
            }
        }

        val fadeInText = ObjectAnimator.ofFloat(centerText, "alpha", 1f)

        AnimatorSet().apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isActive = false
                }
            })
            playTogether(fadeInText, widthAnimator)
            start()
        }
    }

    private fun expandTrail() {
        if (!trailEnabled || trail == null) return
        trail!!.layoutParams = trail!!.layoutParams.apply {
            width = (swipeBtn.x + collapsedWidth).toInt()
        }
    }

    private fun moveButtonBack() {
        val positionAnimator = ValueAnimator.ofFloat(swipeBtn.x, 16f).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 200
            addUpdateListener {
                swipeBtn.x = it.animatedValue as Float
                expandTrail()
            }
        }

        val fadeInText = ObjectAnimator.ofFloat(centerText, "alpha", 1f)

        AnimatorSet().apply {
            playTogether(positionAnimator, fadeInText)
            start()
        }
    }

    private fun isTouchOutsideInitialPosition(event: MotionEvent, view: View): Boolean {
        return event.x > view.x + view.width
    }

    private fun convertPixelsToSp(px: Float, context: Context): Float {
        return px / context.resources.displayMetrics.scaledDensity
    }

    interface OnActiveListener {
        fun onActive()
    }
}