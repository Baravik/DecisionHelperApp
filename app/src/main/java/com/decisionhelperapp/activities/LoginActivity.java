package com.decisionhelperapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.auth.GoogleSignInHelper;
import com.decisionhelperapp.viewmodel.LoginViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private LoginViewModel loginViewModel;
    private EditText emailField, passwordField;
    private ProgressBar progressBar;
    private GoogleSignInHelper googleSignInHelper;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        googleSignInHelper = new GoogleSignInHelper(this);

        emailField = findViewById(R.id.editTextEmail);
        passwordField = findViewById(R.id.editTextPassword);
        progressBar = findViewById(R.id.progressBar);

        Button loginButton = findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(v -> {
            Log.d(TAG, "Login button clicked");
            loginViewModel.login(emailField.getText().toString(), passwordField.getText().toString());
        });

        Button registerButton = findViewById(R.id.buttonRegister);
        registerButton.setOnClickListener(v -> showRegistrationDialog());

        ImageButton googleSignInBtn = findViewById(R.id.googleSignInButton);
        googleSignInBtn.setOnClickListener(v -> {
            Log.d(TAG, "Google Sign-In button clicked");
            googleSignInHelper.launchSignIn(googleSignInLauncher);
        });

        // Register ViewModel observers before ActivityResult launcher
        observeViewModel();

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Log.d(TAG, "Google Sign-In intent returned RESULT_OK");
                        googleSignInHelper.handleSignInResult(result.getData(), new GoogleSignInHelper.SignInCallback() {
                            public void onSuccess(GoogleSignInAccount account) {
                                Log.d(TAG, "Google account received: " + account.getEmail());
                                loginViewModel.loginWithGoogle(account);
                            }

                            public void onError(Exception e) {
                                Log.e(TAG, "Google Sign-In Failed", e);
                                Toast.makeText(LoginActivity.this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.w(TAG, "Google Sign-In was cancelled or failed");
                    }
                }
        );
    }

    private void observeViewModel() {
        loginViewModel.getUser().observe(this, user -> {
            Log.d("LoginActivity", "✅ Observed user: " + (user != null ? user.getEmail() : "null"));
            if (user != null) {
                if (isFinishing()) {
                    Log.e("LoginActivity", "❌ Activity is finishing, canceling navigation!");
                    return;
                }

                // Save userId to SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences("DecisionHelperPrefs", MODE_PRIVATE).edit();
                editor.putString("userId", user.getId());
                editor.apply();

                Log.d("LoginActivity", "🚀 Navigating to MainActivity with userId: " + user.getId());
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("USER_ID", user.getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        loginViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error: " + error);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        loginViewModel.getLoading().observe(this, loading -> {
            progressBar.setVisibility(loading != null && loading ? View.VISIBLE : View.GONE);
            Log.d(TAG, "Loading state: " + loading);
        });
    }

    private void showRegistrationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_registration, null);
        builder.setView(view);

        final EditText nameInput = view.findViewById(R.id.editTextName);
        final EditText emailInput = view.findViewById(R.id.editTextRegEmail);
        final EditText passwordInput = view.findViewById(R.id.editTextRegPassword);
        final EditText confirmInput = view.findViewById(R.id.editTextConfirmPassword);

        builder.setPositiveButton("Register", (dialog, which) -> {
            String password = passwordInput.getText().toString();
            String confirm = confirmInput.getText().toString();

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Register button clicked");
            loginViewModel.register(
                    nameInput.getText().toString().trim(),
                    emailInput.getText().toString().trim(),
                    password
            );
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }
}