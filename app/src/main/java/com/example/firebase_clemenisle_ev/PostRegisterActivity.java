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
import android.os.CountDownTimer;
import android.os.Handler;
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

import com.example.firebase_clemenisle_ev.Classes.Credentials;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PostRegisterActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ImageView activityIconImage, closeImage;
    TextView tvActivityName, tvCaption;
    Button resendButton, updateEAButton;
    ProgressBar progressBar, dialogProgressBar;

    Context myContext;
    Resources myResources;

    String registeredTitleText = "Registration Successful";
    String updatedEmailTitleText = "Email Address Updated";
    String defaultLogText = "Email verification link has been sent to\n";
    String defaultResendText = "Resend Email Verification Link";
    String defaultUpdateText = "Update Email Address";

    String emailAddress, password;
    boolean success, remember, fromRegister;
    String name, caption, resend, update;

    int colorRed, colorBlack, colorGreen, colorInitial, selectedColor;
    ColorStateList cslInitial, cslBlue, cslRed;

    CountDownTimer countDownTimer, autoLoginTimer;
    int startMin = 3, startSec = 0;
    long startTime ;
    long currentTime, loginTime;

    Dialog dialog;

    EditText etEmailAddress;
    TextInputLayout tlEmailAddress;
    Button updateButton;
    ImageView dialogCloseImage;

    String userId;
    String newEmailAddress;

    boolean userUpdated = false, dbUserUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_register);

        activityIconImage = findViewById(R.id.activityIconImage);
        closeImage = findViewById(R.id.closeImage);
        tvActivityName = findViewById(R.id.tvActivityName);
        tvCaption = findViewById(R.id.tvCaption);
        resendButton = findViewById(R.id.resendButton);
        updateEAButton = findViewById(R.id.updateEAButton);
        progressBar = findViewById(R.id.progressBar);

        myContext = PostRegisterActivity.this;
        myResources = myContext.getResources();

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));
        cslRed = ColorStateList.valueOf(myResources.getColor(R.color.red));

        Intent intent = getIntent();
        emailAddress = intent.getStringExtra("emailAddress");
        password = intent.getStringExtra("password");
        remember = intent.getBooleanExtra("remember", false);
        success = intent.getBooleanExtra("success", true);
        fromRegister = intent.getBooleanExtra("fromRegister", true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        colorRed = myResources.getColor(R.color.red);
        colorBlack = myResources.getColor(R.color.black);
        colorGreen = myResources.getColor(R.color.green);
        colorInitial = myResources.getColor(R.color.initial);

        startTime = getMSec(startMin, startSec);

        setInfo();
    }

    private void setInfo() {
        firebaseUser.reload();
        if(dialog != null) dialog.dismiss();
        if(dialogProgressBar != null) dialogProgressBar.setVisibility(View.GONE);

        setScreenEnabled(true);

        if(success) {
            if(fromRegister) {
                name = registeredTitleText;
            }
            else {
                name = updatedEmailTitleText;
            }
            caption = defaultLogText + emailAddress;
            resendButton.setEnabled(false);
            updateEAButton.setEnabled(false);
            selectedColor = colorBlack;

            runTime();
            autoLoginTime();
        }
        else {
            if(fromRegister) {
                name = "Email Error";
                caption = "Failed to send the email verification link";
                selectedColor = colorRed;
            }
            else {
                name = "Unverified Account";
                caption = "Please verify your account using email verification link";
                selectedColor = colorBlack;

                autoLoginTime();
            }
            resendButton.setEnabled(true);
            updateEAButton.setEnabled(true);
        }

        updateInfo();

        if(firebaseUser != null) {
            userId = firebaseUser.getUid();
            if(!firebaseUser.isEmailVerified()) {
                resendButton.setOnClickListener(view -> resendEmailVerificationLink());

                updateEAButton.setOnClickListener(view -> showUpdateEmailDialog());

                closeImage.setOnClickListener(view -> onBackPressed());
            }
        }
        else {
            name = "Auth Error";
            caption = "Failed to get the current user";
            activityIconImage.setImageResource(R.drawable.ic_baseline_error_outline_24);
            setScreenEnabled(false);
            closeImage.setEnabled(true);
            selectedColor = colorRed;

            updateInfo();
        }
    }

    private void showUpdateEmailDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_email_layout);

        etEmailAddress = dialog.findViewById(R.id.etEmailAddress);
        tlEmailAddress = dialog.findViewById(R.id.tlEmailAddress);
        updateButton = dialog.findViewById(R.id.updateButton);
        dialogCloseImage = dialog.findViewById(R.id.dialogCloseImage);
        dialogProgressBar = dialog.findViewById(R.id.dialogProgressBar);

        etEmailAddress.setOnFocusChangeListener((view1, b) -> {
            if(!tlEmailAddress.isErrorEnabled()) {
                if(b) {
                    tlEmailAddress.setStartIconTintList(cslBlue);
                }
                else {
                    tlEmailAddress.setStartIconTintList(cslInitial);
                }
            }
        });

        etEmailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkEmailAddressInput();
            }
        });

        updateButton.setOnClickListener(view -> checkEmailAddressIfExisting());

        dialogCloseImage.setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();
    }

    private void checkEmailAddressIfExisting() {
        tlEmailAddress.setStartIconTintList(cslInitial);
        setScreenEnabled(false);
        dialogProgressBar.setVisibility(View.VISIBLE);

        DatabaseReference usersRef = firebaseDatabase.getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    boolean eaExisting = false;
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DataSnapshot ea = dataSnapshot.child("emailAddress");
                        if(ea.exists()) {
                            String eaValue = ea.getValue(String.class);
                            if(newEmailAddress.equals(eaValue)) {
                                eaExisting = true;
                            }
                        }
                    }

                    if(!userUpdated) {
                        if(eaExisting) {
                            tlEmailAddress.setErrorEnabled(true);
                            tlEmailAddress.setError("This Email Address is already registered");
                            tlEmailAddress.setStartIconTintList(cslRed);
                            vEA = false;

                            setScreenEnabled(true);
                            progressBar.setVisibility(View.GONE);
                        }
                        else {
                            tlEmailAddress.setErrorEnabled(false);
                            tlEmailAddress.setError(null);
                            tlEmailAddress.setStartIconTintList(cslInitial);
                            vEA = true;

                            updateAccount();
                        }
                    }
                }
                else {
                    updateAccount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();

                setScreenEnabled(true);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateAccount() {
        firebaseUser.updateEmail(newEmailAddress)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        userUpdated = true;
                        updateDatabase();
                    }
                    else {
                        updateFailed();
                    }
                });
    }

    private void updateDatabase() {
        if(firebaseUser != null) {
            if(!dbUserUpdated) {
                DatabaseReference usersRef = firebaseDatabase.getReference("users")
                        .child(userId).child("emailAddress");
                usersRef.setValue(newEmailAddress)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                dbUserUpdated = true;
                                emailAddress = newEmailAddress;
                                sendEmailVerificationLink();
                            }
                            else {
                                rollbackUser();
                            }
                        });
            }
        }
    }

    private void rollbackUser() {
        if(firebaseUser != null) {
            firebaseUser.updateEmail(emailAddress)
                    .addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    updateFailed();
                }
                else {
                    updateDatabase();
                }
            });
        }
    }

    private void updateFailed() {
        Toast.makeText(myContext,
                "Update failed, please try again",
                Toast.LENGTH_SHORT
        ).show();
        setScreenEnabled(true);
        dialogProgressBar.setVisibility(View.GONE);
    }

    int tryCount = 0;
    private void sendEmailVerificationLink() {
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        success = true;
                        setInfo();
                    }
                    else {
                        if(tryCount == 5) {
                            success = false;
                            setInfo();
                        }
                        else {
                            tryCount++;
                            sendEmailVerificationLink();
                        }
                    }
                });
    }


    boolean vEA = false;
    private void checkEmailAddressInput() {
        newEmailAddress = etEmailAddress.getText().toString();

        if (!newEmailAddress.equals(emailAddress)) {
            if (Credentials.validEmailAddress(newEmailAddress)) {
                tlEmailAddress.setErrorEnabled(false);
                tlEmailAddress.setError(null);
                tlEmailAddress.setStartIconTintList(cslBlue);
                vEA = true;
            }
            else {
                tlEmailAddress.setErrorEnabled(true);
                tlEmailAddress.setError("Invalid Email Address");
                tlEmailAddress.setStartIconTintList(cslRed);
                vEA = false;
            }
        }
        else {
            tlEmailAddress.setErrorEnabled(true);
            tlEmailAddress.setError("This is already your current email address");
            tlEmailAddress.setStartIconTintList(cslRed);
            vEA = false;
        }

        updateButton.setEnabled(vEA);
    }

    private void updateInfo() {
        tvActivityName.setText(name);
        tvActivityName.setTextColor(selectedColor);
        tvCaption.setText(caption);
        tvCaption.setTextColor(selectedColor);
        activityIconImage.setColorFilter(selectedColor);
    }

    private long getMSec(int min, int sec) {
        min += sec / 60;
        sec = sec % 60;

        return ((min * 60L) + sec) * 1000;
    }

    boolean continueLogin = true;
    private void autoLoginTime() {
        int min = 30;
        int sec = 0;

        continueLogin = true;
        loginTime = getMSec(min, sec);

        autoLoginTimer = new CountDownTimer(loginTime, 1000) {
            @Override
            public void onTick(long l) {
                autoLogin();
            }

            @Override
            public void onFinish() {
                onBackPressed();
            }
        }.start();
    }

    boolean available = true;
    private void autoLogin() {
        if(available) {
            available = false;
            if(firebaseUser != null) {
                firebaseUser.reload();

                if(firebaseUser.getEmail() != null) {
                    if(!firebaseUser.getEmail().equals(emailAddress)) {
                        caption = "Your email address has been reset to " + firebaseUser.getEmail();
                        selectedColor = colorBlack;
                        updateInfo();

                        continueLogin = false;
                        autoLoginTimer.cancel();
                        if(countDownTimer != null) countDownTimer.cancel();
                    }
                }

                if(firebaseUser.isEmailVerified() && continueLogin) {
                    name = "Verified";
                    caption = null;
                    activityIconImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    setScreenEnabled(false);
                    selectedColor = colorGreen;

                    updateInfo();
                    progressBar.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(() -> {
                        sendSharedPreferences();

                        Intent newIntent = new Intent(myContext, MainActivity.class);
                        startActivity(newIntent);
                        finishAffinity();
                    }, 2000);
                }
                else {
                    available = true;
                }
            }
            else {
                available = true;
            }
        }
    }

    private void sendSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("loggedIn", true);
        editor.putBoolean("remember", remember);
        if(remember) {
            editor.putString("emailAddress", emailAddress);
            editor.putString("password", password);
        }
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(countDownTimer != null) countDownTimer.cancel();
        if(autoLoginTimer != null) autoLoginTimer.cancel();
        firebaseAuth.signOut();
    }

    private void runTime() {
        currentTime = startTime;
        countDownTimer = new CountDownTimer(currentTime, 1000) {
            @Override
            public void onTick(long l) {
                currentTime = l;
                resend = defaultResendText + " (" + updatedTime(currentTime) + ")";
                resendButton.setText(resend);
                update = defaultUpdateText + " (" + updatedTime(currentTime) + ")";
                updateEAButton.setText(update);
            }

            @Override
            public void onFinish() {
                resendButton.setEnabled(true);
                updateEAButton.setEnabled(true);
                resendButton.setText(defaultResendText);
                updateEAButton.setText(defaultUpdateText);
            }
        }.start();
    }

    private String updatedTime(long timeValue) {
        String result;
        int min, sec;

        min = (int) timeValue / 1000 / 60;
        sec = (int) (timeValue / 1000) % 60;

        if(String.valueOf(sec).length() == 1) {
            result = min + ":0" + sec;
        }
        else {
            result = min + ":" + sec;
        }

        return result;
    }

    private void resendEmailVerificationLink() {
        setScreenEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    setScreenEnabled(true);
                    progressBar.setVisibility(View.GONE);

                    if(task.isSuccessful()) {
                        name = "Email Sent";
                        caption = "Email verification link has been resent to " + emailAddress;
                        resendButton.setEnabled(false);
                        updateEAButton.setEnabled(false);
                        selectedColor = colorBlack;

                        runTime();
                        autoLoginTimer.cancel();
                        autoLoginTime();
                    }
                    else {
                        name = "Email Error";
                        caption = "Failed to send the email verification link";
                        selectedColor = colorRed;
                    }

                    updateInfo();
                });
    }

    private void setScreenEnabled(boolean value) {
        closeImage.setEnabled(value);
        resendButton.setEnabled(value);
        updateEAButton.setEnabled(value);
        if(dialog != null) dialog.setCanceledOnTouchOutside(value);
        if(tlEmailAddress != null) tlEmailAddress.setEnabled(value);
        if(updateButton != null) updateButton.setEnabled(value);
        if(dialogCloseImage != null) dialogCloseImage.setEnabled(value);

        if(value) {
            if (dialogCloseImage != null) dialogCloseImage.setColorFilter(colorRed);
        }
        else {
            if (dialogCloseImage != null) dialogCloseImage.setColorFilter(colorInitial);
        }
    }
}