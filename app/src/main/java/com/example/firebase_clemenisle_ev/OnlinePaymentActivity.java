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
import com.example.firebase_clemenisle_ev.Adapters.ReferenceNumberAdapter;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.ReferenceNumber;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OnlinePaymentActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout buttonLayout;
    Button refundButton;
    TextView tvActivityCaption, tvHelp,
            tvPrice2, tvCreditedAmount2, tvBalance2, tvLog;
    ImageView helpImage, reloadImage;
    RecyclerView referenceNumberView;
    ProgressBar progressBar;

    Context myContext;
    Resources myResources;

    String bookingId;

    List<ReferenceNumber> referenceNumberList = new ArrayList<>();
    ReferenceNumberAdapter referenceNumberAdapter;

    String userId;

    boolean isLoggedIn = false;

    String defaultCaptionText = "Please send your payment to this/these GCash number/s: ",
            defaultLogText = "No Record";

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
        editor.putBoolean("isRemembered", false);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_payment);

        tvActivityCaption = findViewById(R.id.tvActivityCaption);

        helpImage = findViewById(R.id.helpImage);
        tvHelp = findViewById(R.id.tvHelp);

        tvPrice2 = findViewById(R.id.tvPrice2);
        tvCreditedAmount2 = findViewById(R.id.tvCreditedAmount2);
        tvBalance2 = findViewById(R.id.tvBalance2);

        buttonLayout = findViewById(R.id.buttonLayout);
        refundButton = findViewById(R.id.refundButton);

        tvLog = findViewById(R.id.tvLog);
        reloadImage = findViewById(R.id.reloadImage);
        referenceNumberView = findViewById(R.id.referenceNumberView);
        progressBar = findViewById(R.id.progressBar);

        myContext = OnlinePaymentActivity.this;
        myResources = myContext.getResources();

        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");

        initSharedPreferences();

        firebaseAuth = FirebaseAuth.getInstance();
        if(isLoggedIn) {
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
            else userId = firebaseUser.getUid();
        }

        try {
            Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage);
        }
        catch (Exception ignored) {}

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        referenceNumberView.setLayoutManager(linearLayout);
        referenceNumberAdapter = new ReferenceNumberAdapter(myContext, referenceNumberList);
        referenceNumberView.setAdapter(referenceNumberAdapter);

        helpImage.setOnClickListener(view -> openHelp());
        tvHelp.setOnClickListener(view -> openHelp());

        getReferenceNumber();
    }

    private void openHelp() {
        Intent intent = new Intent(myContext, HelpActivity.class);
        myContext.startActivity(intent);
    }

    private void getReferenceNumber() {
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        firebaseDatabase.getReference("gCashNumberList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder captionText = new StringBuilder(defaultCaptionText);

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String gCashNumber = dataSnapshot.getValue(String.class);
                        captionText.append("\n\t● ").append(gCashNumber);
                    }
                }

                tvActivityCaption.setText(captionText.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        usersRef.child(userId).child("bookingList").child(bookingId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double price, creditedAmount = 0, balance;
                referenceNumberList.clear();

                if(snapshot.exists()) {
                    DataSnapshot referenceNumberListRef = snapshot.child("referenceNumberList");
                    if(referenceNumberListRef.exists()) {
                        for(DataSnapshot dataSnapshot : referenceNumberListRef.getChildren()) {
                            ReferenceNumber referenceNumber = dataSnapshot.getValue(ReferenceNumber.class);
                            if(referenceNumber != null) creditedAmount += referenceNumber.getValue();
                            referenceNumberList.add(referenceNumber);
                        }
                    }

                    DataSnapshot priceRef = snapshot.child("bookingType").child("price");
                    if(priceRef.exists()) {
                        price = priceRef.getValue(Double.class);
                        balance = price - creditedAmount;

                        String priceText = "₱" + price;
                        String creditedAmountText = "₱" + creditedAmount;
                        String balanceText = "₱" + balance;

                        if(priceText.split("\\.")[1].length() == 1) priceText += 0;
                        if(creditedAmountText.split("\\.")[1].length() == 1) creditedAmountText += 0;
                        if(balanceText.split("\\.")[1].length() == 1) balanceText += 0;

                        tvPrice2.setText(priceText);
                        tvCreditedAmount2.setText(creditedAmountText);
                        tvBalance2.setText(balanceText);
                    }

                    DataSnapshot statusRef = snapshot.child("status");
                    if(statusRef.exists()) {
                        String status = statusRef.getValue(String.class);
                        referenceNumberAdapter.setStatus(status);

                        ConstraintLayout.LayoutParams layoutParams =
                                (ConstraintLayout.LayoutParams) tvLog.getLayoutParams();

                        buttonLayout.setVisibility(View.GONE);

                        if(status != null && (status.equals("Pending") || status.equals("Booked"))) {
                            layoutParams.setMargins(layoutParams.leftMargin, dpToPx(64),
                                    layoutParams.rightMargin, layoutParams.bottomMargin);
                        }
                        else {
                            if(status != null && !status.equals("Completed"))
                                buttonLayout.setVisibility(View.VISIBLE);
                            layoutParams.setMargins(layoutParams.leftMargin, dpToPx(24),
                                    layoutParams.rightMargin, layoutParams.bottomMargin);

                            refundButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            });
                        }

                        tvLog.setLayoutParams(layoutParams);
                    }
                }

                if(referenceNumberList.size() > 0) finishLoading();
                else errorLoading(defaultLogText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                errorLoading(error.toString());
            }
        });
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    private void finishLoading() {
        referenceNumberAdapter.notifyDataSetChanged();

        progressBar.setVisibility(View.GONE);
    }

    private void errorLoading(String error) {
        referenceNumberList.clear();
        referenceNumberAdapter.notifyDataSetChanged();

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
}