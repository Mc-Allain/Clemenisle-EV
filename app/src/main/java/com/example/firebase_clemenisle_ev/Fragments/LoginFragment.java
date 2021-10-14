package com.example.firebase_clemenisle_ev.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Classes.Credentials;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.MainActivity;
import com.example.firebase_clemenisle_ev.PostRegisterActivity;
import com.example.firebase_clemenisle_ev.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
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

    int colorBlue, colorInitial;
    ColorStateList cslInitial, cslBlue, cslRed;

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

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));
        cslRed = ColorStateList.valueOf(myResources.getColor(R.color.red));

        tvRegister.setOnClickListener(view1 -> tabPosInterface.sendTabPos(1));

        firebaseAuth = FirebaseAuth.getInstance();

        tvForgot.setOnClickListener(view1 -> {
        });

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

        editor.putBoolean("loggedIn", true);
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
                emailAddress.length() >= 14 && password.length() >= 8);
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