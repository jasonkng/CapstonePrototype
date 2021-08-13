package com.jason.capstone.Home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.jason.capstone.R;
import com.jason.capstone.Utility.HistoryAdapter;
import com.jason.capstone.Utility.HistoryModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    private String KEY_COLLECTION = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;
    private HistoryAdapter adapter;
    private static final String TAG = "HistoryFragment";

    private static final String ARG_PARAM1 = "param1";

    private String mParam1;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(String param1) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        getActivity().setTitle("History");
        KEY_COLLECTION = mParam1;
        collectionReference = db.collection(KEY_COLLECTION);
        setUpRecyclerView(v);

        return v;
    }

    private void setUpRecyclerView(View v) {
        Query query = collectionReference;
        FirestoreRecyclerOptions<HistoryModel> options = new FirestoreRecyclerOptions.Builder<HistoryModel>()
                .setQuery(query, HistoryModel.class)
                .build();

        adapter = new HistoryAdapter(options);
        RecyclerView recyclerView = v.findViewById(R.id.recycler_history);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
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