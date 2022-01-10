package com.example.firebase_clemenisle_ev;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
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
import com.example.firebase_clemenisle_ev.Adapters.OnlinePaymentAdapter;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.IWalletTransaction;
import com.example.firebase_clemenisle_ev.Classes.OnlinePayment;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OnlinePaymentActivity extends AppCompatActivity implements OnlinePaymentAdapter.OnInitiatePaymentListener {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout refundedAmountLayout;
    TextView tvActivityTitle, tvActivityCaption, tvActivityCaption2, tvActivityCaption3, tvHelp, tvView,
            tvPrice2, tvCreditedAmount2, tvBalance2, tvRefundedAmount2, tvLog;
    ImageView helpImage, reloadImage;
    RecyclerView referenceNumberView;
    ProgressBar progressBar;

    Context myContext;
    Resources myResources;

    String bookingId;
    boolean fromIWallet;

    List<OnlinePayment> onlinePaymentList = new ArrayList<>();
    List<String> referenceNumberValueList = new ArrayList<>();
    OnlinePaymentAdapter onlinePaymentAdapter;

    List<IWalletTransaction> transactionList = new ArrayList<>();

    String userId;

    boolean isLoggedIn = false;

    String defaultCaptionText = "Please send your payment to this/these GCash number/s: ",
            defaultLogText = "No Record";

    ColorStateList cslInitial, cslBlue, cslRed;
    int colorRed, colorBlue, colorInitial;

    String referenceNumberValue;

    Dialog dialog;
    TextView tvDialogTitle, tvDialogCaption;
    EditText etReferenceNumber;
    TextInputLayout tlReferenceNumber;
    Button submitButton;
    ImageView dialogCloseImage;
    ProgressBar dialogProgressBar;

    double iWalletAmount, price, amount;

    Dialog dialog2;
    TextView tvDialogTitle2, tvDialogCaption2;
    EditText etAmount;
    TextInputLayout tlAmount;
    Button submitButton2;
    ImageView dialogCloseImage2;
    ProgressBar dialogProgressBar2;

    long submitPressedTime;
    Toast submitToast;

    boolean isGeneratingTransactionId = false;

    boolean inDriverModule;

    Dialog confirmationDialog;
    ImageView confirmationDialogCloseImage;
    TextView tvDialogTitleConfirmation, tvDialogCaptionConfirmation;
    Button confirmationDialogConfirmButton, confirmationDialogCancelButton;
    ProgressBar confirmationDialogProgressBar;

    boolean isConfirmationDialogEnabled;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        isConfirmationDialogEnabled = sharedPreferences.getBoolean("isConfirmationDialogEnabled", true);
    }

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("isRemembered", false);
        editor.apply();

        NotificationManager notificationManager = (NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_payment);


        tvActivityTitle = findViewById(R.id.tvActivityTitle);
        tvActivityCaption = findViewById(R.id.tvActivityCaption);
        tvActivityCaption2 = findViewById(R.id.tvActivityCaption2);
        tvActivityCaption3 = findViewById(R.id.tvActivityCaption3);

        helpImage = findViewById(R.id.helpImage);
        tvHelp = findViewById(R.id.tvHelp);
        tvView = findViewById(R.id.tvView);

        refundedAmountLayout = findViewById(R.id.refundedAmountLayout);

        tvPrice2 = findViewById(R.id.tvPrice2);
        tvCreditedAmount2 = findViewById(R.id.tvCreditedAmount2);
        tvBalance2 = findViewById(R.id.tvBalance2);
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
        cslRed = ColorStateList.valueOf(myResources.getColor(R.color.red));

        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");
        fromIWallet = intent.getBooleanExtra("fromIWallet", false);
        inDriverModule = intent.getBooleanExtra("inDriverModule", false);

        initSharedPreferences();
        initAddReferenceNumberDialog();
        iniIWalletPaymentDialog();
        initConfirmationDialog();

        firebaseAuth = FirebaseAuth.getInstance();
        if(isLoggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) firebaseUser.reload();
            if(firebaseUser == null) {
                firebaseAuth.signOut();
                sendLoginPreferences();

                Toast.makeText(
                        myContext,
                        "Failed to get the current user. Account logged out.",
                        Toast.LENGTH_LONG
                ).show();
            }
            else {
                if(inDriverModule) {
                    tvView.setVisibility(View.GONE);
                    userId = intent.getStringExtra("passengerUserId");
                }
                else {
                    tvView.setVisibility(View.VISIBLE);
                    userId = firebaseUser.getUid();
                }
            }
        }
        else {
            Toast.makeText(
                    myContext,
                    "You must logged in to access this information",
                    Toast.LENGTH_LONG
            ).show();
            onBackPressed();
        }

        try {
            Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage);
        }
        catch (Exception ignored) {}

        if(inDriverModule) tvActivityTitle.setText("Passenger's Online Payment");

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        referenceNumberView.setLayoutManager(linearLayout);
        onlinePaymentAdapter = new OnlinePaymentAdapter(myContext, onlinePaymentList, inDriverModule);
        referenceNumberView.setAdapter(onlinePaymentAdapter);
        onlinePaymentAdapter.setInitiatePaymentListener(this);

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
        getTransactionList();
    }

    private void getTransactionList() {
        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                if(snapshot.exists()) {
                    User user = new User(snapshot);
                    transactionList.addAll(user.getTransactionList());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setDialogScreenEnabled(boolean value) {
        dialog.setCanceledOnTouchOutside(value);
        dialog.setCancelable(value);
        tlReferenceNumber.setEnabled(value);
        submitButton.setEnabled(value);

        dialog2.setCanceledOnTouchOutside(value);
        dialog2.setCancelable(value);
        tlAmount.setEnabled(value);
        submitButton2.setEnabled(value);

        confirmationDialog.setCanceledOnTouchOutside(value);
        confirmationDialog.setCancelable(value);
        confirmationDialogConfirmButton.setEnabled(value);
        confirmationDialogCancelButton.setEnabled(value);

        if(value) {
            dialogCloseImage.getDrawable().setTint(colorRed);
            dialogCloseImage2.getDrawable().setTint(colorRed);
            confirmationDialogCloseImage.getDrawable().setTint(colorRed);
        }
        else {
            dialogCloseImage.getDrawable().setTint(colorInitial);
            dialogCloseImage2.getDrawable().setTint(colorInitial);
            confirmationDialogCloseImage.getDrawable().setTint(colorInitial);
        }
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
                submitButton.setEnabled(referenceNumberValue.length() == tlReferenceNumber.getCounterMaxLength());
            }
        });

        submitButton.setOnClickListener(view -> {
            if(isConfirmationDialogEnabled) {
                openConfirmationDialog("Add Reference Number",
                        "Do you want to submit the Reference Number of " + referenceNumberValue + "?" );
                confirmationDialogConfirmButton.setOnClickListener(view1 -> {
                    if(!inDriverModule) submitReferenceNumber();
                });
            }
            else {
                if (submitPressedTime + 2500 > System.currentTimeMillis()) {
                    submitToast.cancel();

                    if(!inDriverModule) submitReferenceNumber();
                } else {
                    submitToast = Toast.makeText(myContext,
                            "Press again to submit", Toast.LENGTH_SHORT);
                    submitToast.show();
                }

                submitPressedTime = System.currentTimeMillis();
            }
        });

        dialogCloseImage.setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
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
        if(isConfirmationDialogEnabled)
            confirmationDialogProgressBar.setVisibility(View.VISIBLE);
        else dialogProgressBar.setVisibility(View.VISIBLE);
        setDialogScreenEnabled(false);

        tlReferenceNumber.setStartIconTintList(cslInitial);

        if(isReferenceNumberExisting(referenceNumberValue)) {
            Toast.makeText(
                    myContext,
                    "The Reference Number is already existing",
                    Toast.LENGTH_LONG
            ).show();

            if(isConfirmationDialogEnabled)
                confirmationDialogProgressBar.setVisibility(View.GONE);
            else dialogProgressBar.setVisibility(View.GONE);
            setDialogScreenEnabled(true);
            return;
        }

        String opId = getOpId();

        OnlinePayment onlinePayment = new OnlinePayment(opId,
                new DateTimeToString().getDateAndTime(), 0);
        onlinePayment.setReferenceNumber(referenceNumberValue);

        if(!inDriverModule) addOnlinePayment(onlinePayment, opId, 0);
    }

    private String getOpId() {
        String opIdSuffix = String.valueOf(onlinePaymentList.size() + 1);
        if(opIdSuffix.length() == 1) opIdSuffix = "0" + opIdSuffix;
        return "OP" + opIdSuffix;
    }

    private void addOnlinePayment(OnlinePayment onlinePayment, String opId, int sender) {
        DatabaseReference rnRef = usersRef.child(userId).child("bookingList").child(bookingId).
                child("onlinePaymentList").child(opId);
        rnRef.setValue(onlinePayment).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                dialog.dismiss();
                dialog2.dismiss();
                confirmationDialog.dismiss();

                if(sender == 0) {
                    Toast.makeText(
                            myContext,
                            "Successfully submitted a reference number",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                else if(sender == 1) {
                    String amountValue = "₱" + amount;
                    if(amountValue.split("\\.")[1].length() == 1) amountValue += 0;

                    Toast.makeText(
                            myContext,
                            "Successfully paid using iWallet. Amount: " + amountValue,
                            Toast.LENGTH_SHORT
                    ).show();
                }
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

            if(isConfirmationDialogEnabled)
                confirmationDialogProgressBar.setVisibility(View.GONE);
            else {
                dialogProgressBar.setVisibility(View.GONE);
                dialogProgressBar2.setVisibility(View.GONE);
            }
            setDialogScreenEnabled(true);

            tlReferenceNumber.setStartIconTintList(cslInitial);
            tlReferenceNumber.clearFocus();
            tlReferenceNumber.requestFocus();

            tlAmount.setStartIconTintList(cslInitial);
            tlAmount.clearFocus();
            tlAmount.requestFocus();
        });
    }

    private void iniIWalletPaymentDialog() {
        dialog2 = new Dialog(myContext);
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog2.setContentView(R.layout.dialog_input_iwallet_payment_layout);

        tvDialogTitle2 = dialog2.findViewById(R.id.tvDialogTitle);
        tvDialogCaption2 = dialog2.findViewById(R.id.tvDialogCaption);
        etAmount = dialog2.findViewById(R.id.etAmount);
        tlAmount = dialog2.findViewById(R.id.tlAmount);
        submitButton2 = dialog2.findViewById(R.id.submitButton);
        dialogCloseImage2 = dialog2.findViewById(R.id.dialogCloseImage);
        dialogProgressBar2 = dialog2.findViewById(R.id.dialogProgressBar);

        etAmount.setOnFocusChangeListener((view1, b) -> {
            if(!tlAmount.isErrorEnabled()) {
                if(b) {
                    tlAmount.setStartIconTintList(cslBlue);
                }
                else {
                    tlAmount.setStartIconTintList(cslInitial);
                }
            }
        });

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(etAmount.getText().toString().length() >= 1)
                    amount = Double.parseDouble(etAmount.getText().toString());
                else amount = 0;

                double maxAmount = iWalletAmount;

                if(maxAmount == 0) {
                    tlAmount.setErrorEnabled(true);
                    String error = "You do not have enough iWallet to pay";
                    tlAmount.setError(error);
                    tlAmount.setErrorTextColor(cslRed);
                    tlAmount.setStartIconTintList(cslRed);
                }
                else if(amount == 0) {
                    tlAmount.setErrorEnabled(true);
                    String error = "Amount must be at least ₱1.00";
                    tlAmount.setError(error);
                    tlAmount.setErrorTextColor(cslRed);
                    tlAmount.setStartIconTintList(cslRed);
                }
                else if(amount > price) {
                    amount = price;
                    etAmount.setText(String.valueOf(amount));
                }
                else if(amount > maxAmount) {
                    tlAmount.setErrorEnabled(true);

                    String iWallet = "₱" + maxAmount;
                    if(iWallet.split("\\.")[1].length() == 1) iWallet += 0;
                    String error = "You only have " + iWallet + " in your iWallet";

                    tlAmount.setError(error);
                    tlAmount.setErrorTextColor(cslRed);
                    tlAmount.setStartIconTintList(cslRed);
                }
                else {
                    tlAmount.setErrorEnabled(false);
                    tlAmount.setError(null);
                    tlAmount.setStartIconTintList(cslBlue);
                }

                submitButton2.setEnabled(amount > 0 && amount <= maxAmount);
            }
        });

        submitButton2.setOnClickListener(view -> {
            if(isConfirmationDialogEnabled) {
                String amountValue = "₱" + amount;
                if(amountValue.split("\\.")[1].length() == 1) amountValue += 0;

                openConfirmationDialog("Use IWallet as Payment",
                        "Do you want to pay " + amountValue + "?" );
                confirmationDialogConfirmButton.setOnClickListener(view1 -> generateTransactionId());
            }
            else {
                if (submitPressedTime + 2500 > System.currentTimeMillis()) {
                    submitToast.cancel();

                    if(!inDriverModule) generateTransactionId();
                } else {
                    submitToast = Toast.makeText(myContext,
                            "Press again to submit", Toast.LENGTH_SHORT);
                    submitToast.show();
                }

                submitPressedTime = System.currentTimeMillis();
            }
        });

        dialogCloseImage2.setOnClickListener(view -> dialog2.dismiss());

        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog2.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        dialog2.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog2.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void generateTransactionId() {
        if(isConfirmationDialogEnabled)
            confirmationDialogProgressBar.setVisibility(View.VISIBLE);
        else dialogProgressBar2.setVisibility(View.VISIBLE);
        setDialogScreenEnabled(false);

        tlAmount.setStartIconTintList(cslInitial);

        isGeneratingTransactionId = false;
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!isGeneratingTransactionId) {
                    isGeneratingTransactionId = true;

                    DateTimeToString dateTimeToString = new DateTimeToString();
                    String yearId = dateTimeToString.getYear2Suffix();
                    int month = Integer.parseInt(dateTimeToString.getMonthNo()) + 1;
                    String monthId = String.valueOf(month);
                    if(monthId.length() == 1) monthId = "0" + monthId;
                    String dayId = dateTimeToString.getDay();
                    if(dayId.length() == 1) dayId = "0" + dayId;

                    String transactionId = "T" + yearId + "-" + monthId + dayId;

                    int suffixCount = 0;

                    if(snapshot.exists()) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = new User(dataSnapshot);

                            List<IWalletTransaction> transactionList = user.getTransactionList();
                            if(transactionList.size() > 0) {
                                for(IWalletTransaction transaction : transactionList) {
                                    if(transaction.getId().startsWith(transactionId)) {
                                        suffixCount++;
                                    }
                                }
                            }
                        }
                    }
                    String idSuffix = String.valueOf(suffixCount);
                    if(idSuffix.length() == 1) idSuffix = "0" + idSuffix;

                    transactionId += "-" + idSuffix;

                    submitIWalletPayment(transactionId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                isGeneratingTransactionId = false;

                if(isConfirmationDialogEnabled)
                    confirmationDialogProgressBar.setVisibility(View.GONE);
                else dialogProgressBar2.setVisibility(View.GONE);
                setDialogScreenEnabled(true);
            }
        });
    }

    private void submitIWalletPayment(String transactionId) {
        String opId = getOpId();

        OnlinePayment onlinePayment = new OnlinePayment(opId,
                new DateTimeToString().getDateAndTime(), amount);
        onlinePayment.setiWalletUsed(true);
        onlinePayment.setNotified(false);

        if(!inDriverModule) addToTransactionList(transactionId, onlinePayment, opId);
    }

    private void addToTransactionList(String tId, OnlinePayment onlinePayment, String opId) {
        IWalletTransaction transaction = new IWalletTransaction(tId,
                new DateTimeToString().getDateAndTime(), "Payment", amount);
        transaction.setBookingId(bookingId);

        usersRef.child(userId).child("iWalletTransactionList").child(tId).setValue(transaction).
                addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        double newIWallet = iWalletAmount - amount;
                        usersRef.child(userId).child("iwallet").setValue(newIWallet);

                        addOnlinePayment(onlinePayment, opId, 1);
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

                        if(isConfirmationDialogEnabled)
                            confirmationDialogProgressBar.setVisibility(View.GONE);
                        else dialogProgressBar2.setVisibility(View.GONE);
                        setDialogScreenEnabled(true);
                    }
                });
    }

    private void openConfirmationDialog(String title, String caption) {
        tvDialogTitleConfirmation.setText(title);
        tvDialogCaptionConfirmation.setText(caption);
        confirmationDialog.show();
    }

    private void initConfirmationDialog() {
        confirmationDialog = new Dialog(myContext);
        confirmationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        confirmationDialog.setContentView(R.layout.dialog_confirmation_layout);

        confirmationDialogCloseImage = confirmationDialog.findViewById(R.id.dialogCloseImage);
        tvDialogTitleConfirmation = confirmationDialog.findViewById(R.id.tvDialogTitle);
        tvDialogCaptionConfirmation = confirmationDialog.findViewById(R.id.tvDialogCaption);
        confirmationDialogConfirmButton = confirmationDialog.findViewById(R.id.confirmButton);
        confirmationDialogCancelButton = confirmationDialog.findViewById(R.id.cancelButton);
        confirmationDialogProgressBar = confirmationDialog.findViewById(R.id.dialogProgressBar);

        confirmationDialogCloseImage.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialogCancelButton.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        confirmationDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        confirmationDialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        confirmationDialog.getWindow().setGravity(Gravity.BOTTOM);
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
                tvDialogCaption.setText(captionText.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                price = 0;
                double creditedAmount = 0, balance, refundedAmount = 0;
                String status = "Booked";
                StringBuilder helpText = new StringBuilder("Current iWallet Amount: ");
                boolean isPaid = false;

                onlinePaymentList.clear();
                referenceNumberValueList.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);
                        List<Booking> bookingList = user.getBookingList();

                        if(user.getId().equals(userId)) {
                            iWalletAmount = user.getIWallet();
                            String iWallet = "₱" + iWalletAmount;
                            if(iWallet.split("\\.")[1].length() == 1) iWallet += 0;

                            helpText.append(iWallet);
                        }

                        for(Booking booking : bookingList) {
                            List<OnlinePayment> onlinePaymentList1 =
                                    booking.getReferenceNumberList();

                            for(OnlinePayment onlinePayment : onlinePaymentList1) {
                                if(onlinePayment != null) {
                                    referenceNumberValueList.add(onlinePayment.getReferenceNumber());

                                    if(user.getId().equals(userId) && booking.getId().equals(bookingId)) {
                                        creditedAmount += onlinePayment.getValue();

                                        onlinePaymentList.add(onlinePayment);
                                    }
                                }
                            }

                            if(booking.getId().equals(bookingId)) {
                                price = booking.getBookingType().getPrice();
                                refundedAmount = booking.getRefundedAmount();
                                status = booking.getStatus();
                                isPaid = booking.isPaid();
                            }
                        }

                        List<IWalletTransaction> transactionList = user.getTransactionList();
                        for (IWalletTransaction transaction : transactionList) {
                            String referenceNumber = transaction.getReferenceNumber();
                            if (referenceNumber != null)
                                referenceNumberValueList.add(referenceNumber);
                        }
                    }
                }

                balance = price - creditedAmount;
                if(balance <= 0) {
                    onlinePaymentAdapter.setCompletePayment(true);
                    balance = 0;

                    if(!isPaid && !inDriverModule) usersRef.child(userId).child("bookingList").child(bookingId).
                            child("paid").setValue(true);
                }
                else {
                    onlinePaymentAdapter.setCompletePayment(false);

                    if(isPaid && !inDriverModule) usersRef.child(userId).child("bookingList").child(bookingId).
                            child("paid").setValue(false);
                }
                refundedAmountLayout.setVisibility(View.GONE);

                if(refundedAmount != 0) refundedAmountLayout.setVisibility(View.VISIBLE);

                String priceText = "₱" + price;
                String creditedAmountText = "₱" + creditedAmount;
                String balanceText = "₱" + balance;
                String refundedAmountText = "₱" + refundedAmount;

                if(priceText.split("\\.")[1].length() == 1) priceText += 0;
                if(creditedAmountText.split("\\.")[1].length() == 1) creditedAmountText += 0;
                if(balanceText.split("\\.")[1].length() == 1) balanceText += 0;
                if(refundedAmountText.split("\\.")[1].length() == 1) refundedAmountText += 0;

                tvPrice2.setText(priceText);
                tvCreditedAmount2.setText(creditedAmountText);
                tvBalance2.setText(balanceText);
                tvRefundedAmount2.setText(refundedAmountText);

                onlinePaymentAdapter.setStatus(status);

                helpText.append("\nAmount to Pay: ").append(priceText);
                tlAmount.setHelperText(helpText.toString());

                ConstraintLayout.LayoutParams layoutParams =
                        (ConstraintLayout.LayoutParams) tvLog.getLayoutParams();

                if((status.equals("Pending") || status.equals("Booked")) && !inDriverModule) {
                    setActivityCaptionVisibility(true);
                    layoutParams.setMargins(layoutParams.leftMargin, dpToPx(96),
                            layoutParams.rightMargin, layoutParams.bottomMargin);
                }
                else {
                    setActivityCaptionVisibility(false);
                    layoutParams.setMargins(layoutParams.leftMargin, dpToPx(24),
                            layoutParams.rightMargin, layoutParams.bottomMargin);
                }

                tvLog.setLayoutParams(layoutParams);

                Collections.sort(onlinePaymentList, (referenceNumber, t1) -> {
                    DateTimeToString dateTimeToString = new DateTimeToString();
                    dateTimeToString.setFormattedSchedule(referenceNumber.getTimestamp());
                    String rnTS = dateTimeToString.getDateNo(true) + " " +
                            dateTimeToString.getTime(true);
                    dateTimeToString.setFormattedSchedule(t1.getTimestamp());
                    String rnTS1 = dateTimeToString.getDateNo(true) + " " +
                            dateTimeToString.getTime(true);

                    return rnTS1.compareToIgnoreCase(rnTS);
                });

                if(onlinePaymentList.size() > 0) finishLoading();
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
        onlinePaymentAdapter.notifyDataSetChanged();

        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void errorLoading(String error) {
        onlinePaymentList.clear();
        onlinePaymentAdapter.notifyDataSetChanged();

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void addReferenceNumber() {
        etReferenceNumber.setText(null);

        tlReferenceNumber.setErrorEnabled(false);
        tlReferenceNumber.setError(null);
        tlReferenceNumber.setStartIconTintList(cslInitial);

        tlReferenceNumber.clearFocus();
        tlReferenceNumber.requestFocus();
        dialog.show();
    }

    @Override
    public void useIWallet() {
        etAmount.setText("0");

        tlAmount.setErrorEnabled(false);
        tlAmount.setError(null);
        tlAmount.setStartIconTintList(cslInitial);

        tlAmount.clearFocus();
        tlAmount.requestFocus();
        dialog2.show();
    }
}