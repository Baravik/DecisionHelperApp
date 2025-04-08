package com.decisionhelperapp.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;

import com.OpenU.decisionhelperapp.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MenuHelper {

    /**
     * Updates the user menu icon with the user's profile photo
     * @param context Activity context
     * @param menu The menu containing the user action item
     */
    public static void updateUserMenuIcon(Context context, Menu menu) {
        MenuItem userMenuItem = menu.findItem(R.id.action_user);
        if (userMenuItem != null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            
            if (user != null && user.getPhotoUrl() != null) {
                // Load user profile image into the menu icon
                Glide.with(context)
                    .load(user.getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            userMenuItem.setIcon(resource);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            userMenuItem.setIcon(R.drawable.default_profile);
                        }
                    });
            } else {
                // Set default profile icon
                userMenuItem.setIcon(R.drawable.default_profile);
            }
        }
    }
}
