package com.decisionhelperapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final String PREF_NAME = "DecisionHelperPrefs";
    private static final String KEY_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String userId = prefs.getString(KEY_USER_ID, null);

        if (userId != null) {
            Log.d(TAG, "✅ Found userId in SharedPreferences: " + userId);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        } else {
            Log.d(TAG, "❌ No userId found, redirecting to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish(); // Close splash
    }
}