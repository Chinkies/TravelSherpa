package com.example.travelshare.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.travelshare.model.Post;
import com.example.travelshare.repository.FireStoreCallBack;
import com.example.travelshare.repository.PostRepository;
import com.example.travelshare.repository.StorageRepository;

import java.util.List;

public class PostViewModel extends ViewModel {
    private final PostRepository postRepository = new PostRepository();
    private StorageRepository storageRepository = new StorageRepository();

    private MutableLiveData<List<Post>> posts = new MutableLiveData<>();
    private MutableLiveData<Boolean> postCreated = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();


    public LiveData<List<Post>> getPosts() { return posts; }
    public LiveData<Boolean> getPostCreated() { return postCreated; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

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

    public void publishPost(Post post, android.net.Uri imageUri) {
        isLoading.setValue(true);

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
}