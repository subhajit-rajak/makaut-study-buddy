package com.subhajitrajak.makautstudybuddy.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.subhajitrajak.makautstudybuddy.R;

public class SwipeButton extends RelativeLayout {
    private static final int START = 0;
    private static final int CENTER = 1;
    private static final int END = 2;
    private ImageView swipeBtn;
    private TextView centerText;
    private ViewGroup background, trail;
    private int collapsedHeight, collapsedWidth;
    private boolean trailEnabled = false, hasActiveStatus = false, hasFinishAnimation = true;
    private Context context;
    private OnActiveListener onActiveListener;
    private boolean isActive = false;

    public SwipeButton(Context context) {
        super(context);
        init(context, null, -1, -1);
    }

    public SwipeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1, -1);
    }

    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, -1);
    }

    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnActiveListener(OnActiveListener onActiveListener) {
        this.onActiveListener = onActiveListener;
    }

    public void setInnerText(String text) {
        centerText.setText(text);
    }

    public void setInnerTextColor(int color) {
        centerText.setTextColor(color);
    }

    public void setInnerTextSize(int size) {
        centerText.setTextSize(size);
    }

    public void setInnerTextPadding(int innerTextPadding) {
        centerText.setPadding((int) innerTextPadding, (int) innerTextPadding, (int) innerTextPadding, (int) innerTextPadding);
    }

    public void setInnerTextPaddings(int left, int top, int right, int bottom) {
        centerText.setPadding(left, top, right, bottom);
    }

    public void setInnerTextGravity(int gravity) {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switch (gravity) {
            case START: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                layoutParams.leftMargin = 32;
                break;
            }
            case CENTER: {
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                break;
            }
            case END: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                layoutParams.rightMargin = 32;
                break;
            }
        }
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        background.updateViewLayout(centerText, layoutParams);
    }

    public void setOuterBackgroundDrawable(Drawable drawable) {
        if (drawable != null)
            background.setBackground(drawable);
        else
            background.setBackground(ContextCompat.getDrawable(context, R.drawable.swipe_rounded_background));
    }

    public void setOuterBackgroundTint(int color) {
        if (color != -1)
            background.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    public void setOuterBackgroundHeight(float height) {
        ViewGroup.LayoutParams backgroundLayoutParam = background.getLayoutParams();
        backgroundLayoutParam.height = (int) height;
        background.setLayoutParams(backgroundLayoutParam);
    }

    public void setButtonBackgroundDrawable(Drawable drawable) {
        if (drawable != null)
            swipeBtn.setBackground(drawable);
        else
            swipeBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.swipe_btn_background));
    }

    public void setButtonBackgroundTint(int color) {
        if (color != -1)
            swipeBtn.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    public void setButtonBackgroundImage(Drawable drawable) {
        swipeBtn.setImageDrawable(drawable);
    }

    public void setButtonWidth(int width) {
        ViewGroup.LayoutParams layoutParams = swipeBtn.getLayoutParams();
        layoutParams.width = width;
        swipeBtn.setLayoutParams(layoutParams);
    }

    public void setButtonHeight(int height) {
        ViewGroup.LayoutParams layoutParams = swipeBtn.getLayoutParams();
        layoutParams.height = height;
        swipeBtn.setLayoutParams(layoutParams);
    }

    public void setButtonPadding(int padding) {
        swipeBtn.setPadding(padding, padding, padding, padding);
    }

    public void setTrailEnabled(boolean enabled) {
        this.trailEnabled = enabled;
    }

    public void setTrailBackgroundTint(int color) {
        trail.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.context = context;
        background = new RelativeLayout(context);
        LayoutParams layoutParamsView = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        addView(background, layoutParamsView);

        final TextView centerText = new TextView(context);
        this.centerText = centerText;
        centerText.setGravity(Gravity.CENTER);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        background.addView(centerText, layoutParams);

        swipeBtn = new ImageView(context);

        if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeButton,defStyleAttr, R.style.default_swipe_button_style);
            //----- HANDLE THE OUTER BACKGROUND -----//
            setOuterBackgroundDrawable(typedArray.getDrawable(R.styleable.SwipeButton_outer_background_drawable));
            setOuterBackgroundTint(typedArray.getColor(R.styleable.SwipeButton_outer_background_tint, -1));
            setOuterBackgroundHeight(typedArray.getDimension(R.styleable.SwipeButton_outer_background_height, ViewGroup.LayoutParams.WRAP_CONTENT));

            //----- HANDLE THE INNER TEXT -----//
            centerText.setText(typedArray.getText(R.styleable.SwipeButton_inner_text));
            centerText.setTextColor(typedArray.getColor(R.styleable.SwipeButton_inner_text_color, Color.WHITE));
            Typeface typeface = ResourcesCompat.getFont(context, R.font.montserrat_semibold);
            centerText.setTypeface(typeface);

            float innerTextPadding = typedArray.getDimension(R.styleable.SwipeButton_inner_text_padding, -1);
            float innerTextLeftPadding = typedArray.getDimension(R.styleable.SwipeButton_inner_text_left_padding, 0);
            float innerTextTopPadding = typedArray.getDimension(R.styleable.SwipeButton_inner_text_top_padding, 0);
            float innerTextRightPadding = typedArray.getDimension(R.styleable.SwipeButton_inner_text_right_padding, 0);
            float innerTextBottomPadding = typedArray.getDimension(R.styleable.SwipeButton_inner_text_bottom_padding, 0);
            if (innerTextPadding != -1)
                centerText.setPadding((int) innerTextPadding, (int) innerTextPadding, (int) innerTextPadding, (int) innerTextPadding);
            else
                centerText.setPadding((int) innerTextLeftPadding, (int) innerTextTopPadding, (int) innerTextRightPadding, (int) innerTextBottomPadding);

            float textSize = convertPixelsToSp(typedArray.getDimension(R.styleable.SwipeButton_inner_text_size, 0), context);

            if (textSize != 0) centerText.setTextSize(textSize);
            else centerText.setTextSize(12);
            setInnerTextGravity(typedArray.getInt(R.styleable.SwipeButton_inner_text_gravity, 1));

            //----- HANDLE THE SWIPE BTN -----//
            collapsedWidth = (int) typedArray.getDimension(R.styleable.SwipeButton_button_width, ViewGroup.LayoutParams.WRAP_CONTENT);
            collapsedHeight = (int) typedArray.getDimension(R.styleable.SwipeButton_button_height, ViewGroup.LayoutParams.WRAP_CONTENT);

            setButtonBackgroundDrawable(typedArray.getDrawable(R.styleable.SwipeButton_button_background_drawable));
            setButtonBackgroundTint(typedArray.getColor(R.styleable.SwipeButton_button_background_tint, -1));
            setButtonBackgroundImage(typedArray.getDrawable(R.styleable.SwipeButton_button_background_src));

            LayoutParams btnLayoutParam = new LayoutParams(collapsedWidth, collapsedHeight);
            btnLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            btnLayoutParam.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            btnLayoutParam.leftMargin = 16;
            btnLayoutParam.rightMargin = 16;
            addView(swipeBtn, btnLayoutParam);
            setButtonPadding((int) typedArray.getDimension(R.styleable.SwipeButton_button_background_padding, 0));
            swipeBtn.setElevation(1);

            //----- HANDLE THE TRAIL -----//
            trailEnabled = typedArray.getBoolean(R.styleable.SwipeButton_trail_enabled, false);
            int trailBackgroundTint = typedArray.getColor(R.styleable.SwipeButton_trail_background_tint, getResources().getColor(R.color.swipe_button_gray));
            Drawable trailBackgroundDrawable = typedArray.getDrawable(R.styleable.SwipeButton_outer_background_drawable);

            if (trailEnabled) {
                trail = new RelativeLayout(context);
                LayoutParams trailLayoutParams = new LayoutParams(collapsedWidth, collapsedHeight);
                trailLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                trailLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                trail.setBackgroundTintList(ColorStateList.valueOf(trailBackgroundTint));
                trail.setElevation(0);
                if (trailBackgroundDrawable != null)
                    trail.setBackground(trailBackgroundDrawable);
                else
                    trail.setBackground(ContextCompat.getDrawable(context, R.drawable.swipe_rounded_background));

                addView(trail, trailLayoutParams);
            }

            //----- HANDLE THE ACTIVE STATUS -----//
            hasActiveStatus = typedArray.getBoolean(R.styleable.SwipeButton_has_active_status, false);
            hasFinishAnimation = typedArray.getBoolean(R.styleable.SwipeButton_has_finish_animation, true);
            typedArray.recycle();
        }
        setOnTouchListener(getButtonTouchListener());
    }

    private OnTouchListener getButtonTouchListener() {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        return !isTouchOutsideInitialPosition(event, swipeBtn);
                    case MotionEvent.ACTION_MOVE:
                        if (event.getX() > swipeBtn.getWidth() / 2 && event.getX() + swipeBtn.getWidth() / 2 < getWidth()) {
                            swipeBtn.setX(event.getX() - swipeBtn.getWidth() / 2);
                            centerText.setAlpha(1 - 1.3f * (swipeBtn.getX() + swipeBtn.getWidth()) / getWidth());
                        }

                        //DON'T LET THE BUTTON GET OUTSIDE THE SCREEN
                        if (event.getX() + swipeBtn.getWidth() / 2 > getWidth() && swipeBtn.getX() + swipeBtn.getWidth() / 2 < getWidth()) {
                            swipeBtn.setX(getWidth() - swipeBtn.getWidth());
                        }

                        if (event.getX() < swipeBtn.getWidth() / 2) {
                            swipeBtn.setX(0);
                        }
                        expandTrail();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (isActive) {
                            if (hasFinishAnimation)
                                deactivateButton();
                            if (onActiveListener != null)
                                onActiveListener.onActive();
                        } else {
                            if (swipeBtn.getX() + swipeBtn.getWidth() > background.getWidth() * 0.9) {
                                if (hasActiveStatus) {
                                    activateButton();
                                } else {
                                    if (onActiveListener != null)
                                        onActiveListener.onActive();
                                    if (hasFinishAnimation)
                                        moveButtonBack();
                                }
                            } else
                                moveButtonBack();
                        }
                        return true;
                }
                return false;
            }
        };
    }

    private void activateButton() {
        final ValueAnimator positionAnimator = ValueAnimator.ofFloat(swipeBtn.getX(), background.getWidth() - swipeBtn.getWidth() - 16);
        positionAnimator.addUpdateListener(animation -> {
            float x = (Float) positionAnimator.getAnimatedValue();
            swipeBtn.setX(x);
        });

        background.setPadding(16, 0, 16, 0);


        final ValueAnimator widthAnimator = ValueAnimator.ofInt(
                collapsedWidth,
                background.getWidth()
        );

        widthAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = swipeBtn.getLayoutParams();
            params.width = (Integer) widthAnimator.getAnimatedValue();
            swipeBtn.setLayoutParams(params);
        });


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isActive = true;

                // Restore left margin explicitly
                LayoutParams params = (LayoutParams) swipeBtn.getLayoutParams();
                params.leftMargin = 16;
                swipeBtn.setLayoutParams(params);
            }
        });

        animatorSet.playTogether(positionAnimator, widthAnimator);
        animatorSet.start();
    }

    private void deactivateButton() {
        final ValueAnimator widthAnimator = ValueAnimator.ofInt(
                swipeBtn.getWidth(),
                collapsedWidth
        );

        widthAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = swipeBtn.getLayoutParams();
            params.width = (Integer) widthAnimator.getAnimatedValue();
            swipeBtn.setLayoutParams(params);
            expandTrail();
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isActive = false;
            }
        });

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(centerText, "alpha", 1);

        animatorSet.playTogether(objectAnimator, widthAnimator);
        animatorSet.start();
    }

    private void expandTrail() {
        if (!trailEnabled) return;
        ViewGroup.LayoutParams trailLayoutParams = trail.getLayoutParams();
        trailLayoutParams.width = (int) (swipeBtn.getX() + collapsedWidth);
        trail.setLayoutParams(trailLayoutParams);
    }

    private void moveButtonBack() {
        final ValueAnimator positionAnimator = ValueAnimator.ofFloat(swipeBtn.getX(), 16);
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(animation -> {
            float x = (Float) positionAnimator.getAnimatedValue();
            swipeBtn.setX(x);
            expandTrail();
        });
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(centerText, "alpha", 1);
        positionAnimator.setDuration(200);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator, positionAnimator);
        animatorSet.start();
    }

    private boolean isTouchOutsideInitialPosition(MotionEvent event, View view) {
        return event.getX() > view.getX() + view.getWidth();
    }

    float convertPixelsToSp(float px, Context context) {
        return px / context.getResources().getDisplayMetrics().scaledDensity;
    }

}
