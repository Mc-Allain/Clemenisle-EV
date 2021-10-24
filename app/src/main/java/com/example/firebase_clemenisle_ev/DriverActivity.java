package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Adapters.BookingAdapter;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DriverActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);

    TextView tvLog;
    ImageView reloadImage;
    RecyclerView bookingView;
    Button exitButton;
    ProgressBar progressBar;

    Context myContext;
    Resources myResources;

    String defaultLogText = "No Record";

    BookingAdapter bookingAdapter;

    List<Booking> processingBookingList = new ArrayList<>();

    long backPressedTime;
    Toast backToast;

    private void sendDriverModePreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isDriver", false);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        tvLog = findViewById(R.id.tvLog);
        reloadImage = findViewById(R.id.reloadImage);
        bookingView = findViewById(R.id.bookingView);
        exitButton = findViewById(R.id.exitButton);
        progressBar = findViewById(R.id.progressBar);

        myContext = DriverActivity.this;
        myResources = myContext.getResources();

        Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage);

        LinearLayoutManager linearLayout1 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        bookingView.setLayoutManager(linearLayout1);
        bookingAdapter = new BookingAdapter(myContext, processingBookingList);
        bookingView.setAdapter(bookingAdapter);

        getProcessingBooking();

        exitButton.setOnClickListener(view -> {
            sendDriverModePreferences();

            Intent intent = new Intent(myContext, MainActivity.class);
            startActivity(intent);
            finishAffinity();
        });
    }

    private void getProcessingBooking() {
        DatabaseReference usersRef = firebaseDatabase.getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                processingBookingList.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User thisUser = new User(dataSnapshot);
                        List<Booking> bookingList = thisUser.getBookingList();

                        for(Booking booking : bookingList) {
                            if(booking.getStatus().equals("Processing"))
                                processingBookingList.add(booking);
                        }
                    }
                }

                if(processingBookingList.size() > 0) {
                    finishLoading();
                }
                else errorLoading(defaultLogText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();

                errorLoading(error.toString());
            }
        });
    }

    private void finishLoading() {
        bookingAdapter.setInDriverMode(true);

        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        bookingView.setVisibility(View.VISIBLE);
    }

    private void errorLoading(String error) {
        processingBookingList.clear();
        bookingAdapter.notifyDataSetChanged();

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        bookingView.setVisibility(View.GONE);
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