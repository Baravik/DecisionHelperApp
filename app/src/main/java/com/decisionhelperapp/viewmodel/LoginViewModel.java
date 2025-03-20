package com.decisionhelperapp.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.decisionhelperapp.database.UserDAO;
import com.decisionhelperapp.models.User;
import com.decisionhelperapp.repository.DecisionRepository;
import com.decisionhelperapp.utils.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginViewModel extends AndroidViewModel {
    private static final String TAG = "LoginViewModel";

    private MutableLiveData<User> loggedInUser = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isAuthSuccessful = new MutableLiveData<>(false);
    
    private DecisionRepository repository;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public LoginViewModel(Application application) {
        super(application);
        repository = new DecisionRepository(application.getApplicationContext());
    }

    // LiveData getters
    public LiveData<User> getLoggedInUser() {
        return loggedInUser;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsAuthSuccessful() {
        return isAuthSuccessful;
    }

    // Login with email and password
    public void loginWithEmailPassword(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            errorMessage.setValue("Please enter email and password");
            return;
        }

        isLoading.setValue(true);
        
        // Check if user exists in database using repository
        repository.getUserByEmail(email, new UserDAO.SingleUserCallback() {
            @Override
            public void onCallback(User user) {
                if (user == null) {
                    errorMessage.setValue("Email/password doesn't exist");
                    isLoading.setValue(false);
                    return;
                }

                // Verify password
                String hashedPassword = Utils.hashPassword(password);
                if (user.getPasswordHash() != null && user.getPasswordHash().equals(hashedPassword)) {
                    // Update last login date
                    user.setLastLoginDate(new java.util.Date());
                    repository.updateUser(user, new UserDAO.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            loggedInUser.setValue(user);
                            isAuthSuccessful.setValue(true);
                            isLoading.setValue(false);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to update user login date", e);
                            // Still proceed with login even if update fails
                            loggedInUser.setValue(user);
                            isAuthSuccessful.setValue(true);
                            isLoading.setValue(false);
                        }
                    });
                } else {
                    errorMessage.setValue("Invalid password");
                    isLoading.setValue(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error checking user by email", e);
                errorMessage.setValue("Login failed: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    // Handle Google Sign-In
    public void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        isLoading.setValue(true);
        
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Sign in success
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        checkOrCreateGoogleUser(firebaseUser, account);
                    }
                } else {
                    // Sign in fails
                    Log.w(TAG, "Firebase auth with Google failed", task.getException());
                    errorMessage.setValue("Authentication Failed");
                    isLoading.setValue(false);
                }
            });
    }

    private void checkOrCreateGoogleUser(FirebaseUser firebaseUser, GoogleSignInAccount account) {
        final String email = firebaseUser.getEmail();
        final String name = firebaseUser.getDisplayName();
        final String id = firebaseUser.getUid();
        final String profilePictureUrl = account.getPhotoUrl() != null ? 
                                        account.getPhotoUrl().toString() : null;

        repository.getUserByEmail(email, new UserDAO.SingleUserCallback() {
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

                    repository.updateUser(user, new UserDAO.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            loggedInUser.setValue(user);
                            isAuthSuccessful.setValue(true);
                            isLoading.setValue(false);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to update existing user", e);
                            // Still proceed with login
                            loggedInUser.setValue(user);
                            isAuthSuccessful.setValue(true);
                            isLoading.setValue(false);
                        }
                    });
                } else {
                    // User doesn't exist, create new user
                    User newUser = new User(email, name, id, profilePictureUrl, true);

                    repository.addUser(newUser, new UserDAO.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            loggedInUser.setValue(newUser);
                            isAuthSuccessful.setValue(true);
                            isLoading.setValue(false);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to create new user", e);
                            errorMessage.setValue("Failed to create user: " + e.getMessage());
                            isLoading.setValue(false);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error checking for existing user", e);
                errorMessage.setValue("Authentication error: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    // Register a new user
    public void registerUser(String name, String email, String password, String confirmPassword) {
        if (name == null || name.isEmpty() || 
            email == null || email.isEmpty() || 
            password == null || password.isEmpty()) {
            errorMessage.setValue("All fields are required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("Passwords don't match");
            return;
        }

        isLoading.setValue(true);
        
        // First check if user with this email already exists
        repository.getUserByEmail(email, new UserDAO.SingleUserCallback() {
            @Override
            public void onCallback(User existingUser) {
                if (existingUser != null) {
                    errorMessage.setValue("Email already in use");
                    isLoading.setValue(false);
                    return;
                }

                // Create new user
                String userId = "user_" + System.currentTimeMillis();
                String hashedPassword = Utils.hashPassword(password);
                User newUser = new User(email, name, userId, hashedPassword);

                repository.addUser(newUser, new UserDAO.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        loggedInUser.setValue(newUser);
                        isAuthSuccessful.setValue(true);
                        isLoading.setValue(false);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to register user", e);
                        errorMessage.setValue("Registration failed: " + e.getMessage());
                        isLoading.setValue(false);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error checking for existing user", e);
                errorMessage.setValue("Registration error: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }
}