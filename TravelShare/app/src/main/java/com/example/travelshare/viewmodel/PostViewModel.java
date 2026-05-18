package com.example.travelshare.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelshare.model.Lieu;
import com.example.travelshare.model.Post;
import com.example.travelshare.repository.AuthRespository;
import com.example.travelshare.repository.FireStoreCallBack;
import com.example.travelshare.repository.LieuRepository;
import com.example.travelshare.repository.PostRepository;
import com.example.travelshare.repository.StorageRepository;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PostViewModel extends ViewModel {
    private final PostRepository postRepository = new PostRepository();
    private final LieuRepository lieuRepository = new LieuRepository();
    private final StorageRepository storageRepository = new StorageRepository();
    private final AuthRespository authRespository = new AuthRespository();

    private MutableLiveData<List<Post>> posts = new MutableLiveData<>();
    private MutableLiveData<List<Post>> groupPost = new MutableLiveData<>();
    private MutableLiveData<Boolean> postCreated = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Post> selectedPost = new MutableLiveData<>();
    private MutableLiveData<List<String>> officialTags = new MutableLiveData<>();
    private MutableLiveData<List<Post>> userPosts = new MutableLiveData<>();

    public LiveData<List<Post>> getPosts() { return posts; }
    public LiveData<List<Post>> getGroupPost() { return groupPost; }
    public LiveData<Boolean> getPostCreated() { return postCreated; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Post> getSelectedPost() { return selectedPost; }
    public LiveData<List<String>> getOfficialTags() { return officialTags; }
    public LiveData<List<Post>> getUserPosts() { return userPosts; }

    public void selectPost(Post post) { selectedPost.setValue(post); }

    public void loadFeed() {
        isLoading.setValue(true);
        postRepository.fetchFeedPosts(new FireStoreCallBack<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                posts.postValue(result);
                isLoading.postValue(false);
            }
            @Override
            public void onFailure(String e) {
                isLoading.postValue(false);
                errorMessage.postValue(e);
            }
        });
    }

    public void loadPostsByTag(String tag) {
        isLoading.setValue(true);
        postRepository.fetchPostsByTag(tag, new FireStoreCallBack<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                posts.postValue(result);
                isLoading.postValue(false);
            }
            @Override
            public void onFailure(String e) {
                isLoading.postValue(false);
                errorMessage.postValue(e);
            }
        });
    }

    public void publishPost(Post post, Uri imageUri, String address, Context context) {
        isLoading.setValue(true);

        new Thread(() -> {
            GeoPoint geoPoint = null;
            if (address != null && !address.trim().isEmpty()) {
                geoPoint = getLocationFromAddress(address, context);
                if (geoPoint == null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        isLoading.setValue(false);
                        errorMessage.setValue("Adresse introuvable.");
                    });
                    return;
                }
            }

            final GeoPoint finalGeo = geoPoint;
            new Handler(Looper.getMainLooper()).post(() -> {
                post.setLocation(finalGeo);
                // On sauvegarde l'adresse textuelle dans le post pour faciliter la recherche
                post.setIndication(address); 

                if (finalGeo != null) {
                    String lieuId = "USER_PLACE_" + System.currentTimeMillis();
                    Lieu nouveauLieu = new Lieu(lieuId, address, "Découverte", 0.0, 30, "Facile", true, finalGeo.getLatitude(), finalGeo.getLongitude(), "24h/24");
                    post.setLieuId(lieuId);
                    
                    lieuRepository.saveLieu(nouveauLieu, new FireStoreCallBack<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            handleImageUpload(post, imageUri);
                        }
                        @Override
                        public void onFailure(String error) {
                            handleImageUpload(post, imageUri);
                        }
                    });
                } else {
                    handleImageUpload(post, imageUri);
                }
            });
        }).start();
    }

    private void handleImageUpload(Post post, Uri imageUri) {
        if (imageUri != null) {
            storageRepository.uploadGroupImage(imageUri, post.getAuthorId() + "_" + System.currentTimeMillis(), new FireStoreCallBack<String>() {
                @Override
                public void onSuccess(String imageUrl) {
                    post.setImageUrl(imageUrl);
                    savePostToFirestore(post);
                }
                @Override
                public void onFailure(String e) {
                    isLoading.setValue(false);
                    errorMessage.setValue(e);
                }
            });
        } else {
            savePostToFirestore(post);
        }
    }

    private void savePostToFirestore(Post post) {
        postRepository.createPost(post, new FireStoreCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                postCreated.postValue(true);
                isLoading.postValue(false);
                loadFeed();
            }
            @Override
            public void onFailure(String e) {
                postCreated.postValue(false);
                isLoading.postValue(false);
                errorMessage.postValue(e);
            }
        });
    }

    private GeoPoint getLocationFromAddress(String strAddress, Context context) {
        android.location.Geocoder coder = new android.location.Geocoder(context);
        try {
            List<android.location.Address> address = coder.getFromLocationName(strAddress, 1);
            if (address != null && !address.isEmpty()) {
                return new GeoPoint(address.get(0).getLatitude(), address.get(0).getLongitude());
            }
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public void updatePostCommentCount(String postId, int count) {
        List<Post> currentPosts = posts.getValue();
        if (currentPosts != null) {
            for (Post p : currentPosts) {
                if (p.getId().equals(postId)) {
                    p.setCommentCount(count);
                    break;
                }
            }
            posts.setValue(new ArrayList<>(currentPosts));
        }

        if (selectedPost.getValue() != null && selectedPost.getValue().getId().equals(postId)) {
            Post p = selectedPost.getValue();
            if (p.getCommentCount() == count) return;
            p.setCommentCount(count);
            selectedPost.setValue(p);

            postRepository.updatePost(p, new FireStoreCallBack<Void>() {
                @Override public void onSuccess(Void result) {}
                @Override public void onFailure(String e) { errorMessage.postValue("Erreur update : " + e); }
            });
        }
    }

    public void loadUserPosts(String userId) {
        isLoading.setValue(true);
        postRepository.fetchUserPosts(userId, new FireStoreCallBack<List<Post>>() {
            @Override public void onSuccess(List<Post> result) { userPosts.postValue(result); isLoading.postValue(false); }
            @Override public void onFailure(String e) { isLoading.postValue(false); errorMessage.postValue(e); }
        });
    }

    public void loadGroupPosts(String groupId) {
        isLoading.setValue(true);
        postRepository.fetchGroupPosts(groupId, new FireStoreCallBack<List<Post>>() {
            @Override public void onSuccess(List<Post> result) { groupPost.postValue(result); isLoading.postValue(false); }
            @Override public void onFailure(String e) { isLoading.postValue(false); errorMessage.postValue(e); }
        });
    }

    public void loadOfficialTags() {
        postRepository.fetchOfficialTags(new FireStoreCallBack<List<String>>() {
            @Override public void onSuccess(List<String> result) { officialTags.postValue(result); }
            @Override public void onFailure(String e) { errorMessage.postValue("Erreur tags : " + e); }
        });
    }

    public void resetPostCreated() { postCreated.setValue(false); }

    public void toggleLike(Post post, String userId) {
        if (userId == null) return;
        if (post.getLikers() == null) post.setLikers(new ArrayList<>());
        boolean alreadyLiked = post.getLikers().contains(userId);
        if (alreadyLiked) { post.getLikers().remove(userId); post.setLikesCount(post.getLikesCount() - 1); }
        else { post.getLikers().add(userId); post.setLikesCount(post.getLikesCount() + 1); }
        selectedPost.setValue(post);
        postRepository.toggleLike(post, userId, !alreadyLiked, new FireStoreCallBack<Void>() {
            @Override public void onSuccess(Void result) {}
            @Override public void onFailure(String e) { errorMessage.postValue("Erreur like : " + e); }
        });
    }
}
