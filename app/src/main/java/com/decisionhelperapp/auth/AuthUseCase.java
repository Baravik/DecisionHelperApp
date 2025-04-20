package com.decisionhelperapp.auth;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.decisionhelperapp.database.UserDAO;
import com.decisionhelperapp.models.User;
import com.decisionhelperapp.repository.DecisionRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.*;

import java.text.MessageFormat;
import java.util.Objects;

public class AuthUseCase {

    private final DecisionRepository repository;
    private final FirebaseAuth mAuth;

    private final MutableLiveData<User> _loggedInUser = new MutableLiveData<>();
    public final LiveData<User> loggedInUser = _loggedInUser;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    public AuthUseCase(DecisionRepository repository) {
        this.repository = repository;
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            _errorMessage.postValue("Please enter email and password");
            return;
        }

        _isLoading.postValue(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            fetchUser(firebaseUser.getUid());
                        } else {
                            _errorMessage.postValue("Login failed: No user data");
                            _isLoading.postValue(false);
                        }
                    } else {
                        _errorMessage.postValue("Login failed: " + Objects.requireNonNull(task.getException()).getMessage());
                        _isLoading.postValue(false);
                    }
                });
    }

    private void fetchUser(String uid) {
        Log.d("AuthUseCase", "🔍 fetchUser called with uid: " + uid);
        repository.getUserById(uid, new UserDAO.SingleUserCallback() {
            @Override
            public void onCallback(User user) {
                if (user != null) {
                    Log.d("AuthUseCase", "✅ user found in Firestore: " + user.getEmail());
                    repository.updateUser(user, new UserDAO.ActionCallback() {
                        public void onSuccess() {
                            Log.d("AuthUseCase", "📤 user updated");
                            _loggedInUser.postValue(user);
                            _isLoading.postValue(false);
                        }

                        public void onFailure(Exception e) {
                            Log.e("AuthUseCase", "⚠️ user update failed", e);
                            _loggedInUser.postValue(user);
                            _isLoading.postValue(false);
                        }
                    });
                } else {
                    Log.w("AuthUseCase", "❌ user not found in Firestore. Creating fallback user.");

                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        User fallbackUser = new User(
                                firebaseUser.getEmail(),
                                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User",
                                firebaseUser.getUid()
                        );

                        repository.addUser(fallbackUser, new UserDAO.ActionCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("AuthUseCase", "✅ fallback user created in Firestore");
                                _loggedInUser.postValue(fallbackUser);
                                _isLoading.postValue(false);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("AuthUseCase", "❌ failed to create fallback user", e);
                                _errorMessage.postValue("Failed to create user record");
                                _isLoading.postValue(false);
                            }
                        });
                    } else {
                        _errorMessage.postValue("Authentication error: no Firebase user available");
                        _isLoading.postValue(false);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AuthUseCase", "🚨 Firestore error", e);
                _errorMessage.postValue("Firestore error: " + e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    public void register(String name, String email, String password) {
        _isLoading.postValue(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User newUser = new User(email, name, firebaseUser.getUid());

                            repository.addUser(newUser, new UserDAO.ActionCallback() {
                                public void onSuccess() {
                                    _loggedInUser.postValue(newUser);
                                    _isLoading.postValue(false);
                                }

                                public void onFailure(Exception e) {
                                    _errorMessage.postValue("Registration failed: " + e.getMessage());
                                    _isLoading.postValue(false);
                                }
                            });
                        } else {
                            _errorMessage.postValue("Registration failed: No user info returned");
                            _isLoading.postValue(false);
                        }
                    } else {
                        _errorMessage.postValue(MessageFormat.format("Auth Error: {0}", Objects.requireNonNull(task.getException()).getMessage()));
                        _isLoading.postValue(false);
                    }
                });
    }

    public void loginWithGoogle(GoogleSignInAccount account) {
        _isLoading.postValue(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String email = firebaseUser.getEmail();
                            String name = firebaseUser.getDisplayName();
                            String uid = firebaseUser.getUid();
                            String profileUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "";

                            User user = new User(email, name, uid, profileUrl);
                            repository.addUser(user, new UserDAO.ActionCallback() {
                                public void onSuccess() {
                                    _loggedInUser.postValue(user);
                                    _isLoading.postValue(false);
                                }

                                public void onFailure(Exception e) {
                                    _errorMessage.postValue("Failed to create user");
                                    _isLoading.postValue(false);
                                }
                            });
                        } else {
                            _errorMessage.postValue("Google sign-in failed: No user info returned");
                            _isLoading.postValue(false);
                        }
                    } else {
                        _errorMessage.postValue("Google Sign-In failed");
                        _isLoading.postValue(false);
                    }
                });
    }
}