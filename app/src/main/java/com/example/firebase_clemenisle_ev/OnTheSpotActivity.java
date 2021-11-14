package com.example.firebase_clemenisle_ev;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.Capture;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.Fragments.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentManager;

public class OnTheSpotActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    private final static int MAP_SETTINGS_REQUEST = 1;

    ImageView profileImage, driverProfileImage, thumbnail, moreImage, locateImage, locateDestinationImage, viewQRImage,
            chatImage, driverImage, passImage, stopImage, checkImage, rateImage, remarksImage, reloadImage;
    TextView tvUserFullName, tvPassenger, tvDriverFullName, tvPlateNumber, tvPickUpTime, tvDropOffTime,
            tvBookingId, tvSchedule, tvTypeName, tvPrice, tvOriginLocation2, tvDestinationSpot2,
            tvLocate, tvLocateDestination, tvViewQR, tvChat, tvDriver, tvPass, tvStop, tvCheck, tvRate, tvRemarks, tvLog;
    ExpandableTextView extvMessage;
    ConstraintLayout buttonLayout, buttonLayout2, bookingInfoLayout, bookingInfoButtonLayout,
            userInfoLayout, driverInfoLayout, timeInfoLayout;
    Button cancelButton, dropOffButton;
    FrameLayout mapLayout;
    ProgressBar progressBar;

    TextView tvCurrentLocation, tvLocateOnTheSpot, tvMapSettings;
    ImageView currentLocationImage, locateOnTheSpotImage, mapSettingsImage;

    Context myContext;
    Resources myResources;

    int colorGreen, colorInitial, colorBlue, colorRed;
    ColorStateList cslInitial, cslBlue;

    String userId, driverUserId, taskDriverUserId;

    boolean isLoggedIn = false, inDriverModule = false;

    DatabaseReference bookingListRef;

    String bookingId, schedule, typeName, price, status, message, previousDriverUserId;
    LatLng originLocation, destinationSpotLocation;
    String pickUpTime, dropOffTime, reason, remarks;

    SimpleTouristSpot destinationSpot;

    MapFragment mapFragment;
    FragmentManager fragmentManager = getSupportFragmentManager();

    boolean isOnScreen = false, isOptionShown = false;

    Handler optionHandler = new Handler();
    Runnable optionRunnable;

    String defaultLogText = "Failed to get current booking data";

    long lastPressSec = 0;
    int pressCount = 0;
    Toast cancelToast, errorToast;
    
    long dropOffPressedTime;
    Toast dropOffToast;

    String cancelButtonText = "Cancel Booking", cancellingButtonText = "Cancelling…";

    Dialog dialog;
    ImageView dialogCloseImage, preferencesImage;
    TextView tvMessage, tvMessage2, tvPreferences;

    boolean isShowBookingAlertEnabled;

    Dialog qrCodeDialog;
    ImageView qrCodeDialogCloseImage, qrCodeImage;

    boolean isScanning = false;

    DateTimeToString dateTimeToString;

    Calendar calendar = Calendar.getInstance();
    int calendarYear, calendarMonth, calendarDay;

    CountDownTimer statusTimer;

    List<User> users = new ArrayList<>();

    String defaultPassengerText = "Passenger", requestText = "Your Task on Request";

    List<Booking> taskList3 = new ArrayList<>();

    String pickUpTimeText = "<b>Pick-up Time</b>: ", dropOffTimeText = "<b>Drop-off Time</b>: ";

    Dialog dialog2;
    ImageView dialogCloseImage2;
    Button dialogSubmitButton;
    ProgressBar dialogProgressBar;

    EditText etReason;
    TextInputLayout tlReason;
    String reasonValue;

    Dialog dialog3;
    ImageView dialogCloseImage3, star1Image, star2Image, star3Image, star4Image, star5Image;
    Button dialogSubmitButton2;
    ProgressBar dialogProgressBar2;

    EditText etRemarks;
    TextInputLayout tlRemarks;
    String remarksValue;
    int starValue = 0;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        isShowBookingAlertEnabled = sharedPreferences.getBoolean("isShowBookingAlertEnabled", true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_the_spot);

        userInfoLayout = findViewById(R.id.userInfoLayout);
        tvUserFullName = findViewById(R.id.tvUserFullName);
        tvPassenger = findViewById(R.id.tvPassenger);
        profileImage = findViewById(R.id.profileImage);

        driverInfoLayout = findViewById(R.id.driverInfoLayout);
        tvDriverFullName = findViewById(R.id.tvDriverFullName);
        tvPlateNumber = findViewById(R.id.tvPlateNumber);
        driverProfileImage = findViewById(R.id.driverProfileImage);

        timeInfoLayout = findViewById(R.id.timeInfoLayout);
        tvPickUpTime = findViewById(R.id.tvPickUpTime);
        tvDropOffTime = findViewById(R.id.tvDropOffTime);

        thumbnail = findViewById(R.id.thumbnail);
        tvBookingId = findViewById(R.id.tvBookingId);
        tvSchedule = findViewById(R.id.tvSchedule);
        tvTypeName = findViewById(R.id.tvTypeName);
        tvPrice = findViewById(R.id.tvPrice);
        tvOriginLocation2 = findViewById(R.id.tvOriginLocation2);
        tvDestinationSpot2 = findViewById(R.id.tvDestinationSpot2);
        tvLocate = findViewById(R.id.tvLocate);
        tvLocateDestination = findViewById(R.id.tvLocateDestination);
        tvLog = findViewById(R.id.tvLog);
        extvMessage = findViewById(R.id.extvMessage);

        buttonLayout = findViewById(R.id.buttonLayout);
        bookingInfoLayout = findViewById(R.id.bookingInfoLayout);
        bookingInfoButtonLayout = findViewById(R.id.bookingInfoButtonLayout);
        cancelButton = findViewById(R.id.cancelButton);
        moreImage = findViewById(R.id.moreImage);
        locateImage = findViewById(R.id.locateImage);
        locateDestinationImage = findViewById(R.id.locateDestinationImage);

        tvViewQR = findViewById(R.id.tvViewQR);
        viewQRImage = findViewById(R.id.viewQRImage);
        tvChat = findViewById(R.id.tvChat);
        chatImage = findViewById(R.id.chatImage);
        tvDriver = findViewById(R.id.tvDriver);
        driverImage = findViewById(R.id.driverImage);
        tvPass = findViewById(R.id.tvPass);
        passImage = findViewById(R.id.passImage);
        tvStop = findViewById(R.id.tvStop);
        stopImage = findViewById(R.id.stopImage);
        tvCheck = findViewById(R.id.tvCheck);
        checkImage = findViewById(R.id.checkImage);
        tvRate = findViewById(R.id.tvRate);
        rateImage = findViewById(R.id.rateImage);
        tvRemarks = findViewById(R.id.tvRemarks);
        remarksImage = findViewById(R.id.remarksImage);

        reloadImage = findViewById(R.id.reloadImage);
        mapLayout = findViewById(R.id.mapLayout);
        progressBar = findViewById(R.id.progressBar);

        tvCurrentLocation = findViewById(R.id.tvCurrentLocation);
        currentLocationImage = findViewById(R.id.currentLocationImage);
        tvLocateOnTheSpot = findViewById(R.id.tvLocateOnTheSpot);
        locateOnTheSpotImage = findViewById(R.id.locateOnTheSpotImage);
        tvMapSettings = findViewById(R.id.tvMapSettings);
        mapSettingsImage = findViewById(R.id.mapSettingsImage);

        buttonLayout2 = findViewById(R.id.buttonLayout2);
        dropOffButton = findViewById(R.id.dropOffButton);

        myContext = OnTheSpotActivity.this;
        myResources = getResources();

        colorGreen = myResources.getColor(R.color.green);
        colorInitial = myResources.getColor(R.color.initial);
        colorBlue = myResources.getColor(R.color.blue);
        colorRed = myResources.getColor(R.color.red);

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));

        optionRunnable = () -> closeOption();

        initSharedPreferences();
        initBookingAlertDialog();
        initQRCodeDialog();
        initReasonDialog();
        initRateTheDriverDialog();

        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");
        inDriverModule = intent.getBooleanExtra("inDriverModule", false);
        if(inDriverModule) {
            status = intent.getStringExtra("status");
            previousDriverUserId = intent.getStringExtra("previousDriverUserId");
        }

        isOnScreen = true;

        try {
            Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage);
        }
        catch (Exception ignored) {}

        firebaseAuth = FirebaseAuth.getInstance();
        if(isLoggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) {
                firebaseUser.reload();
                if(inDriverModule) {
                    driverUserId = firebaseUser.getUid();
                    userId = intent.getStringExtra("userId");
                    isScanning = intent.getBooleanExtra("isScanning", false);

                    if(isScanning) scanQRCode();
                }
                else userId = firebaseUser.getUid();
            }
        }

        getBookingData();

        cancelButton.setOnClickListener(view -> {
            if(cancelToast != null) {
                cancelToast.cancel();
            }

            if(errorToast != null) {
                errorToast.cancel();
            }

            if(lastPressSec + 3000 > System.currentTimeMillis()) {
                pressCount++;

                if(pressCount == 5) {
                    progressBar.setVisibility(View.VISIBLE);

                    cancelButton.setEnabled(false);
                    cancelButton.setText(cancellingButtonText);
                    cancelBooking();
                }
            }
            else {
                pressCount = 1;
            }

            lastPressSec = System.currentTimeMillis();

            cancelToast = Toast.makeText(myContext,
                    "Press " + (5 - pressCount) +" more time(s) to cancel booking",
                    Toast.LENGTH_SHORT);

            if(pressCount < 5) {
                cancelToast.show();
            }
            else {
                pressCount = 0;
            }
        });

        moreImage.setOnClickListener(view -> {
            if(!isOptionShown) {
                openOption();
            }
            else {
                closeOption();
            }
        });

        tvLocate.setOnClickListener(view -> openMap("O-00", originLocation,
                "Origin Location", 2));
        locateImage.setOnClickListener(view -> openMap("O-00", originLocation,
                "Origin Location", 2));

        tvLocateDestination.setOnClickListener(view -> openMap(destinationSpot.getId(),
                destinationSpotLocation, destinationSpot.getName(), 0));
        locateDestinationImage.setOnClickListener(view -> openMap(destinationSpot.getId(),
                destinationSpotLocation, destinationSpot.getName(), 0));

        currentLocationImage.setOnClickListener(view ->
                mapFragment.getUserCurrentLocation(originLocation, "Origin Location"));
        tvCurrentLocation.setOnClickListener(view ->
                mapFragment.getUserCurrentLocation(originLocation, "Origin Location"));

        locateOnTheSpotImage.setOnClickListener(view -> mapFragment.locateOnTheSpot("Origin Location"));
        tvLocateOnTheSpot.setOnClickListener(view -> mapFragment.locateOnTheSpot("Origin Location"));

        mapSettingsImage.setOnClickListener(view -> openMapSettings());
        tvMapSettings.setOnClickListener(view -> openMapSettings());

        dropOffButton.setOnClickListener(view -> {
            if (dropOffPressedTime + 2500 > System.currentTimeMillis()) {
                dropOffToast.cancel();
                completeTask();
            } else {
                dropOffToast = Toast.makeText(myContext,
                        "Press again to drop off", Toast.LENGTH_SHORT);
                dropOffToast.show();
            }

            dropOffPressedTime = System.currentTimeMillis();
        });

        getUsers();
    }

    private void openRateTheDriverDialog() {
        etRemarks.setText(null);
        clickStar(0);

        tlRemarks.setErrorEnabled(false);
        tlRemarks.setError(null);
        tlRemarks.setStartIconTintList(cslInitial);

        tlRemarks.clearFocus();
        tlRemarks.requestFocus();

        dialogSubmitButton2.setOnClickListener(view -> rate());

        dialog3.show();
    }

    private void initRateTheDriverDialog() {
        dialog3 = new Dialog(myContext);
        dialog3.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog3.setContentView(R.layout.dialog_input_rate_layout);

        etRemarks = dialog3.findViewById(R.id.etRemarks);
        tlRemarks = dialog3.findViewById(R.id.tlRemarks);
        dialogSubmitButton2 = dialog3.findViewById(R.id.submitButton);
        dialogCloseImage3 = dialog3.findViewById(R.id.dialogCloseImage);
        dialogProgressBar2 = dialog3.findViewById(R.id.dialogProgressBar);

        star1Image = dialog3.findViewById(R.id.star1Image);
        star2Image = dialog3.findViewById(R.id.star2Image);
        star3Image = dialog3.findViewById(R.id.star3Image);
        star4Image = dialog3.findViewById(R.id.star4Image);
        star5Image = dialog3.findViewById(R.id.star5Image);

        star1Image.setOnClickListener(view -> clickStar(1));
        star2Image.setOnClickListener(view -> clickStar(2));
        star3Image.setOnClickListener(view -> clickStar(3));
        star4Image.setOnClickListener(view -> clickStar(4));
        star5Image.setOnClickListener(view -> clickStar(5));

        etRemarks.setOnFocusChangeListener((view1, b) -> {
            if(!tlRemarks.isErrorEnabled()) {
                if(b) {
                    tlRemarks.setStartIconTintList(cslBlue);
                }
                else {
                    tlRemarks.setStartIconTintList(cslInitial);
                }
            }
        });

        etRemarks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                remarksValue = etRemarks.getText().toString().trim();
            }
        });

        dialogCloseImage3.setOnClickListener(view -> dialog3.dismiss());

        dialog3.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog3.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        dialog3.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog3.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void clickStar(int count) {
        starValue = count;

        star1Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        star2Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        star3Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        star4Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        star5Image.setImageResource(R.drawable.ic_baseline_star_outline_24);

        if(count >= 1) star1Image.setImageResource(R.drawable.ic_baseline_star_24);
        if(count >= 2) star2Image.setImageResource(R.drawable.ic_baseline_star_24);
        if(count >= 3) star3Image.setImageResource(R.drawable.ic_baseline_star_24);
        if(count >= 4) star4Image.setImageResource(R.drawable.ic_baseline_star_24);
        if(count >= 5) star5Image.setImageResource(R.drawable.ic_baseline_star_24);

        dialogSubmitButton2.setEnabled(count != 0);
    }

    private void rate() {
        getDriverUserId();

        if(taskDriverUserId != null) {
            dialogProgressBar2.setVisibility(View.VISIBLE);
            setDialogScreenEnabled(false);

            usersRef.child(userId).child("bookingList").
                    child(bookingId).child("rating").setValue(starValue);

            usersRef.child(taskDriverUserId).child("taskList").
                    child(bookingId).child("remarks").setValue(remarksValue);
            usersRef.child(taskDriverUserId).child("taskList").
                    child(bookingId).child("rating").setValue(starValue)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Toast.makeText(
                                    myContext,
                                    "Successfully rated the driver",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        else {
                            Toast.makeText(
                                    myContext,
                                    "Failed to rate the driver",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        dialogProgressBar2.setVisibility(View.GONE);
                        setDialogScreenEnabled(true);
                        dialog3.dismiss();
                    });
        }
    }

    private void openReasonDialog(Booking booking) {
        etReason.setText(reason);

        tlReason.setErrorEnabled(false);
        tlReason.setError(null);
        tlReason.setStartIconTintList(cslInitial);

        tlReason.clearFocus();
        tlReason.requestFocus();

        dialogSubmitButton.setOnClickListener(view -> submitReason(booking));

        dialog2.show();
    }

    private void setDialogScreenEnabled(boolean value) {
        dialog.setCanceledOnTouchOutside(value);
        dialog.setCancelable(value);
        tlReason.setEnabled(value);
        dialogSubmitButton.setEnabled(value);

        dialog2.setCanceledOnTouchOutside(value);
        dialog2.setCancelable(value);
        tlRemarks.setEnabled(value);
        dialogSubmitButton2.setEnabled(value);

        if(value) {
            dialogCloseImage.getDrawable().setTint(colorRed);
            dialogCloseImage2.getDrawable().setTint(colorRed);
        }
        else {
            dialogCloseImage.getDrawable().setTint(colorInitial);
            dialogCloseImage2.getDrawable().setTint(colorInitial);
        }
    }

    private void submitReason(Booking booking) {
        dialogProgressBar.setVisibility(View.VISIBLE);
        setDialogScreenEnabled(false);
        usersRef.child(driverUserId).child("taskList").
                child(booking.getId()).child("reason").setValue(reasonValue)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) passTask(booking);
                    else {
                        Toast.makeText(
                                myContext,
                                "Failed to pass the task",
                                Toast.LENGTH_LONG
                        ).show();
                        dialogProgressBar.setVisibility(View.GONE);
                        setDialogScreenEnabled(true);
                    }
                });
    }

    private void initReasonDialog() {
        dialog2 = new Dialog(myContext);
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog2.setContentView(R.layout.dialog_input_pass_task_reason_layout);

        etReason = dialog2.findViewById(R.id.etReason);
        tlReason = dialog2.findViewById(R.id.tlReason);
        dialogSubmitButton = dialog2.findViewById(R.id.submitButton);
        dialogCloseImage2 = dialog2.findViewById(R.id.dialogCloseImage);
        dialogProgressBar = dialog2.findViewById(R.id.dialogProgressBar);

        etReason.setOnFocusChangeListener((view1, b) -> {
            if(!tlReason.isErrorEnabled()) {
                if(b) {
                    tlReason.setStartIconTintList(cslBlue);
                }
                else {
                    tlReason.setStartIconTintList(cslInitial);
                }
            }
        });

        etReason.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                reasonValue = etReason.getText().toString().trim();
                dialogSubmitButton.setEnabled(reasonValue.length() > 0);
            }
        });

        dialogCloseImage2.setOnClickListener(view -> dialog2.dismiss());

        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog2.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        dialog2.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog2.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void completeTask() {
        usersRef.child(userId).child("bookingList").
                child(bookingId).child("dropOffTime").
                setValue(new DateTimeToString().getDateAndTime());

        usersRef.child(driverUserId).child("taskList").
                child(bookingId).child("status").setValue("Completed");
        usersRef.child(driverUserId).child("taskList").
                child(bookingId).child("dropOffTime").
                setValue(new DateTimeToString().getDateAndTime()).
        addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(
                        myContext,
                        "The Task is now Completed",
                        Toast.LENGTH_SHORT
                ).show();

                status = "Completed";
                updateInfo();
                getUsers();
            }
            else {
                Toast.makeText(
                        myContext,
                        "Failed to complete the task",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void getUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);
                        users.add(user);
                    }
                }

                if(inDriverModule) {
                    Query task3Query = usersRef.child(driverUserId).child("taskList").
                            orderByChild("status").equalTo("Ongoing");

                    task3Query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            taskList3.clear();

                            if(snapshot.exists()) {
                                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    Booking task = new Booking(dataSnapshot);
                                    taskList3.add(task);
                                }
                            }

                            driverInfoLayout.setVisibility(View.GONE);
                            userInfoLayout.setVisibility(View.VISIBLE);

                            String messageText = "";
                            if(message.length() > 0) {
                                messageText = "<b>Message</b>: " + message;
                                extvMessage.setText(fromHtml(messageText));
                            }

                            getDriverUserId();
                            getReason();
                            getRemarks();

                            String reasonText = messageText;
                            if((status.equals("Request") || status.equals("Passed")) &&
                                    reason != null && reason.length() > 0 &&
                                    taskDriverUserId.equals(driverUserId)) {
                                reasonText += "<br><br><b>Your Reason</b>: " + reason;
                                extvMessage.setText(fromHtml(reasonText));
                            }

                            String remarksText = reasonText;
                            if((status.equals("Cancelled") || status.equals("Failed")) &&
                                    remarks != null && remarks.length() > 0) {
                                remarksText += "<br><br><b>Your Remarks</b>: " + remarks;
                                extvMessage.setText(fromHtml(remarksText));
                            }

                            getUserInfo();
                            if(inDriverModule && (status.equals("Passed") ||
                                    previousDriverUserId != null && previousDriverUserId.length() > 0 ||
                                    status.equals("Request") && !taskDriverUserId.equals(userId)))
                                getDriverInfo();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(
                                    myContext,
                                    error.toString(),
                                    Toast.LENGTH_LONG
                            ).show();

                            taskList3.clear();
                        }
                    });
                }
                else {
                    userInfoLayout.setVisibility(View.GONE);
                    driverInfoLayout.setVisibility(View.GONE);

                    tvViewQR.setVisibility(View.VISIBLE);
                    viewQRImage.setVisibility(View.VISIBLE);

                    tvViewQR.setVisibility(View.VISIBLE);
                    viewQRImage.setVisibility(View.VISIBLE);

                    tvViewQR.setOnClickListener(view -> viewQRCode());
                    viewQRImage.setOnClickListener(view -> viewQRCode());

                    getDriverInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getReason() {
        for(User user : users) {
            if(user.getId().equals(driverUserId)) {
                List<Booking> taskList = user.getTaskList();
                for(Booking task : taskList) {
                    if(task.getId().equals(bookingId)) {
                        reason = task.getReason();
                        return;
                    }
                }
            }
        }
    }

    private void getRemarks() {
        for(User user : users) {
            if(user.getId().equals(driverUserId)) {
                List<Booking> taskList = user.getTaskList();
                for(Booking task : taskList) {
                    if(task.getId().equals(bookingId)) {
                        remarks = task.getRemarks();
                        return;
                    }
                }
            }
        }
    }

    private void openChat() {
        Intent intent = new Intent(myContext, ChatActivity.class);
        intent.putExtra("taskId", bookingId);
        intent.putExtra("inDriverModule", inDriverModule);
        getDriverUserId();
        if(!inDriverModule) intent.putExtra("driverUserId", taskDriverUserId);
        myContext.startActivity(intent);
    }

    private void getDriverInfo() {
        for(User user : users) {
            if(previousDriverUserId != null && previousDriverUserId.length() > 0 &&
                    user.getId().equals(previousDriverUserId) &&
                    !status.equals("Request") && !status.equals("Passed")) {
                String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
                tvDriverFullName.setText(fromHtml(fullName));

                String plateNumber = "Previous Driver";
                tvPlateNumber.setText(plateNumber);

                try {
                    Glide.with(myContext).load(user.getProfileImage())
                            .placeholder(R.drawable.image_loading_placeholder)
                            .into(driverProfileImage);
                }
                catch (Exception ignored) {}

                driverInfoLayout.setVisibility(View.VISIBLE);

                return;
            }
            else if(!status.equals("Booked") || previousDriverUserId == null || previousDriverUserId.length() == 0) {
                List<Booking> taskList = user.getTaskList();
                for(Booking task : taskList) {
                    if(task.getId().equals(bookingId) && !task.getStatus().equals("Passed")) {
                        String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                        if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
                        tvDriverFullName.setText(fromHtml(fullName));

                        String plateNumber = "<b>Plate Number</b>: " + user.getPlateNumber();
                        if(status.equals("Request")) plateNumber = "Driver on Request";
                        if(status.equals("Passed")) plateNumber = "Current Driver";
                        tvPlateNumber.setText(fromHtml(plateNumber));

                        try {
                            Glide.with(myContext).load(user.getProfileImage())
                                    .placeholder(R.drawable.image_loading_placeholder)
                                    .into(driverProfileImage);
                        }
                        catch (Exception ignored) {}

                        driverUserId = user.getId();
                        driverInfoLayout.setVisibility(View.VISIBLE);

                        return;
                    }
                }
            }
        }
    }

    private void getDriverUserId() {
        for(User user : users) {
            List<Booking> taskList = user.getTaskList();
            for(Booking task : taskList) {
                if(task.getId().equals(bookingId) && !task.getStatus().equals("Passed")) {
                    taskDriverUserId = user.getId();
                    return;
                }
            }
        }
    }

    private void startTimer(Booking booking) {
        if(statusTimer != null) statusTimer.cancel();
        statusTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                if(userId != null) {
                    checkBooking(booking);
                }

                start();
            }
        }.start();
    }

    private void checkBooking(Booking booking) {
        dateTimeToString = new DateTimeToString();
        dateTimeToString.setFormattedSchedule(booking.getSchedule());
        int bookingYear = Integer.parseInt(dateTimeToString.getYear());
        int bookingMonth = Integer.parseInt(dateTimeToString.getMonthNo());
        int bookingDay = Integer.parseInt(dateTimeToString.getDay());

        calendarYear = calendar.get(Calendar.YEAR);
        calendarMonth = calendar.get(Calendar.MONTH);
        calendarDay = calendar.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date().getTime());
        int hour = Integer.parseInt(currentTime.split(":")[0]);
        int min = Integer.parseInt(currentTime.split(":")[1]);

        int bookingHour = Integer.parseInt(dateTimeToString.getRawHour());
        int bookingMin = Integer.parseInt(dateTimeToString.getMin());

        int minDifference;
        int hrDifference;

        if(isToday(bookingDay, bookingMonth, bookingYear)) {
            hrDifference = bookingHour - hour;

            if(min < bookingMin) minDifference = bookingMin - min;
            else {
                minDifference = 60 - (min - bookingMin);
                if(minDifference < 60) hrDifference--;
                else minDifference = 0;
            }

            if(booking.getStatus().equals("Booked") &&
                    booking.getBookingType().getId().equals("BT99") &&
                    (hrDifference < -1 || (hrDifference == -1 && minDifference <= 50)))
                buttonLayout.setVisibility(View.VISIBLE);
        }
    }

    private boolean isToday(int bookingDay, int bookingMonth, int bookingYear) {
        return (bookingDay == calendarDay && bookingMonth == calendarMonth && bookingYear == calendarYear);
    }

    private void viewQRCode() {
        MultiFormatWriter writer = new MultiFormatWriter();

        try {
            BitMatrix matrix = writer.encode(bookingId, BarcodeFormat.QR_CODE,
                    1024, 1024);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);

            qrCodeImage.setImageBitmap(bitmap);
            qrCodeDialog.show();
        } catch (WriterException e) {
            e.printStackTrace();

            Toast.makeText(
                    myContext,
                    "Failed to view QR Code. Please try again later.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void getUserInfo() {
        for(User user : users) {
            List<Booking> bookingList = user.getBookingList();
            for(Booking booking : bookingList) {
                if(booking.getId().equals(bookingId)) {
                    String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                    if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
                    tvUserFullName.setText(fromHtml(fullName));

                    try {
                        Glide.with(myContext).load(user.getProfileImage())
                                .placeholder(R.drawable.image_loading_placeholder)
                                .into(profileImage);
                    }
                    catch (Exception ignored) {}

                    String takeTask = "Take Task";
                    tvDriver.setText(takeTask);

                    tvDriver.setEnabled(true);
                    driverImage.setEnabled(true);

                    tvDriver.setTextColor(colorBlue);
                    driverImage.getDrawable().setTint(colorBlue);

                    booking.setStatus(status);

                    tvPassenger.setText(defaultPassengerText);
                    tvPassenger.setTextColor(colorInitial);

                    switch (status) {
                        case "Pending":
                            tvChat.setVisibility(View.GONE);
                            chatImage.setVisibility(View.GONE);

                            if (driverUserId.equals(user.getId()) || taskList3.size() > 0) {
                                if(taskList3.size() > 0) {
                                    takeTask = "Currently Unavailable";
                                    tvDriver.setText(takeTask);

                                    tvDriver.setVisibility(View.VISIBLE);
                                    driverImage.setVisibility(View.VISIBLE);

                                    tvDriver.setEnabled(false);
                                    driverImage.setEnabled(false);

                                    tvDriver.setTextColor(colorInitial);
                                    driverImage.getDrawable().setTint(colorInitial);
                                }
                                else {
                                    tvDriver.setVisibility(View.GONE);
                                    driverImage.setVisibility(View.GONE);
                                }
                            }
                            else {
                                tvDriver.setVisibility(View.VISIBLE);
                                driverImage.setVisibility(View.VISIBLE);

                                tvDriver.setOnClickListener(view -> takeTask(booking, false));
                                driverImage.setOnClickListener(view -> takeTask(booking, false));
                            }

                            tvPass.setVisibility(View.GONE);
                            passImage.setVisibility(View.GONE);
                            tvStop.setVisibility(View.GONE);
                            stopImage.setVisibility(View.GONE);
                            tvCheck.setVisibility(View.GONE);
                            checkImage.setVisibility(View.GONE);
                            break;
                        case "Booked":
                            tvChat.setVisibility(View.VISIBLE);
                            chatImage.setVisibility(View.VISIBLE);

                            tvChat.setOnClickListener(view -> openChat());
                            chatImage.setOnClickListener(view -> openChat());

                            tvDriver.setVisibility(View.GONE);
                            driverImage.setVisibility(View.GONE);
                            tvPass.setVisibility(View.VISIBLE);
                            passImage.setVisibility(View.VISIBLE);

                            tvPass.setOnClickListener(view -> openReasonDialog(booking));
                            passImage.setOnClickListener(view -> openReasonDialog(booking));

                            tvStop.setVisibility(View.GONE);
                            stopImage.setVisibility(View.GONE);
                            tvCheck.setVisibility(View.VISIBLE);
                            checkImage.setVisibility(View.VISIBLE);

                            tvCheck.setOnClickListener(view -> scanQRCode());
                            checkImage.setOnClickListener(view -> scanQRCode());
                            break;
                        case "Request":
                            tvPassenger.setVisibility(View.VISIBLE);

                            if(driverUserId.equals(taskDriverUserId) || taskList3.size() > 0) {
                                tvPassenger.setText(requestText);
                                tvPassenger.setTextColor(colorGreen);

                                tvChat.setVisibility(View.VISIBLE);
                                chatImage.setVisibility(View.VISIBLE);

                                tvChat.setOnClickListener(view -> openChat());
                                chatImage.setOnClickListener(view -> openChat());

                                if(taskList3.size() > 0) {
                                    takeTask = "Currently Unavailable";
                                    tvDriver.setText(takeTask);

                                    tvDriver.setVisibility(View.VISIBLE);
                                    driverImage.setVisibility(View.VISIBLE);

                                    tvDriver.setEnabled(false);
                                    driverImage.setEnabled(false);

                                    tvDriver.setTextColor(colorInitial);
                                    driverImage.getDrawable().setTint(colorInitial);
                                }
                                else {
                                    tvDriver.setVisibility(View.GONE);
                                    driverImage.setVisibility(View.GONE);
                                }

                                tvDriver.setVisibility(View.GONE);
                                driverImage.setVisibility(View.GONE);
                                tvPass.setVisibility(View.GONE);
                                passImage.setVisibility(View.GONE);
                                tvStop.setVisibility(View.VISIBLE);
                                stopImage.setVisibility(View.VISIBLE);

                                tvStop.setOnClickListener(view -> stopRequest(booking));
                                stopImage.setOnClickListener(view -> stopRequest(booking));

                                tvCheck.setVisibility(View.VISIBLE);
                                checkImage.setVisibility(View.VISIBLE);

                                tvCheck.setOnClickListener(view -> scanQRCode());
                                checkImage.setOnClickListener(view -> scanQRCode());
                            }
                            else {
                                tvChat.setVisibility(View.GONE);
                                chatImage.setVisibility(View.GONE);

                                if(driverUserId.equals(userId)) {
                                    tvDriver.setVisibility(View.GONE);
                                    driverImage.setVisibility(View.GONE);
                                }
                                else {
                                    tvDriver.setVisibility(View.VISIBLE);
                                    driverImage.setVisibility(View.VISIBLE);

                                    tvDriver.setOnClickListener(view -> takeTask(booking, true));
                                    driverImage.setOnClickListener(view -> takeTask(booking, true));
                                }

                                tvPass.setVisibility(View.GONE);
                                passImage.setVisibility(View.GONE);
                                tvStop.setVisibility(View.GONE);
                                stopImage.setVisibility(View.GONE);
                                tvCheck.setVisibility(View.GONE);
                                checkImage.setVisibility(View.GONE);
                            }
                            break;
                        default:
                            tvChat.setVisibility(View.GONE);
                            chatImage.setVisibility(View.GONE);
                            tvDriver.setVisibility(View.GONE);
                            driverImage.setVisibility(View.GONE);
                            tvPass.setVisibility(View.GONE);
                            passImage.setVisibility(View.GONE);
                            tvStop.setVisibility(View.GONE);
                            stopImage.setVisibility(View.GONE);
                            tvCheck.setVisibility(View.GONE);
                            checkImage.setVisibility(View.GONE);
                            break;
                    }

                    break;
                }
            }
        }
    }

    private void stopRequest(Booking booking) {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.child(driverUserId).child("taskList").
                child(booking.getId()).child("status").setValue("Booked")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(
                                myContext,
                                "You stopped your task's request",
                                Toast.LENGTH_LONG
                        ).show();

                        status = "Booked";
                        getUsers();
                    }
                    else {
                        Toast.makeText(
                                myContext,
                                "Failed to stop the task's request",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void passTask(Booking booking) {
        usersRef.child(driverUserId).child("taskList").
                child(booking.getId()).child("status").setValue("Request")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(
                                myContext,
                                "Your task is now on request",
                                Toast.LENGTH_LONG
                        ).show();
                        dialog2.dismiss();

                        status = "Request";
                        getUsers();
                    }
                    else {
                        Toast.makeText(
                                myContext,
                                "Failed to pass the task",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    dialogProgressBar.setVisibility(View.GONE);
                    setDialogScreenEnabled(true);
                });
    }

    @SuppressWarnings("deprecation")
    public void scanQRCode() {
        IntentIntegrator intentIntegrator = new IntentIntegrator((Activity) myContext);
        intentIntegrator.setPrompt("Press volume up key to toggle flash.");
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setCaptureActivity(Capture.class);
        intentIntegrator.initiateScan();
        isScanning = true;

        Toast.makeText(
                myContext,
                "Please scan the QR Code of your passenger's booking Record",
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(isScanning) {
            IntentResult intentResult = IntentIntegrator.parseActivityResult(
                    requestCode, resultCode, data
            );

            if(intentResult.getContents() != null) {
                if(intentResult.getContents().equals(bookingId)) {
                    usersRef.child(userId).child("bookingList").
                            child(bookingId).child("status").setValue("Completed");
                    usersRef.child(userId).child("bookingList").
                            child(bookingId).child("pickUpTime").
                            setValue(new DateTimeToString().getDateAndTime());

                    usersRef.child(driverUserId).child("taskList").
                            child(bookingId).child("status").setValue("Ongoing");
                    usersRef.child(driverUserId).child("taskList").
                            child(bookingId).child("pickUpTime").
                            setValue(new DateTimeToString().getDateAndTime()).
                    addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Toast.makeText(
                                    myContext,
                                    "QR Code successfully scanned. The Service is now initiated.",
                                    Toast.LENGTH_LONG
                            ).show();

                            status = "Ongoing";
                            updateInfo();
                            getUsers();
                        }
                        else {
                            Toast.makeText(
                                    myContext,
                                    "Failed to pass the task",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
                }
                else {
                    Toast.makeText(
                            myContext,
                            "QR Code does not matched",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
            else {
                Toast.makeText(
                        myContext,
                        "There is no QR Code scanned",
                        Toast.LENGTH_LONG
                ).show();
            }

            isScanning = false;
        }
        else {
            if(resultCode == RESULT_OK && requestCode == MAP_SETTINGS_REQUEST) {
                mapFragment.mapSettingsRequestResult("Origin Location");
            }
        }
    }

    private void takeTask(Booking booking, boolean fromRequest) {
        progressBar.setVisibility(View.VISIBLE);
        String status = "Booked";
        booking.setTimestamp(new DateTimeToString().getDateAndTime());
        Booking driverTask = new Booking(booking);
        driverTask.setStatus(status);

        if(fromRequest) {
            driverTask.setPreviousDriverUserId(taskDriverUserId);
            usersRef.child(taskDriverUserId).child("taskList").
                    child(driverTask.getId()).child("status").setValue("Passed");
        }

        DatabaseReference bookingListRef = usersRef.child(userId).
                child("bookingList").child(driverTask.getId());

        DatabaseReference taskListRef = usersRef.child(driverUserId).child("taskList").
                child(booking.getId());
        taskListRef.setValue(driverTask).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                bookingListRef.child("notified").setValue(false);
                bookingListRef.child("read").setValue(false);
                bookingListRef.child("status").setValue(status).
                        addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()) {
                                Toast.makeText(
                                        myContext,
                                        "Successfully taken the task",
                                        Toast.LENGTH_SHORT
                                ).show();

                                progressBar.setVisibility(View.GONE);
                            }
                            else errorTask();
                        });
                progressBar.setVisibility(View.GONE);
            }
            else errorTask();
        });
    }

    private void errorTask() {
        Toast.makeText(
                myContext,
                "Failed to take the task. Please try again.",
                Toast.LENGTH_LONG
        ).show();

        progressBar.setVisibility(View.GONE);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        if(html == null) {
            return new SpannableString("");
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        }
        else {
            return Html.fromHtml(html);
        }
    }

    private void openMapSettings() {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", destinationSpot.getId());
        intent.putExtra("lat", destinationSpot.getLat());
        intent.putExtra("lng", destinationSpot.getLng());
        intent.putExtra("name", destinationSpot.getName());
        intent.putExtra("type", 0);
        intent.putExtra("fromBooking", true);
        ((Activity) myContext).startActivityForResult(intent, MAP_SETTINGS_REQUEST);
    }

    private void initBookingAlertDialog() {
        dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_on_the_spot_booking_alert_layout);

        dialogCloseImage = dialog.findViewById(R.id.dialogCloseImage);
        tvMessage = dialog.findViewById(R.id.tvMessage);
        tvMessage2 = dialog.findViewById(R.id.tvMessage2);
        tvPreferences = dialog.findViewById(R.id.tvPreferences);
        preferencesImage = dialog.findViewById(R.id.preferencesImage);

        dialogCloseImage.setOnClickListener(view -> dialog.dismiss());

        preferencesImage.setOnClickListener(view -> openPreferences());
        tvPreferences.setOnClickListener(view -> openPreferences());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        dialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCanceledOnTouchOutside(false);
    }

    private void openPreferences() {
        Intent intent = new Intent(myContext, PreferenceActivity.class);
        myContext.startActivity(intent);
    }

    private void initQRCodeDialog() {
        qrCodeDialog = new Dialog(myContext);
        qrCodeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        qrCodeDialog.setContentView(R.layout.dialog_qrcode_layout);

        qrCodeDialogCloseImage = qrCodeDialog.findViewById(R.id.dialogCloseImage);
        qrCodeImage = qrCodeDialog.findViewById(R.id.qrCodeImage);

        qrCodeDialogCloseImage.setOnClickListener(view -> qrCodeDialog.dismiss());

        qrCodeDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        qrCodeDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_white_layout));
    }

    private void updateInfo() {
        String img = destinationSpot.getImg();
        try {
            Glide.with(myContext).load(img).placeholder(R.drawable.image_loading_placeholder).
                    override(Target.SIZE_ORIGINAL).into(thumbnail);
        }
        catch (Exception ignored) {}

        String originLocationText = "Latitude: " + originLocation.latitude +
                "\nLongitude: " + originLocation.longitude;

        tvBookingId.setText(bookingId);
        tvOriginLocation2.setText(originLocationText);
        tvDestinationSpot2.setText(destinationSpot.getName());
        tvSchedule.setText(schedule);
        tvTypeName.setText(typeName);
        tvPrice.setText(price);
        extvMessage.setText(message);

        int color = 0;

        buttonLayout.setVisibility(View.GONE);
        buttonLayout2.setVisibility(View.GONE);

        switch (status) {
            case "Pending":
                color = myResources.getColor(R.color.orange);

                if(!inDriverModule) {
                    buttonLayout.setVisibility(View.VISIBLE);
                    if (isShowBookingAlertEnabled) dialog.show();
                }
                break;
            case "Request":
            case "Booked":
                color = myResources.getColor(R.color.green);

                if(isShowBookingAlertEnabled && !inDriverModule) dialog.show();
                break;
            case "Ongoing":
                if(inDriverModule) buttonLayout2.setVisibility(View.VISIBLE);
            case "Completed":
                color = myResources.getColor(R.color.blue);
                break;
            case "Passed":
            case "Cancelled":
            case "Failed":
                color = myResources.getColor(R.color.red);
                break;
        }

        tvBookingId.setBackgroundColor(color);
        tvPrice.setTextColor(color);

        mapFragment = new MapFragment();

        Bundle bundle = new Bundle();
        bundle.putString("id", destinationSpot.getId());
        bundle.putDouble("lat", destinationSpot.getLat());
        bundle.putDouble("lng", destinationSpot.getLng());
        bundle.putString("name", destinationSpot.getName());
        bundle.putInt("type", 0);
        bundle.putBoolean("fromBookingRecord", true);
        mapFragment.setArguments(bundle);

        fragmentManager.beginTransaction().replace(mapLayout.getId(), mapFragment).commit();
        mapFragment.setCurrentLocation(originLocation);

        tvChat.setVisibility(View.GONE);
        chatImage.setVisibility(View.GONE);
        tvRemarks.setVisibility(View.GONE);
        remarksImage.setVisibility(View.GONE);

        if(status.equals("Booked") || status.equals("Request")) {
            tvChat.setVisibility(View.VISIBLE);
            chatImage.setVisibility(View.VISIBLE);

            tvChat.setOnClickListener(view -> openChat());
            chatImage.setOnClickListener(view -> openChat());
        }
        else if(status.equals("Cancelled") || status.equals("Failed")) {
            tvRemarks.setVisibility(View.VISIBLE);
            remarksImage.setVisibility(View.VISIBLE);
        }

        timeInfoLayout.setVisibility(View.GONE);

        if(pickUpTime != null && pickUpTime.length() > 0) {
            timeInfoLayout.setVisibility(View.VISIBLE);
            tvPickUpTime.setText(fromHtml(pickUpTimeText + pickUpTime));
        }

        if(dropOffTime != null && dropOffTime.length() > 0)
            timeInfoLayout.setVisibility(View.VISIBLE);
        else dropOffTime = "Unset";
        tvDropOffTime.setText(fromHtml(dropOffTimeText + dropOffTime));
    }

    private void openMap(String id, LatLng latlng, String locationName, int type) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("lat", latlng.latitude);
        intent.putExtra("lng", latlng.longitude);
        intent.putExtra("name", locationName);
        intent.putExtra("type", type);
        myContext.startActivity(intent);
    }

    private void openOption() {
        moreImage.setEnabled(false);

        optionHandler.removeCallbacks(optionRunnable);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(bookingInfoLayout);

        constraintSet.clear(bookingInfoButtonLayout.getId(), ConstraintSet.TOP);
        constraintSet.connect(bookingInfoButtonLayout.getId(), ConstraintSet.BOTTOM,
                moreImage.getId(), ConstraintSet.BOTTOM);

        setTransition(bookingInfoButtonLayout);
        constraintSet.applyTo(bookingInfoLayout);

        isOptionShown = true;
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_close_24);
        moreImage.getDrawable().setTint(myContext.getResources().getColor(R.color.red));

        optionHandler.postDelayed(optionRunnable, 3000);
    }

    private void closeOption() {
        moreImage.setEnabled(false);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(bookingInfoLayout);

        constraintSet.clear(bookingInfoButtonLayout.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(bookingInfoButtonLayout.getId(), ConstraintSet.TOP,
                bookingInfoLayout.getId(), ConstraintSet.BOTTOM);

        setTransition(bookingInfoButtonLayout);
        constraintSet.applyTo(bookingInfoLayout);

        isOptionShown = false;
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_more_horiz_24);
        moreImage.getDrawable().setTint(myContext.getResources().getColor(R.color.black));
    }

    private void setTransition(ConstraintLayout constraintLayout) {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);
        TransitionManager.beginDelayedTransition(constraintLayout, transition);
    }

    private void getBookingData() {
        progressBar.setVisibility(View.VISIBLE);

        bookingListRef = firebaseDatabase.getReference("users").child(userId)
                .child("bookingList").child(bookingId);
        bookingListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Booking booking = new Booking(snapshot);
                    destinationSpot = booking.getDestinationSpot();
                    destinationSpotLocation =
                            new LatLng(destinationSpot.getLat(), destinationSpot.getLng());

                    originLocation =
                            new LatLng(booking.getOriginLat(), booking.getOriginLng());

                    schedule = booking.getSchedule();

                    String currentStatus = booking.getStatus();
                    if(!inDriverModule || (status != null && !status.equals("Request") && !status.equals("Ongoing")) ||
                            !currentStatus.equals("Booked") && !currentStatus.equals("Completed")) {
                        status = currentStatus;
                    }

                    typeName = booking.getBookingType().getName();
                    price = String.valueOf(booking.getBookingType().getPrice());
                    if(price.split("\\.")[1].length() == 1) price += 0;

                    message = booking.getMessage();

                    pickUpTime = booking.getPickUpTime();
                    dropOffTime = booking.getDropOffTime();

                    int rating = booking.getRating();

                    tvRate.setVisibility(View.GONE);
                    rateImage.setVisibility(View.GONE);
                    if(!inDriverModule && status.equals("Completed") && rating == 0) {
                        tvRate.setVisibility(View.VISIBLE);
                        rateImage.setVisibility(View.VISIBLE);

                        tvRate.setOnClickListener(view -> openRateTheDriverDialog());
                        rateImage.setOnClickListener(view -> openRateTheDriverDialog());
                    }

                    if(!inDriverModule) startTimer(booking);
                    finishLoading();
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isOnScreen = false;
    }

    private void finishLoading() {
        if(isOnScreen) updateInfo();

        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void errorLoading(String error) {
        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void cancelBooking() {
        progressBar.setVisibility(View.VISIBLE);
        String prevStatus = status;
        usersRef.child(userId).child("bookingList").child(bookingId).child("status").setValue("Cancelled")
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()) {
                        if(prevStatus.equals("Booked")) {
                            usersRef.child(driverUserId).child("taskList").child(bookingId).
                                    child("status").setValue("Failed").addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful()) {
                                            Toast.makeText(
                                                    myContext,
                                                    "Successfully cancelled the booking",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            progressBar.setVisibility(View.GONE);
                                        }
                                        else {
                                            usersRef.child(userId).child("bookingList").child(bookingId).
                                                    child("status").setValue(prevStatus);

                                            cancelFailed();
                                        }
                                    });
                        }
                        else {
                            Toast.makeText(
                                    myContext,
                                    "Successfully cancelled the booking",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                    else cancelFailed();
                });
    }

    private void cancelFailed() {
        errorToast = Toast.makeText(myContext,
                "Failed to cancel the booking. Please try again.",
                Toast.LENGTH_LONG);
        errorToast.show();

        cancelButton.setEnabled(true);
        cancelButton.setText(cancelButtonText);
        progressBar.setVisibility(View.GONE);
    }
}