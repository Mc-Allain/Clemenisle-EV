package com.example.firebase_clemenisle_ev;

import android.app.Activity;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    BottomNavigationView mainNav;
    NavController mainNavCtrlr;
    NavHostFragment navHostFragment;
    FloatingActionButton fab;

    Context myContext;
    Resources myResources;

    int colorGreen, colorRed, colorInitial;
    ColorStateList cslInitial, cslBlue, cslRed;

    long backPressedTime;
    Toast backToast;

    String userId;
    boolean loggedIn = false;
    String password = null;

    Dialog passwordDialog;
    TextView tvDialogTitle, tvDialogCaption;
    ImageView passwordDialogCloseImage;
    Button passwordUpdateButton;
    ProgressBar passwordDialogProgressBar;

    EditText etPassword, etConfirmPassword;
    TextInputLayout tlPassword, tlConfirmPassword;

    ImageView pwLengthCheckImage, pwUpperCheckImage, pwLowerCheckImage, pwNumberCheckImage, pwSymbolCheckImage;
    TextView tvPWLength, tvPWUpper, tvPWLower, tvPWNumber, tvPWSymbol;

    String newPassword = "", newConfirmPassword = "";

    boolean vPWL = false, vPWU = false, vPWLw = false, vPWN = false, vPWS = false, vCPW = false;
    boolean vCPWL = false, vCPWU = false, vCPWLw = false, vCPWN = false, vCPWS = false;

    boolean isPasswordUpdate = false;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        loggedIn = sharedPreferences.getBoolean("loggedIn", false);
    }

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("loggedIn", false);
        editor.putBoolean("remember", false);
        editor.putString("emailAddress", null);
        editor.putString("password", null);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainNav = findViewById(R.id.bottomNavigationView);
        mainNav.setBackground(null);

        myContext = MainActivity.this;
        myResources = myContext.getResources();

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));
        cslRed = ColorStateList.valueOf(myResources.getColor(R.color.red));

        colorGreen = myResources.getColor(R.color.green);
        colorRed = myResources.getColor(R.color.red);
        colorInitial = myResources.getColor(R.color.initial);

        Intent intent = getIntent();
        password = intent.getStringExtra("password");

        initSharedPreferences();

        firebaseAuth = FirebaseAuth.getInstance();
        if(loggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) firebaseUser.reload();
            if(firebaseUser == null) {
                firebaseAuth.signOut();
                sendLoginPreferences();

                Toast.makeText(
                        myContext,
                        "Failed to get the current user",
                        Toast.LENGTH_SHORT
                ).show();
            }
            else {
                userId = firebaseUser.getUid();
            }
        }

        if(password != null) {
            if(!isCurrentPasswordValid()) {
                initPasswordDialog();
                showPasswordDialog();
            }
        }

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        mainNavCtrlr = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(mainNav, mainNavCtrlr);

        fab = findViewById(R.id.floatingActionButton);
        fab.setColorFilter(getResources().getColor(R.color.white));

        fab.setOnClickListener(view -> {
            Intent newIntent;
            if(loggedIn) {
                newIntent = new Intent(myContext, BookingActivity.class);
            }
            else {
                newIntent = new Intent(myContext, LoginActivity.class);
            }
            startActivity(newIntent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(password != null) {
            if(!isCurrentPasswordValid() && !isPasswordUpdate) {
                firebaseAuth.signOut();
                sendLoginPreferences();

                Toast.makeText(
                        myContext,
                        "Your account has been logged out",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private boolean isCurrentPasswordValid() {
        if(password.length() >= 8) vCPWL = true;
        else vCPWL = false;

        if(password.matches(".*[A-Z].*"))vCPWU = true;
        else vCPWU = false;

        if(password.matches(".*[a-z].*"))vCPWLw = true;
        else vCPWLw = false;

        if(password.matches(".*[0-9].*")) vCPWN = true;
        else vCPWN = false;

        if(password.matches("[A-Za-z0-9]*")) vCPWS = true;
        else vCPWS = false;

        return vCPWL && vCPWU && vCPWLw && vCPWN && vCPWS;
    }

    private void showPasswordDialog() {
        if(newPassword != null) {
            etPassword.setText(null);
            tlPassword.setErrorEnabled(false);
            tlPassword.setError(null);
            tlPassword.setStartIconTintList(cslInitial);

            etConfirmPassword.setText(null);
            tlConfirmPassword.setErrorEnabled(false);
            tlConfirmPassword.setError(null);
            tlConfirmPassword.setStartIconTintList(cslInitial);
        }
        tlPassword.clearFocus();
        tlPassword.requestFocus();

        tvPWLength.setTextColor(colorInitial);
        tvPWUpper.setTextColor(colorInitial);
        tvPWLower.setTextColor(colorInitial);
        tvPWNumber.setTextColor(colorInitial);
        tvPWSymbol.setTextColor(colorInitial);

        pwLengthCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
        pwUpperCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
        pwLowerCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
        pwNumberCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
        pwSymbolCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);

        pwLengthCheckImage.setColorFilter(colorInitial);
        pwUpperCheckImage.setColorFilter(colorInitial);
        pwLowerCheckImage.setColorFilter(colorInitial);
        pwNumberCheckImage.setColorFilter(colorInitial);
        pwSymbolCheckImage.setColorFilter(colorInitial);

        vPWL = false; vPWU = false; vPWLw = false; vPWN = false; vPWS = false; vCPW = false;

        isPasswordUpdate = false;

        passwordDialog.show();
    }

    private void initPasswordDialog() {
        passwordDialog = new Dialog(myContext);
        passwordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        passwordDialog.setContentView(R.layout.dialog_update_password_layout);

        tvDialogTitle = passwordDialog.findViewById(R.id.tvDialogTitle);
        tvDialogCaption = passwordDialog.findViewById(R.id.tvDialogCaption);
        passwordDialogCloseImage = passwordDialog.findViewById(R.id.dialogCloseImage);
        passwordUpdateButton = passwordDialog.findViewById(R.id.updateButton);
        passwordDialogProgressBar = passwordDialog.findViewById(R.id.progressBar);

        etPassword = passwordDialog.findViewById(R.id.etPassword);
        etConfirmPassword = passwordDialog.findViewById(R.id.etConfirmPassword);
        tlPassword = passwordDialog.findViewById(R.id.tlPassword);
        tlConfirmPassword = passwordDialog.findViewById(R.id.tlConfirmPassword);

        pwLengthCheckImage = passwordDialog.findViewById(R.id.pwLengthCheckImage);
        pwUpperCheckImage = passwordDialog.findViewById(R.id.pwUpperCheckImage);
        pwLowerCheckImage = passwordDialog.findViewById(R.id.pwLowerCheckImage);
        pwNumberCheckImage = passwordDialog.findViewById(R.id.pwNumberCheckImage);
        pwSymbolCheckImage = passwordDialog.findViewById(R.id.pwSymbolCheckImage);
        tvPWLength = passwordDialog.findViewById(R.id.tvPWLength);
        tvPWUpper = passwordDialog.findViewById(R.id.tvPWUpper);
        tvPWLower = passwordDialog.findViewById(R.id.tvPWLower);
        tvPWNumber = passwordDialog.findViewById(R.id.tvPWNumber);
        tvPWSymbol = passwordDialog.findViewById(R.id.tvPWSymbol);

        String dialogTitle = "Weak Password";
        String dialogCaption = "Please input a new password.";
        tvDialogTitle.setText(dialogTitle);
        tvDialogTitle.setTextColor(colorRed);
        tvDialogCaption.setText(dialogCaption);

        passwordDialog.setCanceledOnTouchOutside(false);
        passwordDialogCloseImage.setVisibility(View.GONE);

        passwordDialog.setOnDismissListener(dialogInterface -> finishAffinity());

        etPassword.setOnFocusChangeListener((view1, b) -> {
            if(!tlPassword.isErrorEnabled()) {
                if(b) {
                    tlPassword.setStartIconTintList(cslBlue);
                }
                else {
                    tlPassword.setStartIconTintList(cslInitial);
                }
            }
        });

        etConfirmPassword.setOnFocusChangeListener((view1, b) -> {
            if(!tlConfirmPassword.isErrorEnabled()) {
                if (b) {
                    tlConfirmPassword.setStartIconTintList(cslBlue);
                }
                else {
                    tlConfirmPassword.setStartIconTintList(cslInitial);
                }
            }
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkPasswordInput(1);
            }
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkPasswordInput(2);
            }
        });

        passwordUpdateButton.setOnClickListener(view -> updatePassword());

        passwordDialogCloseImage.setOnClickListener(view -> passwordDialog.dismiss());

        passwordDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        passwordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        passwordDialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        passwordDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void checkPasswordInput(int sender) {
        newPassword = etPassword.getText().toString();
        newConfirmPassword = etConfirmPassword.getText().toString();

        switch(sender) {
            case 1:
                if(newPassword.length() >= 6) {
                    pwLengthCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwLengthCheckImage.setColorFilter(colorGreen);
                    tvPWLength.setTextColor(colorGreen);
                    vPWL = true;
                }
                else {
                    pwLengthCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwLengthCheckImage.setColorFilter(colorRed);
                    tvPWLength.setTextColor(colorRed);
                    vPWL = false;
                }

                if(newPassword.matches(".*[A-Z].*")) {
                    pwUpperCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwUpperCheckImage.setColorFilter(colorGreen);
                    tvPWUpper.setTextColor(colorGreen);
                    vPWU = true;
                }
                else {
                    pwUpperCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwUpperCheckImage.setColorFilter(colorRed);
                    tvPWUpper.setTextColor(colorRed);
                    vPWU = false;
                }

                if(newPassword.matches(".*[a-z].*")) {
                    pwLowerCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwLowerCheckImage.setColorFilter(colorGreen);
                    tvPWLower.setTextColor(colorGreen);
                    vPWLw = true;
                }
                else {
                    pwLowerCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwLowerCheckImage.setColorFilter(colorRed);
                    tvPWLower.setTextColor(colorRed);
                    vPWLw = false;
                }

                if(newPassword.matches(".*[0-9].*")) {
                    pwNumberCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwNumberCheckImage.setColorFilter(colorGreen);
                    tvPWNumber.setTextColor(colorGreen);
                    vPWN = true;
                }
                else {
                    pwNumberCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwNumberCheckImage.setColorFilter(colorRed);
                    tvPWNumber.setTextColor(colorRed);
                    vPWN = false;
                }

                if(newPassword.matches("[A-Za-z0-9]*")) {
                    pwSymbolCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwSymbolCheckImage.setColorFilter(colorGreen);
                    tvPWSymbol.setTextColor(colorGreen);
                    vPWS = true;
                }
                else {
                    pwSymbolCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwSymbolCheckImage.setColorFilter(colorRed);
                    tvPWSymbol.setTextColor(colorRed);
                    vPWS = false;
                }

                if(vPWL && vPWU && vPWLw && vPWN && vPWS) {
                    tlPassword.setErrorEnabled(false);
                    tlPassword.setError(null);
                    tlPassword.setStartIconTintList(cslBlue);
                }
                else {
                    tlPassword.setErrorEnabled(true);
                    tlPassword.setError("Weak Password");
                    tlPassword.setStartIconTintList(cslRed);
                }

                if(newConfirmPassword.length() > 0) {
                    if(newConfirmPassword.equals(newPassword)) {
                        tlConfirmPassword.setErrorEnabled(false);
                        tlConfirmPassword.setError(null);
                        tlConfirmPassword.setStartIconTintList(cslInitial);
                        vCPW = true;
                    }
                    else {
                        tlConfirmPassword.setErrorEnabled(true);
                        tlConfirmPassword.setError("Password does not matched");
                        tlConfirmPassword.setStartIconTintList(cslRed);
                        vCPW = false;
                    }
                }

                break;
            case 2:
                if(newConfirmPassword.length() > 0) {
                    if(newConfirmPassword.equals(newPassword)) {
                        tlConfirmPassword.setErrorEnabled(false);
                        tlConfirmPassword.setError(null);
                        tlConfirmPassword.setStartIconTintList(cslBlue);
                        vCPW = true;
                    }
                    else {
                        tlConfirmPassword.setErrorEnabled(true);
                        tlConfirmPassword.setError("Password does not matched");
                        tlConfirmPassword.setStartIconTintList(cslRed);
                        vCPW = false;
                    }
                }
                else {
                    tlConfirmPassword.setErrorEnabled(true);
                    tlConfirmPassword.setError("Please re-enter your password");
                    tlConfirmPassword.setStartIconTintList(cslRed);
                    vCPW = false;
                }
                break;
        }

        passwordUpdateButton.setEnabled(vPWL && vPWU && vPWLw && vPWN && vPWS && vCPW);
    }

    private void updatePassword() {
        setPasswordDialogScreenEnabled(false);
        passwordDialogProgressBar.setVisibility(View.VISIBLE);

        firebaseUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) finishPasswordUpdate();
                    else {
                        String error = "";
                        if(task.getException() != null)
                            error = task.getException().toString();

                        if(error.contains("RecentLogin")) {
                            proceedToMainActivity();

                            Toast.makeText(
                                    myContext,
                                    "This operation is sensitive and requires recent authentication." +
                                            "Please log in again before trying this request.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        else errorPasswordUpdate();
                    }
                });
    }

    private void setPasswordDialogScreenEnabled(boolean value) {
        passwordUpdateButton.setEnabled(value);
        tlPassword.setEnabled(value);
        tlConfirmPassword.setEnabled(value);
    }

    private void finishPasswordUpdate() {
        isPasswordUpdate = true;
        proceedToMainActivity();

        Toast.makeText(
                myContext,
                "Successfully updated the Password. Please log in again.",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void errorPasswordUpdate() {
        Toast.makeText(
                myContext,
                "Failed to update the Password. Please try again.",
                Toast.LENGTH_LONG
        ).show();

        setPasswordDialogScreenEnabled(true);
        passwordDialogProgressBar.setVisibility(View.GONE);
    }

    private void proceedToMainActivity() {
        sendLoginPreferences();

        Intent intent = new Intent(myContext, MainActivity.class);
        myContext.startActivity(intent);
        ((Activity) myContext).finishAffinity();
    }

    @Override
    public void onBackPressed() {
        if(backPressedTime + 2500 > System.currentTimeMillis()) {
            backToast.cancel();
            finish();
        }
        else {
            backToast = Toast.makeText(myContext,
                    "Press back again to exit",
                    Toast.LENGTH_SHORT);
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}