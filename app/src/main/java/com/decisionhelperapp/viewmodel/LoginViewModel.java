package com.decisionhelperapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.decisionhelperapp.auth.AuthUseCase;
import com.decisionhelperapp.models.User;
import com.decisionhelperapp.repository.DecisionRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class LoginViewModel extends AndroidViewModel {

    private final AuthUseCase authUseCase;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        DecisionRepository repository = new DecisionRepository(application.getApplicationContext());
        authUseCase = new AuthUseCase(repository);
    }

    public void login(String email, String password) {
        authUseCase.login(email, password);
    }

    public void register(String name, String email, String password) {
        authUseCase.register(name, email, password);
    }

    public void loginWithGoogle(GoogleSignInAccount account) {
        authUseCase.loginWithGoogle(account);
    }

    public LiveData<User> getUser() {
        return authUseCase.loggedInUser;
    }

    public LiveData<String> getError() {
        return authUseCase.errorMessage;
    }

    public LiveData<Boolean> getLoading() {
        return authUseCase.isLoading;
    }
}