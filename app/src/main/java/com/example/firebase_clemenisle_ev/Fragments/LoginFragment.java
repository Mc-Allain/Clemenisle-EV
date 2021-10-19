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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Classes.Credentials;
import com.example.firebase_clemenisle_ev.MainActivity;
import com.example.firebase_clemenisle_ev.PostRegisterActivity;
import com.example.firebase_clemenisle_ev.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout constraintLayout;
    TextView tvRegister, tvForgot, tvError;
    CheckBox cbRemember;
    TextInputLayout tlEmailAddress, tlPassword;
    EditText etEmailAddress, etPassword;
    Button continueButton;
    ProgressBar progressBar;

    Context myContext;
    Resources myResources;

    String emailAddress, password;

    int colorBlue, colorRed, colorInitial;
    ColorStateList cslInitial, cslBlue, cslRed;

    Dialog dialog;
    TextView tvDialogTitle, tvDialogCaption;
    EditText etDEmailAddress;
    TextInputLayout tlDEmailAddress;
    Button submitButton;
    ImageView dialogCloseImage;
    ProgressBar dialogProgressBar;

    String dialogEmailAddress;
    boolean vEA = false;

    TabPosInterface tabPosInterface;

    public interface TabPosInterface {
        void sendTabPos(int pos);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        constraintLayout = view.findViewById(R.id.constraintLayout);
        tvRegister = view.findViewById(R.id.tvRegister);
        tvForgot = view.findViewById(R.id.tvForgot);
        tvError = view.findViewById(R.id.tvError);
        cbRemember = view.findViewById(R.id.cbRemember);
        tlEmailAddress = view.findViewById(R.id.tlEmailAddress);
        tlPassword = view.findViewById(R.id.tlPassword);
        etEmailAddress = view.findViewById(R.id.etEmailAddress);
        etPassword = view.findViewById(R.id.etPassword);
        continueButton = view.findViewById(R.id.continueButton);
        progressBar = view.findViewById(R.id.progressBar);

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        initEmailAddressDialog();

        colorBlue = myResources.getColor(R.color.blue);
        colorRed = myResources.getColor(R.color.red);
        colorInitial = myResources.getColor(R.color.initial);

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));
        cslRed = ColorStateList.valueOf(myResources.getColor(R.color.red));

        tvRegister.setOnClickListener(view1 -> tabPosInterface.sendTabPos(1));

        firebaseAuth = FirebaseAuth.getInstance();

        tvForgot.setOnClickListener(view1 -> showEmailAddressDialog());

        continueButton.setOnClickListener(view1 -> {
            tlEmailAddress.setStartIconTintList(cslInitial);
            tlPassword.setStartIconTintList(cslInitial);

            loginAccount();
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
                checkLoginInput();
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

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkLoginInput();
            }
        });

        return view;
    }

    private void showEmailAddressDialog() {
        if(dialogEmailAddress != null) {
            etDEmailAddress.setText(null);
            tlDEmailAddress.setErrorEnabled(false);
            tlDEmailAddress.setError(null);
            tlDEmailAddress.setStartIconTintList(cslInitial);
        }
        etDEmailAddress.clearFocus();
        etDEmailAddress.requestFocus();

        dialog.show();
    }

    private void initEmailAddressDialog() {
        dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_email_address_layout);

        tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        tvDialogCaption = dialog.findViewById(R.id.tvDialogCaption);
        etDEmailAddress = dialog.findViewById(R.id.etEmailAddress);
        tlDEmailAddress = dialog.findViewById(R.id.tlEmailAddress);
        submitButton = dialog.findViewById(R.id.updateButton);
        dialogCloseImage = dialog.findViewById(R.id.dialogCloseImage);
        dialogProgressBar = dialog.findViewById(R.id.dialogProgressBar);

        String dialogTitle = "Forgot Password?";
        String dialogCaption = "Please input your email address of forgotten password account.";
        tvDialogTitle.setText(dialogTitle);
        tvDialogCaption.setText(dialogCaption);

        etDEmailAddress.setOnFocusChangeListener((view1, b) -> {
            if(!tlDEmailAddress.isErrorEnabled()) {
                if(b) {
                    tlDEmailAddress.setStartIconTintList(cslBlue);
                }
                else {
                    tlDEmailAddress.setStartIconTintList(cslInitial);
                }
            }
        });

        etDEmailAddress.addTextChangedListener(new TextWatcher() {
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

        submitButton.setOnClickListener(view -> checkEmailAddressRegistration());

        dialogCloseImage.setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void checkEmailAddressInput() {
        dialogEmailAddress = etDEmailAddress.getText().toString();

        if (Credentials.validEmailAddress(dialogEmailAddress)) {
            tlDEmailAddress.setErrorEnabled(false);
            tlDEmailAddress.setError(null);
            tlDEmailAddress.setStartIconTintList(cslBlue);
            vEA = true;
        }
        else {
            tlDEmailAddress.setErrorEnabled(true);
            tlDEmailAddress.setError("Invalid Email Address");
            tlDEmailAddress.setStartIconTintList(cslRed);
            vEA = false;
        }

        submitButton.setEnabled(vEA);
    }

    private void checkEmailAddressRegistration() {
        setDialogScreenEnabled(false);
        dialogProgressBar.setVisibility(View.VISIBLE);
        tlDEmailAddress.setStartIconTintList(cslInitial);

        firebaseAuth.fetchSignInMethodsForEmail(dialogEmailAddress)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        if(task.getResult().getSignInMethods().isEmpty()) {
                            tlDEmailAddress.setErrorEnabled(true);
                            tlDEmailAddress.setError("Unregistered Email Address");
                            tlDEmailAddress.setStartIconTintList(cslRed);

                            setDialogScreenEnabled(true);
                            submitButton.setEnabled(false);
                            dialogProgressBar.setVisibility(View.GONE);
                        }
                        else sendForgotPasswordEmailLink();
                    }
                    else {
                        String error = "";
                        if(task.getException() != null)
                            error = task.getException().toString();

                        if(error.length() > 0) {
                            Toast.makeText(
                                    myContext,
                                    error,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        setDialogScreenEnabled(true);
                        dialogProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void sendForgotPasswordEmailLink() {
        firebaseAuth.sendPasswordResetEmail(dialogEmailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(
                                    myContext,
                                    "The email link to reset your password " +
                                            "has been sent to your email.",
                                    Toast.LENGTH_LONG
                            ).show();

                            dialog.dismiss();
                        }
                        else {
                            String error = "";
                            if(task.getException() != null)
                                error = task.getException().toString();

                            if(error.length() > 0) {
                                Toast.makeText(
                                        myContext,
                                        error,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                        setDialogScreenEnabled(true);
                        dialogProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void setDialogScreenEnabled(boolean value) {
        dialog.setCanceledOnTouchOutside(value);
        tlDEmailAddress.setEnabled(value);
        submitButton.setEnabled(value);
        dialogCloseImage.setEnabled(value);

        if(value) dialogCloseImage.setColorFilter(colorRed);
        else dialogCloseImage.setColorFilter(colorInitial);
    }

    private void loginAccount() {
        setScreenEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.signInWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        firebaseUser = firebaseAuth.getCurrentUser();
                        if(firebaseUser != null) {
                            if(firebaseUser.isEmailVerified()) {
                                sendSharedPreferences();

                                Intent intent = new Intent(myContext, MainActivity.class);
                                intent.putExtra("password", password);
                                startActivity(intent);
                                ((Activity) myContext).finishAffinity();
                            }
                            else {
                                setScreenEnabled(true);
                                progressBar.setVisibility(View.GONE);

                                Intent intent = new Intent(myContext, PostRegisterActivity.class);
                                intent.putExtra("emailAddress", emailAddress);
                                intent.putExtra("password", password);
                                intent.putExtra("remember", cbRemember.isChecked());
                                intent.putExtra("success", false);
                                intent.putExtra("fromRegister", false);
                                startActivity(intent);
                            }
                        }
                        else loginFailed("Failed to get the current user");
                    }
                    else {
                        if(task.getException() != null)
                            loginFailed(task.getException().toString());
                    }
                });
    }

    private void loginFailed(String error) {
        String caption;
        if(error.toLowerCase().contains("no user record") ||
                error.toLowerCase().contains("password is invalid")) {
            caption = "Unregistered account";
        }
        else if(error.toLowerCase().contains("network error")) {
            caption = "Network error. Please try again.";
        }
        else if(error.toLowerCase().contains("internal error")) {
            caption = "Internal error. Please try again.";
        }
        else {
            caption = error;
        }
        tvError.setText(caption);

        setScreenEnabled(true);
        progressBar.setVisibility(View.GONE);

        tvError.setVisibility(View.VISIBLE);
        tlEmailAddress.setErrorEnabled(true);
        tlEmailAddress.setError("  ");
        tlEmailAddress.setStartIconTintList(cslRed);
        tlPassword.setErrorEnabled(true);
        tlPassword.setError("  ");
        tlPassword.setStartIconTintList(cslRed);
    }

    private void sendSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", true);
        editor.putBoolean("remember", cbRemember.isChecked());
        if(cbRemember.isChecked()) {
            editor.putString("emailAddress", emailAddress);
            editor.putString("password", password);
        }
        editor.apply();
    }

    private void checkLoginInput() {
        emailAddress = etEmailAddress.getText().toString();
        password = etPassword.getText().toString();

        tvError.setVisibility(View.GONE);
        tlEmailAddress.setErrorEnabled(false);
        tlEmailAddress.setError(null);
        tlPassword.setErrorEnabled(false);
        tlPassword.setError(null);

        if(etEmailAddress.isFocused()) {
            tlEmailAddress.setStartIconTintList(cslBlue);
        }
        else {
            tlEmailAddress.setStartIconTintList(cslInitial);
        }

        if(etPassword.isFocused()) {
            tlPassword.setStartIconTintList(cslBlue);
        }
        else {
            tlPassword.setStartIconTintList(cslInitial);
        }

        continueButton.setEnabled(Credentials.validEmailAddress(emailAddress) &&
                emailAddress.length() >= 14 && password.length() >= 6);
    }

    private void setScreenEnabled(boolean value) {
        tlEmailAddress.setEnabled(value);
        tlPassword.setEnabled(value);
        cbRemember.setEnabled(value);
        continueButton.setEnabled(value);
        tvRegister.setEnabled(value);
        tvForgot.setEnabled(value);

        if(value) {
            tvRegister.setTextColor(colorBlue);
            tvForgot.setTextColor(colorBlue);
        }
        else {
            tvRegister.setTextColor(colorInitial);
            tvForgot.setTextColor(colorInitial);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            tabPosInterface = (TabPosInterface) activity;
        }
        catch(Exception exception) {
            Toast.makeText(
                    context,
                    "Interface error",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}