package com.example.travelshare.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelshare.model.Notification;
import com.example.travelshare.repository.FireStoreCallBack;
import com.example.travelshare.repository.NotificationRepository;

import java.util.List;

public class NotificationViewModel extends ViewModel {
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<Notification>> getNotifications() { return notifications; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadNotifications(String userId) {
        isLoading.setValue(true);
        notificationRepository.fetchNotificationsForUser(userId, new FireStoreCallBack<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> result) {
                notifications.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(String e) {
                errorMessage.postValue(e);
                isLoading.postValue(false);
            }
        });
    }

    public void sendNotification(Notification notification) {
        notificationRepository.createNotification(notification, new FireStoreCallBack<String>() {
            @Override
            public void onSuccess(String result) {}
            @Override
            public void onFailure(String e) {
                errorMessage.postValue(e);
            }
        });
    }

    public void markAsRead(String notificationId) {
        notificationRepository.markAsRead(notificationId);
    }
}
