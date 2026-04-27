package com.example.travelshare.repository;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StorageRepository {

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public void uploadGroupImage(Uri imageUri, String groupId, FireStoreCallBack<String> callback) {
//        StorageReference ref = storage.getReference()
//                .child("images/groups/" + groupId + ".jpg");
//
//        ref.putFile(imageUri)
//                .addOnSuccessListener(taskSnapshot -> {
//                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
//                        callback.onSuccess(uri.toString());
//                    });
//                })
//                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));

        //solution temporaire le temps de trouver où stocker les images
        new android.os.Handler().postDelayed(() -> {
            String fakeUrl = "https://picsum.photos/seed/" + groupId + "/400/400";
            callback.onSuccess(fakeUrl);
        }, 1000);
    }
}
