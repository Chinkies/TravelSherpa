package com.example.travelshare.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelshare.model.Comment;
import com.example.travelshare.model.Notification;
import com.example.travelshare.model.Post;
import com.example.travelshare.model.User;
import com.example.travelshare.repository.CommentRepository;
import com.example.travelshare.repository.FireStoreCallBack;
import com.example.travelshare.repository.NotificationRepository;

import java.util.List;

public class CommentViewModel extends ViewModel {

    private final CommentRepository commentRepository = new CommentRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();

    private final MutableLiveData<List<Comment>> comments = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<Comment>> getComment() { return comments; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void fetchComment(String postId) {
        isLoading.setValue(true);
        commentRepository.fetchCommentsForPost(postId, new FireStoreCallBack<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> result) {
                comments.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(String e) {
                errorMessage.postValue(e);
                isLoading.postValue(false);
            }
        });
    }

    public void addComment(Comment comment, Post post, User currentUser) {
        isLoading.setValue(true);
        commentRepository.addComment(comment, new FireStoreCallBack<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Envoyer une notification au créateur du post
                if (post != null && currentUser != null && !post.getAuthorId().equals(currentUser.getId())) {
                    Notification notif = new Notification(
                            post.getAuthorId(),
                            currentUser.getId(),
                            currentUser.getPseudo(),
                            currentUser.getProfilePictureUrl(),
                            "COMMENT",
                            post.getId(),
                            currentUser.getPseudo() + " a commenté votre publication : " + comment.getText()
                    );
                    notificationRepository.createNotification(notif, new FireStoreCallBack<String>() {
                        @Override public void onSuccess(String result) {}
                        @Override public void onFailure(String e) {}
                    });
                }
                fetchComment(comment.getPostId());
            }

            @Override
            public void onFailure(String e) {
                errorMessage.postValue(e);
                isLoading.postValue(false);
            }
        });
    }
}
