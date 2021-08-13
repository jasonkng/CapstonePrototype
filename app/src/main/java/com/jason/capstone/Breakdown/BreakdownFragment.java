package com.jason.capstone.Breakdown;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jason.capstone.Mail.JavaMailAPI;
import com.jason.capstone.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BreakdownFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final static String KEY_COLLECTION = "BuildingDefects";
    private CollectionReference collectionReference = db.collection(KEY_COLLECTION);
    private static final String TAG = "BreakdownFragment";

    private static final String KEY_COMPONENT = "component";
    private static final String KEY_DEFECT = "defect";
    private static final String KEY_LENGTH = "length";
    private static final String KEY_DATE = "date";
    private static final String KEY_PRIORITY = "priority";


    private Button mContactButton, mResolutionButton;
    private Spinner mSpinner;
    private TextView mComponent, mDefect, mLength, mDate, mPriority;
    private RelativeLayout mRelativeLayout;
    ArrayAdapter<String> adapter;
    List<String> spinnerDataList;
    Map<String, Object> info;
    String[] s;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_breakdown, container, false);
        mContactButton = v.findViewById(R.id.breakdown_contact);
        mResolutionButton = v.findViewById(R.id.breakdown_resolution);
        mSpinner = v.findViewById(R.id.breakdown_spinner);
        mRelativeLayout = v.findViewById(R.id.bd_textView);

        mComponent = v.findViewById(R.id.bd_component);
        mDefect = v.findViewById(R.id.bd_defect);
        mLength = v.findViewById(R.id.bd_length);
        mDate = v.findViewById(R.id.bd_date);
        mPriority = v.findViewById(R.id.bd_priority);

        SharedPreferences shared = getActivity().getSharedPreferences("admin", Context.MODE_PRIVATE);
        boolean admin = shared.getBoolean("currentUser", false);

        if (admin) {
            initializeSpinner();
            mSpinner.setOnItemSelectedListener(this);
            mContactButton.setOnClickListener(v1 -> contactVendor());
            mResolutionButton.setOnClickListener(v2 -> resolveDefect());
        } else Toast.makeText(getActivity(), "You are not an admin", Toast.LENGTH_SHORT).show();

        return v;
    }

    private void resolveDefect() {
        if(s[2] != null){
            db.collection(KEY_COLLECTION).document(s[2]).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                }
            });
        }
        adapter.notifyDataSetChanged();
    }

    private void contactVendor() {
        if(s[0] != null){
            String u = s[0].toLowerCase();
            db.collection("Users").document(u).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    if(snapshot.exists()){
                        String mEmail = snapshot.get("email").toString();
                        String mSubject = "Building Defect: " + s[0] + s[1];
                        String mMessage = "Dear Vendor\n\nA new concrete defect has been reported in the building, please send over contractor to remedy the issue. More details" +
                                "can be found in the application.\n\nBest Regards,\nOwner of XYZ Building";

                        JavaMailAPI javaMailAPI = new JavaMailAPI(getActivity(), mEmail, mSubject, mMessage);
                        javaMailAPI.execute();

                        Toast.makeText(getActivity(), "Vendor Contacted", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else Toast.makeText(getActivity(), "No defect currently selected", Toast.LENGTH_SHORT).show();

    }

    private void initializeSpinner() {
        spinnerDataList = new ArrayList<>();
        spinnerDataList.add("Select Building Defect");
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot d : task.getResult()) {
                        spinnerDataList.add(d.getString("component") + " " + d.getId());
                        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item
                                , spinnerDataList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.notifyDataSetChanged();
                        mSpinner.setAdapter(adapter);
                    }
                } else
                    Toast.makeText(getActivity(), "No defect at the moment", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String component = parent.getItemAtPosition(position).toString();
        if (!component.contains("Select")) {
            s = component.split(" ");
            populateTextField(s[2]);
        }
    }

    private void populateTextField(String componentId) {
        db.collection(KEY_COLLECTION).document(componentId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                info = snapshot.getData();
                mComponent.setText(info.get(KEY_COMPONENT).toString());
                mDefect.setText(info.get(KEY_DEFECT).toString());
                mDate.setText(info.get(KEY_DATE).toString());
                mLength.setText(info.get(KEY_LENGTH).toString());
                mPriority.setText(info.get(KEY_PRIORITY).toString());
                mRelativeLayout.setVisibility(View.VISIBLE);
                mContactButton.setVisibility(View.VISIBLE);
                mResolutionButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}