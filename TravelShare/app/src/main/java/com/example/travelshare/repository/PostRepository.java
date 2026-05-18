package com.example.travelshare.repository;

import com.example.travelshare.model.Post;
import com.example.travelshare.model.Tags;
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

            db.collection("users").document(post.getAuthorId())
                    .update("nbPublications", FieldValue.increment(1));

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
                .whereEqualTo("public", true)
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

    public void fetchPostsByLieu(String lieuId, FireStoreCallBack<List<Post>> callback) {
        db.collection(COLLECTION_POSTS)
                .whereEqualTo("lieuId", lieuId)
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

    public void toggleLike(Post post, String userId, boolean isLiked, FireStoreCallBack<Void> callBack) {
        DocumentReference postReference = db.collection(COLLECTION_POSTS).document(post.getId());
        DocumentReference authorReference = db.collection("users").document(post.getAuthorId());

        if (isLiked) {
            postReference.update(
                    "likesCount", FieldValue.increment(1),
                    "likers", FieldValue.arrayUnion(userId)
            ).addOnSuccessListener(v -> {
                authorReference.update("nbLikes", FieldValue.increment(1));
                callBack.onSuccess(null);
            });
        } else {
            postReference.update(
                    "likesCount", FieldValue.increment(-1),
                    "likers", FieldValue.arrayRemove(userId)
            ).addOnSuccessListener(v -> {
                authorReference.update("nbLikes", FieldValue.increment(-1));
                callBack.onSuccess(null);
            });
        }
    }

    public void fetchGroupPosts(String groupId, FireStoreCallBack<List<Post>> callback) {
        db.collection(COLLECTION_POSTS)
                .whereArrayContains("groupIds", groupId)
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

    public void fetchPostsByTag(String tag, FireStoreCallBack<List<Post>> callback) {
        db.collection("posts")
                .whereEqualTo("public", true)
                .whereArrayContains("tags", tag)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        posts.add(doc.toObject(Post.class));
                    }
                    callback.onSuccess(posts);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void fetchOfficialTags(FireStoreCallBack<List<String>> callback) {
        db.collection("tags").document("tags")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Tags tags = documentSnapshot.toObject(Tags.class);

                        if (tags != null) {
                            callback.onSuccess(tags.getTags());
                        } else {
                            callback.onSuccess(new ArrayList<>());
                        }
                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
