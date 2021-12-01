package com.example.firebase_clemenisle_ev.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Adapters.LikedSpotAdapter;
import com.example.firebase_clemenisle_ev.Adapters.SpotWithCounterAdapter;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.Credentials;
import com.example.firebase_clemenisle_ev.Classes.DateTimeDifference;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.Route;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.IWalletActivity;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;

public class LoggedInUserProfileFragment extends Fragment {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    private final static int PICK_IMAGE_REQUEST = 1;

    ProgressBar progressBar;
    TextView tvGreet, tvGreet2;
    ImageView profileImage;

    ConstraintLayout fullNameLayout;
    TextView tvFullName2;
    ImageView updateFullNameImage;

    ConstraintLayout accountDetailsLayout;
    TextView tvEmailAddress2;
    ImageView updateEmailAddressImage, updatePasswordImage;

    ConstraintLayout iWalletLayout;
    TextView tvIWallet;
    ImageView viewIWalletImage;

    ConstraintLayout incomeLayout;
    TextView tvIncomeToday2, tvIncomeThisWeek2, tvIncomeThisMonth2, tvIncomeThisYear2,
            tvTotalIncome2, tvAmountToRemit2, tvAmountToClaim2;
    ImageView viewIncomeImage;

    TextView tvLikedSpotBadge;
    RecyclerView likedSpotView;

    TextView tvBookedSpotBadge;
    RecyclerView bookedSpotView;

    TextView tvVisitedSpotBadge;
    RecyclerView visitedSpotView;

    Context myContext;
    Resources myResources;

    int colorGreen, colorRed, colorInitial, colorBlack, colorWhite;
    ColorStateList cslInitial, cslBlue, cslRed;

    String defaultGreetText = "こんにちは (Hello)\nWelcome to Clemenisle-EV",
            lastName, firstName, middleName, emailAddress;

    User user;

    String userId;
    boolean isLoggedIn = false, isDriver = false;

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
    ProgressBar passwordDialogProgressBar, roundProgressBar;

    DatabaseReference usersRef;

    EditText etLastName, etFirstName, etMiddleName, etPassword, etConfirmPassword, etEmailAddress;
    TextInputLayout tlLastName, tlFirstName, tlMiddleName, tlPassword, tlConfirmPassword, tlEmailAddress;

    ImageView pwLengthCheckImage, pwUpperCheckImage, pwLowerCheckImage, pwNumberCheckImage, pwSymbolCheckImage;
    TextView tvPWLength, tvPWUpper, tvPWLower, tvPWNumber, tvPWSymbol;

    String newLastName = "", newFirstName = "", newMiddleName = "",
            newPassword = "", newConfirmPassword = "", newEmailAddress = "";

    boolean vLN = false, vFN = false, vMN = true;
    boolean vPWL = false, vPWU = false, vPWLw = false, vPWN = false, vPWS = false, vCPW = false;
    boolean vEA = false;

    LikedSpotAdapter likedSpotAdapter;
    List<SimpleTouristSpot> likedSpots = new ArrayList<>();

    SpotWithCounterAdapter bookedSpotAdapter;
    List<Route> bookedSpots = new ArrayList<>();

    SpotWithCounterAdapter visitedSpotAdapter;
    List<Route> visitedSpots = new ArrayList<>();

    Dialog profileImageDialog;
    ImageView profileImageDialogCloseImage, dialogProfileImage;
    Button chooseImageButton, uploadButton, removeButton;
    ProgressBar profileImageDialogProgressBar;

    Uri profileImageUri;

    boolean isOnScreen = false;

    Dialog reLoginDialog;
    ImageView reLoginDialogCloseImage;

