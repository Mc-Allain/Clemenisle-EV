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
import com.example.firebase_clemenisle_ev.Adapters.IWalletTransactionAdapter;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.IWalletTransaction;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class IWalletActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    TextView tvIWallet, tvBadge;
    RecyclerView transactionView;

    ImageView reloadImage;
    TextView  tvLog;
    ProgressBar progressBar;

    Button topUpButton, transferButton;

    Context myContext;
    Resources myResources;

    List<IWalletTransaction> transactionList = new ArrayList<>();
    IWalletTransactionAdapter iWalletTransactionAdapter;

    String bookingId;

    String userId;
    User user;

    boolean isLoggedIn = false;

    String defaultCaptionText = "Please send your payment to this/these GCash number/s: ",
            defaultLogText = "No Record";

    ColorStateList cslInitial, cslBlue, cslRed;
    int colorRed, colorBlue, colorInitial;

    String mobileNumber;
    double amount = 0, minAmount = 100;

    Dialog transferDialog;
    TextView tvDialogTitle, tvDialogCaption;
    EditText etMobileNumber, etAmount;
    TextInputLayout tlMobileNumber, tlAmount;
    Button submitButton;
    ImageView dialogCloseImage;
    ProgressBar dialogProgressBar;

    String referenceNumberValue;

    Dialog dialog;
    TextView tvDialogTitle2, tvDialogCaption2;
    EditText etReferenceNumber;
    TextInputLayout tlReferenceNumber;
    Button submitButton2;
    ImageView dialogCloseImage2;
    ProgressBar dialogProgressBar2;

    long submitPressedTime;
    Toast submitToast;

    boolean isGeneratingTransactionId = false;

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
        tvBadge = findViewById(R.id.tvBadge);
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
        cslRed = ColorStateList.valueOf(myResources.getColor(R.color.red));

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);
        colorRed = myResources.getColor(R.color.red);

        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");

        initSharedPreferences();
        initTransferDialog();
        initTopUpDialog();

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
        transactionView.setLayoutManager(linearLayout);
        iWalletTransactionAdapter = new IWalletTransactionAdapter(myContext, transactionList, bookingId);
        transactionView.setAdapter(iWalletTransactionAdapter);

        topUpButton.setOnClickListener(view -> openTopUpDialog());

        getTransactionList();

        transferButton.setOnClickListener(view -> openTransferDialog());
    }

    public void openTopUpDialog() {
        etReferenceNumber.setText(null);

        tlReferenceNumber.setErrorEnabled(false);
        tlReferenceNumber.setError(null);
        tlReferenceNumber.setStartIconTintList(cslInitial);

        tlReferenceNumber.clearFocus();
        tlReferenceNumber.requestFocus();
        dialog.show();
    }
    
    private void openTransferDialog() {
        etMobileNumber.setText(null);
        etAmount.setText("0");

        tlMobileNumber.setErrorEnabled(false);
        tlMobileNumber.setError(null);
        tlAmount.setErrorEnabled(false);
        tlAmount.setError(null);

        tlMobileNumber.setStartIconTintList(cslInitial);
        tlAmount.setStartIconTintList(cslInitial);

        String[] iWalletSplit = tvIWallet.getText().toString().split("₱");
        if(iWalletSplit.length > 1) {
            double maxAmount = Double.parseDouble(iWalletSplit[1]);

            if(maxAmount < minAmount) {
                tlAmount.setErrorEnabled(true);
                String error = "You do not have enough iWallet to transfer";
                tlAmount.setError(error);
                tlAmount.setErrorTextColor(cslRed);
                tlAmount.setStartIconTintList(cslRed);
            }

            etMobileNumber.clearFocus();
            etMobileNumber.requestFocus();

            transferDialog.show();
        }
    }

    private void initTopUpDialog() {
        dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_input_reference_number_layout);

        tvDialogTitle2 = dialog.findViewById(R.id.tvDialogTitle);
        tvDialogCaption2 = dialog.findViewById(R.id.tvDialogCaption);
        etReferenceNumber = dialog.findViewById(R.id.etReferenceNumber);
        tlReferenceNumber = dialog.findViewById(R.id.tlReferenceNumber);
        submitButton2 = dialog.findViewById(R.id.submitButton);
        dialogCloseImage2 = dialog.findViewById(R.id.dialogCloseImage);
        dialogProgressBar2 = dialog.findViewById(R.id.dialogProgressBar);

        String dialogTitle = "Top-up";
        tvDialogTitle2.setText(dialogTitle);

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
                submitButton2.setEnabled(referenceNumberValue.length() == tlReferenceNumber.getCounterMaxLength());
            }
        });

        submitButton2.setOnClickListener(view -> {
            if (submitPressedTime + 2500 > System.currentTimeMillis()) {
                submitToast.cancel();
                
                generateTransactionId(1);
            } else {
                submitToast = Toast.makeText(myContext,
                        "Press again to submit", Toast.LENGTH_SHORT);
                submitToast.show();
            }

            submitPressedTime = System.currentTimeMillis();
        });

        dialogCloseImage2.setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void submitReferenceNumber(String wtId) {
        setDialogScreenEnabled(false);
        tlReferenceNumber.setStartIconTintList(cslInitial);

        IWalletTransaction transaction = new IWalletTransaction(wtId,
                new DateTimeToString().getDateAndTime(), "Top-up", 0);
        transaction.setReferenceNumber(referenceNumberValue);
        addTransactionToDatabase(transaction, wtId);
    }

    private void initTransferDialog() {
        transferDialog = new Dialog(myContext);
        transferDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        transferDialog.setContentView(R.layout.dialog_transfer_iwallet_layout);

        tvDialogTitle = transferDialog.findViewById(R.id.tvDialogTitle);
        tvDialogCaption = transferDialog.findViewById(R.id.tvDialogCaption);
        etMobileNumber = transferDialog.findViewById(R.id.etMobileNumber);
        tlMobileNumber = transferDialog.findViewById(R.id.tlMobileNumber);
        etAmount = transferDialog.findViewById(R.id.etAmount);
        tlAmount = transferDialog.findViewById(R.id.tlAmount);
        submitButton = transferDialog.findViewById(R.id.submitButton);
        dialogCloseImage = transferDialog.findViewById(R.id.dialogCloseImage);
        dialogProgressBar = transferDialog.findViewById(R.id.dialogProgressBar);

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

                if(mobileNumber.length() != 13) {
                    tlMobileNumber.setErrorEnabled(true);
                    String error = "Invalid Mobile Number";
                    tlMobileNumber.setError(error);
                    tlMobileNumber.setErrorTextColor(cslRed);
                    tlMobileNumber.setStartIconTintList(cslRed);
                }
                else {
                    tlMobileNumber.setErrorEnabled(false);
                    tlMobileNumber.setError(null);
                    tlMobileNumber.setStartIconTintList(cslBlue);
                }

                checkTransferInput();
            }
        });

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
                String[] iWalletSplit = tvIWallet.getText().toString().split("₱");
                if(iWalletSplit.length > 1) {
                    double maxAmount = Double.parseDouble(iWalletSplit[1]);

                    if(maxAmount < minAmount) {
                        tlAmount.setErrorEnabled(true);
                        String error = "You do not have enough iWallet to transfer";
                        tlAmount.setError(error);
                        tlAmount.setErrorTextColor(cslRed);
                        tlAmount.setStartIconTintList(cslRed);
                    }
                    else if(amount < minAmount) {
                        tlAmount.setErrorEnabled(true);
                        String error = "Amount must be at least ₱100.00";
                        tlAmount.setError(error);
                        tlAmount.setErrorTextColor(cslRed);
                        tlAmount.setStartIconTintList(cslRed);
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

                    checkTransferInput();
                }
            }
        });

        submitButton.setOnClickListener(view -> {
            if (submitPressedTime + 2500 > System.currentTimeMillis()) {
                submitToast.cancel();

                generateTransactionId(0);
            } else {
                submitToast = Toast.makeText(myContext,
                        "Press again to submit", Toast.LENGTH_SHORT);
                submitToast.show();
            }

            submitPressedTime = System.currentTimeMillis();
        });

        dialogCloseImage.setOnClickListener(view -> transferDialog.dismiss());

        transferDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        transferDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        transferDialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        transferDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void checkTransferInput() {
        String[] iWalletSplit = tvIWallet.getText().toString().split("₱");
        if(iWalletSplit.length > 1) {
            double maxAmount = Double.parseDouble(iWalletSplit[1]);
            submitButton.setEnabled(
                    mobileNumber.length() == 13 && amount >= minAmount && amount <= maxAmount
            );
        }
    }

    private void setDialogScreenEnabled(boolean value) {
        transferDialog.setCanceledOnTouchOutside(false);
        tlMobileNumber.setEnabled(value);
        tlAmount.setEnabled(value);
        submitButton.setEnabled(value);

        dialog.setCanceledOnTouchOutside(false);
        etReferenceNumber.setEnabled(value);
        tlReferenceNumber.setEnabled(value);
        submitButton2.setEnabled(value);

        if(value) {
            dialogCloseImage.getDrawable().setTint(colorRed);
            dialogCloseImage2.getDrawable().setTint(colorRed);
        }
        else {
            dialogCloseImage.getDrawable().setTint(colorInitial);
            dialogCloseImage2.getDrawable().setTint(colorInitial);
        }
    }

    private void generateTransactionId(int sender) {
        dialogProgressBar.setVisibility(View.VISIBLE);
        dialogProgressBar2.setVisibility(View.VISIBLE);
        setDialogScreenEnabled(false);

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

                    if(sender == 0) submitRequest(transactionId);
                    else if(sender == 1) submitReferenceNumber(transactionId);
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

                setDialogScreenEnabled(true);
            }
        });
    }

    private void submitRequest(String wtId) {
        setDialogScreenEnabled(false);
        tlMobileNumber.setStartIconTintList(cslInitial);
        tlAmount.setStartIconTintList(cslInitial);

        IWalletTransaction transaction = new IWalletTransaction(wtId,
                new DateTimeToString().getDateAndTime(), "Transfer", amount);
        transaction.setMobileNumber(mobileNumber);
        addTransactionToDatabase(transaction, wtId);
    }

    private void addTransactionToDatabase(IWalletTransaction transaction, String wtId) {
        String category = transaction.getCategory();
        usersRef.child(userId).child("iWalletTransactionList").child(wtId).setValue(transaction).
                addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        String[] iWalletSplit = tvIWallet.getText().toString().split("₱");
                        if(iWalletSplit.length > 1) {
                            if(category.equals("Transfer")) {
                                double iWallet = Double.parseDouble(iWalletSplit[1]);
                                double newIWallet = iWallet - amount;
                                usersRef.child(userId).child("iWallet").setValue(newIWallet);

                                Toast.makeText(
                                        myContext,
                                        "Successfully requested for transfer." +
                                                "It will take at least 24 hours to take effect.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                            else if(category.equals("Top-up")) {
                                Toast.makeText(
                                        myContext,
                                        "Successfully submitted the reference number." +
                                                "It will take at least 24 hours to take effect.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
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
                    dialogProgressBar.setVisibility(View.GONE);
                    dialogProgressBar2.setVisibility(View.GONE);
                    setDialogScreenEnabled(true);
                    transferDialog.dismiss();
                    dialog.dismiss();
                });
    }

    private void getTransactionList() {
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        transactionView.setVisibility(View.INVISIBLE);

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

                tvDialogCaption2.setText(captionText.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                if(snapshot.exists()) {
                    user = new User(snapshot);
                    transactionList.addAll(user.getTransactionList());
                }

                Collections.reverse(transactionList);
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
        String helpText = "Current iWallet Amount: " + iWallet;
        tlAmount.setHelperText(helpText);

        tvBadge.setText(String.valueOf(transactionList.size()));

        progressBar.setVisibility(View.GONE);
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        transactionView.setVisibility(View.VISIBLE);
    }

    private void errorLoading(String error) {
        transactionList.clear();
        iWalletTransactionAdapter.notifyDataSetChanged();

        String iWallet = "₱" + user.getIWallet();
        if(iWallet.split("\\.")[1].length() == 1) iWallet += 0;

        tvIWallet.setText(iWallet);

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        transactionView.setVisibility(View.INVISIBLE);
    }
}