package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Adapters.IncomeTransactionAdapter;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.IncomeTransaction;
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

public class AmountToClaimActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef;

    TextView tvAmountToClaim, tvLog, tvBadge;
    ImageView reloadImage;
    RecyclerView transactionView;
    ProgressBar progressBar;

    Context myContext;
    Resources myResources;

    User user;

    String userId;

    IncomeTransactionAdapter incomeTransactionAdapter;

    List<IncomeTransaction> transactionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount_to_claim);

        tvAmountToClaim = findViewById(R.id.tvAmountToClaim);
        tvLog = findViewById(R.id.tvLog);
        tvBadge = findViewById(R.id.tvBadge);
        reloadImage = findViewById(R.id.reloadImage);
        transactionView = findViewById(R.id.transactionView);
        progressBar = findViewById(R.id.progressBar);

        myContext = AmountToClaimActivity.this;
        myResources = getResources();

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        usersRef = firebaseDatabase.getReference("users").child(userId);

        try {
            Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage);
        }
        catch (Exception ignored) {}

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        transactionView.setLayoutManager(linearLayout);
        incomeTransactionAdapter = new IncomeTransactionAdapter(myContext, transactionList);
        transactionView.setAdapter(incomeTransactionAdapter);

        getCurrentUser();
    }

    private void getCurrentUser() {
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        transactionView.setVisibility(View.INVISIBLE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = null;
                transactionList.clear();
                if(snapshot.exists()) {
                    user = new User(snapshot);
                    transactionList.addAll(user.getAmountToClaimTransactionList());
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
        incomeTransactionAdapter.notifyDataSetChanged();

        String valueText = "₱" + user.getAmountToClaim();
        if(valueText.split("\\.")[1].length() == 1) valueText += 0;
        tvAmountToClaim.setText(valueText);

        tvBadge.setText(String.valueOf(transactionList.size()));

        progressBar.setVisibility(View.GONE);
        transactionView.setVisibility(View.VISIBLE);
    }

    private void errorLoading(String error) {
        transactionList.clear();

        incomeTransactionAdapter.notifyDataSetChanged();

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        transactionView.setVisibility(View.INVISIBLE);
    }
}