package com.jason.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jason.capstone.Breakdown.BreakdownFragment;
import com.jason.capstone.Home.HomeFragment;
import com.jason.capstone.Report.ReportFragment;


public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        loadFragment(new HomeFragment());


        getAuthorizedUser(user.getEmail());


    }

        private BottomNavigationView.OnNavigationItemSelectedListener navListener =
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch(item.getItemId()){
                            case R.id.nav_home:
                                selectedFragment = new HomeFragment();
                                break;
                            case R.id.nav_report:
                                selectedFragment = new ReportFragment();
                                break;
                            case R.id.nav_breakdown:
                                selectedFragment = new BreakdownFragment();
                                break;
                        }
                        return loadFragment(selectedFragment);
                    }
                };

    private boolean loadFragment(Fragment selectedFragment) {
        if  (selectedFragment!=null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        }
        return false;
    }

    private void getAuthorizedUser(String email) {
        db.collection("Users").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists() && snapshot.get("role").equals("admin")) {
                    SharedPreferences sharedPreferences = getSharedPreferences("admin", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("currentUser", true);
                    editor.apply();
                    Log.e("MainActivity", "Admin check");
                }
            }
        });
    }

}