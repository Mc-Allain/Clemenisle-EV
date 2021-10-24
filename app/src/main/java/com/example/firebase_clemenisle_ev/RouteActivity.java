package com.example.firebase_clemenisle_ev;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.ms.square.android.expandabletextview.ExpandableTextView;

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

    ImageView thumbnail, moreImage, locateImage, locateEndImage, reloadImage, paidImage;
    TextView tvBookingId, tvSchedule, tvTypeName, tvPrice, tvStartStation2, tvEndStation2,
            tvLocate, tvLocateEnd, tvLog;
    ExpandableTextView extvMessage;
    RecyclerView routeView;
    ConstraintLayout buttonLayout, bookingInfoLayout, bookingInfoButtonLayout;
    Button cancelButton, onlinePaymentButton;
    ProgressBar progressBar;

    int columnCount = 2;
    List<Route> routeList = new ArrayList<>();
    RouteAdapter routeAdapter;

    Context myContext;
    Resources myResources;

    String userId;

    boolean isLoggedIn = false;

    DatabaseReference bookingListRef;

    String bookingId, schedule, typeName, price, startStationName, endStationName, status, message;
    boolean isLatest, isPaid;

    Station startStation, endStation;

    boolean isOnScreen = false, isOptionShown = false;

    Handler optionHandler = new Handler();
    Runnable optionRunnable;

    String defaultLogText = "No Record";

    long lastPressSec = 0;
    int pressCount = 0;
    Toast cancelToast, errorToast;

    String cancelButtonText = "Cancel Booking", cancellingButtonText = "Cancelling…";

    Dialog dialog;
    ImageView dialogCloseImage;
    TextView tvMessage, tvMessage2;

    boolean isShowBookingAlertEnabled;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        isShowBookingAlertEnabled = sharedPreferences.getBoolean("isShowBookingAlertEnabled", true);
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
        tvLocate = findViewById(R.id.tvLocate);
        tvLocateEnd = findViewById(R.id.tvLocateEnd);
        tvLog = findViewById(R.id.tvLog);
        extvMessage = findViewById(R.id.extvMessage);

        routeView = findViewById(R.id.routeView);
        buttonLayout = findViewById(R.id.buttonLayout);
        bookingInfoLayout = findViewById(R.id.bookingInfoLayout);
        bookingInfoButtonLayout = findViewById(R.id.bookingInfoButtonLayout);
        cancelButton = findViewById(R.id.cancelButton);
        onlinePaymentButton = findViewById(R.id.onlinePaymentButton);
        moreImage = findViewById(R.id.moreImage);
        locateImage = findViewById(R.id.locateImage);
        locateEndImage = findViewById(R.id.locateEndImage);
        reloadImage = findViewById(R.id.reloadImage);
        paidImage = findViewById(R.id.paidImage);
        progressBar = findViewById(R.id.progressBar);

        myContext = RouteActivity.this;
        myResources = getResources();

        optionRunnable = () -> closeOption();

        initSharedPreferences();
        initBookingAlertDialog();

        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");
        isLatest = intent.getBooleanExtra("isLatest", false);

        isOnScreen = true;

        Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage);

        firebaseAuth = FirebaseAuth.getInstance();
        if(isLoggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) {
                firebaseUser.reload();
                userId = firebaseUser.getUid();
            }
        }

        GridLayoutManager gridLayoutManager =
                new GridLayoutManager(myContext, columnCount, GridLayoutManager.VERTICAL, false);
        routeView.setLayoutManager(gridLayoutManager);
        routeAdapter = new RouteAdapter(myContext, routeList, columnCount, bookingId, status, isLatest, isLoggedIn);
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
            if(!isOptionShown) {
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

        onlinePaymentButton.setOnClickListener(view -> Toast.makeText(
                myContext,
                "Not yet implemented",
                Toast.LENGTH_LONG
        ).show());

        paidImage.setOnLongClickListener(view -> {
            Toast.makeText(
                    myContext,
                    "Paid",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        });
    }

    private void initBookingAlertDialog() {
        dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_booking_alert_layout);

        dialogCloseImage = dialog.findViewById(R.id.dialogCloseImage);
        tvMessage = dialog.findViewById(R.id.tvMessage);
        tvMessage2 = dialog.findViewById(R.id.tvMessage2);

        dialogCloseImage.setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCanceledOnTouchOutside(false);
    }

    private void updateInfo() {
        Glide.with(myContext).load(routeList.get(0).getImg())
                .placeholder(R.drawable.image_loading_placeholder).into(thumbnail);

        tvBookingId.setText(bookingId);
        tvStartStation2.setText(startStationName);
        tvEndStation2.setText(endStationName);
        tvSchedule.setText(schedule);
        tvTypeName.setText(typeName);
        tvPrice.setText(price);
        extvMessage.setText(message);

        int color = 0;
        Drawable backgroundDrawable = myResources.getDrawable(R.color.blue);

        buttonLayout.setVisibility(View.GONE);

        switch (status) {
            case "Processing":
                color = myResources.getColor(R.color.orange);
                backgroundDrawable = myResources.getDrawable(R.color.orange);
                buttonLayout.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                if(isShowBookingAlertEnabled) dialog.show();
                break;
            case "Booked":
                color = myResources.getColor(R.color.green);
                backgroundDrawable = myResources.getDrawable(R.color.green);
                buttonLayout.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
                if(isShowBookingAlertEnabled) dialog.show();
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

        if(isPaid) paidImage.setVisibility(View.VISIBLE);
        else paidImage.setVisibility(View.GONE);

        paidImage.setColorFilter(color);
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

        isOptionShown = true;
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

        isOptionShown = false;
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

                    startStationName = startStation.getName();
                    endStationName = endStation.getName();

                    schedule = booking.getSchedule();
                    status = booking.getStatus();
                    message = booking.getMessage();

                    typeName = booking.getBookingType().getName();
                    price = String.valueOf(booking.getBookingType().getPrice());
                    if(price.split("\\.")[1].length() == 1) price += 0;

                    for(Route route : booking.getRouteList()) {
                        if(!route.isDeactivated()) {
                            routeList.add(route);
                        }
                    }

                    isPaid = booking.isPaid();
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
        isOnScreen = false;
    }

    private void finishLoading() {
        routeAdapter.setStatus(status);

        if(isOnScreen) updateInfo();

        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        routeView.setVisibility(View.VISIBLE);
    }

    private void errorLoading(String error) {
        routeAdapter.notifyDataSetChanged();

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        routeView.setVisibility(View.INVISIBLE);
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