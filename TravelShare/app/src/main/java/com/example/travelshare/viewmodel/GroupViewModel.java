package com.example.travelshare.viewmodel;

import android.net.Uri;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelshare.model.Group;
import com.example.travelshare.model.User;
import com.example.travelshare.repository.FireStoreCallBack;
import com.example.travelshare.repository.GroupRepository;
import com.example.travelshare.repository.StorageRepository;
import com.example.travelshare.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class GroupViewModel extends ViewModel {

    private GroupRepository groupRepository = new GroupRepository();
    private UserRepository userRepository = new UserRepository();
    private StorageRepository storageRepository = new StorageRepository();

    private MutableLiveData<Boolean> groupCreated = new MutableLiveData<>();
    private MutableLiveData<List<Group>> allGroups = new MutableLiveData<>();
    private MutableLiveData<Group> selectedGroup = new MutableLiveData<>();

    private MutableLiveData<List<User>> admins = new MutableLiveData<>();
    private MutableLiveData<List<User>> members = new MutableLiveData<>();
    private MutableLiveData<List<User>> searchResults = new MutableLiveData<>();

    public LiveData<Boolean> getGroupCreated() { return groupCreated; }
    public LiveData<List<Group>> getAllGroups() { return allGroups; }
    public LiveData<Group> getSelectedGroup() { return selectedGroup; }
    public LiveData<List<User>> getAdmins() { return admins; }
    public LiveData<List<User>> getMembers() { return members; }
    public LiveData<List<User>> getSearchResults() { return searchResults; }

    public void createNewGroup(String name, String description, String userId, Uri imageUri) {
        Group newGroup = new Group(name, description, userId);

        groupRepository.createGroup(newGroup, new FireStoreCallBack<String>() {
            @Override
            public void onSuccess(String groupId) {
                newGroup.setGroupId(groupId);

                if (imageUri != null) {
                    storageRepository.uploadGroupImage(imageUri, groupId, new FireStoreCallBack<String>() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            newGroup.setImageUrl(imageUrl);
                            finalizeCreation(newGroup);
                        }
                        @Override
                        public void onFailure(String e) { finalizeCreation(newGroup); }
                    });
                } else {
                    finalizeCreation(newGroup);
                }
            }
            @Override
            public void onFailure(String e) { groupCreated.postValue(false); }
        });
    }

    private void finalizeCreation(Group group) {
        groupRepository.updateGroup(group, new FireStoreCallBack<Void>() {
            @Override
            public void onSuccess(Void result) {
                selectedGroup.postValue(group);
                groupCreated.postValue(true);
            }
            @Override
            public void onFailure(String e) {
                groupCreated.postValue(false);
            }
        });
    }

    public void selectGroup(Group group){
        selectedGroup.setValue(group);
    }
    public void resetGroupCreated() {
        groupCreated.setValue(false);
    }
    public void fetchGroups() {
        groupRepository.fetchGroups(new FireStoreCallBack<List<Group>>() {
            @Override
            public void onSuccess(List<Group> result) {
                allGroups.postValue(result);
            }

            @Override
            public void onFailure(String errorMessage) {
                allGroups.postValue(new ArrayList<>());
            }
        });
    }

    public void fetchGroupsForUser(String userId) {
        groupRepository.fetchUserGroups(userId, new FireStoreCallBack<List<Group>>() {
            @Override
            public void onSuccess(List<Group> result) {
                allGroups.postValue(result);
            }

            @Override
            public void onFailure(String errorMessage) {
                allGroups.postValue(new ArrayList<>());
            }
        });
    }

    public void fetchMembersDetails() {
        Group currentGroup = selectedGroup.getValue();
        if (currentGroup == null) return;

        List<String> allIds = new ArrayList<>(currentGroup.getMembers().keySet());

        userRepository.fetchUsersFromIds(allIds, new FireStoreCallBack<List<User>>() {
            @Override
            public void onSuccess(List<User> allUsers) {
                List<User> adminList = new ArrayList<>();
                List<User> memberList = new ArrayList<>();

                for (User u : allUsers) {
                    Boolean isAdmin = currentGroup.getMembers().get(u.getId());
                    if (isAdmin != null && isAdmin) {
                        adminList.add(u);
                    } else {
                        memberList.add(u);
                    }
                }
                admins.postValue(adminList);
                members.postValue(memberList);
            }

            @Override
            public void onFailure(String errorMessage) {}
        });
    }

    public void updateGroupInfo(String newName, String newDescription, Uri imageUri) {
        Group g = selectedGroup.getValue();
        if (g != null) {
            g.setGroupName(newName);
            g.setDescription(newDescription);

            if (imageUri != null){
                storageRepository.uploadGroupImage(imageUri, g.getGroupId(), new FireStoreCallBack<String>() {
                    @Override
                    public void onSuccess(String firebaseUrl) {
                        g.setImageUrl(firebaseUrl);
                        saveGroupToFirestore(g);
                    }
                    @Override
                    public void onFailure(String error) { }
                });
            } else {
                saveGroupToFirestore(g);
            }
        }
    }

    private void saveGroupToFirestore(Group g) {
        groupRepository.updateGroup(g, new FireStoreCallBack<Void>() {
            @Override
            public void onSuccess(Void result) {
                selectedGroup.postValue(g);
            }
            @Override
            public void onFailure(String errorMessage) {}
        });
    }

    public void searchUsers(String query) {
        userRepository.searchUsers(query, new FireStoreCallBack<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                searchResults.postValue(result);
            }

            @Override
            public void onFailure(String errorMessage) {}
        });
    }

    public void promoteToAdmin(User user) {
        Group g = selectedGroup.getValue();
        if (g != null) {
            g.getMembers().put(user.getId(), true);
            updateGroupMembersInDb(g);
        }
    }

    public void removeMember(User user) {
        Group g = selectedGroup.getValue();
        if (g != null) {
            g.getMembers().remove(user.getId());
            updateGroupMembersInDb(g);
        }
    }

    public void addMember(User user) {
        Group g = selectedGroup.getValue();
        if (g != null) {
            g.getMembers().put(user.getId(), false);
            updateGroupMembersInDb(g);
        }
    }

    private void updateGroupMembersInDb(Group g) {
        groupRepository.updateGroup(g, new FireStoreCallBack<Void>() {
            @Override
            public void onSuccess(Void user) {
                selectedGroup.postValue(g);
                fetchMembersDetails();
            }

            @Override
            public void onFailure(String errorMessage) {}
        });
    }

    public void deleteCurrentGroup(FireStoreCallBack<Void> callBack){
        if (selectedGroup.getValue() != null) {
            groupRepository.deleteGroup(selectedGroup.getValue().getGroupId(), callBack);
        }
    }
}
