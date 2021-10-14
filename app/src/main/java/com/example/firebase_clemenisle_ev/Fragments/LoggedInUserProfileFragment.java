package com.example.firebase_clemenisle_ev.Fragments;

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
import android.view.LayoutInflater;
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
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.MainActivity;
import com.example.firebase_clemenisle_ev.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

public class LoggedInUserProfileFragment extends Fragment {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ProgressBar progressBar;
    TextView tvGreet;

    ConstraintLayout fullNameLayout;
    TextView tvLastName, tvFirstNameMiddleName;
    ImageView updateFullNameImage;

    ConstraintLayout accountDetailsLayout;
    TextView tvEmailAddress2;
    ImageView updateEmailAddressImage, updatePasswordImage;

    Context myContext;
    Resources myResources;

    int colorGreen, colorRed, colorInitial, colorBlack, colorWhite;
    ColorStateList cslInitial, cslBlue, cslRed;

    String defaultGreetText = "こんにちは (Hello)", lastName, firstName, middleName;
    String emailAddress;

    User user;

    String userId;
    boolean loggedIn = false;

    Dialog fullNameDialog;
    ImageView fullNameDialogCloseImage;
    Button fullNameUpdateButton;
    ProgressBar fullNameDialogProgressBar;

    boolean isLastNameUpdated = false, isFirstNameUpdated = false, isMiddleNameUpdated = false;

    Dialog emailAddressDialog;
    ImageView emailAddressDialogCloseImage;
    Button emailAddressUpdateButton;
    ProgressBar emailAddressDialogProgressBar;

    Dialog passwordDialog;
    ImageView passwordDialogCloseImage;
    Button passwordUpdateButton;
    ProgressBar passwordDialogProgressBar;

    DatabaseReference usersRef;

    EditText etLastName, etFirstName, etMiddleName,
            etPassword, etConfirmPassword, etCurrentPassword, etEmailAddress;
    TextInputLayout tlLastName, tlFirstName, tlMiddleName,
            tlPassword, tlConfirmPassword, tlCurrentPassword, tlEmailAddress;

    ImageView pwLengthCheckImage, pwUpperCheckImage, pwLowerCheckImage, pwNumberCheckImage, pwSymbolCheckImage;
    TextView tvPWLength, tvPWUpper, tvPWLower, tvPWNumber, tvPWSymbol;

    String newLastName = "", newFirstName = "", newMiddleName = "",
            newPassword = "", newConfirmPassword = "", newCurrentPassword = "", newEmailAddress = "";

