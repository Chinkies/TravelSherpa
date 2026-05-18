package com.example.travelshare.repository;

import com.example.travelshare.model.Lieu;
import com.example.travelshare.travelpath.Etape;
import com.example.travelshare.travelpath.Parcours;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class ParcoursRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String COLLECTION_PARCOURS = "parcours";
    private final String COLLECTION_LIEUX = "lieux";

    public void saveParcours(String userId, Parcours parcours, FireStoreCallBack<Void> callback) {
        WriteBatch batch = db.batch();

        String parcoursId = db.collection("users").document(userId).collection(COLLECTION_PARCOURS).document().getId();
        parcours.setId(parcoursId);

        batch.set(db.collection("users").document(userId).collection(COLLECTION_PARCOURS).document(parcoursId), parcours);

        for (Etape etape : parcours.getListeEtapes()) {
            Lieu lieu = etape.getLieu();
            if (lieu.getId() != null) {
                batch.set(db.collection(COLLECTION_LIEUX).document(lieu.getId()), lieu);
            }
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void fetchUserParcours(String userId, FireStoreCallBack<List<Parcours>> callback) {
        db.collection("users").document(userId).collection(COLLECTION_PARCOURS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(queryDocumentSnapshots.toObjects(Parcours.class));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
