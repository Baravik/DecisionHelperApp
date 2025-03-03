package com.decisionhelperapp.activities;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.OpenU.decisionhelperapp.R;
import com.decisionhelperapp.viewmodel.AdminViewModel;

import com.decisionhelperapp.activities.BaseActivity;

public class AdminActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Removed direct DB/business logic calls
        // Initialize AdminViewModel
        AdminViewModel adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        // Removed the reference to adminStatusTextView since it does not exist in layout
        // Removed: TextView statusTextView = findViewById(R.id.adminStatusTextView);
        // Removed: adminViewModel.getStatus().observe(this, status -> { statusTextView.setText(status); });

        // TODO: Add functionality for managing questions/quizzes
    }
}