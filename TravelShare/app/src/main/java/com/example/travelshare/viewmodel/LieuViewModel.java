package com.example.travelshare.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelshare.model.Lieu;
import com.example.travelshare.model.Post;
import com.example.travelshare.repository.FireStoreCallBack;
import com.example.travelshare.repository.LieuRepository;
import com.example.travelshare.repository.PostRepository;
import com.example.travelshare.repository.UserRepository;

import java.util.List;

public class LieuViewModel extends ViewModel {
    private final LieuRepository lieuRepository = new LieuRepository();
    private final PostRepository postRepository = new PostRepository();
    private final UserRepository userRepository = new UserRepository();
    
    private MutableLiveData<Lieu> selectedLieu = new MutableLiveData<>();
    private MutableLiveData<List<Post>> lieuPosts = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<Lieu> getSelectedLieu() { return selectedLieu; }
    public LiveData<List<Post>> getLieuPosts() { return lieuPosts; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadLieu(String lieuId) {
        if (lieuId == null || lieuId.isEmpty()) return;
        
        isLoading.setValue(true);
        lieuRepository.getLieuById(lieuId, new FireStoreCallBack<Lieu>() {
            @Override
            public void onSuccess(Lieu result) {
                selectedLieu.postValue(result);
                loadPostsForLieu(lieuId);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    private void loadPostsForLieu(String lieuId) {
        postRepository.fetchPostsByLieu(lieuId, new FireStoreCallBack<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                lieuPosts.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    public void toggleFavorite(String userId, String lieuId, boolean isAdd) {
        userRepository.toggleFavoriteLieu(userId, lieuId, isAdd, new FireStoreCallBack<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Success
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
            }
        });
    }

    public void selectLieu(Lieu lieu) {
        selectedLieu.setValue(lieu);
        if (lieu != null) loadPostsForLieu(lieu.getId());
    }
}
