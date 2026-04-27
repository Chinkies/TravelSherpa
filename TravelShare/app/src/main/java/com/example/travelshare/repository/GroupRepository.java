package com.example.travelshare.repository;

import com.example.travelshare.model.Group;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String COLLECTION_GROUPS = "groups";

    public void createGroup(Group group, FireStoreCallBack<String> callback) {
        com.google.firebase.firestore.DocumentReference ref = db.collection(COLLECTION_GROUPS).document();
        group.setGroupId(ref.getId());
        ref.set(group)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(ref.getId());
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateLastPostDate(String groupId){
        db.collection(COLLECTION_GROUPS).document(groupId)
                .update("lastActivityDate", new Date());
    }

    public void fetchGroups(FireStoreCallBack<List<Group>> callBack) {
        db.collection(COLLECTION_GROUPS)
                .orderBy("lastActivityDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Group> groupList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Group group = doc.toObject(Group.class);
                        groupList.add(group);
                    }
                    callBack.onSuccess(groupList);
                })
                .addOnFailureListener(e -> callBack.onFailure(e.getMessage()));
    }

    public void fetchUserGroups(String userId, FireStoreCallBack<List<Group>> callback) {
        db.collection(COLLECTION_GROUPS)
                .whereEqualTo("members." + userId, true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Group> userGroups = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Group group = doc.toObject(Group.class);
                        userGroups.add(group);
                    }
                    callback.onSuccess(userGroups);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateGroup(Group group, FireStoreCallBack<Void> callback) {
        db.collection("groups").document(group.getGroupId())
                .set(group)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteGroup(String groupId, FireStoreCallBack<Void> callback) {
        db.collection("groups").document(groupId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
