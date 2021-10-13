package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView mainNav;
    NavController mainNavCtrlr;
    NavHostFragment navHostFragment;
    FloatingActionButton fab;

    Context myContext;

    long backPressedTime;
    Toast backToast;

    boolean loggedIn = false;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        loggedIn = sharedPreferences.getBoolean("loggedIn", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainNav = findViewById(R.id.bottomNavigationView);
        mainNav.setBackground(null);

        myContext = MainActivity.this;

        initSharedPreferences();

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        mainNavCtrlr = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(mainNav, mainNavCtrlr);

        fab = findViewById(R.id.floatingActionButton);
        fab.setColorFilter(getResources().getColor(R.color.white));

        fab.setOnClickListener(view -> {
            Intent intent;
            if(loggedIn) {
                intent = new Intent(myContext, BookingActivity.class);
            }
            else {
                intent = new Intent(myContext, LoginActivity.class);
            }
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        if(backPressedTime + 2500 > System.currentTimeMillis()) {
            backToast.cancel();
            finish();
        }
        else {
            backToast = Toast.makeText(myContext,
                    "Press back again to exit",
                    Toast.LENGTH_SHORT);
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}