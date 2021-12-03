package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Adapters.BookingAdapter;
import com.example.firebase_clemenisle_ev.Adapters.IncomeDayAdapter;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
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

public class IncomeFromTaskActivity extends AppCompatActivity implements
        IncomeDayAdapter.OnItemClickListener, BookingAdapter.OnActionClickListener {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef;

    TextView tvMonthYear, tvMonthIncome, tvLog;
    RecyclerView incomeDayView, taskView;
    ImageView reloadImage;
    ProgressBar progressBar;

    Context myContext;

    User user;

    String userId, monthYear, query;

    List<Booking> taskList = new ArrayList<>(), dayTaskList = new ArrayList<>();

    IncomeDayAdapter incomeDayAdapter;
    BookingAdapter bookingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_from_task);

        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvMonthIncome = findViewById(R.id.tvMonthIncome);
        incomeDayView = findViewById(R.id.incomeDayView);
        taskView = findViewById(R.id.taskView);

        tvLog = findViewById(R.id.tvLog);
        reloadImage = findViewById(R.id.reloadImage);
        progressBar = findViewById(R.id.progressBar);

        myContext = IncomeFromTaskActivity.this;

        try {
            Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage);
        }
        catch (Exception ignored) {}

        Intent intent = getIntent();
        userId  = intent.getStringExtra("userId");
        monthYear  = intent.getStringExtra("monthYear");
        query = 1 + " " + monthYear;

        tvMonthYear.setText(monthYear);

        usersRef = firebaseDatabase.getReference("users").child(userId);

        getCurrentUser();

        DateTimeToString dateTimeToString = new DateTimeToString();
        dateTimeToString.setFormattedSchedule(1 + " " + monthYear + " | 12:00 AM");
        int itemCount = dateTimeToString.getMaximumDaysInMonthOfYear();

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false);
        incomeDayView.setLayoutManager(linearLayout);
        incomeDayAdapter = new IncomeDayAdapter(myContext, itemCount, monthYear, taskList);
        incomeDayView.setAdapter(incomeDayAdapter);
        incomeDayAdapter.setOnItemClickListener(this);

        LinearLayoutManager linearLayout2 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        taskView.setLayoutManager(linearLayout2);
        bookingAdapter = new BookingAdapter(myContext, dayTaskList);
        taskView.setAdapter(bookingAdapter);
        bookingAdapter.setOnActionClickListener(this);
    }

    private void getCurrentUser() {
        progressBar.setVisibility(View.VISIBLE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = null;
                taskList.clear();
                if(snapshot.exists()) {
                    user = new User(snapshot);
                    taskList.addAll(user.getTaskList());
                    finishLoading();
                }
                else {
                    Toast.makeText(myContext, "Failed to get the current user", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    onBackPressed();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(myContext, error.toString(), Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                onBackPressed();
            }
        });
    }

    private void finishLoading() {
        incomeDayAdapter.notifyDataSetChanged();

        double monthIncome = 0;
        for(Booking task : taskList) {
            if(task.getSchedule().contains(monthYear) && task.getStatus().equals("Completed"))
                monthIncome += task.getBookingType().getPrice();
        }

        String monthIncomeText = "₱" + monthIncome;
        if(monthIncomeText.split("\\.")[1].length() == 1) monthIncomeText += 0;
        tvMonthIncome.setText(monthIncomeText);

        getTaskList();

        progressBar.setVisibility(View.GONE);
    }

    private void getTaskList() {
        dayTaskList.clear();

        for(Booking task : taskList) {
            String scheduleDate = task.getSchedule().split("\\|")[0].trim();
            if(scheduleDate.equals(query) && task.getStatus().equals("Completed"))
                dayTaskList.add(task);
        }

        bookingAdapter.setInDriverMode(true);

        if(dayTaskList.size() == 0) {
            tvLog.setVisibility(View.VISIBLE);
            reloadImage.setVisibility(View.VISIBLE);
        }
        else {
            tvLog.setVisibility(View.GONE);
            reloadImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void sendQuery(String query) {
        this.query = query;
        getTaskList();
    }

    @Override
    public void setProgressBarToVisible(boolean value) {
        if(value) progressBar.setVisibility(View.VISIBLE);
        else progressBar.setVisibility(View.GONE);
    }
}