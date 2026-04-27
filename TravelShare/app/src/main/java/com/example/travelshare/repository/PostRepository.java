package com.example.travelshare.repository;

import com.example.travelshare.model.Post;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String COLLECTION_POSTS = "posts";

    public void createPost(Post post, FireStoreCallBack<String> callback) {
        DocumentReference ref = db.collection(COLLECTION_POSTS).document();
        post.setId(ref.getId());

        ref.set(post).addOnSuccessListener(aVoid -> {
            if (post.getGroupIds() != null && !post.getGroupIds().isEmpty()) {
                for (String groupId : post.getGroupIds()) {
                    db.collection("groups").document(groupId)
                            .update(
                                    "publications", FieldValue.arrayUnion(post.getId()),
                                    "lastActivityDate", new Date()
                            );
                }
            }
            callback.onSuccess(post.getId());
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void fetchUserPosts(String userId, FireStoreCallBack<List<Post>> callback) {
        db.collection(COLLECTION_POSTS)
                .whereEqualTo("authorId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> postList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        postList.add(doc.toObject(Post.class));
                    }
                    callback.onSuccess(postList);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void fetchFeedPosts(FireStoreCallBack<List<Post>> callback) {
        db.collection(COLLECTION_POSTS)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> postList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Post post = doc.toObject(Post.class);
                        postList.add(post);
                    }
                    callback.onSuccess(postList);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updatePost(Post post, FireStoreCallBack<Void> callback) {
        db.collection(COLLECTION_POSTS).document(post.getId())
                .set(post)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deletePost(String postId, FireStoreCallBack<Void> callback) {
        db.collection(COLLECTION_POSTS).document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
