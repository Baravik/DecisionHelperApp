package com.decisionhelperapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.decisionhelperapp.adapters.UserAdapter;
import com.decisionhelperapp.models.User;
import com.decisionhelperapp.viewmodel.UsersViewModel;
import com.OpenU.decisionhelperapp.R;

public class UsersActivity extends BaseActivity implements UserAdapter.OnUserClickListener {
    
    private UsersViewModel usersViewModel;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        // Initialize views
        recyclerView = findViewById(R.id.usersRecyclerView);
        progressBar = findViewById(R.id.usersProgressBar);
        emptyView = findViewById(R.id.usersEmptyView);
        
        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize ViewModel
        usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);
        
        // Observe the users list
        usersViewModel.getUserList().observe(this, users -> {
            if (users != null && !users.isEmpty()) {
                userAdapter = new UserAdapter(users, this);
                recyclerView.setAdapter(userAdapter);
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
        
        // Observe error messages
        usersViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe loading state
        usersViewModel.getIsLoading().observe(this, isLoading -> progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));
        
        // Load users data
        usersViewModel.loadUsers();
    }

    @Override
    public void onUserClick(User user) {
        // Handle user click - e.g., show details, edit, etc.
        usersViewModel.setSelectedUser(user);
        Toast.makeText(this, "Selected user: " + user.getName(), Toast.LENGTH_SHORT).show();
    }
}