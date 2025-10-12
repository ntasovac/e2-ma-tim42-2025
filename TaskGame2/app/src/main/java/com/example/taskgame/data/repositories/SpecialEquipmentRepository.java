package com.example.taskgame.data.repositories;

import com.example.taskgame.domain.models.SpecialEquipment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SpecialEquipmentRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference col = db.collection("specialEquipment");

    /* ---------- Create ---------- */
    public void create(SpecialEquipment eq, final CreateCallback cb) {
        if (eq.getId() == null || eq.getId().isEmpty()) {
            eq.setId(col.document().getId());
        }
        col.document(eq.getId())
                .set(eq)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.onSuccess(eq.getId());
                    else cb.onFailure(t.getException());
                });
    }

    /* ---------- Get All ---------- */
    public void getAll(final GetAllCallback cb) {
        col.get().addOnCompleteListener(t -> {
            if (!t.isSuccessful()) { cb.onFailure(t.getException()); return; }
            List<SpecialEquipment> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : t.getResult()) {
                SpecialEquipment eq = d.toObject(SpecialEquipment.class);
                if (eq.getId() == null || eq.getId().isEmpty()) eq.setId(d.getId());
                list.add(eq);
            }
            cb.onSuccess(list);
        });
    }

    /* ---------- Get by ID ---------- */
    public void getById(String id, final GetOneCallback cb) {
        col.document(id).get().addOnCompleteListener(t -> {
            if (!t.isSuccessful()) { cb.onFailure(t.getException()); return; }
            DocumentSnapshot d = t.getResult();
            if (d != null && d.exists()) {
                SpecialEquipment eq = d.toObject(SpecialEquipment.class);
                if (eq != null && (eq.getId() == null || eq.getId().isEmpty())) eq.setId(d.getId());
                cb.onSuccess(eq);
            } else {
                cb.onSuccess(null);
            }
        });
    }

    /* ---------- Callbacks ---------- */
    public interface CreateCallback { void onSuccess(String id); void onFailure(Exception e); }
    public interface GetAllCallback { void onSuccess(List<SpecialEquipment> list); void onFailure(Exception e); }
    public interface GetOneCallback { void onSuccess(SpecialEquipment eq); void onFailure(Exception e); }
}
