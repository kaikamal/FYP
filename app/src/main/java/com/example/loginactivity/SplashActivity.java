package com.example.loginactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;


public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_TIME_OUT = 4000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final ImageView imageView = findViewById(R.id.splash_image);
        final ScaleAnimation pulseAnimation = new ScaleAnimation(
                1.0f, 1.2f, // Start and end scale X
                1.0f, 1.2f, // Start and end scale Y
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point X
                Animation.RELATIVE_TO_SELF, 0.5f  // Pivot point Y
        );
        pulseAnimation.setDuration(1000); // Duration in milliseconds
        pulseAnimation.setRepeatMode(Animation.REVERSE);
        pulseAnimation.setRepeatCount(Animation.INFINITE);

        // Start the pulsating animation
        imageView.startAnimation(pulseAnimation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, Register.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}