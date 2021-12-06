package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Adapters.IncomeYearAdapter;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.DateTimeDifference;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class IncomeDataActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef;

    ConstraintLayout contentLayout;
    TextView tvIncomeToday2, tvIncomeThisWeek2, tvIncomeThisMonth2, tvIncomeThisYear2,
            tvTotalIncome2, tvAmountToRemit2, tvAmountToClaim2, tvIncomeShare;
    ImageView viewRemittanceHistoryImage, viewClaimHistoryImage, incomeSummaryArrowImage;
    RecyclerView yearIncomeView;
    ProgressBar progressBar;

    Context myContext;

    User user;

    String userId;

    List<Booking> taskList = new ArrayList<>();

    IncomeYearAdapter incomeYearAdapter;

    boolean isContentShown = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_data);

        contentLayout = findViewById(R.id.contentLayout);

        tvIncomeToday2 = findViewById(R.id.tvIncomeToday2);
        tvIncomeThisWeek2 = findViewById(R.id.tvIncomeThisWeek2);
        tvIncomeThisMonth2 = findViewById(R.id.tvIncomeThisMonth2);
        tvIncomeThisYear2 = findViewById(R.id.tvIncomeThisYear2);
        tvTotalIncome2 = findViewById(R.id.tvTotalIncome2);
        tvAmountToRemit2 = findViewById(R.id.tvAmountToRemit2);
        tvAmountToClaim2 = findViewById(R.id.tvAmountToClaim2);
        tvIncomeShare = findViewById(R.id.tvIncomeShare);

        viewRemittanceHistoryImage = findViewById(R.id.viewRemittanceHistoryImage);
        viewClaimHistoryImage = findViewById(R.id.viewClaimHistoryImage);
        incomeSummaryArrowImage = findViewById(R.id.incomeSummaryArrowImage);

        yearIncomeView = findViewById(R.id.yearIncomeView);

        progressBar = findViewById(R.id.progressBar);

        myContext = IncomeDataActivity.this;

        Intent intent = getIntent();
        userId  = intent.getStringExtra("userId");

        usersRef = firebaseDatabase.getReference("users").child(userId);

        incomeSummaryArrowImage.setOnClickListener(view -> showContentLayout());

        int currentYear = Integer.parseInt(new DateTimeToString().getYear());
        currentYear = Math.max(currentYear, 2022);

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        yearIncomeView.setLayoutManager(linearLayout);
        incomeYearAdapter = new IncomeYearAdapter(myContext, currentYear, taskList, userId);
        yearIncomeView.setAdapter(incomeYearAdapter);

        viewRemittanceHistoryImage.setOnClickListener(view -> {
            Intent intent1 = new Intent(myContext, AmountToRemitActivity.class);
            intent1.putExtra("userId", userId);
            startActivity(intent1);
        });

        viewClaimHistoryImage.setOnClickListener(view -> {
            Intent intent1 = new Intent(myContext, AmountToClaimActivity.class);
            intent1.putExtra("userId", userId);
            startActivity(intent1);
        });

        tvIncomeShare.setOnLongClickListener(view -> {
            Toast.makeText(
                    myContext,
                    "Income Share",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        });

        getCurrentUser();
    }

    private void showContentLayout() {
        if(isContentShown) {
            contentLayout.setVisibility(View.GONE);
            incomeSummaryArrowImage.setImageResource(R.drawable.ic_baseline_expand_more_24);
        }
        else {
            contentLayout.setVisibility(View.VISIBLE);
            incomeSummaryArrowImage.setImageResource(R.drawable.ic_baseline_expand_less_24);
        }
        isContentShown = !isContentShown;
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
        incomeYearAdapter.notifyDataSetChanged();

        double incomeToday = 0, incomeThisWeek = 0, incomeThisMonth = 0, incomeThisYear = 0,
                totalIncome = 0, amountToRemit = user.getAmountToRemit(), amountToClaim = user.getAmountToClaim();

        for(Booking task : taskList) {
            if(task.getStatus().equals("Completed")) {
                DateTimeDifference dateTimeDifference = new DateTimeDifference(task.getSchedule());
                int dayDifference = dateTimeDifference.getDayDifference();
                int monthDifference = dateTimeDifference.getMonthDifference();
                int yearDifference = dateTimeDifference.getYearDifference();

                if(yearDifference == 0) {
                    if(monthDifference == 0) {
                        if(dayDifference < 7) incomeThisWeek += task.getBookingType().getPrice();
                        if(dayDifference == 0) incomeToday += task.getBookingType().getPrice();
                        incomeThisMonth += task.getBookingType().getPrice();
                    }
                    incomeThisYear += task.getBookingType().getPrice();
                }
                else if(monthDifference == 1 && dayDifference < 7)
                    incomeThisWeek += task.getBookingType().getPrice();
                totalIncome += task.getBookingType().getPrice();
            }
        }

        String incomeTodayText = "₱" + incomeToday;
        if(incomeTodayText.split("\\.")[1].length() == 1) incomeTodayText += 0;
        tvIncomeToday2.setText(incomeTodayText);

        String incomeThisWeekText = "₱" + incomeThisWeek;
        if(incomeThisWeekText.split("\\.")[1].length() == 1) incomeThisWeekText += 0;
        tvIncomeThisWeek2.setText(incomeThisWeekText);

        String incomeThisMonthText = "₱" + incomeThisMonth;
        if(incomeThisMonthText.split("\\.")[1].length() == 1) incomeThisMonthText += 0;
        tvIncomeThisMonth2.setText(incomeThisMonthText);

        String incomeThisYearText = "₱" + incomeThisYear;
        if(incomeThisYearText.split("\\.")[1].length() == 1) incomeThisYearText += 0;
        tvIncomeThisYear2.setText(incomeThisYearText);

        String totalIncomeText = "₱" + totalIncome;
        if(totalIncomeText.split("\\.")[1].length() == 1) totalIncomeText += 0;
        tvTotalIncome2.setText(totalIncomeText);

        String amountToRemitText = "₱" + amountToRemit;
        if(amountToRemitText.split("\\.")[1].length() == 1) amountToRemitText += 0;
        tvAmountToRemit2.setText(amountToRemitText);

        String amountToClaimText = "₱" + amountToClaim;
        if(amountToClaimText.split("\\.")[1].length() == 1) amountToClaimText += 0;
        tvAmountToClaim2.setText(amountToClaimText);

        double incomeShare = Math.round(user.getIncomeShare() * 100);
        String incomeShareText = "(" + String.valueOf(incomeShare).split("\\.")[0] + "%)";
        tvIncomeShare.setText(incomeShareText);

        progressBar.setVisibility(View.GONE);
    }
}