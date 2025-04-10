package com.decisionhelperapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.OpenU.decisionhelperapp.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends BaseActivity {

    private CircleImageView profileImage;
    private TextView textUserName;
    private TextView textUserId;
    private TextView textLoginMethod;
    private Button btnLogout;
    private Button btnConnectGmail;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Set the title in the ActionBar (inherited from BaseActivity)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Profile");
        }

        // Initialize views
        profileImage = findViewById(R.id.user_profile_image);
        textUserName = findViewById(R.id.text_user_name);
        textUserId = findViewById(R.id.text_user_id);
        textLoginMethod = findViewById(R.id.text_login_method);
        btnLogout = findViewById(R.id.btn_logout);
        btnConnectGmail = findViewById(R.id.btn_connect_gmail);

        // Load user data
        loadUserData();

        // Set up logout button
        btnLogout.setOnClickListener(v -> logoutUser());
        
        // Set up connect with Gmail button
        btnConnectGmail.setOnClickListener(v -> connectWithGmail());
    }

    private void loadUserData() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // Display user information
            textUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            textUserId.setText(user.getUid());
            
            // Determine login method
            boolean isGmailLogin = user.getProviderData().stream()
                    .anyMatch(userInfo -> userInfo.getProviderId().equals("google.com"));
            textLoginMethod.setText(isGmailLogin ? "Google Account" : "Email/Password");
            
            // Show or hide the connect with Gmail button based on login method
            btnConnectGmail.setVisibility(isGmailLogin ? android.view.View.GONE : android.view.View.VISIBLE);

            // Load profile image
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.default_profile)
                    .into(profileImage);
            }
        } else {
            // If somehow user is not logged in, redirect to login
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void connectWithGmail() {
        // Create a Google Sign-In intent
        Intent signInIntent = LoginActivity.createGoogleSignInIntent(this);
        startActivity(signInIntent);
        finish(); // Close this activity so when user returns they'll see updated profile
    }

    private void redirectToLogin() {
        Intent intent = new Intent(UserActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
