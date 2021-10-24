package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Adapters.BookingAdapter;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    List<Booking> processingBookingList = new ArrayList<>(),
            isNotOnTheSpotBookingList = new ArrayList<>(),
            isNotPaidBookingList = new ArrayList<>();

    DateTimeToString dateTimeToString;

    Calendar calendar = Calendar.getInstance();
    int calendarYear, calendarMonth, calendarDay;

    CountDownTimer statusTimer;

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

    private void startTimer() {
        if(statusTimer != null) statusTimer.cancel();
        statusTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                for(Booking booking : processingBookingList) {
                    checkBooking(booking);
                }

                start();
            }
        }.start();
    }

    private void changeBookingStatusToFailed(String bookingId) {
        DatabaseReference usersRef = firebaseDatabase.getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User thisUser = new User(dataSnapshot);
                        List<Booking> bookingList = thisUser.getBookingList();

                        for(Booking booking : bookingList) {
                            if(booking.getId().equals(bookingId)) {
                                usersRef.child(thisUser.getId()). child("bookingList").
                                        child(booking.getId()).child("status").setValue("Failed");
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

    private void checkBooking(Booking booking) {
        dateTimeToString = new DateTimeToString();
        dateTimeToString.setFormattedSchedule(booking.getSchedule());
        int bookingYear = Integer.parseInt(dateTimeToString.getYear());
        int bookingMonth = Integer.parseInt(dateTimeToString.getMonthNo());
        int bookingDay = Integer.parseInt(dateTimeToString.getDay());

        calendarYear = calendar.get(Calendar.YEAR);
        calendarMonth = calendar.get(Calendar.MONTH);
        calendarDay = calendar.get(Calendar.DAY_OF_MONTH);

        if(bookingYear < calendarYear ||
                (bookingMonth < calendarMonth && bookingYear == calendarYear) ||
                (bookingDay < calendarDay && bookingMonth == calendarMonth && bookingYear == calendarYear)) {

            changeBookingStatusToFailed(booking.getId());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date().getTime());
        int hour = Integer.parseInt(currentTime.split(":")[0]);
        int min = Integer.parseInt(currentTime.split(":")[1]);

        int bookingHour = Integer.parseInt(dateTimeToString.getRawHour());
        int bookingMin = Integer.parseInt(dateTimeToString.getMin());

        int minDifference;
        int hrDifference;

        if(hasBookingToday(bookingDay, bookingMonth, bookingYear)) {
            hrDifference = bookingHour - hour;

            if(min < bookingMin) minDifference = bookingMin - min;
            else {
                minDifference = 60 - (min - bookingMin);
                if(minDifference < 60) hrDifference--;
                else minDifference = 0;
            }

            setBookingStatusFromProcessingToFailed(booking, hrDifference, minDifference);
        }
    }

    private void setBookingStatusFromProcessingToFailed(Booking booking, int hrDifference, int minDifference) {
        if(booking.getStatus().equals("Processing") &&
                !booking.getBookingType().getId().equals("BT99") &&
                (hrDifference < 0 || (hrDifference == 0 && minDifference == 0)))
            changeBookingStatusToFailed(booking.getId());
    }

    private boolean hasBookingToday(int bookingDay, int bookingMonth, int bookingYear) {
        return (bookingDay == calendarDay && bookingMonth == calendarMonth && bookingYear == calendarYear);
    }

    private void getProcessingBooking() {
        DatabaseReference usersRef = firebaseDatabase.getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                processingBookingList.clear();
                isNotOnTheSpotBookingList.clear();
                isNotPaidBookingList.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User thisUser = new User(dataSnapshot);
                        List<Booking> bookingList = thisUser.getBookingList();

                        for(Booking booking : bookingList) {
                            if(booking.getStatus().equals("Processing"))
                                if(booking.getBookingType().getId().equals("BT99"))
                                    processingBookingList.add(booking);
                                else if(booking.isPaid()) isNotOnTheSpotBookingList.add(booking);
                                else isNotPaidBookingList.add(booking);
                        }
                    }
                }

                Collections.sort(isNotPaidBookingList, (booking, t1) ->
                        booking.getId().compareToIgnoreCase(t1.getId()));

                Collections.sort(isNotOnTheSpotBookingList, (booking, t1) ->
                        booking.getId().compareToIgnoreCase(t1.getId()));

                Collections.sort(processingBookingList, (booking, t1) ->
                        booking.getId().compareToIgnoreCase(t1.getId()));

                processingBookingList.addAll(isNotOnTheSpotBookingList);
                processingBookingList.addAll(isNotPaidBookingList);

                if(processingBookingList.size() > 0) finishLoading();
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
        startTimer();
        bookingAdapter.setInDriverMode(true);

        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        bookingView.setVisibility(View.VISIBLE);
    }

    private void errorLoading(String error) {
        processingBookingList.clear();
        isNotOnTheSpotBookingList.clear();
        isNotPaidBookingList.clear();
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