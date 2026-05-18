package com.example.travelshare.repository;

import com.example.travelshare.model.Lieu;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.List;

public class LieuRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String COLLECTION_LIEUX = "lieux";

    public void saveLieu(Lieu lieu, FireStoreCallBack<Void> callback) {
        if (lieu.getId() == null || lieu.getId().isEmpty()) {
            callback.onFailure("ID du lieu manquant");
            return;
        }

        db.collection(COLLECTION_LIEUX).document(lieu.getId())
                .set(lieu, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void saveLieux(List<Lieu> lieux, FireStoreCallBack<Void> callback) {
        if (lieux == null || lieux.isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        for (int i = 0; i < lieux.size(); i++) {
            final int index = i;
            saveLieu(lieux.get(i), new FireStoreCallBack<Void>() {
                @Override
                public void onSuccess(Void result) {
                    if (index == lieux.size() - 1) {
                        callback.onSuccess(null);
                    }
                }

                @Override
                public void onFailure(String error) {
                    callback.onFailure(error);
                }
            });
        }
    }

    public void getLieuById(String id, FireStoreCallBack<Lieu> callback) {
        db.collection(COLLECTION_LIEUX).document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(documentSnapshot.toObject(Lieu.class));
                    } else {
                        callback.onFailure("Lieu non trouvé");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
