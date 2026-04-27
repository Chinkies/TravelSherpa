package com.example.travelshare.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.travelshare.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRespository {

    private FirebaseAuth mAuth;

    public AuthRespository() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void observeAuthState(MutableLiveData<FirebaseUser> userData) {
        mAuth.addAuthStateListener(firebaseAuth -> {
            userData.postValue(firebaseAuth.getCurrentUser());
        });
    }

    public void login(String email, String password, FireStoreCallBack callBack) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callBack.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callBack.onFailure(task.getException().getMessage());
                    }
                });
    }

    public void register(String email, String password, String pseudo, FireStoreCallBack callBack) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        User newUser = new User(user.getUid(), pseudo, "Nouveau voyageur",
                                "https://picsum.photos/200", 0, 0, 0);

                        FirebaseFirestore.getInstance().collection("users")
                                .document(user.getUid())
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> callBack.onSuccess(user))
                                .addOnFailureListener(e -> callBack.onFailure("Erreur Firestore: " + e.getMessage()));
                    } else {
                        callBack.onFailure(task.getException().getMessage());
                    }
                });
    }

    public void logout() {
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() { return mAuth.getCurrentUser(); }
}
