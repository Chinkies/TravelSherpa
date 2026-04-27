package com.example.travelshare.repository;

import com.google.firebase.auth.FirebaseUser;

public interface FireStoreCallBack<T> {
    void onSuccess(T user);
    void onFailure(String errorMessage);
}
