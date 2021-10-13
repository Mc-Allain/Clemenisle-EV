package com.example.firebase_clemenisle_ev.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoggedInUserProfileFragment extends Fragment {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    TextView tvGreet;
    ProgressBar progressBar;

    Context myContext;
    Resources myResources;

    String defaultGreetText = "こんにちは", greetText;

    User user;

    String userId;
    boolean loggedIn = false;

    int colorRed, colorBlack;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        loggedIn = sharedPreferences.getBoolean("loggedIn", false);
    }

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "logIn", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("loggedIn", false);
        editor.putBoolean("remember", false);
        editor.putString("emailAddress", null);
        editor.putString("password", null);
        editor.apply();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logged_in_user_profile, container, false);

        tvGreet = view.findViewById(R.id.tvGreet);
        progressBar = view.findViewById(R.id.progressBar);

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorRed = myResources.getColor(R.color.red);
        colorBlack = myResources.getColor(R.color.black);

        initSharedPreferences();

        firebaseAuth = FirebaseAuth.getInstance();
        if(loggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) firebaseUser.reload();
            if(firebaseUser == null) {
                firebaseAuth.signOut();
                sendLoginPreferences();

                Toast.makeText(
                        myContext,
                        "Failed to get the current user",
                        Toast.LENGTH_SHORT
                ).show();
            }
            else {
                userId = firebaseUser.getUid();
            }
        }

        getCurrentUser();

        return view;
    }

    private void getCurrentUser() {
        DatabaseReference usersRef = firebaseDatabase.getReference("users").child(userId);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = null;
                if(snapshot.exists()) {
                    user = new User(snapshot);
                    finishLoading();
                }
                else errorLoading("Failed to get the current user");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorLoading(error.toString());
            }
        });
    }

    private void finishLoading() {
        greetText = defaultGreetText + ", " + user.getFirstName();
        tvGreet.setText(greetText);
        tvGreet.setTextColor(colorBlack);
        progressBar.setVisibility(View.GONE);
    }

    private void errorLoading(String error) {
        tvGreet.setText(error);
        progressBar.setVisibility(View.GONE);
        tvGreet.setTextColor(colorRed);
    }
}