package com.decisionhelperapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.viewmodel.LoginViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import android.widget.ImageButton;
import android.widget.ProgressBar;

// Extending BaseActivity instead of AppCompatActivity for consistency
public class LoginActivity extends BaseActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private EditText emailField;
    private EditText passwordField;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private LoginViewModel loginViewModel;
    
    private static final String PREF_NAME = "DecisionHelperPrefs";
    private static final String KEY_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize components
        emailField = findViewById(R.id.editTextEmail);
        passwordField = findViewById(R.id.editTextPassword);
        Button loginButton = findViewById(R.id.buttonLogin);
        Button registerButton = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressBar);
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        
        // Initialize ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        
        // Observe ViewModel changes
        setupObservers();
        
        loginButton.setOnClickListener(v -> {
            
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            loginViewModel.loginWithEmailPassword(email, password);
        });

        registerButton.setOnClickListener(v -> showRegistrationDialog());

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        ImageButton googleSignInButton = findViewById(R.id.googleSignInButton);
        googleSignInButton.setOnClickListener(v -> signIn());
    }
    
    private void setupObservers() {
        // Observe logged in user
        loginViewModel.getLoggedInUser().observe(this, user -> {
            if (user != null) {
                saveUserAndGoToMain(user.getId());
            }
        });
        
        // Observe error messages
        loginViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe loading state
        loginViewModel.getIsLoading().observe(this, isLoading -> progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "Google sign in successful");
                loginViewModel.firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveUserAndGoToMain(String userId) {
        // Save user ID in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
        
        // Navigate to MainActivity
        goToMainActivity(userId);
    }

    private void goToMainActivity(String userId) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish(); // Close LoginActivity
    }

    private void showRegistrationDialog() {
        // Set up the dialog for registration
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_registration, null);
        builder.setView(view);
        builder.setTitle("Register New User");

        final EditText nameInput = view.findViewById(R.id.editTextName);
        final EditText emailInput = view.findViewById(R.id.editTextRegEmail);
        final EditText passwordInput = view.findViewById(R.id.editTextRegPassword);
        final EditText confirmPasswordInput = view.findViewById(R.id.editTextConfirmPassword);

        builder.setPositiveButton("Register", (dialog, which) -> {
            // Get registration data and pass to ViewModel
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();
            
            loginViewModel.registerUser(name, email, password, confirmPassword);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    // Static method to create a Google Sign-In intent from any activity
    public static Intent createGoogleSignInIntent(android.content.Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, gso);
        return googleSignInClient.getSignInIntent();
    }
}
