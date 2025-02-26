package com.decisionhelperapp.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.decisionhelperapp.R;
import com.decisionhelperapp.viewmodel.AdminViewModel;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Removed direct DB/business logic calls
        // Initialize AdminViewModel
        AdminViewModel adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        // Example: Update a TextView with the admin status
        TextView statusTextView = findViewById(R.id.adminStatusTextView);
        adminViewModel.getStatus().observe(this, status -> {
            statusTextView.setText(status);
        });

        // TODO: Add functionality for managing questions/quizzes
    }
}