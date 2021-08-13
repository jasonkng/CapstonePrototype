package com.jason.capstone.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jason.capstone.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText mEmail, mPassword;
    private Button mButton;
    private final static String TAG = "RegisterActivity";
    FirebaseAuth mFirebaseAuth;
    String email;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("Register");

        mFirebaseAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.register_email);
        mPassword = findViewById(R.id.register_password);
        mButton = findViewById(R.id.register_button);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (email.isEmpty()) {
                    mEmail.setError("Please enter email");
                    mEmail.requestFocus();
                } else if (password.isEmpty()) {
                    mPassword.setError("Please enter password");
                    mPassword.requestFocus();
                } else {
                    mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Registration Error, please try again", Toast.LENGTH_SHORT).show();
                                    } else {
                                        updateDatabase();
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    }
                                }
                            });
                }
            }
        });

    }

    private void updateDatabase() {
        Map<String, Object> map = new HashMap<>();
        map.put("email", email);
        map.put("role", "user");
        db.collection("Users").document(email).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG, "Document created");
            }
        });
    }
}