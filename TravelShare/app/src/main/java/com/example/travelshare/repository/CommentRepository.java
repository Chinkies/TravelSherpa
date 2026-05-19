package com.example.travelshare.repository;

import com.example.travelshare.model.Comment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommentRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String COLLECTION_COMMENTS = "comments";

    public void addComment(Comment comment, FireStoreCallBack<Void> callback) {
        DocumentReference ref = db.collection(COLLECTION_COMMENTS).document();
        comment.setId(ref.getId());

        ref.set(comment)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteComment(String commentId, FireStoreCallBack<Void> callback) {
        db.collection(COLLECTION_COMMENTS).document(commentId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void fetchCommentsForPost(String postId, FireStoreCallBack<List<Comment>> callback) {
        db.collection(COLLECTION_COMMENTS)
                .whereEqualTo("postId", postId)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        comments.add(doc.toObject(Comment.class));
                    }
                    callback.onSuccess(comments);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
