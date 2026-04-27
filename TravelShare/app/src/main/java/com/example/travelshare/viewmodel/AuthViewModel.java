package com.example.travelshare.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelshare.repository.FireStoreCallBack;
import com.example.travelshare.repository.AuthRespository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {

    private AuthRespository authRepository;

    private MutableLiveData<FirebaseUser> userData = new MutableLiveData<>();
    private MutableLiveData<String> errorData = new MediatorLiveData<>();

    public AuthViewModel() {
        authRepository = new AuthRespository();
        authRepository.observeAuthState(userData);
    }

    public LiveData<FirebaseUser> getUser() { return userData; }
    public LiveData<String> getError() { return errorData; }
    public FirebaseUser getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    public void login(String email, String password) {
        authRepository.login(email, password, new FireStoreCallBack<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                userData.postValue(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                errorData.postValue(errorMessage);
            }
        });
    }

    public void register(String email, String password, String pseudo) {
        authRepository.register(email, password, pseudo, new FireStoreCallBack<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                userData.postValue(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                errorData.postValue(errorMessage);
            }
        });
    }

    public void logout() {
        authRepository.logout();
        userData.postValue(null);
    }
}
