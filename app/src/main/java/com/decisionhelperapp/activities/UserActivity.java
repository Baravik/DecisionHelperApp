package com.decisionhelperapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.OpenU.decisionhelperapp.R;
import com.bumptech.glide.Glide;
import com.decisionhelperapp.auth.GoogleSignInHelper;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends BaseActivity {

    private static final String TAG = "UserActivity";

    private CircleImageView profileImage;
    private TextView textUserName, textUserId, textLoginMethod;
    private Button btnConnectGmail;
    private FirebaseAuth firebaseAuth;

    private GoogleSignInHelper googleSignInHelper;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        firebaseAuth = FirebaseAuth.getInstance();
        googleSignInHelper = new GoogleSignInHelper(this);

        // Setup action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Profile");
        }

        // Initialize views
        profileImage = findViewById(R.id.user_profile_image);
        textUserName = findViewById(R.id.text_user_name);
        textUserId = findViewById(R.id.text_user_id);
        textLoginMethod = findViewById(R.id.text_login_method);
        Button btnLogout = findViewById(R.id.btn_logout);
        btnConnectGmail = findViewById(R.id.btn_connect_gmail);

        // Register Google Sign-In result handler
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        googleSignInHelper.handleSignInResult(result.getData(), new GoogleSignInHelper.SignInCallback() {
                            @Override
                            public void onSuccess(GoogleSignInAccount account) {
                                Log.d(TAG, "Google account received: " + account.getEmail());
                                firebaseAuthWithGoogle(account);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Google Sign-In Failed", e);
                                Toast.makeText(UserActivity.this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.w(TAG, "Google Sign-In canceled or failed");
                    }
                }
        );

        // Load user data and set up buttons
        loadUserData();

        btnLogout.setOnClickListener(v -> logoutUser());
        btnConnectGmail.setOnClickListener(v -> connectWithGmail());
    }

    private void loadUserData() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            textUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            textUserId.setText(user.getUid());

            boolean isGmailLogin = user.getProviderData().stream()
                    .anyMatch(info -> "google.com".equals(info.getProviderId()));
            textLoginMethod.setText(isGmailLogin ? "Google Account" : "Email/Password");

            btnConnectGmail.setVisibility(isGmailLogin ? android.view.View.GONE : android.view.View.VISIBLE);

            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.default_profile)
                        .into(profileImage);
            }
        } else {
            Log.w(TAG, "User not logged in, redirecting to LoginActivity");
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
        Log.d(TAG, "Launching Google Sign-In");
        googleSignInHelper.launchSignIn(googleSignInLauncher);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Google Sign-In with Firebase succeeded");
                        Toast.makeText(this, "Connected to Gmail", Toast.LENGTH_SHORT).show();
                        loadUserData(); // reload user data
                    } else {
                        Log.e(TAG, "Google Sign-In with Firebase failed", task.getException());
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
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
            getOnBackPressedDispatcher();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}