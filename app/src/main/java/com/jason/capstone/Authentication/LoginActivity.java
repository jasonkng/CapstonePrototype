package com.jason.capstone.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jason.capstone.MainActivity;
import com.jason.capstone.R;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail, mPassword;
    private Button mButton;
    private TextView mTextView;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");

        mFirebaseAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        mButton = findViewById(R.id.login_button);
        mTextView = findViewById(R.id.login_textview);

        mTextView.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else
                    Toast.makeText(LoginActivity.this, "Please login", Toast.LENGTH_SHORT).show();
            }
        };

        mButton.setOnClickListener(v -> {
            String email = mEmail.getText().toString();
            String password = mPassword.getText().toString();
            if (email.isEmpty()) {
                mEmail.setError("Please enter email");
                mEmail.requestFocus();
            } else if (password.isEmpty()) {
                mPassword.setError("Please enter password");
                mPassword.requestFocus();
            } else {
                mFirebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

        });
    }
}