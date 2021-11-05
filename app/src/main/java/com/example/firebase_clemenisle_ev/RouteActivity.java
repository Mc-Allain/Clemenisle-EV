package com.example.firebase_clemenisle_ev;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Adapters.RouteAdapter;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.Capture;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.Route;
import com.example.firebase_clemenisle_ev.Classes.Station;
import com.example.firebase_clemenisle_ev.Classes.User;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RouteActivity extends AppCompatActivity implements
        RouteAdapter.OnVisitClickListener {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ImageView profileImage, driverProfileImage, thumbnail, moreImage, locateImage, locateEndImage, viewQRImage,
            chatImage, driverImage, passImage, stopImage, checkImage, reloadImage, paidImage;
    TextView tvUserFullName, tvPassenger, tvDriverFullName, tvPlateNumber, tvPickUpTime, tvDropOffTime,
            tvBookingId, tvSchedule, tvTypeName, tvPrice, tvStartStation2, tvEndStation2, tvLocate,
            tvLocateEnd, tvViewQR, tvChat, tvDriver, tvPass, tvStop, tvCheck, tvLog;
    ExpandableTextView extvMessage;
    RecyclerView routeView;
    ConstraintLayout buttonLayout, buttonLayout2, bookingInfoLayout, bookingInfoButtonLayout,
            userInfoLayout, driverInfoLayout, timeInfoLayout;
    Button cancelButton, onlinePaymentButton, dropOffButton;
    ProgressBar progressBar;

    int columnCount = 2;
    List<Route> routeList = new ArrayList<>();
    RouteAdapter routeAdapter;

    List<Booking> bookingList = new ArrayList<>();

    Context myContext;
    Resources myResources;

    int colorGreen, colorInitial, colorBlue;

    String userId, driverUserId, taskDriverUserId;

    boolean isLoggedIn = false, inDriverModule = false, isScanning = false;

    DatabaseReference bookingListRef;

    String bookingId, schedule, typeName, price, startStationName, endStationName, status,
            message, previousDriverUserId;
    boolean isLatest, isPaid;
    String pickUpTime, dropOffTime;

    Station startStation, endStation;

    boolean isOnScreen = false, isOptionShown = false;

    Handler optionHandler = new Handler();
    Runnable optionRunnable;

    String defaultLogText = "No Record";

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

    List<User> users = new ArrayList<>();

    String defaultPassengerText = "Passenger", requestText = "Your Task on Request";

    List<Booking> taskList3 = new ArrayList<>();

    String pickUpTimeText = "<b>Pick-up Time</b>: ", dropOffTimeText = "<b>Drop-off Time</b>: ";

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
        setContentView(R.layout.activity_route);

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
        tvStartStation2 = findViewById(R.id.tvStartStation2);
        tvEndStation2 = findViewById(R.id.tvEndStation2);
        tvLocate = findViewById(R.id.tvLocate);
        tvLocateEnd = findViewById(R.id.tvLocateEnd);
        tvLog = findViewById(R.id.tvLog);
        extvMessage = findViewById(R.id.extvMessage);

        routeView = findViewById(R.id.routeView);
        buttonLayout = findViewById(R.id.buttonLayout);
        bookingInfoLayout = findViewById(R.id.bookingInfoLayout);
        bookingInfoButtonLayout = findViewById(R.id.bookingInfoButtonLayout);
        cancelButton = findViewById(R.id.cancelButton);
        onlinePaymentButton = findViewById(R.id.onlinePaymentButton);
        moreImage = findViewById(R.id.moreImage);
        locateImage = findViewById(R.id.locateImage);
        locateEndImage = findViewById(R.id.locateEndImage);

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

        reloadImage = findViewById(R.id.reloadImage);
        paidImage = findViewById(R.id.paidImage);
        progressBar = findViewById(R.id.progressBar);

        buttonLayout2 = findViewById(R.id.buttonLayout2);
        dropOffButton = findViewById(R.id.dropOffButton);

        myContext = RouteActivity.this;
        myResources = getResources();

        colorGreen = myResources.getColor(R.color.green);
        colorInitial = myResources.getColor(R.color.initial);
        colorBlue = myResources.getColor(R.color.blue);

        optionRunnable = () -> closeOption();

        initSharedPreferences();
        initBookingAlertDialog();
        initQRCodeDialog();

        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");
        inDriverModule = intent.getBooleanExtra("inDriverModule", false);
        isLatest = intent.getBooleanExtra("isLatest", false);
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

        bookingListRef = usersRef.child(userId).child("bookingList").child(bookingId);

        GridLayoutManager gridLayoutManager =
                new GridLayoutManager(myContext, columnCount, GridLayoutManager.VERTICAL, false);
        routeView.setLayoutManager(gridLayoutManager);
        routeAdapter = new RouteAdapter(myContext, routeList, columnCount, bookingId, status,
                isLatest, isLoggedIn, inDriverModule);
        routeView.setAdapter(routeAdapter);
        routeAdapter.setOnVisitClickListener(this);

        getRoute();

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

                    routeView.setEnabled(false);
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

        tvLocate.setOnClickListener(view -> openMap(startStation));
        locateImage.setOnClickListener(view -> openMap(startStation));

        tvLocateEnd.setOnClickListener(view -> openMap(endStation));
        locateEndImage.setOnClickListener(view -> openMap(endStation));

        onlinePaymentButton.setOnClickListener(view -> openOnlinePayment());

        paidImage.setOnLongClickListener(view -> {
            Toast.makeText(
                    myContext,
                    "Paid",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        });

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

    private void openOnlinePayment() {
        Intent intent = new Intent(myContext, OnlinePaymentActivity.class);
        intent.putExtra("bookingId", bookingId);
        myContext.startActivity(intent);
    }

    private void completeTask() {
        usersRef.child(userId).child("bookingList").
                child(bookingId).child("dropOffTime").
                setValue(new DateTimeToString().getDateAndTime());

        usersRef.child(driverUserId).child("taskList").
                child(bookingId).child("status").setValue("Completed");
        usersRef.child(driverUserId).child("taskList").
                child(bookingId).child("dropOffTime").
                setValue(new DateTimeToString().getDateAndTime());

        Toast.makeText(
                myContext,
                "The Task is now Completed",
                Toast.LENGTH_SHORT
        ).show();

        status = "Completed";
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

                            getDriverUserId();

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

                    extvMessage.setVisibility(View.VISIBLE);

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

                            tvPass.setOnClickListener(view -> passTask(booking));
                            passImage.setOnClickListener(view -> passTask(booking));

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
                                tvPassenger.setText(defaultPassengerText);
                                tvPassenger.setTextColor(colorInitial);

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

                            extvMessage.setVisibility(View.GONE);
                            break;
                    }

                    break;
                }
            }
        }
    }

    private void stopRequest(Booking booking) {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.child(userId).child("taskList").
                child(booking.getId()).child("status").setValue("Booked")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(
                                myContext,
                                "You stopped your task's request",
                                Toast.LENGTH_LONG
                        ).show();
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
        progressBar.setVisibility(View.VISIBLE);
        usersRef.child(driverUserId).child("taskList").
                child(booking.getId()).child("status").setValue("Request")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(
                                myContext,
                                "Your task is now on request",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    else {
                        Toast.makeText(
                                myContext,
                                "Failed to pass the task",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });
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

    private void initBookingAlertDialog() {
        dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_booking_alert_layout);

        dialogCloseImage = dialog.findViewById(R.id.dialogCloseImage);
        tvMessage = dialog.findViewById(R.id.tvMessage);
        tvMessage2 = dialog.findViewById(R.id.tvMessage2);
        tvPreferences = dialog.findViewById(R.id.tvPreferences);
        preferencesImage = dialog.findViewById(R.id.preferencesImage);

        preferencesImage.setOnClickListener(view -> openPreferences());
        tvPreferences.setOnClickListener(view -> openPreferences());

        dialogCloseImage.setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCanceledOnTouchOutside(false);
    }

    private void openPreferences() {
        Intent intent = new Intent(myContext, PreferenceActivity.class);
        myContext.startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    public void scanQRCode() {
        IntentIntegrator intentIntegrator = new IntentIntegrator((Activity) myContext);
        intentIntegrator.setPrompt("Press volume up key to toggle flash.");
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setCaptureActivity(Capture.class);
        intentIntegrator.initiateScan();

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
                        setValue(new DateTimeToString().getDateAndTime());

                Toast.makeText(
                        myContext,
                        "QR Code successfully scanned. The Service is now initiated.",
                        Toast.LENGTH_LONG
                ).show();

                status = "Ongoing";
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
        qrCodeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    }

    private void updateInfo() {
        try {
            Glide.with(myContext).load(routeList.get(0).getImg())
                    .placeholder(R.drawable.image_loading_placeholder).into(thumbnail);
        }
        catch (Exception ignored) {}

        tvBookingId.setText(bookingId);
        tvStartStation2.setText(startStationName);
        tvEndStation2.setText(endStationName);
        tvSchedule.setText(schedule);
        tvTypeName.setText(typeName);
        tvPrice.setText(price);
        extvMessage.setText(message);

        int color = 0;

        buttonLayout.setVisibility(View.VISIBLE);
        buttonLayout2.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        if(inDriverModule) buttonLayout.setVisibility(View.GONE);

        switch (status) {
            case "Pending":
                color = myResources.getColor(R.color.orange);

                cancelButton.setVisibility(View.VISIBLE);
                if(!inDriverModule && isShowBookingAlertEnabled)dialog.show();
                break;
            case "Request":
            case "Booked":
                color = myResources.getColor(R.color.green);

                if(!inDriverModule && isShowBookingAlertEnabled)dialog.show();
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

        if(isPaid) paidImage.setVisibility(View.VISIBLE);
        else paidImage.setVisibility(View.GONE);

        paidImage.getDrawable().setTint(color);

        if(status.equals("Booked")) {
            tvChat.setVisibility(View.VISIBLE);
            chatImage.setVisibility(View.VISIBLE);

            tvChat.setOnClickListener(view -> openChat());
            chatImage.setOnClickListener(view -> openChat());
        }
        else if(!status.equals("Request")) {
            tvChat.setVisibility(View.GONE);
            chatImage.setVisibility(View.GONE);
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

    private void openMap(Station station) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", station.getId());
        intent.putExtra("lat", station.getLat());
        intent.putExtra("lng", station.getLng());
        intent.putExtra("name", station.getName());
        intent.putExtra("type", 1);
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

    private void setLatest() {
        isLatest = bookingList.get(0).getId().equals(bookingId) &&
                status.equals("Completed");
        routeAdapter.setLatest(isLatest);
    }

    private void getCompletedBookingList() {
        Query bookingQuery = usersRef.child(userId).child("bookingList").
                orderByChild("status").equalTo("Completed");

        bookingQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Booking booking = new Booking(dataSnapshot);
                        bookingList.add(booking);
                    }
                }
                Collections.reverse(bookingList);

                if(bookingList.size() > 0) setLatest();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void getRoute() {
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        bookingListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                routeList.clear();
                if(snapshot.exists()) {
                    Booking booking = new Booking(snapshot);
                    startStation = booking.getStartStation();
                    endStation = booking.getEndStation();

                    startStationName = startStation.getName();
                    endStationName = endStation.getName();

                    schedule = booking.getSchedule();

                    String currentStatus = booking.getStatus();
                    if(!inDriverModule || (status != null && !status.equals("Request") && !status.equals("Ongoing")) ||
                            !currentStatus.equals("Booked") && !currentStatus.equals("Completed")) {
                        status = currentStatus;
                    }

                    typeName = booking.getBookingType().getName();
                    price = String.valueOf(booking.getBookingType().getPrice());
                    if(price.split("\\.")[1].length() == 1) price += 0;

                    for(Route route : booking.getRouteList())
                        if(!route.isDeactivated()) routeList.add(route);

                    message = booking.getMessage();
                    isPaid = booking.isPaid();

                    pickUpTime = booking.getPickUpTime();
                    dropOffTime = booking.getDropOffTime();
                }
                if(routeList.size() > 0) finishLoading();
                else errorLoading(defaultLogText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
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
        getCompletedBookingList();
        routeAdapter.setStatus(status);

        if(isOnScreen) updateInfo();

        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        routeView.setVisibility(View.VISIBLE);
    }

    private void errorLoading(String error) {
        routeAdapter.notifyDataSetChanged();

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        routeView.setVisibility(View.INVISIBLE);
    }

    private void cancelBooking() {
        progressBar.setVisibility(View.VISIBLE);
        bookingListRef.child("status").setValue("Cancelled")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(
                                myContext,
                                "Successfully cancelled the booking",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                    else {
                        errorToast = Toast.makeText(myContext,
                                "Failed to cancel the booking. Please try again.",
                                Toast.LENGTH_LONG);
                        errorToast.show();

                        routeView.setEnabled(true);
                        cancelButton.setEnabled(true);
                        cancelButton.setText(cancelButtonText);
                    }
                    progressBar.setVisibility(View.GONE);
                });
    }

    @Override
    public void setProgressBarToVisible() {
        progressBar.setVisibility(View.VISIBLE);
    }
}