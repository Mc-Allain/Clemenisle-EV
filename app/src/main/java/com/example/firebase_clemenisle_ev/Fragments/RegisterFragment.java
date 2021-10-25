package com.example.firebase_clemenisle_ev.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Classes.Credentials;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.MainActivity;
import com.example.firebase_clemenisle_ev.PostRegisterActivity;
import com.example.firebase_clemenisle_ev.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

public class RegisterFragment extends Fragment {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    TextView tvFragmentName, tvFragmentCaption, tvSteps;
    ScrollView scrollView;
    ConstraintLayout firstConstraint, secondConstraint, thirdConstraint;
    EditText etLastName, etFirstName, etMiddleName, etPassword, etConfirmPassword, etEmailAddress;
    TextInputLayout tlLastName, tlFirstName, tlMiddleName, tlPassword, tlConfirmPassword, tlEmailAddress;
    Button continueButton, backButton;

    ImageView pwLengthCheckImage, pwUpperCheckImage, pwLowerCheckImage, pwNumberCheckImage, pwSymbolCheckImage;
    TextView tvPWLength, tvPWUpper, tvPWLower, tvPWNumber, tvPWSymbol;

    ProgressBar progressBar;

    String lastName = "", firstName = "", middleName = "",
            password = "", confirmPassword = "", emailAddress = "";

    String defaultButtonText = "Continue", submitButtonText = "Submit";

    Context myContext;
    Resources myResources;

    int colorGreen, colorRed, colorInitial;
    ColorStateList cslInitial, cslBlue, cslRed;

    boolean vLN = false, vFN = false, vMN = true;
    boolean vPWL = false, vPWU = false, vPWLw = false, vPWN = false, vPWS = false, vCPW = false;
    boolean vEA = false;

    int tryCount = 0;

    public int currentStep = 1, endStep = 3;

    boolean isRegistered = false, isAdded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        tvFragmentName = view.findViewById(R.id.tvFragmentName);
        tvFragmentCaption = view.findViewById(R.id.tvFragmentCaption);
        tvSteps = view.findViewById(R.id.tvSteps);

        scrollView = view.findViewById(R.id.scrollView);

        firstConstraint = view.findViewById(R.id.firstConstraint);
        etLastName = view.findViewById(R.id.etLastName);
        etFirstName = view.findViewById(R.id.etFirstName);
        etMiddleName = view.findViewById(R.id.etMiddleName);
        tlLastName = view.findViewById(R.id.tlLastName);
        tlFirstName = view.findViewById(R.id.tlFirstName);
        tlMiddleName = view.findViewById(R.id.tlMiddleName);

        secondConstraint = view.findViewById(R.id.secondConstraint);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        tlPassword = view.findViewById(R.id.tlPassword);
        tlConfirmPassword = view.findViewById(R.id.tlConfirmPassword);
        pwLengthCheckImage = view.findViewById(R.id.pwLengthCheckImage);
        pwUpperCheckImage = view.findViewById(R.id.pwUpperCheckImage);
        pwLowerCheckImage = view.findViewById(R.id.pwLowerCheckImage);
        pwNumberCheckImage = view.findViewById(R.id.pwNumberCheckImage);
        pwSymbolCheckImage = view.findViewById(R.id.pwSymbolCheckImage);
        tvPWLength = view.findViewById(R.id.tvPWLength);
        tvPWUpper = view.findViewById(R.id.tvPWUpper);
        tvPWLower = view.findViewById(R.id.tvPWLower);
        tvPWNumber = view.findViewById(R.id.tvPWNumber);
        tvPWSymbol = view.findViewById(R.id.tvPWSymbol);

        thirdConstraint = view.findViewById(R.id.thirdConstraint);
        etEmailAddress = view.findViewById(R.id.etEmailAddress);
        tlEmailAddress = view.findViewById(R.id.tlEmailAddress);

        continueButton = view.findViewById(R.id.continueButton);
        backButton = view.findViewById(R.id.backButton);

