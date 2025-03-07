package com.decisionhelperapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import com.OpenU.decisionhelperapp.R;
import com.google.firebase.FirebaseApp;

public class SplashActivity extends Activity {

    private static final int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_splash);

        // Initialize the progress bar assuming there's a ProgressBar in the layout with id 'progress_bar'
        final ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setMax(100);

        // Handler to update the progress bar
        final Handler progressHandler = new Handler();
        final int progressInterval = SPLASH_TIME_OUT / 100; // Update interval for 1% increment
        progressHandler.post(new Runnable() {
            int progress = 0;
            @Override
            public void run() {
                progress++;
                progressBar.setProgress(progress);
                if (progress < 100) {
                    progressHandler.postDelayed(this, progressInterval);
                }
            }
        });

        // Navigate to MainActivity after the splash timeout
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
