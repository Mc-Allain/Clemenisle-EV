package com.example.firebase_clemenisle_ev;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

public class PostRegisterActivity extends AppCompatActivity {

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
    boolean success, isRemembered, fromRegister;
    String name, caption, resend, update;

    int colorRed, colorBlack, colorGreen, colorInitial, selectedColor;
    ColorStateList cslInitial, cslBlue, cslRed;

    CountDownTimer countDownTimer, autoLoginTimer;
    int startMin = 3, startSec = 0;
    long startTime ;
    long currentTime, loginTime;
    long remainingTime, currentMS;

    Dialog dialog;
    EditText etEmailAddress;
    TextInputLayout tlEmailAddress;
    Button updateButton;
    ImageView dialogCloseImage;

    String userId;
    String newEmailAddress;
    boolean vEA = false;
    int tryCount = 0;

    boolean isVerified = false;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("timer", Context.MODE_PRIVATE);
        remainingTime = sharedPreferences.getLong("buttonTimer", getMSec(startMin, startSec));
        currentMS = sharedPreferences.getLong("currentMS", System.currentTimeMillis());
        remainingTime -= (System.currentTimeMillis() - currentMS);
    }

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("isRemembered", false);
        editor.apply();
    }

    private void sendTimerPreferences(long value) {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "timer", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong("buttonTimer", value);
        editor.putLong("currentMS", System.currentTimeMillis());
        editor.apply();
    }

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

        initSharedPreferences();

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));
        cslRed = ColorStateList.valueOf(myResources.getColor(R.color.red));

        Intent intent = getIntent();
        emailAddress = intent.getStringExtra("emailAddress");
        password = intent.getStringExtra("password");
        isRemembered = intent.getBooleanExtra("isRemembered", false);
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

        initEmailAddressDialog();

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

            remainingTime = 0;

            runTime();
            autoLoginTime();
        }
        else {
            if(fromRegister) {
                name = "Email Error";
                caption = "Failed to send the email verification link";
                selectedColor = colorRed;

                resendButton.setEnabled(true);
                updateEAButton.setEnabled(true);
            }
            else {
                name = "Unverified Account";
                caption = "Please verify your account\nusing email verification link";
                selectedColor = colorBlack;

                if(remainingTime > 0) {
                    runTime();

                    resendButton.setEnabled(false);
                    updateEAButton.setEnabled(false);
                }
                else{
                    resendButton.setEnabled(true);
                    updateEAButton.setEnabled(true);
                }
                autoLoginTime();
            }
        }

        updateInfo();

        if(firebaseUser != null) {
            userId = firebaseUser.getUid();

            if(!firebaseUser.isEmailVerified()) {
                resendButton.setOnClickListener(view -> resendEmailVerificationLink());

                updateEAButton.setOnClickListener(view -> showEmailAddressDialog());

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

    private void showEmailAddressDialog() {
        if(newEmailAddress != null) {
            etEmailAddress.setText(null);
            tlEmailAddress.setErrorEnabled(false);
            tlEmailAddress.setError(null);
            tlEmailAddress.setStartIconTintList(cslInitial);
        }
        etEmailAddress.clearFocus();
        etEmailAddress.requestFocus();

        dialog.show();
    }

    private void initEmailAddressDialog() {
        dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_email_address_layout);

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

        updateButton.setOnClickListener(view -> updateAccount());

        dialogCloseImage.setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        dialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void updateAccount() {
        setScreenEnabled(false);
        dialogProgressBar.setVisibility(View.VISIBLE);
        tlEmailAddress.setStartIconTintList(cslInitial);

        firebaseUser.updateEmail(newEmailAddress)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        emailAddress = newEmailAddress;
                        sendEmailVerificationLink();
                    }
                    else {
                        String error = "";
                        if(task.getException() != null)
                            error = task.getException().toString();

                        if(error.contains("UserCollision")) {
                            error = "This Email Address is already registered";

                            tlEmailAddress.setErrorEnabled(true);
                            tlEmailAddress.setError(error);
                            tlEmailAddress.setStartIconTintList(cslRed);

                            setScreenEnabled(true);
                            updateButton.setEnabled(false);
                            dialogProgressBar.setVisibility(View.GONE);
                        }
                        else updateFailed();
                    }
                });
    }

    private void updateFailed() {
        Toast.makeText(myContext,
                "Update failed. Please try again.",
                Toast.LENGTH_LONG
        ).show();

        setScreenEnabled(true);
        dialogProgressBar.setVisibility(View.GONE);
    }

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
        activityIconImage.getDrawable().setTint(selectedColor);
    }

    private long getMSec(int min, int sec) {
        min += sec / 60;
        sec = sec % 60;

        return ((min * 60L) + sec) * 1000;
    }

    private void autoLoginTime() {
        int min = 30;
        int sec = 0;

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

    boolean isAvailable = true;
    private void autoLogin() {
        if(isAvailable) {
            isAvailable = false;

            if(firebaseUser != null) {
                firebaseUser.reload();

                if(firebaseUser.isEmailVerified()) {
                    isVerified = true;
                    name = "Verified";
                    caption = "Logging in…";
                    activityIconImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    setScreenEnabled(false);
                    selectedColor = colorGreen;

                    updateInfo();
                    progressBar.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(() -> {
                        sendSharedPreferences();

                        Intent newIntent = new Intent(myContext, MainActivity.class);
                        newIntent.putExtra("password", password);
                        startActivity(newIntent);
                        finishAffinity();
                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(
                                myContext,
                                "You are logged in using " + firebaseUser.getEmail(),
                                Toast.LENGTH_LONG
                        ).show();
                    }, 2000);
                }
                else {
                    isAvailable = true;
                }
            }
            else {
                isAvailable = true;
            }
        }
    }

    private void sendSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", true);
        editor.putBoolean("isRemembered", isRemembered);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(countDownTimer != null) countDownTimer.cancel();
        if(autoLoginTimer != null) autoLoginTimer.cancel();
        if(!isVerified) {
            firebaseAuth.signOut();
            sendLoginPreferences();
        }

        sendTimerPreferences(currentTime);
    }

    private void runTime() {
        if(remainingTime > 0) currentTime = remainingTime;
        else currentTime = startTime;

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
                currentTime = 0;
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

                        remainingTime = 0;

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
        dialog.setCanceledOnTouchOutside(value);
        dialog.setCancelable(value);
        closeImage.setEnabled(value);
        resendButton.setEnabled(value);
        updateEAButton.setEnabled(value);
        tlEmailAddress.setEnabled(value);
        updateButton.setEnabled(value);
        dialogCloseImage.setEnabled(value);

        if(value) {
            closeImage.getDrawable().setTint(colorRed);
            dialogCloseImage.getDrawable().setTint(colorRed);
        }
        else {
            closeImage.getDrawable().setTint(colorInitial);
            dialogCloseImage.getDrawable().setTint(colorInitial);
        }
    }
}