    boolean vLN = false, vFN = false, vMN = true;
    boolean vPWL = false, vPWU = false, vPWLw = false, vPWN = false, vPWS = false, vCPW = false;
    boolean vEA = false;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logged_in_user_profile, container, false);

        fullNameLayout = view.findViewById(R.id.fullNameLayout);
        tvGreet = view.findViewById(R.id.tvGreet);
        tvLastName = view.findViewById(R.id.tvLastName);
        tvFirstNameMiddleName = view.findViewById(R.id.tvFirstNameMiddleName);
        updateFullNameImage = view.findViewById(R.id.updateFullNameImage);

        accountDetailsLayout = view.findViewById(R.id.accountDetailsLayout);
        tvEmailAddress2 = view.findViewById(R.id.tvEmailAddress2);
        updateEmailAddressImage = view.findViewById(R.id.updateEmailAddressImage);
        updatePasswordImage = view.findViewById(R.id.updatePasswordImage);

        progressBar = view.findViewById(R.id.progressBar);

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));
        cslRed = ColorStateList.valueOf(myResources.getColor(R.color.red));

        colorGreen = myResources.getColor(R.color.green);
        colorRed = myResources.getColor(R.color.red);
        colorInitial = myResources.getColor(R.color.initial);
        colorBlack = myResources.getColor(R.color.black);
        colorWhite = myResources.getColor(R.color.white);

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

        usersRef = firebaseDatabase.getReference("users").child(userId);

        initFullNameDialog();
        initEmailAddressDialog();
        initPasswordDialog();

        getCurrentUser();

        updateFullNameImage.setOnClickListener(view1 -> showFullNameDialog());
        updateEmailAddressImage.setOnClickListener(view1 -> showEmailAddressDialog());
        updatePasswordImage.setOnClickListener(view1 -> showPasswordDialog());

        return view;
    }

    private void showFullNameDialog() {
        etLastName.setText(lastName);
        etFirstName.setText(firstName);
        etMiddleName.setText(middleName);

        tlLastName.setStartIconTintList(cslInitial);
        tlFirstName.setStartIconTintList(cslInitial);
        tlMiddleName.setStartIconTintList(cslInitial);

        etLastName.clearFocus();
        etLastName.requestFocus();

        fullNameDialog.show();
    }

    private void showEmailAddressDialog() {
        etEmailAddress.setText(emailAddress);
        tlEmailAddress.setStartIconTintList(cslInitial);
        etEmailAddress.clearFocus();
        etEmailAddress.requestFocus();

        emailAddressDialog.show();
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

            etCurrentPassword.setText(null);
            tlCurrentPassword.setErrorEnabled(false);
            tlCurrentPassword.setError(null);
            tlCurrentPassword.setStartIconTintList(cslInitial);
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

        passwordDialog.show();
    }

    private void initFullNameDialog() {
        fullNameDialog = new Dialog(myContext);
        fullNameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        fullNameDialog.setContentView(R.layout.dialog_update_full_name_layout);

        fullNameDialogCloseImage = fullNameDialog.findViewById(R.id.dialogCloseImage);
        fullNameUpdateButton = fullNameDialog.findViewById(R.id.updateButton);
        fullNameDialogProgressBar = fullNameDialog.findViewById(R.id.progressBar);

        etLastName = fullNameDialog.findViewById(R.id.etLastName);
        etFirstName = fullNameDialog.findViewById(R.id.etFirstName);
        etMiddleName = fullNameDialog.findViewById(R.id.etMiddleName);
        tlLastName = fullNameDialog.findViewById(R.id.tlLastName);
        tlFirstName = fullNameDialog.findViewById(R.id.tlFirstName);
        tlMiddleName = fullNameDialog.findViewById(R.id.tlMiddleName);

        etLastName.setOnFocusChangeListener((view1, b) -> {
            if(!tlLastName.isErrorEnabled()) {
                if(b) {
                    tlLastName.setStartIconTintList(cslBlue);
                }
                else {
                    while (newLastName.contains("  ")) {
                        newLastName = newLastName.replaceAll(" {2}", " ");
                    }
                    etLastName.setText(newLastName.trim());
                    tlLastName.setStartIconTintList(cslInitial);
                }
            }
        });

        etFirstName.setOnFocusChangeListener((view1, b) -> {
            if(!tlFirstName.isErrorEnabled()) {
                if(b) {
                    tlFirstName.setStartIconTintList(cslBlue);
                }
                else {
                    while (newFirstName.contains("  ")) {
                        newFirstName = newFirstName.replaceAll(" {2}", " ");
                    }
                    etFirstName.setText(newFirstName.trim());
                    tlFirstName.setStartIconTintList(cslInitial);
                }
            }
        });

        etMiddleName.setOnFocusChangeListener((view1, b) -> {
            if(!tlMiddleName.isErrorEnabled()) {
                if(b) {
                    tlMiddleName.setStartIconTintList(cslBlue);
                }
                else {
                    while (newMiddleName.contains("  ")) {
                        newMiddleName = newMiddleName.replaceAll(" {2}", " ");
                    }
                    etMiddleName.setText(newMiddleName.trim());
                    tlMiddleName.setStartIconTintList(cslInitial);
                }
            }
        });

        etLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkNameInput(1);
            }
        });

        etFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkNameInput(2);
            }
        });

        etMiddleName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkNameInput(3);
            }
        });

        fullNameUpdateButton.setOnClickListener(view -> updateFullName());

        fullNameDialogCloseImage.setOnClickListener(view -> fullNameDialog.dismiss());

        fullNameDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        fullNameDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        fullNameDialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        fullNameDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void checkNameInput(int sender) {
        newLastName = etLastName.getText().toString();
        newFirstName = etFirstName.getText().toString();
        newMiddleName = etMiddleName.getText().toString();

        switch(sender) {
            case 1:
                if(newLastName.matches("[A-Za-z Ññ'.]*")) {
                    if(newLastName.length() < 2) {
                        tlLastName.setErrorEnabled(true);
                        tlLastName.setError("Last Name must be at least 2 characters");
                        tlLastName.setStartIconTintList(cslRed);
                        vLN = false;
                    }
                    else {
                        tlLastName.setErrorEnabled(false);
                        tlLastName.setError(null);
                        tlLastName.setStartIconTintList(cslBlue);
                        vLN = true;
                    }
                }
                else {
                    tlLastName.setErrorEnabled(true);
                    tlLastName.setError("Last Name must not contain an invalid character");
                    tlLastName.setStartIconTintList(cslRed);
                    vLN = false;
                }
                break;
            case 2:
                if(newFirstName.matches("[A-Za-z Ññ'.]*")) {
                    if(newFirstName.length() < 2) {
                        tlFirstName.setErrorEnabled(true);
                        tlFirstName.setError("First Name must be at least 2 characters");
                        tlFirstName.setStartIconTintList(cslRed);
                        vFN = false;
                    }
                    else {
                        tlFirstName.setErrorEnabled(false);
                        tlFirstName.setError(null);
                        tlFirstName.setStartIconTintList(cslBlue);
                        vFN = true;
                    }
                }
                else {
                    tlFirstName.setErrorEnabled(true);
                    tlFirstName.setError("First Name must not contain an invalid character");
                    tlFirstName.setStartIconTintList(cslRed);
                    vFN = false;
                }
                break;
            case 3:
                if(newMiddleName.matches("[A-Za-z Ññ'.]*")) {
                    tlMiddleName.setErrorEnabled(false);
                    tlMiddleName.setError(null);
                    tlMiddleName.setStartIconTintList(cslBlue);
                    vMN = true;
                }
                else {
                    tlMiddleName.setErrorEnabled(true);
                    tlMiddleName.setError("Middle Name must not contain an invalid character");
                    tlMiddleName.setStartIconTintList(cslRed);
                    vMN = false;
                }
                break;
        }

        fullNameUpdateButton.setEnabled(
                vLN && vFN && vMN &&
                (
                    !newLastName.equals(lastName) ||
                    !newFirstName.equals(firstName) ||
                    !newMiddleName.equals(middleName)
                )
        );
    }

    private void setFullNameDialogScreenEnabled(boolean value) {
        fullNameDialog.setCanceledOnTouchOutside(value);
        fullNameDialogCloseImage.setEnabled(value);
        fullNameUpdateButton.setEnabled(value);
        tlLastName.setEnabled(value);
        tlFirstName.setEnabled(value);
        tlMiddleName.setEnabled(value);

        if(value) fullNameDialogCloseImage.setColorFilter(colorRed);
        else fullNameDialogCloseImage.setColorFilter(colorInitial);
    }

    private void setFullNameUpdateStatusToFalse() {
        isLastNameUpdated = false;
        isFirstNameUpdated = false;
        isMiddleNameUpdated = false;

        tlLastName.setStartIconTintList(cslInitial);
        tlFirstName.setStartIconTintList(cslInitial);
        tlMiddleName.setStartIconTintList(cslInitial);
    }

    private void updateFullName() {
        setFullNameUpdateStatusToFalse();
        setFullNameDialogScreenEnabled(false);
        fullNameDialogProgressBar.setVisibility(View.VISIBLE);

        usersRef.child("lastName").setValue(newLastName).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                isLastNameUpdated = true;
                finishFullNameUpdate();
            }
            else errorFullNameUpdate();
        });
        usersRef.child("firstName").setValue(newFirstName).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                isFirstNameUpdated = true;
                finishFullNameUpdate();
            }
            else errorFullNameUpdate();
        });
        usersRef.child("middleName").setValue(newMiddleName).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                isMiddleNameUpdated = true;
                finishFullNameUpdate();
            }
            else errorFullNameUpdate();
        });
    }

    private void finishFullNameUpdate() {
        if(isLastNameUpdated && isFirstNameUpdated && isMiddleNameUpdated) {
            Toast.makeText(
                    myContext,
                    "Successfully updated the Full Name",
                    Toast.LENGTH_SHORT
            ).show();

            fullNameDialog.dismiss();
            setFullNameDialogScreenEnabled(true);
            fullNameDialogProgressBar.setVisibility(View.GONE);
        }
    }

    private void errorFullNameUpdate() {
        Toast.makeText(
                myContext,
                "Failed to update the Full Name, please try again.",
                Toast.LENGTH_LONG
        ).show();

        setFullNameDialogScreenEnabled(true);
        fullNameDialogProgressBar.setVisibility(View.GONE);
    }

    private void initEmailAddressDialog() {
        emailAddressDialog = new Dialog(myContext);
        emailAddressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        emailAddressDialog.setContentView(R.layout.dialog_update_email_address_layout);

        etEmailAddress = emailAddressDialog.findViewById(R.id.etEmailAddress);
        tlEmailAddress = emailAddressDialog.findViewById(R.id.tlEmailAddress);
        emailAddressUpdateButton = emailAddressDialog.findViewById(R.id.updateButton);
        emailAddressDialogCloseImage = emailAddressDialog.findViewById(R.id.dialogCloseImage);
        emailAddressDialogProgressBar = emailAddressDialog.findViewById(R.id.dialogProgressBar);

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

        emailAddressUpdateButton.setOnClickListener(view -> updateAccount());

        emailAddressDialogCloseImage.setOnClickListener(view -> emailAddressDialog.dismiss());

        emailAddressDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        emailAddressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        emailAddressDialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        emailAddressDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void setEmailAddressDialogScreenEnabled(boolean value) {
        emailAddressDialog.setCanceledOnTouchOutside(value);
        emailAddressDialogCloseImage.setEnabled(value);
        emailAddressUpdateButton.setEnabled(value);
        tlEmailAddress.setEnabled(value);

        if(value) emailAddressDialogCloseImage.setColorFilter(colorRed);
        else emailAddressDialogCloseImage.setColorFilter(colorInitial);
    }

    private void updateAccount() {
        setEmailAddressDialogScreenEnabled(false);
        emailAddressDialogProgressBar.setVisibility(View.VISIBLE);
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

                        if(error.contains("RecentLogin")) {
                            proceedToMainActivity();

                            Toast.makeText(
                                    myContext,
                                    "This operation is sensitive and requires recent authentication." +
                                    "Log in again before trying this request.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        else if(error.contains("UserCollision")) {
                            error = "This Email Address is already registered";

                            tlEmailAddress.setErrorEnabled(true);
                            tlEmailAddress.setError(error);
                            tlEmailAddress.setStartIconTintList(cslRed);

                            setEmailAddressDialogScreenEnabled(true);
                            emailAddressUpdateButton.setEnabled(false);
                            emailAddressDialogProgressBar.setVisibility(View.GONE);
                        }
                        else errorEmailAddressUpdate();
                    }
                });
    }

    private void errorEmailAddressUpdate() {
        Toast.makeText(myContext,
                "Failed to update the Email Address, please try again.",
                Toast.LENGTH_LONG
        ).show();

        setEmailAddressDialogScreenEnabled(true);
        emailAddressDialogProgressBar.setVisibility(View.GONE);
    }

    int tryCount = 0;
    private void sendEmailVerificationLink() {
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        finishEmailAddressUpdate();
                    }
                    else {
                        if(tryCount == 5) {
                            finishEmailAddressUpdate();
                        }
                        else {
                            tryCount++;
                            sendEmailVerificationLink();
                        }
                    }
                });
    }

    private void finishEmailAddressUpdate() {
        proceedToMainActivity();

        Toast.makeText(
                myContext,
                "Successfully updated the Email Address",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void proceedToMainActivity() {
        sendLoginPreferences();

        Intent intent = new Intent(myContext, MainActivity.class);
        myContext.startActivity(intent);
        ((Activity) myContext).finishAffinity();
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
            tlEmailAddress.setErrorEnabled(false);
            tlEmailAddress.setError(null);
            tlEmailAddress.setStartIconTintList(cslBlue);
            vEA = false;
        }

        emailAddressUpdateButton.setEnabled(vEA);
    }

    private void initPasswordDialog() {
        passwordDialog = new Dialog(myContext);
        passwordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        passwordDialog.setContentView(R.layout.dialog_update_password_layout);

        passwordDialogCloseImage = passwordDialog.findViewById(R.id.dialogCloseImage);
        passwordUpdateButton = passwordDialog.findViewById(R.id.updateButton);
        passwordDialogProgressBar = passwordDialog.findViewById(R.id.progressBar);

        etPassword = passwordDialog.findViewById(R.id.etPassword);
        etConfirmPassword = passwordDialog.findViewById(R.id.etConfirmPassword);
        etCurrentPassword = passwordDialog.findViewById(R.id.etCurrentPassword);
        tlPassword = passwordDialog.findViewById(R.id.tlPassword);
        tlConfirmPassword = passwordDialog.findViewById(R.id.tlConfirmPassword);
        tlCurrentPassword = passwordDialog.findViewById(R.id.tlCurrentPassword);

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
        newCurrentPassword = etCurrentPassword.getText().toString();

        switch(sender) {
            case 1:
                if(newPassword.length() >= 8) {
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

        passwordUpdateButton.setEnabled(vPWL && vPWU && vPWLw && vPWN && vCPW);
    }

    private void updatePassword() {
        setPasswordDialogScreenEnabled(false);
        passwordDialogProgressBar.setVisibility(View.VISIBLE);

        firebaseUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) finishPasswordUpdate();
                    else errorPasswordUpdate();
                });
    }

    private void setPasswordDialogScreenEnabled(boolean value) {
        passwordDialog.setCanceledOnTouchOutside(value);
        passwordDialogCloseImage.setEnabled(value);
        passwordUpdateButton.setEnabled(value);
        tlPassword.setEnabled(value);
        tlConfirmPassword.setEnabled(value);
        tlCurrentPassword.setEnabled(value);

        if(value) passwordDialogCloseImage.setColorFilter(colorRed);
        else passwordDialogCloseImage.setColorFilter(colorInitial);
    }

    private void finishPasswordUpdate() {
        Toast.makeText(
                myContext,
                "Successfully updated the Password",
                Toast.LENGTH_SHORT
        ).show();

        passwordDialog.dismiss();
        setPasswordDialogScreenEnabled(true);
        passwordDialogProgressBar.setVisibility(View.GONE);
    }

    private void errorPasswordUpdate() {
        Toast.makeText(
                myContext,
                "Failed to update the Password, please try again.",
                Toast.LENGTH_LONG
        ).show();

        setPasswordDialogScreenEnabled(true);
        passwordDialogProgressBar.setVisibility(View.GONE);
    }

    private void getCurrentUser() {
        progressBar.setVisibility(View.VISIBLE);
        fullNameLayout.setVisibility(View.GONE);
        accountDetailsLayout.setVisibility(View.GONE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = null;
                if(snapshot.exists()) {
                    user = new User(snapshot);
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
        showFullName();
        showAccountDetails();

        tvGreet.setText(defaultGreetText);
        tvGreet.setTextColor(colorWhite);

        progressBar.setVisibility(View.GONE);
    }

    private void errorLoading(String error) {
        tvGreet.setText(error);
        tvGreet.setTextColor(colorRed);

        progressBar.setVisibility(View.GONE);
    }

    private void showFullName() {
        fullNameLayout.setVisibility(View.VISIBLE);

        lastName = user.getLastName();
        firstName = user.getFirstName();
        middleName = user.getMiddleName();

        String formattedLastName = lastName + ", ";
        String formattedFirstName = firstName;
        if(middleName.length() > 0) formattedFirstName += " " + middleName;

        tvLastName.setText(formattedLastName);
        tvFirstNameMiddleName.setText(formattedFirstName);
    }

    private void showAccountDetails() {
        accountDetailsLayout.setVisibility(View.VISIBLE);

        emailAddress = firebaseUser.getEmail();

        tvEmailAddress2.setText(emailAddress);
    }
}