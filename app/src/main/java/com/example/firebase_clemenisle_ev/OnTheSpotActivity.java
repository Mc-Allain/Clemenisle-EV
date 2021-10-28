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
import android.os.CountDownTimer;
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
import com.example.firebase_clemenisle_ev.Classes.Route;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.Fragments.MapFragment;
import com.google.android.gms.maps.model.LatLng;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
            chatImage, driverImage, passImage, checkImage, reloadImage;
    TextView tvUserFullName, tvPassTaskNote, tvDriverFullName, tvBookingId, tvSchedule, tvTypeName, tvPrice, tvOriginLocation2, tvDestinationSpot2,
            tvLocate, tvLocateDestination, tvViewQR, tvChat, tvDriver, tvPass, tvCheck, tvLog;
    ExpandableTextView extvMessage;
    ConstraintLayout buttonLayout, bookingInfoLayout, bookingInfoButtonLayout, userInfoLayout, driverInfoLayout;
    Button cancelButton;
    FrameLayout mapLayout;
    ProgressBar progressBar;

    TextView tvCurrentLocation, tvLocateOnTheSpot, tvMapSettings;
    ImageView currentLocationImage, locateOnTheSpotImage, mapSettingsImage;

    Context myContext;
    Resources myResources;

    String userId, driverUserId, taskDriverUserId;

    boolean isLoggedIn = false, inDriverMode = false;

    DatabaseReference bookingListRef;

    String bookingId, schedule, typeName, price, status, message;
    LatLng originLocation, destinationSpotLocation;

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
        tvPassTaskNote = findViewById(R.id.tvPassTaskNote);
        profileImage = findViewById(R.id.profileImage);

        driverInfoLayout = findViewById(R.id.driverInfoLayout);
        tvDriverFullName = findViewById(R.id.tvDriverFullName);
        driverProfileImage = findViewById(R.id.driverProfileImage);

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
        tvCheck = findViewById(R.id.tvCheck);
        checkImage = findViewById(R.id.checkImage);

        reloadImage = findViewById(R.id.reloadImage);
        mapLayout = findViewById(R.id.mapLayout);
        progressBar = findViewById(R.id.progressBar);

        tvCurrentLocation = findViewById(R.id.tvCurrentLocation);
        currentLocationImage = findViewById(R.id.currentLocationImage);
        tvLocateOnTheSpot = findViewById(R.id.tvLocateOnTheSpot);
        locateOnTheSpotImage = findViewById(R.id.locateOnTheSpotImage);
        tvMapSettings = findViewById(R.id.tvMapSettings);
        mapSettingsImage = findViewById(R.id.mapSettingsImage);

        myContext = OnTheSpotActivity.this;
        myResources = getResources();

        optionRunnable = () -> closeOption();

        initSharedPreferences();
        initBookingAlertDialog();
        initQRCodeDialog();

        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");
        inDriverMode = intent.getBooleanExtra("inDriverMode", false);
        if(inDriverMode)  status = intent.getStringExtra("status");

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
                "Your Location", 2));
        locateImage.setOnClickListener(view -> openMap("O-00", originLocation,
                "Your Location", 2));

        tvLocateDestination.setOnClickListener(view -> openMap(destinationSpot.getId(),
                destinationSpotLocation, destinationSpot.getName(), 0));
        locateDestinationImage.setOnClickListener(view -> openMap(destinationSpot.getId(),
                destinationSpotLocation, destinationSpot.getName(), 0));

        currentLocationImage.setOnClickListener(view ->
                mapFragment.getUserCurrentLocation(originLocation, "Your Location"));
        tvCurrentLocation.setOnClickListener(view ->
                mapFragment.getUserCurrentLocation(originLocation, "Your Location"));

        locateOnTheSpotImage.setOnClickListener(view -> mapFragment.locateOnTheSpot());
        tvLocateOnTheSpot.setOnClickListener(view -> mapFragment.locateOnTheSpot());

        mapSettingsImage.setOnClickListener(view -> openMapSettings());
        tvMapSettings.setOnClickListener(view -> openMapSettings());

        getUsers();
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

                if(inDriverMode) {
                    driverInfoLayout.setVisibility(View.GONE);
                    userInfoLayout.setVisibility(View.VISIBLE);

                    getUserInfo();
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
        intent.putExtra("bookingId", bookingId);
        intent.putExtra("schedule", schedule);
        intent.putExtra("inDriverMode", inDriverMode);
        myContext.startActivity(intent);
    }

    private void getDriverInfo() {
        for(User user : users) {
            List<Booking> taskList = user.getTaskList();

            for(Booking booking : taskList) {
                if(booking.getId().equals(bookingId)) {
                    String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                    if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
                    tvDriverFullName.setText(fromHtml(fullName));

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

    private void getDriverUserId() {
        for(User user : users) {
            List<Booking> taskList = user.getTaskList();
            for(Booking booking : taskList) {
                if(booking.getId().equals(bookingId)) {
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

                    switch (status) {
                        case "Processing":
                            tvChat.setVisibility(View.GONE);
                            chatImage.setVisibility(View.GONE);

                            if (driverUserId.equals(user.getId())) {
                                tvDriver.setVisibility(View.GONE);
                                driverImage.setVisibility(View.GONE);
                            }
                            else {
                                tvDriver.setVisibility(View.VISIBLE);
                                driverImage.setVisibility(View.VISIBLE);

                                tvDriver.setOnClickListener(view -> takeTask(booking, false));
                                driverImage.setOnClickListener(view -> takeTask(booking, false));
                            }

                            tvPass.setVisibility(View.GONE);
                            passImage.setVisibility(View.GONE);
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

                            tvCheck.setVisibility(View.VISIBLE);
                            checkImage.setVisibility(View.VISIBLE);

                            tvCheck.setOnClickListener(view -> scanQRCode());
                            checkImage.setOnClickListener(view -> scanQRCode());
                            break;
                        case "Request":
                            getDriverUserId();

                            tvPassTaskNote.setVisibility(View.GONE);

                            if(driverUserId.equals(taskDriverUserId)) {
                                tvPassTaskNote.setVisibility(View.VISIBLE);

                                tvChat.setVisibility(View.VISIBLE);
                                chatImage.setVisibility(View.VISIBLE);

                                tvChat.setOnClickListener(view -> openChat());
                                chatImage.setOnClickListener(view -> openChat());

                                tvDriver.setVisibility(View.GONE);
                                driverImage.setVisibility(View.GONE);
                                tvPass.setVisibility(View.GONE);
                                passImage.setVisibility(View.GONE);
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
                            tvCheck.setVisibility(View.GONE);
                            checkImage.setVisibility(View.GONE);
                            break;
                    }

                    break;
                }
            }
        }
    }

    private void passTask(Booking booking) {
        usersRef.child(driverUserId).child("taskList").
                child(booking.getId()).child("status").setValue("Request")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(
                                myContext,
                                "Your Task is now on request",
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

            isScanning = false;
        }
        else {
            if(resultCode == RESULT_OK && requestCode == MAP_SETTINGS_REQUEST) {
                mapFragment.mapSettingsRequestResult();
            }
        }
    }

    private void takeTask(Booking booking, boolean fromRequest) {
        String status = "Booked";
        List<Route> bookingRouteList = booking.getRouteList();
        booking.setTimestamp(new DateTimeToString().getDateAndTime());
        booking.setStatus(status);
        Booking driverTask = new Booking(booking);

        if(fromRequest) {
            usersRef.child(taskDriverUserId).child("taskList").
                    child(driverTask.getId()).removeValue();
        }

        DatabaseReference bookingListRef = usersRef.child(userId).
                child("bookingList").child(driverTask.getId());

        DatabaseReference taskListRef = usersRef.child(driverUserId).child("taskList").
                child(booking.getId());
        taskListRef.setValue(driverTask).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                bookingListRef.child("notify").setValue(true);
                bookingListRef.child("notificationTimestamp").
                        setValue(new DateTimeToString().getDateAndTime());

                if(fromRequest) {
                    bookingListRef.child("chats").removeValue().
                            addOnCompleteListener(task2 -> {
                                if(task2.isSuccessful())
                                    addBookingRoute(bookingRouteList, taskListRef);
                                else errorTask();
                            });
                }
                else {
                    bookingListRef.child("status").setValue(status).
                            addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful())
                                    addBookingRoute(bookingRouteList, taskListRef);
                                else errorTask();
                            });
                }
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
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
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
        qrCodeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
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
        Drawable backgroundDrawable = myResources.getDrawable(R.color.blue);

        buttonLayout.setVisibility(View.GONE);

        switch (status) {
            case "Processing":
                color = myResources.getColor(R.color.orange);
                backgroundDrawable = myResources.getDrawable(R.color.orange);

                if(!inDriverMode) {
                    buttonLayout.setVisibility(View.VISIBLE);
                    if (isShowBookingAlertEnabled) dialog.show();
                }

                break;
            case "Request":
            case "Booked":
                color = myResources.getColor(R.color.green);
                backgroundDrawable = myResources.getDrawable(R.color.green);

                if(isShowBookingAlertEnabled && !inDriverMode) dialog.show();

                break;
            case "Completed":
                color = myResources.getColor(R.color.blue);
                backgroundDrawable = myResources.getDrawable(R.color.blue);
                break;
            case "Cancelled":
            case "Failed":
                color = myResources.getColor(R.color.red);
                backgroundDrawable = myResources.getDrawable(R.color.red);
                break;
        }

        tvBookingId.setBackground(backgroundDrawable);
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
    }

    private void openMap(String id, LatLng latlng, String name, int type) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("lat", latlng.latitude);
        intent.putExtra("lng", latlng.longitude);
        intent.putExtra("name", name);
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
                    if(!inDriverMode) status = booking.getStatus();
                    message = booking.getMessage();

                    typeName = booking.getBookingType().getName();
                    price = String.valueOf(booking.getBookingType().getPrice());
                    if(price.split("\\.")[1].length() == 1) price += 0;

                    startTimer(booking);
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
        String prevStatus = status;
        usersRef.child(userId).child("bookingList").child(bookingId).child("status").setValue("Cancelled")
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if(task.isSuccessful()) {
                        if(prevStatus.equals("Booked")) {
                            usersRef.child(driverUserId).child("taskList").child(bookingId).
                                    child("status").setValue("Failed").addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful()) onBackPressed();
                                        else {
                                            usersRef.child(userId).child("bookingList").child(bookingId).
                                                    child("status").setValue(prevStatus);

                                            errorToast = Toast.makeText(myContext,
                                                    "Failed to cancel the booking. Please try again.",
                                                    Toast.LENGTH_LONG);
                                            errorToast.show();

                                            cancelButton.setEnabled(true);
                                            cancelButton.setText(cancelButtonText);
                                        }
                                    });
                        }
                        else onBackPressed();
                    }
                    else {
                        errorToast = Toast.makeText(myContext,
                                "Failed to cancel the booking. Please try again.",
                                Toast.LENGTH_LONG);
                        errorToast.show();

                        cancelButton.setEnabled(true);
                        cancelButton.setText(cancelButtonText);
                    }
                });
    }
}