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
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.ReferenceNumber;
import com.example.firebase_clemenisle_ev.Classes.User;
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

    ConstraintLayout refundAmountLayout, refundedAmountLayout;
    TextView tvActivityCaption, tvActivityCaption2, tvActivityCaption3, tvHelp, tvView,
            tvPrice2, tvCreditedAmount2, tvBalance2, tvRefundAmount2, tvRefundedAmount2, tvLog;
    ImageView helpImage, reloadImage;
    RecyclerView referenceNumberView;
    ProgressBar progressBar;

    Context myContext;
    Resources myResources;

    String bookingId;
    boolean fromIWallet;

    List<ReferenceNumber> referenceNumberList = new ArrayList<>();
    List<String> referenceNumberValueList = new ArrayList<>();
    ReferenceNumberAdapter referenceNumberAdapter;

    String userId;

    boolean isLoggedIn = false;

    String defaultCaptionText = "Please send your payment to this/these GCash number/s: ",
            defaultLogText = "No Record";

    int colorBlue, colorInitial, colorRed;
    ColorStateList cslInitial, cslBlue;

    String referenceNumberValue;

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
        tvActivityCaption2 = findViewById(R.id.tvActivityCaption2);
        tvActivityCaption3 = findViewById(R.id.tvActivityCaption3);

        helpImage = findViewById(R.id.helpImage);
        tvHelp = findViewById(R.id.tvHelp);
        tvView = findViewById(R.id.tvView);

        refundAmountLayout = findViewById(R.id.refundAmountLayout);
        refundedAmountLayout = findViewById(R.id.refundedAmountLayout);

        tvPrice2 = findViewById(R.id.tvPrice2);
        tvCreditedAmount2 = findViewById(R.id.tvCreditedAmount2);
        tvBalance2 = findViewById(R.id.tvBalance2);
        tvRefundAmount2 = findViewById(R.id.tvRefundAmount2);
        tvRefundedAmount2 = findViewById(R.id.tvRefundedAmount2);

        tvLog = findViewById(R.id.tvLog);
        reloadImage = findViewById(R.id.reloadImage);
        referenceNumberView = findViewById(R.id.referenceNumberView);
        progressBar = findViewById(R.id.progressBar);

        myContext = OnlinePaymentActivity.this;
        myResources = myContext.getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);
        colorRed = myResources.getColor(R.color.red);

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));

        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");
        fromIWallet = intent.getBooleanExtra("fromIWallet", false);

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

        tvView.setOnClickListener(view -> {
            if(fromIWallet) onBackPressed();
            else {
                Intent intent1 = new Intent(myContext, IWalletActivity.class);
                intent1.putExtra("bookingId", bookingId);
                startActivity(intent1);
            }
        });

        getReferenceNumber();
    }

    private void setDialogScreenEnabled(boolean value) {
        dialog.setCanceledOnTouchOutside(value);
        tlReferenceNumber.setEnabled(value);
        submitButton.setEnabled(value);
        dialogCloseImage.setEnabled(value);

        if(value) dialogCloseImage.getDrawable().setTint(colorRed);
        else dialogCloseImage.getDrawable().setTint(colorInitial);
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

    private boolean isReferenceNumberExisting(String targetReferenceNumber) {
        for(String referenceNumberValue : referenceNumberValueList) {
            if(targetReferenceNumber.equals(referenceNumberValue))
                return true;
        }
        return false;
    }

    private void submitReferenceNumber() {
        setDialogScreenEnabled(false);
        dialogProgressBar.setVisibility(View.VISIBLE);

        if(isReferenceNumberExisting(referenceNumberValue)) {
            Toast.makeText(
                    myContext,
                    "The Reference Number is already existing",
                    Toast.LENGTH_LONG
            ).show();

            setDialogScreenEnabled(true);
            dialogProgressBar.setVisibility(View.GONE);
            return;
        }

        String rnIdSuffix = String.valueOf(referenceNumberList.size() + 1);
        if(rnIdSuffix.length() == 1) rnIdSuffix = "0" + rnIdSuffix;
        String rnId = "RN" + rnIdSuffix;

        ReferenceNumber referenceNumber = new ReferenceNumber(rnId, referenceNumberValue,
                new DateTimeToString().getDateAndTime(), 0);

        DatabaseReference rnRef = usersRef.child(userId).child("bookingList").child(bookingId).
                child("referenceNumberList").child(rnId);
        rnRef.setValue(referenceNumber).addOnCompleteListener(task -> {
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
                    setDialogScreenEnabled(true);
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

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double price = 0, creditedAmount = 0, balance = 0, refundAmount = 0, refundedAmount = 0;
                String status = "Booked";

                referenceNumberList.clear();
                referenceNumberValueList.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);
                        List<Booking> bookingList = user.getBookingList();

                        for(Booking booking : bookingList) {
                            List<ReferenceNumber> referenceNumberList1 =
                                    booking.getReferenceNumberList();

                            for(ReferenceNumber referenceNumber : referenceNumberList1) {
                                if(referenceNumber != null) {
                                    referenceNumberValueList.add(referenceNumber.getReferenceNumber());

                                    if(user.getId().equals(userId) && booking.getId().equals(bookingId)) {
                                        creditedAmount += referenceNumber.getValue();
                                        referenceNumber.setUserId(userId);
                                        referenceNumber.setBookingId(bookingId);

                                        referenceNumberList.add(referenceNumber);
                                    }
                                }
                            }

                            if(booking.getId().equals(bookingId)) {
                                price = booking.getBookingType().getPrice();
                                refundedAmount = booking.getRefundedAmount();
                                status = booking.getStatus();
                            }
                        }
                    }
                }

                balance = price - creditedAmount;
                if(balance < 0) {
                    referenceNumberAdapter.setShowAddRN(false);

                    refundAmount = balance * -1;
                    balance = 0;
                    refundAmount -= refundedAmount;
                }
                refundAmountLayout.setVisibility(View.GONE);
                refundedAmountLayout.setVisibility(View.GONE);

                if(refundAmount != 0 || refundedAmount != 0)
                    refundAmountLayout.setVisibility(View.VISIBLE);
                if(refundedAmount != 0) refundedAmountLayout.setVisibility(View.VISIBLE);

                String priceText = "₱" + price;
                String creditedAmountText = "₱" + creditedAmount;
                String balanceText = "₱" + balance;
                String refundAmountText = "₱" + refundAmount;
                String refundedAmountText = "₱" + refundedAmount;

                if(priceText.split("\\.")[1].length() == 1) priceText += 0;
                if(creditedAmountText.split("\\.")[1].length() == 1) creditedAmountText += 0;
                if(balanceText.split("\\.")[1].length() == 1) balanceText += 0;
                if(refundAmountText.split("\\.")[1].length() == 1) refundAmountText += 0;
                if(refundedAmountText.split("\\.")[1].length() == 1) refundedAmountText += 0;

                tvPrice2.setText(priceText);
                tvCreditedAmount2.setText(creditedAmountText);
                tvBalance2.setText(balanceText);
                tvRefundAmount2.setText(refundAmountText);
                tvRefundedAmount2.setText(refundedAmountText);

                referenceNumberAdapter.setStatus(status);

                ConstraintLayout.LayoutParams layoutParams =
                        (ConstraintLayout.LayoutParams) tvLog.getLayoutParams();

                if(status != null && (status.equals("Pending") || status.equals("Booked"))) {
                    setActivityCaptionVisibility(true);
                    layoutParams.setMargins(layoutParams.leftMargin, dpToPx(64),
                            layoutParams.rightMargin, layoutParams.bottomMargin);
                }
                else {
                    setActivityCaptionVisibility(false);
                    layoutParams.setMargins(layoutParams.leftMargin, dpToPx(24),
                            layoutParams.rightMargin, layoutParams.bottomMargin);
                }

                tvLog.setLayoutParams(layoutParams);

                Collections.sort(referenceNumberList, (referenceNumber, t1) -> {
                    DateTimeToString dateTimeToString = new DateTimeToString();
                    dateTimeToString.setFormattedSchedule(referenceNumber.getTimestamp());
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

    private void setActivityCaptionVisibility(boolean value) {
        if(value) {
            tvActivityCaption.setVisibility(View.VISIBLE);
            tvActivityCaption2.setVisibility(View.VISIBLE);
        }
        else {
            tvActivityCaption.setVisibility(View.GONE);
            tvActivityCaption2.setVisibility(View.GONE);
        }
    }


    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    private void finishLoading() {
        referenceNumberAdapter.notifyDataSetChanged();

        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
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
        etReferenceNumber.setText(null);
        dialog.show();
    }
}