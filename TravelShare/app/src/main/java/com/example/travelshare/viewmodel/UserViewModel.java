package com.example.travelshare.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelshare.model.Group;
import com.example.travelshare.model.Post;
import com.example.travelshare.model.User;
import com.example.travelshare.repository.FireStoreCallBack;
import com.example.travelshare.repository.GroupRepository;
import com.example.travelshare.repository.ParcoursRepository;
import com.example.travelshare.repository.PostRepository;
import com.example.travelshare.repository.StorageRepository;
import com.example.travelshare.repository.UserRepository;
import com.example.travelshare.travelpath.Parcours;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserViewModel extends ViewModel {
    private final UserRepository userRepository = new UserRepository();
    private final PostRepository postRepository = new PostRepository();
    private final GroupRepository groupRepository = new GroupRepository();
    private final StorageRepository storageRepository = new StorageRepository();
    private final ParcoursRepository parcoursRepository = new ParcoursRepository();

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<User> selectedUser = new MutableLiveData<>();
    private final MutableLiveData<List<User>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<List<Post>> userPostData = new MutableLiveData<>();
    private final MutableLiveData<List<Group>> userGroupData = new MutableLiveData<>();
    private final MutableLiveData<List<Parcours>> userParcoursData = new MutableLiveData<>();

    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<User> getSelectedUser() { return selectedUser; }
    public LiveData<List<User>> getSearchResults() { return searchResults; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<List<Post>> getUserPost() { return userPostData; }
    public LiveData<List<Group>> getUserGroup() { return userGroupData; }
    public LiveData<List<Parcours>> getUserParcours() { return userParcoursData; }

    public void loadCurrentUser(String userId) {
        userRepository.getUser(userId, new FireStoreCallBack<User>() {
            @Override
            public void onSuccess(User user) {
                currentUser.postValue(user);
            }
            @Override
            public void onFailure(String e) {
                errorMessage.postValue(e);
            }
        });
    }

    public void loadUser(String userId) {
        isLoading.setValue(true);
        userRepository.getUser(userId, new FireStoreCallBack<User>() {
            @Override
            public void onSuccess(User user) {
                selectedUser.postValue(user);
                fetchUserContent(userId);
                isLoading.postValue(false);
            }
            @Override
            public void onFailure(String e) {
                errorMessage.postValue(e);
                isLoading.postValue(false);
            }
        });
    }

    private void fetchUserContent(String userId) {
        postRepository.fetchUserPosts(userId, new FireStoreCallBack<List<Post>>() {
            @Override
            public void onSuccess(List<Post> posts) { userPostData.postValue(posts); }
            @Override
            public void onFailure(String e) { errorMessage.postValue(e); }
        });

        groupRepository.fetchUserGroups(userId, new FireStoreCallBack<List<Group>>() {
            @Override
            public void onSuccess(List<Group> groups) { userGroupData.postValue(groups); }
            @Override
            public void onFailure(String e) { errorMessage.postValue(e); }
        });

        fetchUserParcours(userId);
    }

    public void fetchUserParcours(String userId) {
        isLoading.setValue(true);
        parcoursRepository.fetchUserParcours(userId, new FireStoreCallBack<List<Parcours>>() {
            @Override
            public void onSuccess(List<Parcours> parcoursList) {
                userParcoursData.postValue(parcoursList);
                isLoading.postValue(false);
            }
            @Override
            public void onFailure(String e) {
                errorMessage.postValue(e);
                isLoading.postValue(false);
            }
        });
    }

    public void toggleFollowUser(String userId, String targetUserId, boolean isFollow) {
        userRepository.toggleFollowedUser(userId, targetUserId, isFollow, new FireStoreCallBack<Void>() {
            @Override public void onSuccess(Void result) { loadCurrentUser(userId); }
            @Override public void onFailure(String e) { errorMessage.postValue(e); }
        });
    }

    public void toggleFollowGroup(String userId, String groupId, boolean isFollow) {
        userRepository.toggleFollowedGroup(userId, groupId, isFollow, new FireStoreCallBack<Void>() {
            @Override public void onSuccess(Void result) { loadCurrentUser(userId); }
            @Override public void onFailure(String e) { errorMessage.postValue(e); }
        });
    }

    public void toggleFollowTag(String userId, String tag, boolean isFollow) {
        userRepository.toggleFollowedTag(userId, tag, isFollow, new FireStoreCallBack<Void>() {
            @Override public void onSuccess(Void result) { loadCurrentUser(userId); }
            @Override public void onFailure(String e) { errorMessage.postValue(e); }
        });
    }

    public void toggleFavoriteLieu(String userId, String lieuId, boolean isAdd) {
        userRepository.toggleFavoriteLieu(userId, lieuId, isAdd, new FireStoreCallBack<Void>() {
            @Override public void onSuccess(Void result) { loadCurrentUser(userId); }
            @Override public void onFailure(String e) { errorMessage.postValue(e); }
        });
    }

    public void searchUsers(String query) {
        userRepository.searchUsers(query, new FireStoreCallBack<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                searchResults.postValue(users);
            }
            @Override
            public void onFailure(String e) {
                errorMessage.postValue(e);
            }
        });
    }

    public void updateMyProfile(String userId, String pseudo, String desc) {
        isLoading.setValue(true);
        userRepository.updateProfile(userId, pseudo, desc, new FireStoreCallBack<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadCurrentUser(userId);
                loadUser(userId);
            }
            @Override
            public void onFailure(String e) {
                errorMessage.postValue(e);
                isLoading.postValue(false);
            }
        });
    }
    public void updateProfileWithImage(String userId, String pseudo, String desc, Uri imageUri) {
        isLoading.setValue(true);
        storageRepository.uploadGroupImage(imageUri, userId, new FireStoreCallBack<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                userRepository.updateProfileFull(userId, pseudo, desc, imageUrl, new FireStoreCallBack<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        loadCurrentUser(userId);
                        loadUser(userId);
                    }
                    @Override
                    public void onFailure(String e) { errorMessage.postValue(e); isLoading.postValue(false); }
                });
            }
            @Override
            public void onFailure(String e) { errorMessage.postValue(e); isLoading.postValue(false); }
        });
    }

    public void clearUserData() {
        currentUser.setValue(null);
        selectedUser.setValue(null);
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}
