package com.example.firebase_clemenisle_ev;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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

import com.example.firebase_clemenisle_ev.Adapters.IWalletTransactionAdapter;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.IWalletTransaction;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class IWalletActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    TextView tvIWallet;
    RecyclerView transactionView;

    ImageView reloadImage;
    TextView  tvLog;
    ProgressBar progressBar;

    Button topUpButton, transferButton;

    Context myContext;
    Resources myResources;

    List<IWalletTransaction> transactionList = new ArrayList<>();
    IWalletTransactionAdapter iWalletTransactionAdapter;

    String userId;
    User user;

    boolean isLoggedIn = false;

    String defaultLogText = "No Record";

    ColorStateList cslInitial, cslBlue;
    int colorRed, colorBlue, colorInitial;

    String mobileNumber;

    Dialog transferDialog;
    TextView tvDialogTitle, tvDialogCaption, tvSendOTP;
    EditText etMobileNumber, etOTP;
    TextInputLayout tlMobileNumber, tlOTP;
    Button submitButton;
    ImageView dialogCloseImage, otpImage;
    ProgressBar dialogProgressBar;

    String otpSent;

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
        setContentView(R.layout.activity_iwallet);

        tvIWallet = findViewById(R.id.tvIWallet);
        transactionView = findViewById(R.id.transactionView);

        reloadImage = findViewById(R.id.reloadImage);
        tvLog = findViewById(R.id.tvLog);
        progressBar = findViewById(R.id.progressBar);

        topUpButton = findViewById(R.id.topUpButton);
        transferButton = findViewById(R.id.transferButton);

        myContext = IWalletActivity.this;
        myResources = getResources();

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);
        colorRed = myResources.getColor(R.color.red);

        initSharedPreferences();
        initTransferDialog();

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

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        transactionView.setLayoutManager(linearLayout);
        iWalletTransactionAdapter = new IWalletTransactionAdapter(myContext, transactionList);
        transactionView.setAdapter(iWalletTransactionAdapter);

        getTransactionList();

        transferButton.setOnClickListener(view ->{
            etMobileNumber.setText(null);
            etOTP.setText(null);

            tlMobileNumber.setStartIconTintList(cslInitial);
            tlOTP.setStartIconTintList(cslInitial);

            etMobileNumber.clearFocus();
            etMobileNumber.requestFocus();

            transferDialog.show();
        });
    }

    private void initTransferDialog() {
        transferDialog = new Dialog(myContext);
        transferDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        transferDialog.setContentView(R.layout.dialog_transfer_iwallet_layout);

        tvDialogTitle = transferDialog.findViewById(R.id.tvDialogTitle);
        tvDialogCaption = transferDialog.findViewById(R.id.tvDialogCaption);
        etMobileNumber = transferDialog.findViewById(R.id.etMobileNumber);
        tlMobileNumber = transferDialog.findViewById(R.id.tlMobileNumber);
        etOTP = transferDialog.findViewById(R.id.etOTP);
        tlOTP = transferDialog.findViewById(R.id.tlOTP);
        submitButton = transferDialog.findViewById(R.id.submitButton);
        dialogCloseImage = transferDialog.findViewById(R.id.dialogCloseImage);
        dialogProgressBar = transferDialog.findViewById(R.id.dialogProgressBar);

        tvSendOTP = transferDialog.findViewById(R.id.tvSendOTP);
        otpImage = transferDialog.findViewById(R.id.otpImage);

        etMobileNumber.setOnFocusChangeListener((view1, b) -> {
            if(!tlMobileNumber.isErrorEnabled()) {
                if(b) {
                    tlMobileNumber.setStartIconTintList(cslBlue);
                }
                else {
                    tlMobileNumber.setStartIconTintList(cslInitial);
                }
            }
        });

        etMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String prefixMobileNumber = "";
                if(tlMobileNumber.getPrefixText() != null)
                    prefixMobileNumber = tlMobileNumber.getPrefixText().toString();

                mobileNumber = prefixMobileNumber + etMobileNumber.getText().toString();

                boolean isEnabled = mobileNumber.length() == 13;
                tvSendOTP.setEnabled(isEnabled);
                otpImage.setEnabled(isEnabled);

                if(isEnabled) {
                    tvSendOTP.setTextColor(colorBlue);
                    otpImage.getDrawable().setTint(colorBlue);
                }
                else {
                    tvSendOTP.setTextColor(colorInitial);
                    otpImage.getDrawable().setTint(colorInitial);
                }
            }
        });

        etOTP.setOnFocusChangeListener((view1, b) -> {
            if(!tlOTP.isErrorEnabled()) {
                if(b) {
                    tlOTP.setStartIconTintList(cslBlue);
                }
                else {
                    tlOTP.setStartIconTintList(cslInitial);
                }
            }
        });

        etOTP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        submitButton.setOnClickListener(view -> submitRequest());

        dialogCloseImage.setOnClickListener(view -> transferDialog.dismiss());

        tvSendOTP.setOnClickListener(view -> sendVerificationOTP());
        otpImage.setOnClickListener(view -> sendVerificationOTP());

        transferDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        transferDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        transferDialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        transferDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    @SuppressWarnings("deprecation")
    private void sendVerificationOTP() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobileNumber, 3, TimeUnit.MINUTES, (Activity) TaskExecutors.MAIN_THREAD,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onCodeSent(@NonNull String value,
                                           @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(value, forceResendingToken);
                        otpSent = value;
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        String otp = phoneAuthCredential.getSmsCode();
                        if(otp != null) {
                            dialogProgressBar.setVisibility(View.VISIBLE);
                            verifyOTP(otp);
                        }
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(
                                myContext,
                                e.toString(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void verifyOTP(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpSent, otp);
    }

    private void submitRequest() {

    }

    private void getTransactionList() {
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        transactionView.setVisibility(View.INVISIBLE);

        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                if(snapshot.exists()) {
                    user = new User(snapshot);
                    transactionList.addAll(user.getTransactionList());
                }

                Collections.sort(transactionList, (transaction, t1) -> {
                    DateTimeToString dateTimeToString = new DateTimeToString();
                    dateTimeToString.setFormattedSchedule(transaction.getTimestamp());
                    String wtTS = dateTimeToString.getDateNo(true) + " " +
                            dateTimeToString.getTime(true);
                    dateTimeToString.setFormattedSchedule(t1.getTimestamp());
                    String wtTS1 = dateTimeToString.getDateNo(true) + " " +
                            dateTimeToString.getTime(true);

                    return wtTS1.compareToIgnoreCase(wtTS);
                });

                if(transactionList.size() > 0) finishLoading();
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

    private void finishLoading() {
        iWalletTransactionAdapter.notifyDataSetChanged();

        String iWallet = "₱" + user.getIWallet();
        if(iWallet.split("\\.")[1].length() == 1) iWallet += 0;

        tvIWallet.setText(iWallet);

        progressBar.setVisibility(View.GONE);
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        transactionView.setVisibility(View.VISIBLE);
    }

    private void errorLoading(String error) {
        transactionList.clear();
        iWalletTransactionAdapter.notifyDataSetChanged();

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        transactionView.setVisibility(View.INVISIBLE);
    }
}