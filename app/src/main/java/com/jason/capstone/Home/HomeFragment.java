package com.jason.capstone.Home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.jason.capstone.R;
import com.jason.capstone.Utility.HomeAdapter;
import com.jason.capstone.Utility.HomeModel;

import java.util.Objects;


public class HomeFragment extends Fragment {
    private final static String KEY_COLLECTION = "BuildingDefects";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(KEY_COLLECTION);
    private HomeAdapter adapter;
    private static final String TAG = "HomeFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setTitle("Defects");
        setUpRecyclerView(v);


        return v;
    }

    private void setUpRecyclerView(View v) {
        Query query = collectionReference.orderBy("priority");
        FirestoreRecyclerOptions<HomeModel> options = new FirestoreRecyclerOptions.Builder<HomeModel>()
                .setQuery(query, HomeModel.class)
                .build();
        adapter = new HomeAdapter(options);
        RecyclerView recyclerView = v.findViewById(R.id.home_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                String path = documentSnapshot.getReference().getPath();
                DocumentReference dReference = db.document(path);
                dReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String defect = snapshot.get("defect").toString();

                            if(defect.contentEquals("Large")){
                                Fragment fragment = HistoryFragment.newInstance(defect);
                                FragmentTransaction transaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, fragment);
                                transaction.addToBackStack(null);
                                transaction.commit();

                            }else{
                                Toast.makeText(getActivity(), "No major defect found", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Help");
                    }
                });

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }


}