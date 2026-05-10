package com.example.travelshare.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelshare.model.Comment;
import com.example.travelshare.repository.CommentRepository;
import com.example.travelshare.repository.FireStoreCallBack;

import java.util.List;

public class CommentViewModel extends ViewModel {

    private final CommentRepository commentRepository = new CommentRepository();

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

    public void addComment(Comment comment) {
        isLoading.setValue(true);
        commentRepository.addComment(comment, new FireStoreCallBack<Void>() {
            @Override
            public void onSuccess(Void result) {
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
