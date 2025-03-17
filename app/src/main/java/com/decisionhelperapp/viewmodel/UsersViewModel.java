package com.decisionhelperapp.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.decisionhelperapp.database.UserDAO;
import com.decisionhelperapp.models.User;
import com.decisionhelperapp.repository.DecisionRepository;
import java.util.List;

public class UsersViewModel extends AndroidViewModel {

    private MutableLiveData<List<User>> userList = new MutableLiveData<>();
    private MutableLiveData<User> selectedUser = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private DecisionRepository repository;

    public UsersViewModel(Application application) {
        super(application);
        repository = new DecisionRepository(application.getApplicationContext());
    }

    public LiveData<List<User>> getUserList() {
        return userList;
    }

    public LiveData<User> getSelectedUser() {
        return selectedUser;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void setSelectedUser(User user) {
        selectedUser.setValue(user);
    }

    public void loadUsers() {
        isLoading.setValue(true);
        repository.getAllUsers(new UserDAO.UserCallback() {
            @Override
            public void onCallback(List<User> users) {
                userList.setValue(users);
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to load users: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void getUserById(String userId) {
        isLoading.setValue(true);
        repository.getUserById(userId, new UserDAO.SingleUserCallback() {
            @Override
            public void onCallback(User user) {
                selectedUser.setValue(user);
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to load user: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void addUser(User user) {
        isLoading.setValue(true);
        repository.addUser(user, new UserDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                // Reload the user list after adding
                loadUsers();
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to add user: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void updateUser(User user) {
        isLoading.setValue(true);
        repository.updateUser(user, new UserDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                // Reload the user list after updating
                loadUsers();
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to update user: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void deleteUser(String userId) {
        isLoading.setValue(true);
        repository.deleteUser(userId, new UserDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                // Reload the user list after deletion
                loadUsers();
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to delete user: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }
}