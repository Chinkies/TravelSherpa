package com.example.travelshare.repository;

import com.example.travelshare.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class NotificationRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String COLLECTION_NOTIFICATIONS = "notifications";

    public void createNotification(Notification notification, FireStoreCallBack<String> callback) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    String id = documentReference.getId();
                    documentReference.update("id", id);
                    callback.onSuccess(id);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void fetchNotificationsForUser(String userId, FireStoreCallBack<List<Notification>> callback) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("receiverId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(queryDocumentSnapshots.toObjects(Notification.class));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void markAsRead(String notificationId) {
        db.collection(COLLECTION_NOTIFICATIONS).document(notificationId)
                .update("read", true);
    }
}
