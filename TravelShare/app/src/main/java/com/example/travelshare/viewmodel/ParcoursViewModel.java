package com.example.travelshare.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelshare.repository.FireStoreCallBack;
import com.example.travelshare.repository.ParcoursRepository;
import com.example.travelshare.travelpath.Parcours;

import java.util.List;

public class ParcoursViewModel extends ViewModel {
    private final ParcoursRepository parcoursRepository = new ParcoursRepository();
    
    private MutableLiveData<Boolean> parcoursSaved = new MutableLiveData<>();
    private MutableLiveData<List<Parcours>> userParcours = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> getParcoursSaved() { return parcoursSaved; }
    public LiveData<List<Parcours>> getUserParcours() { return userParcours; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void saveParcours(String userId, Parcours parcours) {
        isLoading.setValue(true);
        parcoursRepository.saveParcours(userId, parcours, new FireStoreCallBack<Void>() {
            @Override
            public void onSuccess(Void result) {
                parcoursSaved.postValue(true);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    public void loadUserParcours(String userId) {
        isLoading.setValue(true);
        parcoursRepository.fetchUserParcours(userId, new FireStoreCallBack<List<Parcours>>() {
            @Override
            public void onSuccess(List<Parcours> result) {
                userParcours.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }
    
    public void resetParcoursSaved() {
        parcoursSaved.setValue(false);
    }
}
