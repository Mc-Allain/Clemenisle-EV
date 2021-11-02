package com.example.firebase_clemenisle_ev;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Adapters.ReferenceNumberAdapter;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.ReferenceNumber;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OnlinePaymentActivity extends AppCompatActivity implements ReferenceNumberAdapter.OnAddRNListener {

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

    String referenceNumberValue;

    int colorBlue, colorInitial;
    ColorStateList cslInitial, cslBlue;

    Dialog dialog;
    TextView tvDialogTitle, tvDialogCaption;
    EditText etReferenceNumber;
    TextInputLayout tlReferenceNumber;
    Button submitButton;
    ImageView dialogCloseImage;
    ProgressBar dialogProgressBar;

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

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));

        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");

        initSharedPreferences();
        initAddReferenceNumberDialog();

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
        referenceNumberAdapter.setOnAddRNListener(this);

        helpImage.setOnClickListener(view -> openHelp());
        tvHelp.setOnClickListener(view -> openHelp());

        getReferenceNumber();
    }

    private void initAddReferenceNumberDialog() {
        dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_input_reference_number_layout);

        tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        tvDialogCaption = dialog.findViewById(R.id.tvDialogCaption);
        etReferenceNumber = dialog.findViewById(R.id.etReferenceNumber);
        tlReferenceNumber = dialog.findViewById(R.id.tlReferenceNumber);
        submitButton = dialog.findViewById(R.id.submitButton);
        dialogCloseImage = dialog.findViewById(R.id.dialogCloseImage);
        dialogProgressBar = dialog.findViewById(R.id.dialogProgressBar);

        etReferenceNumber.setOnFocusChangeListener((view1, b) -> {
            if(!tlReferenceNumber.isErrorEnabled()) {
                if(b) {
                    tlReferenceNumber.setStartIconTintList(cslBlue);
                }
                else {
                    tlReferenceNumber.setStartIconTintList(cslInitial);
                }
            }
        });

        etReferenceNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                referenceNumberValue = etReferenceNumber.getText().toString();
                submitButton.setEnabled(referenceNumberValue.length() != 0);
            }
        });

        submitButton.setOnClickListener(view -> submitReferenceNumber());

        dialogCloseImage.setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void submitReferenceNumber() {
        dialogProgressBar.setVisibility(View.VISIBLE);

        String rnIdSuffix = String.valueOf(referenceNumberList.size() + 1);
        if(rnIdSuffix.length() == 1) rnIdSuffix = "0" + rnIdSuffix;
        String rnId = "RN" + rnIdSuffix;

        ReferenceNumber referenceNumber = new ReferenceNumber(rnId, referenceNumberValue,
                new DateTimeToString().getDateAndTime(), 0);

        usersRef.child(userId).child("bookingList").child(bookingId).
                child("referenceNumberList").child(rnId).setValue(referenceNumber)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        dialog.dismiss();

                        Toast.makeText(
                                myContext,
                                "Successfully submitted a reference number",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                    else {
                        if(task.getException() != null) {
                            String error = task.getException().toString();

                            Toast.makeText(
                                    myContext,
                                    error,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                    dialogProgressBar.setVisibility(View.GONE);
                });
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

                Collections.sort(referenceNumberList, (referenceNumberList, t1) -> {
                    DateTimeToString dateTimeToString = new DateTimeToString();
                    dateTimeToString.setFormattedSchedule(referenceNumberList.getTimestamp());
                    String rnTS = dateTimeToString.getDateNo(true) + " " +
                            dateTimeToString.getTime(true);
                    dateTimeToString.setFormattedSchedule(t1.getTimestamp());
                    String rnTS1 = dateTimeToString.getDateNo(true) + " " +
                            dateTimeToString.getTime(true);

                    return rnTS1.compareToIgnoreCase(rnTS);
                });

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

    @Override
    public void addReferenceNumber() {
        dialog.show();
    }
}