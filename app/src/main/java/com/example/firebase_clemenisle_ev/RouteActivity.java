package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Adapters.RouteAdapter;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.Route;
import com.example.firebase_clemenisle_ev.Classes.Station;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RouteActivity extends AppCompatActivity implements
        RouteAdapter.OnVisitClickListener {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ImageView thumbnail, moreImage, locateImage, locateEndImage, reloadImage;
    TextView tvBookingId, tvSchedule, tvTypeName, tvPrice, tvStartStation2, tvEndStation2,
            tvOption, tvLocate, tvLocateEnd, tvLog;
    RecyclerView routeView;
    ConstraintLayout buttonLayout, bookingInfoLayout, bookingInfoButtonLayout;
    Button cancelButton;
    ProgressBar progressBar;

    int columnCount = 2;
    List<Route> routeList = new ArrayList<>();
    RouteAdapter routeAdapter;

    Context myContext;
    Resources myResources;

    String userId;

    boolean loggedIn = false;

    DatabaseReference bookingListRef;

    String startStationId, endStationId, bookingId, schedule, typeName, price,
            startStationName, endStationName, status;
    boolean latest;

    Station startStation, endStation;

    boolean onScreen = false;

    Handler optionHandler = new Handler();
    Runnable optionRunnable;

    String defaultLogText = "No Records";

    long lastPressSec = 0;
    int pressCount = 0;
    Toast cancelToast, errorToast;

    String cancelButtonText = "Cancel Booking", cancellingButtonText = "Cancelling…";

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        loggedIn = sharedPreferences.getBoolean("loggedIn", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        thumbnail = findViewById(R.id.thumbnail);
        tvBookingId = findViewById(R.id.tvBookingId);
        tvSchedule = findViewById(R.id.tvSchedule);
        tvTypeName = findViewById(R.id.tvTypeName);
        tvPrice = findViewById(R.id.tvPrice);
        tvStartStation2 = findViewById(R.id.tvStartStation2);
        tvEndStation2 = findViewById(R.id.tvEndStation2);
        tvOption = findViewById(R.id.tvOption);
        tvLocate = findViewById(R.id.tvLocate);
        tvLocateEnd = findViewById(R.id.tvLocateEnd);
        tvLog = findViewById(R.id.tvLog);
        routeView = findViewById(R.id.routeView);
        buttonLayout = findViewById(R.id.buttonLayout);
        bookingInfoLayout = findViewById(R.id.bookingInfoLayout);
        bookingInfoButtonLayout = findViewById(R.id.bookingInfoButtonLayout);
        cancelButton = findViewById(R.id.cancelButton);
        moreImage = findViewById(R.id.moreImage);
        locateImage = findViewById(R.id.locateImage);
        locateEndImage = findViewById(R.id.locateEndImage);
        reloadImage = findViewById(R.id.reloadImage);
        progressBar = findViewById(R.id.progressBar);

        myContext = RouteActivity.this;
        myResources = getResources();

        optionRunnable = () -> closeOption();

        initSharedPreferences();

        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");
        schedule = intent.getStringExtra("schedule");
        startStationId = intent.getStringExtra("startStationId");
        startStationName = intent.getStringExtra("startStationName");
        endStationId = intent.getStringExtra("endStationId");
        endStationName = intent.getStringExtra("endStationName");
        status = intent.getStringExtra("status");
        typeName = intent.getStringExtra("typeName");
        price = intent.getStringExtra("price");
        latest = intent.getBooleanExtra("latest", false);

        onScreen = true;

        firebaseAuth = FirebaseAuth.getInstance();
        if(loggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) {
                firebaseUser.reload();
                userId = firebaseUser.getUid();
            }
        }

        Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage);

        tvBookingId.setText(bookingId);
        tvSchedule.setText(schedule);
        tvTypeName.setText(typeName);
        tvPrice.setText(price);
        tvStartStation2.setText(startStationName);
        tvEndStation2.setText(endStationName);

        int color = 0;
        Drawable backgroundDrawable = myResources.getDrawable(R.color.blue);

        switch (status) {
            case "Processing":
                color = myResources.getColor(R.color.orange);
                backgroundDrawable = myResources.getDrawable(R.color.orange);
                buttonLayout.setVisibility(View.VISIBLE);
                break;
            case "Booked":
                color = myResources.getColor(R.color.green);
                backgroundDrawable = myResources.getDrawable(R.color.green);
                break;
            case "Completed":
                color = myResources.getColor(R.color.blue);
                backgroundDrawable = myResources.getDrawable(R.color.blue);
                break;
            case "Cancelled":
            case "Failed":
                color = myResources.getColor(R.color.red);
                backgroundDrawable = myResources.getDrawable(R.color.red);
                break;
        }

        tvBookingId.setBackground(backgroundDrawable);
        tvPrice.setTextColor(color);

        GridLayoutManager gridLayoutManager =
                new GridLayoutManager(myContext, columnCount, GridLayoutManager.VERTICAL, false);
        routeView.setLayoutManager(gridLayoutManager);
        routeAdapter = new RouteAdapter(myContext, routeList, columnCount, bookingId, status, latest, loggedIn);
        routeView.setAdapter(routeAdapter);
        routeAdapter.setOnVisitClickListener(this);

        getRoute();

        cancelButton.setOnClickListener(view -> {
            if(cancelToast != null) {
                cancelToast.cancel();
            }

            if(errorToast != null) {
                errorToast.cancel();
            }

            if(lastPressSec + 3000 > System.currentTimeMillis()) {
                pressCount++;

                if(pressCount == 5) {
                    progressBar.setVisibility(View.VISIBLE);

                    routeView.setEnabled(false);
                    cancelButton.setEnabled(false);
                    cancelButton.setText(cancellingButtonText);
                    cancelBooking();
                }
            }
            else {
                pressCount = 1;
            }

            lastPressSec = System.currentTimeMillis();

            cancelToast = Toast.makeText(myContext,
                    "Press " + (5 - pressCount) +" more time(s) to cancel booking",
                    Toast.LENGTH_SHORT);

            if(pressCount < 5) {
                cancelToast.show();
            }
            else {
                pressCount = 0;
            }
        });

        moreImage.setOnClickListener(view -> {
            if(tvOption.getText().equals("false")) {
                openOption();
            }
            else {
                closeOption();
            }
        });

        tvLocate.setOnClickListener(view -> openMap(startStation));
        locateImage.setOnClickListener(view -> openMap(startStation));

        tvLocateEnd.setOnClickListener(view -> openMap(endStation));
        locateEndImage.setOnClickListener(view -> openMap(endStation));
    }

    private void openMap(Station station) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", station.getId());
        intent.putExtra("lat", station.getLat());
        intent.putExtra("lng", station.getLng());
        intent.putExtra("name", station.getName());
        intent.putExtra("type", 1);
        myContext.startActivity(intent);
    }

    private void openOption() {
        moreImage.setEnabled(false);

        optionHandler.removeCallbacks(optionRunnable);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(bookingInfoLayout);

        constraintSet.clear(bookingInfoButtonLayout.getId(), ConstraintSet.TOP);
        constraintSet.connect(bookingInfoButtonLayout.getId(), ConstraintSet.BOTTOM,
                moreImage.getId(), ConstraintSet.BOTTOM);

        setTransition(bookingInfoButtonLayout);
        constraintSet.applyTo(bookingInfoLayout);

        tvOption.setText("true");
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_close_24);
        moreImage.setColorFilter(myContext.getResources().getColor(R.color.red));

        optionHandler.postDelayed(optionRunnable, 3000);
    }

    private void closeOption() {
        moreImage.setEnabled(false);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(bookingInfoLayout);

        constraintSet.clear(bookingInfoButtonLayout.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(bookingInfoButtonLayout.getId(), ConstraintSet.TOP,
                bookingInfoLayout.getId(), ConstraintSet.BOTTOM);

        setTransition(bookingInfoButtonLayout);
        constraintSet.applyTo(bookingInfoLayout);

        tvOption.setText("false");
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_more_horiz_24);
        moreImage.setColorFilter(myContext.getResources().getColor(R.color.black));
    }

    private void setTransition(ConstraintLayout constraintLayout) {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);
        TransitionManager.beginDelayedTransition(constraintLayout, transition);
    }

    private void getRoute() {
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        bookingListRef = firebaseDatabase.getReference("users").child(userId)
                .child("bookingList").child(bookingId);
        bookingListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                routeList.clear();
                if(snapshot.exists()) {
                    Booking booking = new Booking(snapshot);
                    startStation = booking.getStartStation();
                    endStation = booking.getEndStation();

                    for(Route route : booking.getRouteList()) {
                        if(!route.isDeactivated()) {
                            routeList.add(route);
                        }
                    }
                }
                if(routeList.size() > 0) finishLoading();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onScreen = false;
    }

    private void finishLoading() {
        routeAdapter.notifyDataSetChanged();

        if(onScreen) Glide.with(myContext).load(routeList.get(0).getImg()).into(thumbnail);

        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void errorLoading(String error) {
        routeAdapter.notifyDataSetChanged();

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void cancelBooking() {
        DatabaseReference usersRef = firebaseDatabase.getReference("users").child(userId)
            .child("bookingList").child(bookingId).child("status");
        usersRef.setValue("Cancelled")
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if(task.isSuccessful()) {
                        onBackPressed();
                    }
                    else {
                        errorToast = Toast.makeText(myContext,
                                "Failed to cancel the booking. Please try again.",
                                Toast.LENGTH_LONG);
                        errorToast.show();

                        routeView.setEnabled(true);
                        cancelButton.setEnabled(true);
                        cancelButton.setText(cancelButtonText);
                    }
                });
    }

    @Override
    public void setProgressBarToVisible() {
        progressBar.setVisibility(View.VISIBLE);
    }
}