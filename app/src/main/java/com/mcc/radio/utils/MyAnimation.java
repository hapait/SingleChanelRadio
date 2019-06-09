package com.mcc.radio.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import static android.view.animation.Animation.INFINITE;
import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class MyAnimation {

    static RotateAnimation rotateAnimation;
    static RotateAnimation rotate;

    public static void animateExpand(View view) {
        RotateAnimation rotate =
                new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(400);
        rotate.setFillAfter(true);
        view.setAnimation(rotate);
        view.startAnimation(rotate);
    }

    public static void animateCollapse(View view) {
        RotateAnimation rotate =
                new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(400);
        rotate.setFillAfter(true);
        view.setAnimation(rotate);
        view.startAnimation(rotate);
    }


    public static void showLoading(ImageView imageView) {
        rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(900);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setRepeatCount(INFINITE);
        imageView.setVisibility(View.VISIBLE);
        imageView.startAnimation(rotate);
    }

    public static void stopLoading(ImageView imageView) {
        if (rotate != null ) {
            rotate.cancel();
            imageView.clearAnimation();
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    public static void rotationAnimator(ImageView playerDiskImage) {
        rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(2000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        playerDiskImage.startAnimation(rotateAnimation);
    }

    public static void stopRotationAnimator() {
        if (rotateAnimation != null) {
            rotateAnimation.cancel();
        }
    }

}
