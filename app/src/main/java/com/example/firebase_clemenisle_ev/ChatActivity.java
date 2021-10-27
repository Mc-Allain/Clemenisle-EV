package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ChatActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout userInfoLayout, driverInfoLayout;
    ImageView profileImage, driverProfileImage, sendImage;
    TextView tvUserFullName, tvDriverFullName, tvDriverFullName2;
    RecyclerView chatView;

    TextInputLayout tlMessage;
    EditText etMessage;

    Context myContext;
    Resources myResources;

    String bookingId, passengerId;
    boolean isLoggedIn = false, inDriverMode = false;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("remember", false);
        editor.putString("emailAddress", null);
        editor.putString("password", null);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userInfoLayout = findViewById(R.id.userInfoLayout);
        driverInfoLayout = findViewById(R.id.driverInfoLayout);
        profileImage = findViewById(R.id.profileImage);
        driverProfileImage = findViewById(R.id.driverProfileImage);
        tvUserFullName = findViewById(R.id.tvUserFullName);
        tvDriverFullName = findViewById(R.id.tvDriverFullName);
        tvDriverFullName2 = findViewById(R.id.tvDriverFullName2);
        chatView = findViewById(R.id.chatView);


        tlMessage = findViewById(R.id.tlMessage);
        etMessage = findViewById(R.id.etMessage);
        sendImage = findViewById(R.id.sendImage);

        myContext = ChatActivity.this;
        myResources = getResources();

        initSharedPreferences();

        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");
        inDriverMode = intent.getBooleanExtra("inDriverMode", false);

        firebaseAuth = FirebaseAuth.getInstance();
        if(isLoggedIn && !inDriverMode) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) firebaseUser.reload();
            if(firebaseUser == null) {
                firebaseAuth.signOut();
                sendLoginPreferences();

                Toast.makeText(
                        myContext,
                        "Failed to get the current user",
                        Toast.LENGTH_LONG
                ).show();
            }
            else passengerId = firebaseUser.getUid();
        }

        if(inDriverMode) getUserInfo();
        else getDriverInfo();

        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void getDriverInfo() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User thisUser = new User(dataSnapshot);
                        List<Booking> taskList = thisUser.getTaskList();

                        for(Booking booking : taskList) {
                            if(booking.getId().equals(bookingId)) {
                                String fullName = "<b>" + thisUser.getLastName() + "</b>, " + thisUser.getFirstName();
                                if(thisUser.getMiddleName().length() > 0) fullName += " " + thisUser.getMiddleName();
                                tvDriverFullName.setText(fromHtml(fullName));

                                try {
                                    Glide.with(myContext).load(thisUser.getProfileImage())
                                            .placeholder(R.drawable.image_loading_placeholder)
                                            .into(driverProfileImage);
                                }
                                catch (Exception ignored) {}

                                driverInfoLayout.setVisibility(View.VISIBLE);
                                userInfoLayout.setVisibility(View.GONE);

                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getUserInfo() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User thisUser = new User(dataSnapshot);
                        List<Booking> bookingList = thisUser.getBookingList();

                        for(Booking booking : bookingList) {
                            if(booking.getId().equals(bookingId)) {
                                String fullName = "<b>" + thisUser.getLastName() + "</b>, " + thisUser.getFirstName();
                                if(thisUser.getMiddleName().length() > 0) fullName += " " + thisUser.getMiddleName();
                                tvUserFullName.setText(fromHtml(fullName));

                                try {
                                    Glide.with(myContext).load(thisUser.getProfileImage())
                                            .placeholder(R.drawable.image_loading_placeholder)
                                            .into(profileImage);
                                }
                                catch (Exception ignored) {}

                                userInfoLayout.setVisibility(View.VISIBLE);
                                driverInfoLayout.setVisibility(View.GONE);

                                passengerId = thisUser.getId();

                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        if(html == null) {
            return new SpannableString("");
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        }
        else {
            return Html.fromHtml(html);
        }
    }
}