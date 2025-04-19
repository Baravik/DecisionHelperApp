package com.decisionhelperapp.auth;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;

import com.OpenU.decisionhelperapp.R;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class GoogleSignInHelper {

    private final GoogleSignInClient googleSignInClient;

    public interface SignInCallback {
        void onSuccess(GoogleSignInAccount account);
        void onError(Exception e);
    }

    public GoogleSignInHelper(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public void launchSignIn(ActivityResultLauncher<Intent> launcher) {
        launcher.launch(googleSignInClient.getSignInIntent());
    }

    public void handleSignInResult(Intent data, SignInCallback callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            callback.onSuccess(account);
        } catch (ApiException e) {
            callback.onError(e);
        }
    }
}