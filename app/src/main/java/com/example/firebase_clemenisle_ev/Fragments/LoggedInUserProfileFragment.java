package com.example.firebase_clemenisle_ev.Fragments;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.User;
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
    TextView tvEmailAddress2, tvPassword2;
    ImageView updateEmailAddressImage, updatePasswordImage;

    Context myContext;
    Resources myResources;

    int colorGreen, colorRed, colorInitial, colorBlack, colorWhite;
    ColorStateList cslInitial, cslBlue, cslRed;

    String defaultGreetText = "こんにちは (Hello)", lastName, firstName, middleName;

    User user;

    String userId;
    boolean loggedIn = false;

    Dialog fullNameDialog;

    ImageView fullNameDialogCloseImage;
    ScrollView fullNameScrollView;
    Button fullNameUpdateButton;
    ProgressBar fullNameDialogProgressBar;

    DatabaseReference usersRef;

    EditText etLastName, etFirstName, etMiddleName, etPassword, etConfirmPassword, etEmailAddress;
    TextInputLayout tlLastName, tlFirstName, tlMiddleName, tlPassword, tlConfirmPassword, tlEmailAddress;

    ImageView pwLengthCheckImage, pwUpperCheckImage, pwLowerCheckImage, pwNumberCheckImage, pwSymbolCheckImage;
    TextView tvPWLength, tvPWUpper, tvPWLower, tvPWNumber, tvPWSymbol;

    String newLastName = "", newFirstName = "", newMiddleName = "",
            newPassword = "", newConfirmPassword = "", newEmailAddress = "";

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        loggedIn = sharedPreferences.getBoolean("loggedIn", false);
    }

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "logIn", Context.MODE_PRIVATE);
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
        tvPassword2 = view.findViewById(R.id.tvPassword2);
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

        initFullNameDialog();

        usersRef = firebaseDatabase.getReference("users").child(userId);

        getCurrentUser();

        updateFullNameImage.setOnClickListener(view1 -> showFullNameDialog());

        return view;
    }

    private void showFullNameDialog() {
        etLastName.setText(lastName);
        etFirstName.setText(firstName);
        etMiddleName.setText(middleName);

        tlLastName.setStartIconTintList(cslInitial);
        tlFirstName.setStartIconTintList(cslInitial);
        tlMiddleName.setStartIconTintList(cslInitial);

        etLastName.requestFocus();

        fullNameDialog.show();
    }

    private void initFullNameDialog() {
        fullNameDialog = new Dialog(myContext);
        fullNameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        fullNameDialog.setContentView(R.layout.dialog_update_full_name_layout);

        fullNameDialogCloseImage = fullNameDialog.findViewById(R.id.dialogCloseImage);
        fullNameScrollView = fullNameDialog.findViewById(R.id.scrollView);
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

    boolean vLN = false, vFN = false, vMN = true;
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
        fullNameDialogCloseImage.setEnabled(value);
        tlLastName.setEnabled(value);
        tlFirstName.setEnabled(value);
        tlMiddleName.setEnabled(value);

        if(value) fullNameDialogCloseImage.setColorFilter(colorRed);
        else fullNameDialogCloseImage.setColorFilter(colorInitial);
    }

    private void setFullNameUpdateStatus(boolean value) {
        isLastNameUpdated = value;
        isFirstNameUpdated = value;
        isMiddleNameUpdated = value;
    }

    boolean isLastNameUpdated = false, isFirstNameUpdated = false, isMiddleNameUpdated = false;
    private void updateFullName() {
        setFullNameUpdateStatus(false);
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
                "Failed to update the Full Name, please try again",
                Toast.LENGTH_SHORT
        ).show();

        setFullNameDialogScreenEnabled(true);
        fullNameDialogProgressBar.setVisibility(View.GONE);
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

        String emailAddress = user.getEmailAddress();
        StringBuilder password = new StringBuilder(user.getPassword().substring(0, 2));

        for(int i = 0; i<user.getPassword().length() - 3; i++) {
            password.append("•");
        }
        password.append(user.getPassword().charAt(password.length()));

        tvEmailAddress2.setText(emailAddress);
        tvPassword2.setText(password.toString());
    }
}