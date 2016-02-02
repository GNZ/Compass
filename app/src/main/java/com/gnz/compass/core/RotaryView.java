package com.gnz.compass.core;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;


public class RotaryView {

    private static final int DURATION = 300;
    static final float smoothFactorCompass = 0.5f;
    static final float smoothThresholdCompass = 30f;

    private View associateView;
    private float currentAngle;
    Animation animation;

    public RotaryView(View view) {
        associateView = view;
        currentAngle = 0;
    }

    public void doRotate(float angle) {

        if (animation == null || animation.hasEnded()) {
            float finalAngle = smoothRotation(currentAngle, angle);
            animation = new RotateAnimation(currentAngle, finalAngle,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setFillAfter(true);
            animation.setDuration(DURATION);
            animation.setInterpolator(new LinearInterpolator());
            associateView.startAnimation(animation);
            currentAngle = finalAngle % 360;
        }
    }

    public void changeVisibilityView(boolean visibility) {
        if (visibility) {
            associateView.setVisibility(View.VISIBLE);
        } else {
            associateView.setVisibility(View.GONE);
            associateView.clearAnimation();
        }
    }

    private float smoothRotation(float startAngle, float rotationAngle) {
        float distance = Math.abs(startAngle - rotationAngle);
        float finalAngle = rotationAngle;
        if (distance > 180) {
            distance = 360 - distance;
            if (rotationAngle < startAngle)
                finalAngle += 360;
            else finalAngle -= 360;
        }

        if (distance < smoothThresholdCompass)
            return startAngle + smoothFactorCompass * (finalAngle - startAngle);
        return finalAngle;
    }

}
