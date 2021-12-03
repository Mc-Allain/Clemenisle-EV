package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class IncomeFromTaskActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef;

    TextView tvMonthYear, tvMonthIncome;
    RecyclerView incomeDayView;
    ProgressBar progressBar;

    Context myContext;

    User user;

    String userId, monthYear;

    List<Booking> taskList = new ArrayList<>();

    IncomeDayAdapter incomeDayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_from_task);

        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvMonthIncome = findViewById(R.id.tvMonthIncome);
        incomeDayView = findViewById(R.id.incomeDayView);
        progressBar = findViewById(R.id.progressBar);

        myContext = IncomeFromTaskActivity.this;

        Intent intent = getIntent();
        userId  = intent.getStringExtra("userId");
        monthYear  = intent.getStringExtra("monthYear");

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

        progressBar.setVisibility(View.GONE);
    }
}