        progressBar = view.findViewById(R.id.progressBar);

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorGreen = myResources.getColor(R.color.green);
        colorRed = myResources.getColor(R.color.red);
        colorInitial = myResources.getColor(R.color.initial);

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));
        cslRed = ColorStateList.valueOf(myResources.getColor(R.color.red));

        tvSteps.setText(getStepText());

        continueButton.setOnClickListener(view1 -> {
            if(currentStep == 1) {
                tvFragmentName.setVisibility(View.GONE);
                tvFragmentCaption.setVisibility(View.GONE);

                firstConstraint.setVisibility(View.GONE);
                secondConstraint.setVisibility(View.VISIBLE);

                etPassword.requestFocus();
                scrollView.scrollTo(0,0);

                continueButton.setEnabled(false);
                backButton.setVisibility(View.VISIBLE);
            }
            else if(currentStep == 2) {
                secondConstraint.setVisibility(View.GONE);
                thirdConstraint.setVisibility(View.VISIBLE);

                etEmailAddress.requestFocus();
                scrollView.scrollTo(0,0);

                continueButton.setText(submitButtonText);
                continueButton.setEnabled(false);
            }
            else if(currentStep == 3) {
                registerAccount();
            }

            if(currentStep < endStep) {
                currentStep++;
                tvSteps.setText(getStepText());
            }
        });

        backButton.setOnClickListener(view1 -> {
            if(currentStep == 2) {
                tvFragmentName.setVisibility(View.VISIBLE);
                tvFragmentCaption.setVisibility(View.VISIBLE);

                secondConstraint.setVisibility(View.GONE);
                firstConstraint.setVisibility(View.VISIBLE);

                etPassword.setText(null);
                tlPassword.setErrorEnabled(false);
                tlPassword.setError(null);
                tlPassword.setStartIconTintList(cslInitial);

                etConfirmPassword.setText(null);
                tlConfirmPassword.setErrorEnabled(false);
                tlConfirmPassword.setError(null);
                tlConfirmPassword.setStartIconTintList(cslInitial);

                vPWL = false; vPWU = false; vPWLw = false; vPWN = false; vPWS = false; vCPW = false;

                pwLengthCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                pwUpperCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                pwLowerCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                pwNumberCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                pwSymbolCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);

                pwLengthCheckImage.getDrawable().setTint(colorInitial);
                tvPWLength.setTextColor(colorInitial);
                pwUpperCheckImage.getDrawable().setTint(colorInitial);
                tvPWUpper.setTextColor(colorInitial);
                pwLowerCheckImage.getDrawable().setTint(colorInitial);
                tvPWLower.setTextColor(colorInitial);
                pwNumberCheckImage.getDrawable().setTint(colorInitial);
                tvPWNumber.setTextColor(colorInitial);
                pwSymbolCheckImage.getDrawable().setTint(colorInitial);
                tvPWSymbol.setTextColor(colorInitial);

                checkNameInput(0);
                scrollView.scrollTo(0,0);
                backButton.setVisibility(View.GONE);
            }
            else if(currentStep == 3) {
                thirdConstraint.setVisibility(View.GONE);
                secondConstraint.setVisibility(View.VISIBLE);

                etEmailAddress.setText(null);
                tlEmailAddress.setErrorEnabled(false);
                tlEmailAddress.setError(null);
                tlEmailAddress.setStartIconTintList(cslInitial);

                vEA = false;

                continueButton.setText(defaultButtonText);
                checkPasswordInput(0);
                scrollView.scrollTo(0,0);
            }

            if(currentStep > 1) {
                currentStep--;
                tvSteps.setText(getStepText());
            }
        });

        etLastName.setOnFocusChangeListener((view1, b) -> {
            if(!tlLastName.isErrorEnabled()) {
                if(b) {
                    tlLastName.setStartIconTintList(cslBlue);
                }
                else {
                    while (lastName.contains("  ")) {
                        lastName = lastName.replaceAll(" {2}", " ");
                    }
                    etLastName.setText(lastName.trim());
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
                    while (firstName.contains("  ")) {
                        firstName = firstName.replaceAll(" {2}", " ");
                    }
                    etFirstName.setText(firstName.trim());
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
                    while (middleName.contains("  ")) {
                        middleName = middleName.replaceAll(" {2}", " ");
                    }
                    etMiddleName.setText(middleName.trim());
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

        return view;
    }

    private String getStepText() {
        return "Step " + currentStep + " out of " + endStep;
    }

    public void backPressed() {
        backButton.performClick();
    }

    private void registerAccount() {
        setScreenEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tlEmailAddress.setStartIconTintList(cslInitial);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        isRegistered = true;
                        addToDatabase();
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
                            continueButton.setEnabled(false);
                            progressBar.setVisibility(View.GONE);
                        }
                        else registerFailed();
                    }
                });
    }

    private void addToDatabase() {
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null) {
            String userId = firebaseUser.getUid();
            User user = new User(firstName, userId, lastName, middleName);

            if(!isAdded) {
                DatabaseReference usersRef = firebaseDatabase.getReference("users");
                usersRef.child(userId).setValue(user)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                isAdded = true;
                                sendEmailVerificationLink();
                            }
                            else {
                                rollbackUser();
                            }
                        });
            }
        }
    }

    private void sendEmailVerificationLink() {
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        proceedToNextActivity(true);
                    }
                    else {
                        if(tryCount == 5) {
                            proceedToNextActivity(false);
                        }
                        else {
                            tryCount++;
                            sendEmailVerificationLink();
                        }
                    }
                });
    }

    private void proceedToNextActivity(boolean success) {
        Intent intent = new Intent(myContext, MainActivity.class);

        startActivity(intent);
        ((Activity) myContext).finishAffinity();

        intent = new Intent(myContext, PostRegisterActivity.class);
        intent.putExtra("emailAddress", emailAddress);
        intent.putExtra("password", password);
        intent.putExtra("remember", false);
        intent.putExtra("success", success);
        intent.putExtra("fromRegister", true);

        startActivity(intent);
    }

    private void setScreenEnabled(boolean value) {
        tlEmailAddress.setEnabled(value);
        continueButton.setEnabled(value);
        backButton.setEnabled(value);
    }

    private void rollbackUser() {
        if(firebaseUser != null) {
            firebaseUser.delete().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    registerFailed();
                }
                else {
                    addToDatabase();
                }
            });
        }
    }

    private void registerFailed() {
        Toast.makeText(myContext,
                "Registration failed. Please try again.",
                Toast.LENGTH_LONG
        ).show();

        tlEmailAddress.clearFocus();
        setScreenEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    private void checkNameInput(int sender) {
        lastName = etLastName.getText().toString();
        firstName = etFirstName.getText().toString();
        middleName = etMiddleName.getText().toString();

        switch(sender) {
            case 1:
                if(lastName.matches("[A-Za-z Ññ'.]*")) {
                    if(lastName.length() < 2) {
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
                if(firstName.matches("[A-Za-z Ññ'.]*")) {
                    if(firstName.length() < 2) {
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
                if(middleName.matches("[A-Za-z Ññ'.]*")) {
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

        continueButton.setEnabled(vLN && vFN && vMN);
    }

    private void checkPasswordInput(int sender) {
        password = etPassword.getText().toString();
        confirmPassword = etConfirmPassword.getText().toString();

        switch(sender) {
            case 1:
                if(password.length() >= 6) {
                    pwLengthCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwLengthCheckImage.getDrawable().setTint(colorGreen);
                    tvPWLength.setTextColor(colorGreen);
                    vPWL = true;
                }
                else {
                    pwLengthCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwLengthCheckImage.getDrawable().setTint(colorRed);
                    tvPWLength.setTextColor(colorRed);
                    vPWL = false;
                }

                if(password.matches(".*[A-Z].*")) {
                    pwUpperCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwUpperCheckImage.getDrawable().setTint(colorGreen);
                    tvPWUpper.setTextColor(colorGreen);
                    vPWU = true;
                }
                else {
                    pwUpperCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwUpperCheckImage.getDrawable().setTint(colorRed);
                    tvPWUpper.setTextColor(colorRed);
                    vPWU = false;
                }

                if(password.matches(".*[a-z].*")) {
                    pwLowerCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwLowerCheckImage.getDrawable().setTint(colorGreen);
                    tvPWLower.setTextColor(colorGreen);
                    vPWLw = true;
                }
                else {
                    pwLowerCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwLowerCheckImage.getDrawable().setTint(colorRed);
                    tvPWLower.setTextColor(colorRed);
                    vPWLw = false;
                }

                if(password.matches(".*[0-9].*")) {
                    pwNumberCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwNumberCheckImage.getDrawable().setTint(colorGreen);
                    tvPWNumber.setTextColor(colorGreen);
                    vPWN = true;
                }
                else {
                    pwNumberCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwNumberCheckImage.getDrawable().setTint(colorRed);
                    tvPWNumber.setTextColor(colorRed);
                    vPWN = false;
                }

                if(password.matches("[A-Za-z0-9]*")) {
                    pwSymbolCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwSymbolCheckImage.getDrawable().setTint(colorGreen);
                    tvPWSymbol.setTextColor(colorGreen);
                    vPWS = true;
                }
                else {
                    pwSymbolCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwSymbolCheckImage.getDrawable().setTint(colorRed);
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

                if(confirmPassword.length() > 0) {
                    if(confirmPassword.equals(password)) {
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
                if(confirmPassword.length() > 0) {
                    if(confirmPassword.equals(password)) {
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

        continueButton.setEnabled(vPWL && vPWU && vPWLw && vPWN && vPWS && vCPW);
    }

    private void checkEmailAddressInput() {
        emailAddress = etEmailAddress.getText().toString();

        if (Credentials.validEmailAddress(emailAddress)) {
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

        continueButton.setEnabled(vEA);
    }
}