    List<Booking> taskList = new ArrayList<>();

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logged_in_user_profile, container, false);

        tvGreet = view.findViewById(R.id.tvGreet);
        tvGreet2 = view.findViewById(R.id.tvGreet2);
        profileImage = view.findViewById(R.id.profileImage);

        fullNameLayout = view.findViewById(R.id.fullNameLayout);
        tvFullName2 = view.findViewById(R.id.tvFullName2);
        updateFullNameImage = view.findViewById(R.id.updateFullNameImage);

        accountDetailsLayout = view.findViewById(R.id.accountDetailsLayout);
        tvEmailAddress2 = view.findViewById(R.id.tvEmailAddress2);
        updateEmailAddressImage = view.findViewById(R.id.updateEmailAddressImage);
        updatePasswordImage = view.findViewById(R.id.updatePasswordImage);

        iWalletLayout = view.findViewById(R.id.iWalletLayout);
        tvIWallet = view.findViewById(R.id.tvIWallet);
        viewIWalletImage = view.findViewById(R.id.viewIWalletImage);

        incomeLayout = view.findViewById(R.id.incomeLayout);
        tvIncomeToday2 = view.findViewById(R.id.tvIncomeToday2);
        tvIncomeThisWeek2 = view.findViewById(R.id.tvIncomeThisWeek2);
        tvIncomeThisMonth2 = view.findViewById(R.id.tvIncomeThisMonth2);
        tvIncomeThisYear2 = view.findViewById(R.id.tvIncomeThisYear2);
        tvTotalIncome2 = view.findViewById(R.id.tvTotalIncome2);
        tvAmountToRemit2 = view.findViewById(R.id.tvAmountToRemit2);
        tvAmountToClaim2 = view.findViewById(R.id.tvAmountToClaim2);
        viewIncomeImage = view.findViewById(R.id.viewIncomeImage);

        tvLikedSpotBadge = view.findViewById(R.id.tvLikedSpotBadge);
        likedSpotView = view.findViewById(R.id.likedSpotView);

        tvBookedSpotBadge = view.findViewById(R.id.tvBookedSpotBadge);
        bookedSpotView = view.findViewById(R.id.bookedSpotView);

        tvVisitedSpotBadge = view.findViewById(R.id.tvVisitedSpotBadge);
        visitedSpotView = view.findViewById(R.id.visitedSpotView);

        progressBar = view.findViewById(R.id.progressBar);

        myContext = getContext();
        myResources = getResources();

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

        isOnScreen = true;

        storageReference = firebaseStorage.getReference("profileImages");

        usersRef = firebaseDatabase.getReference("users").child(userId);

        initProfileImageDialog();
        initFullNameDialog();
        initEmailAddressDialog();
        initPasswordDialog();
        initPasswordChangeLoginDialog();

        checkIfDriver();
        getCurrentUser();

        profileImage.setOnClickListener(view1 -> showProfileImageDialog());
        updateFullNameImage.setOnClickListener(view1 -> showFullNameDialog());
        updateEmailAddressImage.setOnClickListener(view1 -> showEmailAddressDialog());
        updatePasswordImage.setOnClickListener(view1 -> showPasswordDialog());

        LinearLayoutManager linearLayout1 =
                new LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false);
        likedSpotView.setLayoutManager(linearLayout1);
        likedSpotAdapter = new LikedSpotAdapter(myContext, likedSpots, userId);
        likedSpotView.setAdapter(likedSpotAdapter);

        LinearLayoutManager linearLayout2 =
                new LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false);
        bookedSpotView.setLayoutManager(linearLayout2);
        bookedSpotAdapter = new SpotWithCounterAdapter(myContext, bookedSpots, 0);
        bookedSpotView.setAdapter(bookedSpotAdapter);

        LinearLayoutManager linearLayout3 =
                new LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false);
        visitedSpotView.setLayoutManager(linearLayout3);
        visitedSpotAdapter = new SpotWithCounterAdapter(myContext, visitedSpots, 1);
        visitedSpotView.setAdapter(visitedSpotAdapter);

        viewIWalletImage.setOnClickListener(view12 -> {
            Intent intent = new Intent(myContext, IWalletActivity.class);
            startActivity(intent);
        });

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

        pwLengthCheckImage.getDrawable().setTint(colorInitial);
        pwUpperCheckImage.getDrawable().setTint(colorInitial);
        pwLowerCheckImage.getDrawable().setTint(colorInitial);
        pwNumberCheckImage.getDrawable().setTint(colorInitial);
        pwSymbolCheckImage.getDrawable().setTint(colorInitial);

        vPWL = false; vPWU = false; vPWLw = false; vPWN = false; vPWS = false; vCPW = false;

        passwordDialog.show();
    }

    private void showProfileImageDialog() {
        if(user != null) {
            try {
                Glide.with(myContext).load(user.getProfileImage())
                        .placeholder(R.drawable.image_loading_placeholder)
                        .into(dialogProfileImage);
            }
            catch (Exception ignored) {}

            chooseImageButton.setEnabled(true);
            if(user.getProfileImage() != null) removeButton.setEnabled(true);
            String uploadText = "Upload";
            uploadButton.setText(uploadText);
            uploadButton.setEnabled(false);

            profileImageDialog.show();
        }
    }

    private void initProfileImageDialog() {
        profileImageDialog = new Dialog(myContext);
        profileImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        profileImageDialog.setContentView(R.layout.dialog_profile_image_action_layout);

        profileImageDialogCloseImage = profileImageDialog.findViewById(R.id.dialogCloseImage);
        dialogProfileImage = profileImageDialog.findViewById(R.id.profileImage);

        chooseImageButton = profileImageDialog.findViewById(R.id.chooseImageButton);
        uploadButton = profileImageDialog.findViewById(R.id.uploadButton);
        removeButton = profileImageDialog.findViewById(R.id.removeButton);

        profileImageDialogProgressBar = profileImageDialog.findViewById(R.id.progressBar);
        roundProgressBar = profileImageDialog.findViewById(R.id.roundProgressBar);

        profileImageDialogCloseImage.setOnClickListener(view -> profileImageDialog.dismiss());

        chooseImageButton.setOnClickListener(view -> {
            if(ActivityCompat.checkSelfPermission(myContext,
                            Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED
            ) openStorage();
            else {
                ActivityCompat.requestPermissions((Activity) myContext,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 44);
            }
        });

        removeButton.setOnClickListener(view -> {
            try {
                Glide.with(myContext).load(R.drawable.image_loading_placeholder)
                        .placeholder(R.drawable.image_loading_placeholder)
                        .into(dialogProfileImage);
            }
            catch (Exception ignored) {}

            removeButton.setEnabled(false);
            String uploadText = "Save";
            uploadButton.setText(uploadText);
            uploadButton.setEnabled(true);
        });

        uploadButton.setOnClickListener(view -> {
            if(uploadButton.getText().equals("Upload")) uploadProfileImage();
            else if(uploadButton.getText().equals("Save")) removeProfileImage();
        });

        profileImageDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        profileImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        profileImageDialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        profileImageDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    @SuppressWarnings("deprecation")
    private void openStorage() {
        Intent newIntent = new Intent();
        newIntent.setType("image/*");
        newIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(newIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 44) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.checkSelfPermission(myContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED
                ) openStorage();
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            profileImageUri = data.getData();

            try {
                Glide.with(myContext).load(profileImageUri)
                        .placeholder(R.drawable.image_loading_placeholder)
                        .into(dialogProfileImage);
            }
            catch (Exception ignored) {}


            removeButton.setEnabled(true);
            String uploadText = "Upload";
            uploadButton.setText(uploadText);
            uploadButton.setEnabled(true);
        }
    }

    private void removeProfileImage() {
        setProfileImageDialogScreenEnabled(false);
        roundProgressBar.setVisibility(View.VISIBLE);

        usersRef.child("profileImage").removeValue().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(
                        myContext,
                        "Successfully removed the profile image",
                        Toast.LENGTH_SHORT
                ).show();

                profileImageDialog.dismiss();
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
            setProfileImageDialogScreenEnabled(true);
            roundProgressBar.setVisibility(View.GONE);
        });
    }

    private String getFileExt(Uri uri) {
        ContentResolver contentResolver = myContext.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadProfileImage() {
        roundProgressBar.setVisibility(View.VISIBLE);
        setProfileImageDialogScreenEnabled(false);

        StorageReference profileImageRef =
                storageReference.child(System.currentTimeMillis() + "-" + userId +
                        "." + getFileExt(profileImageUri));

        profileImageRef.putFile(profileImageUri).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if(task.getResult() != null) {
                    task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()) {
                            if(task1.getResult() != null) {
                                usersRef.child("profileImage").setValue(task1.getResult().toString())
                                        .addOnCompleteListener(task2 -> {
                                            if(task2.isSuccessful()) {
                                                new Handler().postDelayed(() -> profileImageDialogProgressBar.setProgress(0), 1000);

                                                Toast.makeText(
                                                        myContext,
                                                        "Successfully uploaded the profile image",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                profileImageDialog.dismiss();
                                            }
                                            else {
                                                if(task.getException() != null) {
                                                    profileImageDialogProgressBar.setProgress(0);
                                                    roundProgressBar.setVisibility(View.GONE);

                                                    String error = task.getException().toString();
                                                    Toast.makeText(
                                                            myContext,
                                                            error,
                                                            Toast.LENGTH_LONG
                                                    ).show();
                                                }
                                                setProfileImageDialogScreenEnabled(true);
                                            }
                                        });
                            }
                            else {
                                if(task.getException() != null) {
                                    profileImageDialogProgressBar.setProgress(0);
                                    roundProgressBar.setVisibility(View.GONE);

                                    String error = task.getException().toString();
                                    Toast.makeText(
                                            myContext,
                                            error,
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                                setProfileImageDialogScreenEnabled(true);
                            }
                        }
                    });
                }
            }
            else {
                if(task.getException() != null) {
                    profileImageDialogProgressBar.setProgress(0);
                    roundProgressBar.setVisibility(View.GONE);

                    String error = task.getException().toString();
                    Toast.makeText(
                            myContext,
                            error,
                            Toast.LENGTH_LONG
                    ).show();
                }
                setProfileImageDialogScreenEnabled(true);
            }
        }).addOnProgressListener(snapshot -> {
            double progress =
                    (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
            profileImageDialogProgressBar.setProgress((int) progress);
        });
    }

    private void setProfileImageDialogScreenEnabled(boolean value) {
        profileImageDialog.setCanceledOnTouchOutside(value);
        profileImageDialog.setCancelable(value);
        profileImageDialogCloseImage.setEnabled(value);
        chooseImageButton.setEnabled(value);
        removeButton.setEnabled(value);
        uploadButton.setEnabled(value);

        if(value) profileImageDialogCloseImage.getDrawable().setTint(colorRed);
        else profileImageDialogCloseImage.getDrawable().setTint(colorInitial);
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
        fullNameDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        fullNameDialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        fullNameDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void checkNameInput(int sender) {
        newLastName = etLastName.getText().toString().trim();
        newFirstName = etFirstName.getText().toString().trim();
        newMiddleName = etMiddleName.getText().toString().trim();

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
        fullNameDialog.setCancelable(value);
        fullNameDialogCloseImage.setEnabled(value);
        fullNameUpdateButton.setEnabled(value);
        tlLastName.setEnabled(value);
        tlFirstName.setEnabled(value);
        tlMiddleName.setEnabled(value);

        if(value) fullNameDialogCloseImage.getDrawable().setTint(colorRed);
        else fullNameDialogCloseImage.getDrawable().setTint(colorInitial);
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
                "Failed to update the Full Name. Please try again.",
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

        emailAddressUpdateButton.setOnClickListener(view -> updateEmailAddress());

        emailAddressDialogCloseImage.setOnClickListener(view -> emailAddressDialog.dismiss());

        emailAddressDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        emailAddressDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        emailAddressDialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        emailAddressDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void setEmailAddressDialogScreenEnabled(boolean value) {
        emailAddressDialog.setCanceledOnTouchOutside(value);
        emailAddressDialog.setCancelable(value);
        emailAddressDialogCloseImage.setEnabled(value);
        emailAddressUpdateButton.setEnabled(value);
        tlEmailAddress.setEnabled(value);

        if(value) emailAddressDialogCloseImage.getDrawable().setTint(colorRed);
        else emailAddressDialogCloseImage.getDrawable().setTint(colorInitial);
    }

    private void updateEmailAddress() {
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
                            Toast.makeText(
                                    myContext,
                                    "This operation is sensitive and requires recent authentication." +
                                    "Please log in again before trying this request.",
                                    Toast.LENGTH_LONG
                            ).show();

                            reLoginDialog.show();
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
                "Failed to update the Email Address. Please try again.",
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
                "Successfully updated the Email Address. Please log in again.",
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
        passwordDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
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

                if(newPassword.matches(".*[A-Z].*")) {
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

                if(newPassword.matches(".*[a-z].*")) {
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

                if(newPassword.matches(".*[0-9].*")) {
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

                if(newPassword.matches("[A-Za-z0-9]*")) {
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
                            Toast.makeText(
                                    myContext,
                                    "This operation is sensitive and requires recent authentication." +
                                            "Please log in again before trying this request.",
                                    Toast.LENGTH_LONG
                            ).show();

                            reLoginDialog.show();
                        }
                        else errorPasswordUpdate();
                    }
                });
    }

    private void initPasswordChangeLoginDialog() {
        reLoginDialog = new Dialog(myContext);
        reLoginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        reLoginDialog.setContentView(R.layout.dialog_re_login_layout);

        reLoginDialogCloseImage =
                reLoginDialog.findViewById(R.id.dialogCloseImage);

        reLoginDialog.setCanceledOnTouchOutside(false);

        reLoginDialogCloseImage.setOnClickListener(view -> reLoginDialog.dismiss());

        reLoginDialog.setOnDismissListener(dialogInterface -> proceedToMainActivity());

        reLoginDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        reLoginDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        reLoginDialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        reLoginDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void setPasswordDialogScreenEnabled(boolean value) {
        passwordDialog.setCanceledOnTouchOutside(value);
        passwordDialog.setCancelable(value);
        passwordDialogCloseImage.setEnabled(value);
        passwordUpdateButton.setEnabled(value);
        tlPassword.setEnabled(value);
        tlConfirmPassword.setEnabled(value);

        if(value) passwordDialogCloseImage.getDrawable().setTint(colorRed);
        else passwordDialogCloseImage.getDrawable().setTint(colorInitial);
    }

    private void finishPasswordUpdate() {
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

    private void getCurrentUser() {
        progressBar.setVisibility(View.VISIBLE);
        fullNameLayout.setVisibility(View.GONE);
        accountDetailsLayout.setVisibility(View.GONE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = null;
                taskList.clear();
                if(snapshot.exists()) {
                    user = new User(snapshot);
                    taskList.addAll(user.getTaskList());
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

        String iWallet = "₱" + user.getIWallet();
        if(iWallet.split("\\.")[1].length() == 1) iWallet += 0;
        tvIWallet.setText(iWallet);

        incomeLayout.setVisibility(View.GONE);
        if(isDriver) {
            incomeLayout.setVisibility(View.VISIBLE);
            double incomeToday = 0, incomeThisWeek = 0, incomeThisMonth = 0, incomeThisYear = 0,
                    totalIncome = 0, amountToRemit = user.getAmountToRemit(), amountToClaim = user.getAmountToClaim();

            for(Booking task : taskList) {
                if(task.getStatus().equals("Completed")) {
                    DateTimeDifference dateTimeDifference = new DateTimeDifference(task.getSchedule());
                    int dayDifference = dateTimeDifference.getDayDifference();
                    int monthDifference = dateTimeDifference.getMonthDifference();
                    int yearDifference = dateTimeDifference.getYearDifference();

                    if(yearDifference == 0) {
                        if(monthDifference == 0) {
                            if(dayDifference < 7) incomeThisWeek += task.getBookingType().getPrice();
                            if(dayDifference == 0) incomeToday += task.getBookingType().getPrice();
                            incomeThisMonth += task.getBookingType().getPrice();
                        }
                        incomeThisYear += task.getBookingType().getPrice();
                    }
                    else if(monthDifference == 1 && dayDifference < 7)
                        incomeThisWeek += task.getBookingType().getPrice();
                    totalIncome += task.getBookingType().getPrice();
                }
            }

            String incomeTodayText = "₱" + incomeToday;
            if(incomeTodayText.split("\\.")[1].length() == 1) incomeTodayText += 0;
            tvIncomeToday2.setText(incomeTodayText);

            String incomeThisWeekText = "₱" + incomeThisWeek;
            if(incomeThisWeekText.split("\\.")[1].length() == 1) incomeThisWeekText += 0;
            tvIncomeThisWeek2.setText(incomeThisWeekText);

            String incomeThisMonthText = "₱" + incomeThisMonth;
            if(incomeThisMonthText.split("\\.")[1].length() == 1) incomeThisMonthText += 0;
            tvIncomeThisMonth2.setText(incomeThisMonthText);

            String incomeThisYearText = "₱" + incomeThisYear;
            if(incomeThisYearText.split("\\.")[1].length() == 1) incomeThisYearText += 0;
            tvIncomeThisYear2.setText(incomeThisYearText);

            String totalIncomeText = "₱" + totalIncome;
            if(totalIncomeText.split("\\.")[1].length() == 1) totalIncomeText += 0;
            tvTotalIncome2.setText(totalIncomeText);

            String amountToRemitText = "₱" + amountToRemit;
            if(amountToRemitText.split("\\.")[1].length() == 1) amountToRemitText += 0;
            tvAmountToRemit2.setText(amountToRemitText);

            String amountToClaimText = "₱" + amountToClaim;
            if(amountToClaimText.split("\\.")[1].length() == 1) amountToClaimText += 0;
            tvAmountToClaim2.setText(amountToClaimText);
        }

        likedSpots.clear();
        likedSpots.addAll(user.getLikedSpots());
        likedSpotAdapter.notifyDataSetChanged();
        if(likedSpots.size() > 0) likedSpotView.setVisibility(View.VISIBLE);
        else likedSpotView.setVisibility(View.GONE);
        tvLikedSpotBadge.setText(String.valueOf(likedSpots.size()));

        bookedSpots.clear(); visitedSpots.clear();
        for(Booking booking : user.getBookingList()) {
            List<Route> routeSpots = booking.getRouteList();
            if(routeSpots.size() > 0) {
                for(Route route : routeSpots) {
                    if(!isInBookedSpot(route)) {
                        route.setBooks(1);
                        bookedSpots.add(route);
                    }
                    else {
                        bookedSpotAddCounter(route);
                    }

                    if(route.isVisited()) {
                        if(!isInVisitedSpot(route)) {
                            route.setVisits(1);
                            visitedSpots.add(route);
                        }
                        else {
                            visitedSpotAddCounter(route);
                        }
                    }
                }
            }
        }
        sortByStats();

        bookedSpotAdapter.notifyDataSetChanged();
        if(bookedSpots.size() > 0) bookedSpotView.setVisibility(View.VISIBLE);
        else bookedSpotView.setVisibility(View.GONE);
        tvBookedSpotBadge.setText(String.valueOf(bookedSpots.size()));

        visitedSpotAdapter.notifyDataSetChanged();
        if(visitedSpots.size() > 0) visitedSpotView.setVisibility(View.VISIBLE);
        else visitedSpotView.setVisibility(View.GONE);
        tvVisitedSpotBadge.setText(String.valueOf(visitedSpots.size()));

        tvGreet.setText(defaultGreetText);
        tvGreet.setTextColor(colorWhite);
        tvGreet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        tvGreet2.setVisibility(View.VISIBLE);

        chooseImageButton.setEnabled(true);
        if(user.getProfileImage() != null && isOnScreen) {
            try {
                Glide.with(myContext).load(user.getProfileImage())
                        .placeholder(R.drawable.image_loading_placeholder)
                        .into(profileImage);

                Glide.with(myContext).load(user.getProfileImage())
                        .placeholder(R.drawable.image_loading_placeholder)
                        .into(dialogProfileImage);
            }
            catch (Exception ignored) {}

            removeButton.setEnabled(true);
        }
        else {
            try {
                Glide.with(myContext).load(R.drawable.image_loading_placeholder)
                        .placeholder(R.drawable.image_loading_placeholder)
                        .into(profileImage);

                Glide.with(myContext).load(R.drawable.image_loading_placeholder)
                        .placeholder(R.drawable.image_loading_placeholder)
                        .into(dialogProfileImage);
            }
            catch (Exception ignored) {}
        }

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isOnScreen = false;
    }

    private void bookedSpotAddCounter(Route route) {
        if(bookedSpots.size() > 0) {
            for(Route bookedSpot : bookedSpots) {
                if(bookedSpot.getId().equals(route.getId())) {
                    bookedSpot.setBooks(bookedSpot.getBooks() + 1);
                }
            }
        }
    }

    private boolean isInBookedSpot(Route targetRoute) {
        if(bookedSpots.size() > 0) {
            for(Route bookedSpot : bookedSpots) {
                if(bookedSpot.getId().equals(targetRoute.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void visitedSpotAddCounter(Route route) {
        if(visitedSpots.size() > 0) {
            for(Route visitedSpot : visitedSpots) {
                if(visitedSpot.getId().equals(route.getId())) {
                    visitedSpot.setVisits(visitedSpot.getVisits() + 1);
                }
            }
        }
    }

    private boolean isInVisitedSpot(Route targetRoute) {
        if(visitedSpots.size() > 0) {
            for(Route visitedSpot : visitedSpots) {
                if(visitedSpot.getId().equals(targetRoute.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void sortByStats() {
        Collections.sort(bookedSpots, new Comparator<Route>() {
            @Override
            public int compare(Route route, Route t1) {
                return route.getBooks() - t1.getBooks();
            }
        });

        Collections.reverse(bookedSpots);

        Collections.sort(visitedSpots, new Comparator<Route>() {
            @Override
            public int compare(Route route, Route t1) {
                return route.getVisits() - t1.getVisits();
            }
        });

        Collections.reverse(visitedSpots);
    }

    private void errorLoading(String error) {
        tvGreet.setText(error);
        tvGreet.setTextColor(colorRed);
        tvGreet.setGravity(Gravity.CENTER);
        tvGreet2.setVisibility(View.GONE);

        likedSpots.clear();
        likedSpotAdapter.notifyDataSetChanged();
        likedSpotView.setVisibility(View.GONE);
        tvLikedSpotBadge.setText(String.valueOf(likedSpots.size()));

        bookedSpots.clear();
        bookedSpotAdapter.notifyDataSetChanged();
        bookedSpotView.setVisibility(View.GONE);
        tvBookedSpotBadge.setText(String.valueOf(bookedSpots.size()));

        visitedSpots.clear();
        visitedSpotAdapter.notifyDataSetChanged();
        visitedSpotView.setVisibility(View.GONE);
        tvVisitedSpotBadge.setText(String.valueOf(visitedSpots.size()));

        progressBar.setVisibility(View.GONE);
    }

    private void showFullName() {
        fullNameLayout.setVisibility(View.VISIBLE);

        lastName = user.getLastName();
        firstName = user.getFirstName();
        middleName = user.getMiddleName();

        String nameFormat1 = "<b>" + lastName + "</b>, ";
        String nameFormat2 = nameFormat1 + firstName;
        if(middleName.length() > 0) nameFormat2 += " " + middleName;

        tvFullName2.setText(fromHtml(nameFormat2));
    }

    private void showAccountDetails() {
        accountDetailsLayout.setVisibility(View.VISIBLE);

        emailAddress = firebaseUser.getEmail();

        tvEmailAddress2.setText(emailAddress);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        if(html == null){
            return new SpannableString("");
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        }
        else {
            return Html.fromHtml(html);
        }
    }

    private void checkIfDriver() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseDatabase.getReference("users").child(userId).child("driver").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) isDriver = snapshot.getValue(Boolean.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(
                                myContext,
                                "Failed to get the current user",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}