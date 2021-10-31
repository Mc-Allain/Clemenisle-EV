package com.example.firebase_clemenisle_ev;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Classes.AppMetaData;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.Chat;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class DriverActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout headerLayout;

    BottomNavigationView driverNav;
    NavController driverNavCtrlr;
    NavHostFragment navHostFragment;

    Context myContext;
    Resources myResources;

    List<Booking> pendingList = new ArrayList<>();
    List<Booking> driverTaskList = new ArrayList<>(), taskList = new ArrayList<>();

    DateTimeToString dateTimeToString;

    Calendar calendar = Calendar.getInstance();
    int calendarYear, calendarMonth, calendarDay;

    CountDownTimer statusTimer;

    long backPressedTime;
    Toast backToast;

    String userId;
    boolean isLoggedIn = false;

    List<User> users = new ArrayList<>();

    DatabaseReference metaDataRef;

    AppMetaData appMetaData;

    List<String> statusPromptArray = Arrays.asList("Under Development", "Under Maintenance");

    boolean isAppStatusActivityShown = false, isAlertDialogShown = false;

    Dialog dialog;
    ImageView dialogCloseImage, preferencesImage;
    Button updateAppButton;

    Dialog appVersionInfoDialog;
    ImageView appVersionInfoDialogCloseImage;
    TextView tvAppVersionInfoDialogTitle, tvAppVersion, tvPreferences;
    ExpandableTextView extvNewlyAddedFeatures;

    boolean isShowAppVersionInfoEnabled;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        isShowAppVersionInfoEnabled = sharedPreferences.getBoolean("isShowAppVersionInfoEnabled", true);
    }

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("isRemembered", false);
        editor.apply();
    }

    private void sendDriverModePreferences(boolean value) {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("inDriverModule", value);
        editor.putBoolean("isRemembered", true);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        headerLayout = findViewById(R.id.headerLayout);
        driverNav = findViewById(R.id.bottomNavigationView);
        driverNav.setBackground(null);

        myContext = DriverActivity.this;
        myResources = myContext.getResources();

        initSharedPreferences();
        initUpdateApplicationDialog();
        initAppVersionInfoDialog();

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
            else {
                userId = firebaseUser.getUid();
                checkIfDriver();
            }
        }

        appMetaData = new AppMetaData();
        getAppMetaData();
        getUsers();

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if(navHostFragment != null) driverNavCtrlr = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(driverNav, driverNavCtrlr);

        driverNavCtrlr.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(driverNav.getSelectedItemId() == R.id.settingsFragment2)
                headerLayout.setVisibility(View.GONE);
            else headerLayout.setVisibility(View.VISIBLE);
        });
    }

    private void getAppMetaData() {
        metaDataRef = firebaseDatabase.getReference("appMetaData");
        metaDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double latestVersion = 0;
                String status = "Failed to get data";
                boolean showUpdates = false;

                if(snapshot.exists()) {
                    if(snapshot.child("version").exists())
                        latestVersion = snapshot.child("version").getValue(Double.class);
                    if(snapshot.child("status").exists())
                        status = snapshot.child("status").getValue(String.class);
                    if(snapshot.child("showUpdates").exists())
                        showUpdates = snapshot.child("showUpdates").getValue(Boolean.class);
                }

                if(statusPromptArray.contains(status) &&
                        !appMetaData.isDeveloper() && !isAppStatusActivityShown) {
                    Intent newIntent = new Intent(myContext, AppStatusActivity.class);
                    newIntent.putExtra("isErrorStatus", false);
                    startActivity(newIntent);
                    finishAffinity();
                    isAppStatusActivityShown = !isAppStatusActivityShown;
                }

                if(!isAlertDialogShown) {
                    if(appMetaData.getCurrentVersion() < latestVersion) {
                        String appVersion = "Current Version: v" + appMetaData.getCurrentVersion() +
                                "\tLatest Version: v" + latestVersion;
                        tvAppVersion.setText(appVersion);
                        dialog.show();
                    }
                    else if(showUpdates && isShowAppVersionInfoEnabled) {
                        String dialogTitle = "What's new in v" + latestVersion;
                        tvAppVersionInfoDialogTitle.setText(dialogTitle);
                        String newlyAddedFeatures = appMetaData.getNewlyAddedFeatures();
                        extvNewlyAddedFeatures.setText(newlyAddedFeatures);
                        appVersionInfoDialog.show();
                    }
                    isAlertDialogShown = !isAlertDialogShown;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                if(!appMetaData.isDeveloper() && !isAppStatusActivityShown) {
                    Intent intent = new Intent(myContext, AppStatusActivity.class);
                    intent.putExtra("isErrorStatus", true);
                    startActivity(intent);
                    finishAffinity();
                    isAppStatusActivityShown = !isAppStatusActivityShown;
                }
            }
        });
    }

    private void initUpdateApplicationDialog() {
        dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_application_layout);

        dialogCloseImage = dialog.findViewById(R.id.dialogCloseImage);
        tvAppVersion = dialog.findViewById(R.id.tvAppVersion);
        updateAppButton = dialog.findViewById(R.id.updateAppButton);

        updateAppButton.setOnClickListener(view -> {
            Intent newIntent = new Intent(myContext, WebViewActivity.class);
            startActivity(newIntent);
        });

        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnDismissListener(dialogInterface -> finishAffinity());

        dialogCloseImage.setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void initAppVersionInfoDialog() {
        appVersionInfoDialog = new Dialog(myContext);
        appVersionInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        appVersionInfoDialog.setContentView(R.layout.dialog_app_version_info_layout);

        appVersionInfoDialogCloseImage = appVersionInfoDialog.findViewById(R.id.dialogCloseImage);
        tvAppVersionInfoDialogTitle = appVersionInfoDialog.findViewById(R.id.tvDialogTitle);
        extvNewlyAddedFeatures = appVersionInfoDialog.findViewById(R.id.extvNewlyAddedFeatures);
        tvPreferences = appVersionInfoDialog.findViewById(R.id.tvPreferences);
        preferencesImage = appVersionInfoDialog.findViewById(R.id.preferencesImage);

        preferencesImage.setOnClickListener(view -> openPreferences());
        tvPreferences.setOnClickListener(view -> openPreferences());

        appVersionInfoDialogCloseImage.setOnClickListener(view -> appVersionInfoDialog.dismiss());

        appVersionInfoDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        appVersionInfoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        appVersionInfoDialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        appVersionInfoDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void openPreferences() {
        Intent intent = new Intent(myContext, PreferenceActivity.class);
        myContext.startActivity(intent);
    }

    private void checkIfDriver() {
        usersRef.child(userId).child("driver").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    boolean isDriver = snapshot.getValue(Boolean.class);
                    if(!isDriver) {
                        sendDriverModePreferences(false);

                        Toast.makeText(
                                myContext,
                                "The admin removed you as a driver",
                                Toast.LENGTH_LONG
                        ).show();

                        Intent intent = new Intent(myContext, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                }
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

    private void getPendingList() {
        pendingList.clear();
        driverTaskList.clear();
        taskList.clear();

        for(User user : users) {
            List<Booking> bookingList = user.getBookingList();

            for(Booking booking : bookingList) {
                if(booking.getStatus().equals("Pending"))
                    pendingList.add(booking);
            }

            if(user.getId().equals(userId))
                driverTaskList.addAll(user.getTaskList());
            else taskList.addAll(user.getTaskList());
        }

        Collections.sort(pendingList, (booking, t1) ->
                booking.getId().compareToIgnoreCase(t1.getId()));

        startTimer();
    }

    private void startTimer() {
        if(statusTimer != null) statusTimer.cancel();
        statusTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                if(userId != null) {
                    for(Booking booking : pendingList)
                        checkBooking(booking, false);

                    for(Booking task : driverTaskList) {
                        if(task.getStatus().equals("Booked") || task.getStatus().equals("Request"))
                            checkBooking(task, true);
                    }

                    for(Booking task : taskList) {
                        if(task.getStatus().equals("Booked") || task.getStatus().equals("Request"))
                            checkBooking(task, false);
                    }
                }

                start();
            }
        }.start();
    }

    private void setBookingStatusToFailed(Booking targetBooking) {
        for (User user : users) {
            List<Booking> bookingList = user.getBookingList();

            for(Booking booking : bookingList) {
                if(booking.getId().equals(targetBooking.getId())) {
                    usersRef.child(user.getId()).child("bookingList").
                            child(booking.getId()).child("status").setValue("Failed");
                    return;
                }
            }
        }
    }

    private void checkBooking(Booking booking, boolean notifiable) {
        dateTimeToString = new DateTimeToString();
        int maximumDaysInMonthOfYear = dateTimeToString.getMaximumDaysInMonthOfYear();

        dateTimeToString.setFormattedSchedule(booking.getSchedule());
        int bookingYear = Integer.parseInt(dateTimeToString.getYear());
        int bookingMonth = Integer.parseInt(dateTimeToString.getMonthNo());
        int bookingDay = Integer.parseInt(dateTimeToString.getDay());

        calendarYear = calendar.get(Calendar.YEAR);
        calendarMonth = calendar.get(Calendar.MONTH);
        calendarDay = calendar.get(Calendar.DAY_OF_MONTH);

        if(bookingYear < calendarYear ||
                (bookingMonth < calendarMonth && bookingYear == calendarYear) ||
                (bookingDay < calendarDay && bookingMonth == calendarMonth && bookingYear == calendarYear)) {

            setBookingStatusToFailed(booking);
            setTaskStatusToFailed(booking);
        }

        if(notifiable) checkingForTaskNotification(booking, bookingDay, bookingMonth, bookingYear,
                    maximumDaysInMonthOfYear);
    }

    private void checkingForTaskNotification(Booking booking, int bookingDay, int bookingMonth,
                                                int bookingYear, int maximumDaysInMonthOfYear) {

        SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date().getTime());
        int hour = Integer.parseInt(currentTime.split(":")[0]);
        int min = Integer.parseInt(currentTime.split(":")[1]);
        int sec = Integer.parseInt(currentTime.split(":")[2]);

        int bookingHour = Integer.parseInt(dateTimeToString.getRawHour());
        int bookingMin = Integer.parseInt(dateTimeToString.getMin());

        List<Integer> hourArray = Arrays.asList(2, 4, 8, 12, 16, 20);
        List<Integer> minArray = Arrays.asList(1, 5, 10, 15, 20, 30, 45);

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

            setBookingStatusFromPendingToFailed(booking, hrDifference, minDifference);
            setOnTheSpotBookingStatusToFailed(booking, hrDifference, minDifference);

            if(booking.getStatus().equals("Booked") || booking.getStatus().equals("Request")) {
                initNotificationInHours(booking, hourArray, hrDifference + 1, minArray, minDifference, sec);
                initNotificationInMinutes(booking, hrDifference, minArray, minDifference, sec);
            }
        }
        else if(booking.getStatus().equals("Booked") || booking.getStatus().equals("Request")) {
            int dayDifference = bookingDay > calendarDay ?
                    bookingDay - calendarDay : (bookingDay + maximumDaysInMonthOfYear) - calendarDay;

            hrDifference = (bookingHour + (24 * dayDifference)) - hour;

            if(min < bookingMin) minDifference = bookingMin - min;
            else {
                minDifference = 60 - (min - bookingMin);
                if(minDifference < 60) hrDifference--;
                else minDifference = 0;
            }

            initNotificationInHours(booking, hourArray, hrDifference + 1, minArray, minDifference, sec);
        }

        if(booking.getStatus().equals("Booked") || booking.getStatus().equals("Request"))
            getEndPointInfo(booking);
    }

    private void getEndPointInfo(Booking task) {
        for(User user : users) {
            List<Booking> bookingList = user.getBookingList();

            for(Booking booking : bookingList) {
                if(booking.getId().equals(task.getId())) {
                    String fullName = user.getLastName() + ", " + user.getFirstName();
                    if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();

                    List<Chat> chats = booking.getChats();
                    String message = booking.getMessage();
                    if(chats.size() > 0) message = chats.get(chats.size()-1).getMessage();
                    boolean notified = task.isNotified();

                    if(!notified && message.length() > 0) showChatNotification(task, fullName, message);
                    else {
                        usersRef.child(userId).child("taskList").child(task.getId()).child("notified")
                                .setValue(true);
                    }
                    break;
                }
            }
        }
    }

    private void setBookingStatusFromPendingToFailed(Booking booking, int hrDifference, int minDifference) {
        if(booking.getStatus().equals("Pending") &&
                !booking.getBookingType().getId().equals("BT99") &&
                (hrDifference < 0 || (hrDifference == 0 && minDifference == 0))) {
            setBookingStatusToFailed(booking);
            setTaskStatusToFailed(booking);
        }
    }

    private void setOnTheSpotBookingStatusToFailed(Booking booking, int hrDifference, int minDifference) {
        if(booking.getStatus().equals("Pending") &&
                booking.getBookingType().getId().equals("BT99") &&
                (hrDifference < -1 || (hrDifference == -1 && minDifference <= 50))) {
            setBookingStatusToFailed(booking);
            setTaskStatusToFailed(booking);
        }
    }

    private void setTaskStatusToFailed(Booking booking) {
        for (User user : users) {
            List<Booking> taskList = user.getTaskList();

            for(Booking task : taskList) {
                if(task.getId().equals(booking.getId())) {
                    usersRef.child(user.getId()).child("taskList").
                            child(task.getId()).child("status").setValue("Failed");

                    if(user.getId().equals(userId)) {
                        booking.setStatus("Failed");
                        showFailedTaskNotification(booking);
                    }
                    return;
                }
            }
        }
    }

    private void initNotificationInHours(Booking booking, List<Integer> hourArray, int hrDifference,
                                         List<Integer> minArray, int minDifference, int sec) {
        if(!booking.getBookingType().getId().equals("BT99")) {
            if(hourArray.contains(hrDifference) &&
                    (minArray.contains(minDifference) || minDifference == 0) && sec < 5) {
                showUpcomingTaskNotification(booking, hrDifference, "hours");
            }
            else if(hrDifference % 24 == 0) {
                if((minArray.contains(minDifference) || minDifference == 0) && sec < 5) {
                    int day = hrDifference/24;
                    if(day == 1) showUpcomingTaskNotification(booking, day, "day");
                    else showUpcomingTaskNotification(booking, day, "days");
                }
            }
        }
    }

    private void initNotificationInMinutes(Booking booking, int hrDifference,
                                           List<Integer> minArray, int minDifference, int sec) {
        if(!booking.getBookingType().getId().equals("BT99")) {
            if(hrDifference == 0 && minArray.contains(minDifference) && sec < 5) {
                if(minDifference == 1) showUpcomingTaskNotification(booking, minDifference, "minute");
                else showUpcomingTaskNotification(booking, minDifference, "minutes");
            }
            else if(hrDifference == 1 && minDifference == 0 && sec < 5)
                showUpcomingTaskNotification(booking, hrDifference, "hour");
        }
    }

    private NotificationManager getNotificationManager(String channelId) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(
                            channelId,
                            "Clemenisle-EV",
                            NotificationManager.IMPORTANCE_HIGH
                    );
            notificationChannel.setDescription("Task Notification");
            notificationManager.createNotificationChannel(notificationChannel);
        }

        return notificationManager;
    }

    private void showUpcomingTaskNotification(Booking task, int value, String unit) {
        NotificationManager notificationManager = getNotificationManager(task.getId());
        Bitmap icon = BitmapFactory.decodeResource(myResources, R.drawable.front_icon);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(myContext, task.getId())
                        .setSmallIcon(R.drawable.front_icon).setLargeIcon(icon)
                        .setContentTitle("Clemenisle-EV Task Reminder")
                        .setContentText("You only have less than " + value + " " + unit +
                                " before the schedule of your Task (ID: " + task.getId() +").")
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(false);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent = new Intent(myContext, RouteActivity.class);
        notificationIntent.putExtra("bookingId", task.getId());
        notificationIntent.putExtra("inDriverModule", true);
        notificationIntent.putExtra("status", task.getStatus());
        notificationIntent.putExtra("previousDriverUserId", task.getPreviousDriverUserId());
        notificationIntent.putExtra("userId", getPassengerUserId(task.getId()));

        PendingIntent pendingIntent = PendingIntent.getActivity(
                myContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        builder.setFullScreenIntent(pendingIntent, true);
        notificationManager.notify(1, builder.build());
    }

    private void showChatNotification(Booking task, String fullName, String message) {
        NotificationManager notificationManager = getNotificationManager(task.getId());

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(myContext, task.getId())
                        .setSmallIcon(R.drawable.front_icon)
                        .setContentTitle("Clemenisle-EV Chat: " + task.getId())
                        .setContentText(fullName + ": " + message)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(true);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent = new Intent(myContext, ChatActivity.class);
        notificationIntent.putExtra("bookingId", task.getId());
        notificationIntent.putExtra("inDriverModule", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                myContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        builder.setFullScreenIntent(pendingIntent, true);
        notificationManager.notify(1, builder.build());

        usersRef.child(userId).child("taskList").child(task.getId()).child("notified")
                .setValue(true);
    }

    private void showFailedTaskNotification(Booking task) {
        NotificationManager notificationManager = getNotificationManager(task.getId());
        Bitmap icon = BitmapFactory.decodeResource(myResources, R.drawable.front_icon);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(myContext, task.getId())
                        .setSmallIcon(R.drawable.front_icon).setLargeIcon(icon)
                        .setContentTitle("Clemenisle-EV Booking Reminder")
                        .setContentText("You have failed to perform your Task (ID: " + task.getId() +").")
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(false);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent;
        if(task.getBookingType().getId().equals("BT99"))
            notificationIntent = new Intent(myContext, OnTheSpotActivity.class);
        else notificationIntent = new Intent(myContext, RouteActivity.class);

        notificationIntent.putExtra("bookingId", task.getId());
        notificationIntent.putExtra("inDriverModule", true);
        notificationIntent.putExtra("isScanning", false);
        notificationIntent.putExtra("status", task.getStatus());
        notificationIntent.putExtra("previousDriverUserId", task.getPreviousDriverUserId());
        notificationIntent.putExtra("userId", getPassengerUserId(task.getId()));

        PendingIntent pendingIntent = PendingIntent.getActivity(
                myContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        builder.setFullScreenIntent(pendingIntent, true);
        notificationManager.notify(1, builder.build());
    }

    private String getPassengerUserId(String bookingId) {
        for(User user : users) {
            List<Booking> bookingList = user.getBookingList();
            for(Booking booking : bookingList) {
                if(booking.getId().equals(bookingId)) {
                    return user.getId();
                }
            }
        }
        return null;
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
                getPendingList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean isToday(int bookingDay, int bookingMonth, int bookingYear) {
        return (bookingDay == calendarDay && bookingMonth == calendarMonth && bookingYear == calendarYear);
    }

    @Override
    public void onBackPressed() {
        if(backPressedTime + 2500 > System.currentTimeMillis()) {
            backToast.cancel();
            finish();
        }
        else {
            backToast = Toast.makeText(myContext,
                    "Press back again to exit",
                    Toast.LENGTH_SHORT);
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();
    }


    @Override
    @SuppressWarnings("deprecation")
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult intentResult = IntentIntegrator.parseActivityResult(
                requestCode, resultCode, data
        );

        if(intentResult.getContents() != null) {
            Toast.makeText(
                    myContext,
                    intentResult.getContents(),
                    Toast.LENGTH_LONG
            ).show();
        }
        else {
            Toast.makeText(
                    myContext,
                    "There is no QR Code scanned",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}