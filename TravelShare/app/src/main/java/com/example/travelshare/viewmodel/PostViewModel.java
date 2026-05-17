package com.example.travelshare.viewmodel;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.travelshare.model.Post;
import com.example.travelshare.repository.AuthRespository;
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
    private AuthRespository authRespository = new AuthRespository();

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

    public void loadUserPosts(String userId) {
        isLoading.setValue(true);
        postRepository.fetchUserPosts(userId, new FireStoreCallBack<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                userPosts.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(String e) {
                isLoading.postValue(false);
                errorMessage.postValue(e);
            }
        });
    }

    public void loadGroupPosts(String groupId) {
        isLoading.setValue(true);
        postRepository.fetchGroupPosts(groupId, new FireStoreCallBack<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                groupPost.postValue(result);
                isLoading.postValue(false);
            }
            @Override
            public void onFailure(String e) {
                isLoading.postValue(false);
                errorMessage.postValue(e);
            }
        });
    }

    public void loadOfficialTags() {
        postRepository.fetchOfficialTags(new FireStoreCallBack<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                officialTags.postValue(result);
            }

            @Override
            public void onFailure(String e) {
                errorMessage.postValue("Erreur tags : " + e);
            }
        });
    }

    public void resetPostCreated() {
        postCreated.setValue(false);
    }

    public void publishPost(Post post, android.net.Uri imageUri, String address, Context context) {
        isLoading.setValue(true);

        new Thread(() -> {
            GeoPoint location = null;

            if (address != null && !address.trim().isEmpty()) {
                location = getLocationFromAddress(address, context);

                if (location == null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        isLoading.setValue(false);
                        errorMessage.setValue("Adresse introuvable. Soyez plus précis (ex: Ville, Pays ou Rue).");
                    });
                    return;
                }
            }

            final GeoPoint finalLocation = location;

            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                post.setLocation(finalLocation);

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

            });
        }).start();
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

            if (p.getCommentCount() == count) {
                return;
            }

            p.setCommentCount(count);
            selectedPost.setValue(p);

            if (authRespository.getCurrentUser() == null) {
                return;
            }

            postRepository.updatePost(p, new FireStoreCallBack<Void>() {
                @Override
                public void onSuccess(Void result) {}

                @Override
                public void onFailure(String e) {
                    errorMessage.postValue("Erreur update commentaires : " + e);
                }
            });
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

    public void toggleLike(Post post, String userId) {
        if (userId == null) return;

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
            posts.setValue(new ArrayList<>(currentPosts));
        }

        List<Post> currentGroupPosts = groupPost.getValue();
        if (currentGroupPosts != null) {
            groupPost.setValue(new ArrayList<>(currentGroupPosts));
        }

        List<Post> currentUserPosts = userPosts.getValue();
        if (currentUserPosts != null) {
            userPosts.setValue(new ArrayList<>(currentUserPosts));
        }

        postRepository.toggleLike(post, userId, !alreadyLiked, new FireStoreCallBack<Void>() {
            @Override
            public void onSuccess(Void result) {}

            @Override
            public void onFailure(String e) {
                errorMessage.postValue("Erreur lors du like : " + e);
            }
        });
    }

    public void loadPostsByTag(String tag) {
        isLoading.setValue(true);
        postRepository.fetchPostsByTag(tag, new FireStoreCallBack<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                posts.setValue(result);
                isLoading.setValue(false);
            }
            @Override
            public void onFailure(String e) {
                errorMessage.setValue(e);
                isLoading.setValue(false);
            }
        });
    }
}