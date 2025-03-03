package com.decisionhelperapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.decisionhelperapp.database.UserDAO;
import com.decisionhelperapp.models.User;
import java.util.List;

public class UsersViewModel extends ViewModel {

    private MutableLiveData<List<User>> userList = new MutableLiveData<>();
    private MutableLiveData<User> selectedUser = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private UserDAO userDAO = new UserDAO();

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
        userDAO.getAllUsers(new UserDAO.UserCallback() {
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
        userDAO.getUserById(userId, new UserDAO.SingleUserCallback() {
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
        userDAO.addUser(user, new UserDAO.ActionCallback() {
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
        userDAO.updateUser(user, new UserDAO.ActionCallback() {
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
        userDAO.deleteUser(userId, new UserDAO.ActionCallback() {
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