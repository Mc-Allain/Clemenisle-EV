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
import android.graphics.drawable.Drawable;
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
            driverImage, passImage, checkImage, reloadImage, paidImage;
    TextView tvUserFullName, tvDriverFullName, tvBookingId, tvSchedule, tvTypeName, tvPrice, tvStartStation2, tvEndStation2,
            tvLocate, tvLocateEnd, tvViewQR, tvDriver, tvPass, tvCheck, tvLog;
    ExpandableTextView extvMessage;
    RecyclerView routeView;
    ConstraintLayout buttonLayout, bookingInfoLayout, bookingInfoButtonLayout, userInfoLayout, driverInfoLayout;
    Button cancelButton, onlinePaymentButton;
    ProgressBar progressBar;

    int columnCount = 2;
    List<Route> routeList = new ArrayList<>();
    RouteAdapter routeAdapter;

    Context myContext;
    Resources myResources;

    String userId, driverUserId;

    boolean isLoggedIn = false, inDriverMode = false, isScanning;

    DatabaseReference bookingListRef;

    String bookingId, schedule, typeName, price, startStationName, endStationName, status, message;
    boolean isLatest, isPaid;
    String prevStatus;

    Station startStation, endStation;

    boolean isOnScreen = false, isOptionShown = false;

    Handler optionHandler = new Handler();
    Runnable optionRunnable;

    String defaultLogText = "No Record";

    long lastPressSec = 0;
    int pressCount = 0;
    Toast cancelToast, errorToast;

    String cancelButtonText = "Cancel Booking", cancellingButtonText = "Cancelling…";

    Dialog dialog;
    ImageView dialogCloseImage, preferencesImage;
    TextView tvMessage, tvMessage2, tvPreferences;

    boolean isShowBookingAlertEnabled;

    Dialog qrCodeDialog;
    ImageView qrCodeDialogCloseImage, qrCodeImage;

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
        profileImage = findViewById(R.id.profileImage);

        driverInfoLayout = findViewById(R.id.driverInfoLayout);
        tvDriverFullName = findViewById(R.id.tvDriverFullName);
        driverProfileImage = findViewById(R.id.driverProfileImage);

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
        tvDriver = findViewById(R.id.tvDriver);
        driverImage = findViewById(R.id.driverImage);
        tvPass = findViewById(R.id.tvPass);
        passImage = findViewById(R.id.passImage);
        tvCheck = findViewById(R.id.tvCheck);
        checkImage = findViewById(R.id.checkImage);

        reloadImage = findViewById(R.id.reloadImage);
        paidImage = findViewById(R.id.paidImage);
        progressBar = findViewById(R.id.progressBar);

        myContext = RouteActivity.this;
        myResources = getResources();

        optionRunnable = () -> closeOption();

        initSharedPreferences();
        initBookingAlertDialog();
        initQRCodeDialog();

        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");
        inDriverMode = intent.getBooleanExtra("inDriverMode", false);
        isLatest = intent.getBooleanExtra("isLatest", false);
        if(!inDriverMode) prevStatus = intent.getStringExtra("status");

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
                if(inDriverMode) {
                    driverUserId = firebaseUser.getUid();
                    userId = intent.getStringExtra("userId");
                    isScanning = intent.getBooleanExtra("isScanning", false);

                    if(isScanning) scanQRCode();
                }
                else userId = firebaseUser.getUid();
            }
        }

        GridLayoutManager gridLayoutManager =
                new GridLayoutManager(myContext, columnCount, GridLayoutManager.VERTICAL, false);
        routeView.setLayoutManager(gridLayoutManager);
        routeAdapter = new RouteAdapter(myContext, routeList, columnCount, bookingId, status,
                isLatest, isLoggedIn, inDriverMode);
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

        onlinePaymentButton.setOnClickListener(view -> Toast.makeText(
                myContext,
                "Not yet implemented",
                Toast.LENGTH_SHORT
        ).show());

        paidImage.setOnLongClickListener(view -> {
            Toast.makeText(
                    myContext,
                    "Paid",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        });

        if(inDriverMode) {
            driverInfoLayout.setVisibility(View.GONE);
            userInfoLayout.setVisibility(View.VISIBLE);

            getUserInfo(bookingId);
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

            getDriverInfo(bookingId);
        }
    }

    private void getDriverInfo(String bookingId) {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User thisUser = new User(dataSnapshot);
                        List<Booking> taskList = thisUser.getTaskList();

                        for(Booking booking : taskList) {
                            if(booking.getId().equals(bookingId)) {
                                String fullName = "<b>" + thisUser.getLastName() + "</b>, " + thisUser.getFirstName();
                                if(thisUser.getMiddleName().length() > 0) fullName += " " + thisUser.getMiddleName();
                                tvDriverFullName.setText(fromHtml(fullName));

                                try {
                                    Glide.with(myContext).load(thisUser.getProfileImage())
                                            .placeholder(R.drawable.image_loading_placeholder)
                                            .into(driverProfileImage);
                                }
                                catch (Exception ignored) {}

                                driverInfoLayout.setVisibility(View.VISIBLE);

                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
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

    private void getUserInfo(String bookingId) {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User thisUser = new User(dataSnapshot);
                        List<Booking> bookingList = thisUser.getBookingList();

                        for(Booking booking : bookingList) {
                            if(booking.getId().equals(bookingId)) {
                                String fullName = "<b>" + thisUser.getLastName() + "</b>, " + thisUser.getFirstName();
                                if(thisUser.getMiddleName().length() > 0) fullName += " " + thisUser.getMiddleName();
                                tvUserFullName.setText(fromHtml(fullName));

                                try {
                                    Glide.with(myContext).load(thisUser.getProfileImage())
                                            .placeholder(R.drawable.image_loading_placeholder)
                                            .into(profileImage);
                                }
                                catch (Exception ignored) {}

                                if(driverUserId.equals(thisUser.getId())) {
                                    tvDriver.setVisibility(View.GONE);
                                    driverImage.setVisibility(View.GONE);
                                }
                                else if(status.equals("Processing")) {
                                    tvDriver.setVisibility(View.VISIBLE);
                                    driverImage.setVisibility(View.VISIBLE);
                                    tvPass.setVisibility(View.GONE);
                                    passImage.setVisibility(View.GONE);
                                    tvCheck.setVisibility(View.GONE);
                                    checkImage.setVisibility(View.GONE);

                                    tvDriver.setOnClickListener(view -> takeTask(booking));
                                    driverImage.setOnClickListener(view -> takeTask(booking));
                                }
                                else if(status.equals("Booked")) {
                                    tvDriver.setVisibility(View.GONE);
                                    driverImage.setVisibility(View.GONE);
                                    tvPass.setVisibility(View.VISIBLE);
                                    passImage.setVisibility(View.VISIBLE);
                                    tvCheck.setVisibility(View.VISIBLE);
                                    checkImage.setVisibility(View.VISIBLE);

                                    tvCheck.setOnClickListener(view -> scanQRCode());
                                    checkImage.setOnClickListener(view -> scanQRCode());
                                }
                                else {
                                    tvDriver.setVisibility(View.GONE);
                                    driverImage.setVisibility(View.GONE);
                                    tvPass.setVisibility(View.GONE);
                                    passImage.setVisibility(View.GONE);
                                    tvCheck.setVisibility(View.GONE);
                                    checkImage.setVisibility(View.GONE);
                                }

                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void takeTask(Booking booking) {
        String status = "Booked";
        List<Route> bookingRouteList = booking.getRouteList();
        booking.setStatus(status);
        Booking driverTask = new Booking(booking);

        DatabaseReference taskListRef = usersRef.child(driverUserId).child("taskList").
                child(booking.getId());
        taskListRef.setValue(driverTask).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                usersRef.child(userId).child("bookingList").
                        child(driverTask.getId()).child("status").setValue(status).
                        addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful())
                                addBookingRoute(bookingRouteList, taskListRef);
                            else {
                                Toast.makeText(
                                        myContext,
                                        "Failed to take the task. Please try again.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
            }
            else {
                Toast.makeText(
                        myContext,
                        "Failed to take the task. Please try again.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void addBookingRoute(List<Route> bookingRouteList,
                                 DatabaseReference taskListRef) {
        int index = 1;
        for(Route route : bookingRouteList) {
            boolean isLastItem;
            isLastItem = index == bookingRouteList.size();

            DatabaseReference routeSpotsRef =
                    taskListRef.child("routeSpots").child(route.getRouteId());
            routeSpotsRef.setValue(route).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if(isLastItem) {
                        Toast.makeText(
                                myContext,
                                "Successfully taken the task",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
            });
            index++;
        }
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
                usersRef.child(driverUserId).child("taskList").
                        child(bookingId).child("status").setValue("Completed");
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
        Drawable backgroundDrawable = myResources.getDrawable(R.color.blue);

        buttonLayout.setVisibility(View.GONE);

        switch (status) {
            case "Processing":
                color = myResources.getColor(R.color.orange);
                backgroundDrawable = myResources.getDrawable(R.color.orange);

                if(!inDriverMode) {
                    buttonLayout.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.VISIBLE);
                    if(isShowBookingAlertEnabled) dialog.show();
                }

                break;
            case "Booked":
                color = myResources.getColor(R.color.green);
                backgroundDrawable = myResources.getDrawable(R.color.green);

                if(!inDriverMode) {
                    buttonLayout.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.GONE);
                    if(isShowBookingAlertEnabled) dialog.show();
                }
                break;
            case "Completed":
                color = myResources.getColor(R.color.blue);
                backgroundDrawable = myResources.getDrawable(R.color.blue);

                if(!inDriverMode) {
                    if(!prevStatus.equals(status) &&
                            prevStatus.equals("Booked")) {
                        isLatest = true;
                        routeAdapter.setLatest(true);
                    }
                }
                break;
            case "Cancelled":
            case "Failed":
                color = myResources.getColor(R.color.red);
                backgroundDrawable = myResources.getDrawable(R.color.red);
                break;
        }

        if(!inDriverMode) {
            prevStatus = status;

            if(!status.equals("Completed")) {
                isLatest = false;
                routeAdapter.setLatest(false);
            }
        }

        tvBookingId.setBackground(backgroundDrawable);
        tvPrice.setTextColor(color);

        if(isPaid) paidImage.setVisibility(View.VISIBLE);
        else paidImage.setVisibility(View.GONE);

        paidImage.getDrawable().setTint(color);
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

    private void getRoute() {
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        bookingListRef = firebaseDatabase.getReference("users").child(userId)
                .child("bookingList").child(bookingId);
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
                    status = booking.getStatus();
                    message = booking.getMessage();

                    typeName = booking.getBookingType().getName();
                    price = String.valueOf(booking.getBookingType().getPrice());
                    if(price.split("\\.")[1].length() == 1) price += 0;

                    for(Route route : booking.getRouteList()) {
                        if(!route.isDeactivated()) {
                            routeList.add(route);
                        }
                    }

                    isPaid = booking.isPaid();
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
        usersRef.child(userId).child("bookingList").child(bookingId).child("status").setValue("Cancelled")
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if(task.isSuccessful()) {
                        onBackPressed();
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
                });
    }

    @Override
    public void setProgressBarToVisible() {
        progressBar.setVisibility(View.VISIBLE);
    }
}