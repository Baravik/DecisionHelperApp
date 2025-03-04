package com.decisionhelperapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.database.UserDAO;
import com.decisionhelperapp.models.User;
import com.decisionhelperapp.utils.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import android.widget.ImageButton;
import android.content.Intent;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private UserDAO userDAO;
    private EditText emailField;
    private EditText passwordField;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "DecisionHelperPrefs";
    private static final String KEY_FIRST_TIME = "firstTime";
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
        
        // Initialize Firebase Auth and UserDAO
        mAuth = FirebaseAuth.getInstance();
        userDAO = new UserDAO();
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Check if it's the first time opening the app
        if (isFirstTime()) {
            createGuestUser();
        } else {
            // Check if we have a stored user
            String userId = sharedPreferences.getString(KEY_USER_ID, null);
            if (userId != null) {
                // User has logged in before, proceed to MainActivity
                goToMainActivity(userId);
                return;
            }
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithEmailPassword();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegistrationDialog();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        ImageButton googleSignInButton = findViewById(R.id.googleSignInButton);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private boolean isFirstTime() {
        boolean firstTime = sharedPreferences.getBoolean(KEY_FIRST_TIME, true);
        if (firstTime) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_FIRST_TIME, false);
            editor.apply();
        }
        return firstTime;
    }

    private void createGuestUser() {
        // Create a guest user with a generated ID
        String guestId = "guest_" + System.currentTimeMillis();
        User guestUser = new User(guestId);
        userDAO.addUser(guestUser, new UserDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                // Save the guest user ID
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_USER_ID, guestId);
                editor.apply();
                
                Toast.makeText(LoginActivity.this, "Welcome Guest User!", Toast.LENGTH_SHORT).show();
                goToMainActivity(guestId);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to create guest user", e);
                Toast.makeText(LoginActivity.this, "Failed to create guest account", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginWithEmailPassword() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user exists in database
        userDAO.getUserByEmail(email, new UserDAO.SingleUserCallback() {
            @Override
            public void onCallback(User user) {
                if (user == null) {
                    Toast.makeText(LoginActivity.this, "Email/password doesn't exist", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verify password (in a real app, you'd use secure hashing)
                String hashedPassword = Utils.hashPassword(password); // You need to implement this method
                if (user.getPasswordHash() != null && user.getPasswordHash().equals(hashedPassword)) {
                    // Update last login date
                    user.setLastLoginDate(new java.util.Date());
                    userDAO.updateUser(user, new UserDAO.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            // Save user ID for future sessions
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(KEY_USER_ID, user.getId());
                            editor.apply();

                            Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                            goToMainActivity(user.getId());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to update user login date", e);
                            // Still proceed to main activity even if update fails
                            goToMainActivity(user.getId());
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error checking user by email", e);
                Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        checkOrCreateGoogleUser(firebaseUser, account);
                    }
                } else {
                    // Sign in fails
                    Log.w(TAG, "Firebase auth with Google failed", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void checkOrCreateGoogleUser(FirebaseUser firebaseUser, GoogleSignInAccount account) {
        final String email = firebaseUser.getEmail();
        final String name = firebaseUser.getDisplayName();
        final String id = firebaseUser.getUid();
        final String profilePictureUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;

        userDAO.getUserByEmail(email, new UserDAO.SingleUserCallback() {
            @Override
            public void onCallback(User user) {
                if (user != null) {
                    // User exists, update ViaGmail flag and last login
                    user.setViaGmail(true);
                    user.setLastLoginDate(new java.util.Date());
                    user.setLastUpdated(new java.util.Date());
                    if (profilePictureUrl != null) {
                        user.setProfilePictureUrl(profilePictureUrl);
                    }

                    userDAO.updateUser(user, new UserDAO.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            saveUserAndGoToMain(user.getId());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to update existing user", e);
                            saveUserAndGoToMain(user.getId());
                        }
                    });
                } else {
                    // User doesn't exist, create new user
                    User newUser = new User(email, name, id, profilePictureUrl, true);

                    userDAO.addUser(newUser, new UserDAO.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(LoginActivity.this, "New user created with Google", Toast.LENGTH_SHORT).show();
                            saveUserAndGoToMain(id);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to create new user", e);
                            Toast.makeText(LoginActivity.this, "Failed to create user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error checking for existing user", e);
                Toast.makeText(LoginActivity.this, "Authentication error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
            // Handle registration logic
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(LoginActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(name, email, password);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void registerUser(String name, String email, String password) {
        // First check if user with this email already exists
        userDAO.getUserByEmail(email, new UserDAO.SingleUserCallback() {
            @Override
            public void onCallback(User existingUser) {
                if (existingUser != null) {
                    Toast.makeText(LoginActivity.this, "Email already in use", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create new user
                String userId = "user_" + System.currentTimeMillis();
                String hashedPassword = Utils.hashPassword(password); // Implement this method
                User newUser = new User(email, name, userId, hashedPassword);

                userDAO.addUser(newUser, new UserDAO.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(LoginActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        saveUserAndGoToMain(userId);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to register user", e);
                        Toast.makeText(LoginActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error checking for existing user", e);
                Toast.makeText(LoginActivity.this, "Registration error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
