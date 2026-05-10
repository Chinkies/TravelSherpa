package com.example.travelshare.viewmodel;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.travelshare.model.Post;
import com.example.travelshare.repository.FireStoreCallBack;
import com.example.travelshare.repository.PostRepository;
import com.example.travelshare.repository.StorageRepository;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PostViewModel extends ViewModel {
    private final PostRepository postRepository = new PostRepository();
    private StorageRepository storageRepository = new StorageRepository();

    private MutableLiveData<List<Post>> posts = new MutableLiveData<>();
    private MutableLiveData<Boolean> postCreated = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Post> selectedPost = new MutableLiveData<>();


    public LiveData<List<Post>> getPosts() { return posts; }
    public LiveData<Boolean> getPostCreated() { return postCreated; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Post> getSelectedPost() { return selectedPost; }

    public void selectPost(Post post) {
        selectedPost.setValue(post);
    }
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

    public void resetPostCreated() {
        postCreated.setValue(false);
    }

    public void publishPost(Post post, android.net.Uri imageUri, String address, Context context) {
        isLoading.setValue(true);

        if (address != null && !address.isEmpty()) {
            GeoPoint location = getLocationFromAddress(address, context);
            post.setLocation(location);
        }

        if (imageUri != null) {
            storageRepository.uploadGroupImage(imageUri, post.getAuthorId() + "_" + System.currentTimeMillis(), new FireStoreCallBack<String>() {
                @Override
                public void onSuccess(String imageUrl) {
                    post.setImageUrl(imageUrl);
                    savePostToFirestore(post);
                }
                @Override
                public void onFailure(String e) {
                    isLoading.postValue(false);
                    errorMessage.postValue(e);
                }
            });
        } else {
            savePostToFirestore(post);
        }
    }

    private GeoPoint getLocationFromAddress(String strAddress, Context context) {
        if (strAddress == null || strAddress.isEmpty()) return null;

        android.location.Geocoder coder = new android.location.Geocoder(context);
        try {
            List<android.location.Address> address = coder.getFromLocationName(strAddress, 1);
            if (address != null && !address.isEmpty()) {
                android.location.Address loc = address.get(0);
                return new GeoPoint(loc.getLatitude(), loc.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public void toggleLike(Post post, String userId) {
        if (post.getLikers() == null) {
            post.setLikers(new ArrayList<>());
        }

        boolean alreadyLiked = post.getLikers().contains(userId);

        if (alreadyLiked) {
            post.getLikers().remove(userId);
            post.setLikesCount(post.getLikesCount() - 1);
        } else {
            post.getLikers().add(userId);
            post.setLikesCount(post.getLikesCount() + 1);
        }

        selectedPost.setValue(post);

        List<Post> currentPosts = posts.getValue();
        if (currentPosts != null) {
            posts.setValue(currentPosts);
        }

        postRepository.toggleLike(post.getId(), userId, !alreadyLiked, new FireStoreCallBack<Void>() {
            @Override
            public void onSuccess(Void result) {}

            @Override
            public void onFailure(String e) {
                errorMessage.postValue("Erreur lors du like : " + e);
            }
        });
    }
}