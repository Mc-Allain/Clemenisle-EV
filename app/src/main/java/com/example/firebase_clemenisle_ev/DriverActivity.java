package com.example.firebase_clemenisle_ev;

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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

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
    List<Booking> taskList = new ArrayList<>();

    DateTimeToString dateTimeToString;

    Calendar calendar = Calendar.getInstance();
    int calendarYear, calendarMonth, calendarDay;

    CountDownTimer statusTimer;

    long backPressedTime;
    Toast backToast;

    String userId;
    boolean isLoggedIn = false;

    List<User> users = new ArrayList<>();

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        headerLayout = findViewById(R.id.headerLayout);
        driverNav = findViewById(R.id.bottomNavigationView);
        driverNav.setBackground(null);

        myContext = DriverActivity.this;
        myResources = myContext.getResources();

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

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if(navHostFragment != null) driverNavCtrlr = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(driverNav, driverNavCtrlr);

        driverNavCtrlr.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(driverNav.getSelectedItemId() == R.id.settingsFragment2)
                headerLayout.setVisibility(View.GONE);
            else headerLayout.setVisibility(View.VISIBLE);
        });

        getUsers();
    }

    private void getPendingList() {
        taskList.clear();
        pendingList.clear();

        for(User user : users) {
            List<Booking> bookingList = user.getBookingList();

            for(Booking booking : bookingList) {
                if(booking.getStatus().equals("Pending"))
                    pendingList.add(booking);
            }

            if(user.getId().equals(userId))
                taskList.addAll(user.getTaskList());
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
                    for(Booking booking : pendingList) {
                        checkBooking(booking);
                    }

                    for(Booking task : taskList) {
                        if(task.getStatus().equals("Booked") || task.getStatus().equals("Request"))
                            checkBooking(task);
                    }
                }

                start();
            }
        }.start();
    }

    private void setBookingStatusToFailed(String bookingId) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);
                        List<Booking> bookingList = user.getBookingList();

                        for(Booking booking : bookingList) {
                            if(booking.getId().equals(bookingId)) {
                                DatabaseReference bookingListRef = usersRef.child(user.getId()).
                                        child("bookingList").child(bookingId);
                                bookingListRef.child("status").setValue("Failed");
                                bookingListRef.child("chats").removeValue();
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

    private void checkBooking(Booking booking) {
        dateTimeToString = new DateTimeToString();
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

            setBookingStatusToFailed(booking.getId());
            setTaskStatusToFailed(booking.getId());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date().getTime());
        int hour = Integer.parseInt(currentTime.split(":")[0]);
        int min = Integer.parseInt(currentTime.split(":")[1]);
        int sec = Integer.parseInt(currentTime.split(":")[2]);

        int bookingHour = Integer.parseInt(dateTimeToString.getRawHour());
        int bookingMin = Integer.parseInt(dateTimeToString.getMin());

        List<String> hourArray = Arrays.asList("2", "4", "8", "12", "16", "20");
        List<String> minArray = Arrays.asList("1", "5", "10", "15", "20", "30", "45");

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
                    bookingDay - calendarDay : (bookingDay + 30) - calendarDay;

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
            setBookingStatusToFailed(booking.getId());
            setTaskStatusToFailed(booking.getId());
        }
    }

    private void setOnTheSpotBookingStatusToFailed(Booking booking, int hrDifference, int minDifference) {
        if(booking.getStatus().equals("Pending") &&
                booking.getBookingType().getId().equals("BT99") &&
                (hrDifference < -1 || (hrDifference == -1 && minDifference <= 50))) {
            setBookingStatusToFailed(booking.getId());
            setTaskStatusToFailed(booking.getId());
        }
    }

    private void setTaskStatusToFailed(String bookingId) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);
                        List<Booking> taskList = user.getTaskList();

                        for(Booking booking : taskList) {
                            if(booking.getId().equals(bookingId)) {
                                usersRef.child(user.getId()).child("taskList").
                                        child(booking.getId()).child("status").setValue("Failed");
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

    private void initNotificationInHours(Booking booking, List<String> hourArray, int hrDifference,
                                         List<String> minArray, int minDifference, int sec) {
        if(!booking.getBookingType().getId().equals("BT99")) {
            if(hourArray.contains(String.valueOf(hrDifference)) &&
                    (minArray.contains(String.valueOf(minDifference)) || minDifference == 0) && sec < 5) {
                showUpcomingBookingNotification(booking, hrDifference, "hours");
            }
            else if(hrDifference % 24 == 0) {
                if((minArray.contains(String.valueOf(minDifference)) || minDifference == 0) && sec < 5)
                    showUpcomingBookingNotification(booking, hrDifference/24, "day");
            }
        }
    }

    private void initNotificationInMinutes(Booking booking, int hrDifference,
                                           List<String> minArray, int minDifference, int sec) {
        if(!booking.getBookingType().getId().equals("BT99")) {
            if(hrDifference == 0 && minArray.contains(String.valueOf(minDifference)) && sec < 5) {
                if(minDifference == 1) showUpcomingBookingNotification(booking, minDifference, "minute");
                else showUpcomingBookingNotification(booking, minDifference, "minutes");
            }
            else if(hrDifference == 1 && minDifference == 0 && sec < 5)
                showUpcomingBookingNotification(booking, hrDifference, "hour");
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

    private void showUpcomingBookingNotification(Booking booking, int value, String unit) {
        NotificationManager notificationManager = getNotificationManager(booking.getId());
        Bitmap icon = BitmapFactory.decodeResource(myResources, R.drawable.front_icon);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(myContext, booking.getId())
                        .setSmallIcon(R.drawable.front_icon).setLargeIcon(icon)
                        .setContentTitle("Clemenisle-EV Task Reminder")
                        .setContentText("You only have less than " + value + " " + unit +
                                " before the schedule of your Task (ID: " + booking.getId() +").")
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(false);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent = new Intent(myContext, RouteActivity.class);
        notificationIntent.putExtra("bookingId", booking.getId());
        notificationIntent.putExtra("inDriverModule", true);
        notificationIntent.putExtra("status", booking.getStatus());
        notificationIntent.putExtra("userId", getPassengerUserId(booking.getId()));

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