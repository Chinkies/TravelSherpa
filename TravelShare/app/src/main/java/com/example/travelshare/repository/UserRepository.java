package com.example.travelshare.repository;

import com.example.travelshare.model.User;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_USERS = "users";

    public void getUser(String userId, FireStoreCallBack<User> callback) {
        db.collection(COLLECTION_USERS).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(documentSnapshot.toObject(User.class));
                    } else {
                        callback.onFailure("Utilisateur introuvable");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void fetchUsersFromIds(List<String> ids, FireStoreCallBack<List<User>> callback) {
        if (ids == null || ids.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        db.collection(COLLECTION_USERS)
                .whereIn("id", ids)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> userList = queryDocumentSnapshots.toObjects(User.class);
                    callback.onSuccess(userList);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void searchUsers(String query, FireStoreCallBack<List<User>> callback) {
        db.collection(COLLECTION_USERS)
                .orderBy("pseudo")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(10)
                .get()
                .addOnSuccessListener(snapshots -> callback.onSuccess(snapshots.toObjects(User.class)))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateProfile(String userId, String pseudo, String desc, FireStoreCallBack<Void> callback) {
        db.collection(COLLECTION_USERS).document(userId)
                .update("pseudo", pseudo, "description", desc)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateProfilePicture(String userId, String imageUrl, FireStoreCallBack<Void> callback) {
        db.collection(COLLECTION_USERS).document(userId)
                .update("profilePictureUrl", imageUrl)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateProfileFull(String userId, String pseudo, String desc, String url, FireStoreCallBack<Void> callback) {
        db.collection("users").document(userId)
                .update("pseudo", pseudo, "description", desc, "profilePictureUrl", url)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void toggleFavoriteLieu(String userId, String lieuId, boolean isAdd, FireStoreCallBack<Void> callback) {
        if (isAdd) {
            db.collection(COLLECTION_USERS).document(userId)
                    .update("favoriteLieuIds", FieldValue.arrayUnion(lieuId))
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            db.collection(COLLECTION_USERS).document(userId)
                    .update("favoriteLieuIds", FieldValue.arrayRemove(lieuId))
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        }
    }
}