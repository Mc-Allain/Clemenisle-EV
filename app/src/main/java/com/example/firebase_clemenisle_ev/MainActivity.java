package com.example.firebase_clemenisle_ev;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    BottomNavigationView mainNav;
    NavController mainNavCtrlr;
    NavHostFragment navHostFragment;
    FloatingActionButton fab;

    Context myContext;
    Resources myResources;

    int colorGreen, colorRed, colorInitial;
    ColorStateList cslInitial, cslBlue, cslRed;

    long backPressedTime;
    Toast backToast;

    String userId;
    boolean isLoggedIn = false;
    String password = null;

    Dialog passwordDialog;
    TextView tvDialogTitle, tvDialogCaption;
    ImageView passwordDialogCloseImage;
    Button passwordUpdateButton;
    ProgressBar passwordDialogProgressBar;

    EditText etPassword, etConfirmPassword;
    TextInputLayout tlPassword, tlConfirmPassword;

    ImageView pwLengthCheckImage, pwUpperCheckImage, pwLowerCheckImage, pwNumberCheckImage, pwSymbolCheckImage;
    TextView tvPWLength, tvPWUpper, tvPWLower, tvPWNumber, tvPWSymbol;

    String newPassword = "", newConfirmPassword = "";

    boolean vPWL = false, vPWU = false, vPWLw = false, vPWN = false, vPWS = false, vCPW = false;
    boolean vCPWL = false, vCPWU = false, vCPWLw = false, vCPWN = false, vCPWS = false;

    boolean isPasswordUpdated = false;

    Calendar calendar = Calendar.getInstance();
    int calendarYear, calendarMonth, calendarDay;

    List<Booking> bookingList1 = new ArrayList<>();
    List<Booking> bookingList2 = new ArrayList<>();

    boolean success1, success2;

    DateTimeToString dateTimeToString;

    CountDownTimer notificationTimer;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("remember", false);
        editor.putString("emailAddress", null);
        editor.putString("password", null);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainNav = findViewById(R.id.bottomNavigationView);
        mainNav.setBackground(null);

        myContext = MainActivity.this;
        myResources = myContext.getResources();

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));
        cslRed = ColorStateList.valueOf(myResources.getColor(R.color.red));

        colorGreen = myResources.getColor(R.color.green);
        colorRed = myResources.getColor(R.color.red);
        colorInitial = myResources.getColor(R.color.initial);

        Intent intent = getIntent();
        password = intent.getStringExtra("password");

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
                        Toast.LENGTH_SHORT
                ).show();
            }
            else {
                userId = firebaseUser.getUid();
            }
        }

        if(password != null) {
            if(!isCurrentPasswordValid()) {
                initPasswordDialog();
                showPasswordDialog();
            }
        }

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if(navHostFragment != null) mainNavCtrlr = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(mainNav, mainNavCtrlr);

        dateTimeToString = new DateTimeToString();
        if(userId != null) getBooking();

        fab = findViewById(R.id.floatingActionButton);
        fab.setColorFilter(getResources().getColor(R.color.white));

        fab.setOnClickListener(view -> {
            Intent newIntent;
            if(isLoggedIn) {
                newIntent = new Intent(myContext, BookingActivity.class);
            }
            else {
                newIntent = new Intent(myContext, LoginActivity.class);
            }
            startActivity(newIntent);
        });
    }

    private void startTimer() {
        if(notificationTimer != null) notificationTimer.cancel();
        notificationTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                for(Booking booking : bookingList1) {
                    checkBooking(booking);
                }
                for(Booking booking : bookingList2) {
                    checkBooking(booking);
                }

                start();
            }
        }.start();
    }

    private void checkBooking(Booking booking) {
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

            firebaseDatabase.getReference("users").child(userId).
                    child("bookingList").child(booking.getId()).child("status").setValue("Failed");
        }

        checkingForBookingNotification(booking, bookingDay, bookingMonth, bookingYear);
    }

    private void checkingForBookingNotification(Booking booking, int bookingDay,
                                                int bookingMonth, int bookingYear) {

        SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
        String currentTime = sdf.format(new Date().getTime());
        int hour = Integer.parseInt(currentTime.split(":")[0]);
        int min = Integer.parseInt(currentTime.split(":")[1]);
        int sec = Integer.parseInt(currentTime.split(":")[2]);

        int bookingHour = Integer.parseInt(dateTimeToString.getRawHour());
        int bookingMin = Integer.parseInt(dateTimeToString.getMin());

        List<String> hourArray = Arrays.asList("1", "8", "16");
        List<String> minArray = Arrays.asList("1", "5", "10", "15", "20", "30", "45");

        int minDifference;
        int hrDifference;

        if(hasBookingToday(bookingDay, bookingMonth, bookingYear)) {
            if(bookingHour == hour + 1) {
                minDifference = (bookingMin + 60) - min;
                if(minDifference >= 60) hrDifference = 1;
                else hrDifference = 0;
            }
            else if (bookingHour == hour) {
                hrDifference = 0;
                minDifference = bookingMin - min;
            }
            else {
                hrDifference = bookingHour - hour;
                if(min < bookingMin) minDifference = bookingMin - min;
                else {
                    minDifference = 60 - (min - bookingMin);
                    if(minDifference < 60) hrDifference--;
                    else minDifference = 0;
                }

                if(booking.getStatus().equals("Booked"))
                    initNotificationInHours(booking, hourArray, hrDifference, minArray, minDifference, sec);

                if(booking.getStatus().equals("Processing") &&
                        (hrDifference < 0 || (hrDifference == 0 && minDifference == 0)))
                    firebaseDatabase.getReference("users").child(userId).
                            child("bookingList").child(booking.getId()).child("status").setValue("Failed");
                return;
            }
            if(minDifference == 60) minDifference = 0;
            if(booking.getStatus().equals("Booked"))
                initNotificationInMinutes(booking, hrDifference, minArray, minDifference, sec);
        }
        else if(hasBookingTomorrow(bookingDay, bookingMonth, bookingYear) &&
                booking.getStatus().equals("Booked")) {
            hrDifference = (bookingHour + 24) - hour;
            if(min < bookingMin) minDifference = bookingMin - min;
            else {
                minDifference = 60 - (min - bookingMin);
                if(minDifference < 60) hrDifference--;
                else minDifference = 0;
            }
            initNotificationInHours(booking, hourArray, hrDifference, minArray, minDifference, sec);
        }
    }

    private void initNotificationInHours(Booking booking, List<String> hourArray, int hrDifference,
                                         List<String> minArray, int minDifference, int sec) {
        if(hourArray.contains(String.valueOf(hrDifference)) &&
                (minArray.contains(String.valueOf(minDifference)) || minDifference == 0) && sec < 5) {
            if(hrDifference == 1) showUpcomingBookingNotification(booking, hrDifference, "hour");
            else showUpcomingBookingNotification(booking, hrDifference, "hours");
        }
        else if(hrDifference == 24) {
            if((minArray.contains(String.valueOf(minDifference)) || minDifference == 0) && sec < 5)
                showUpcomingBookingNotification(booking, 1, "day");
        }
    }

    private void initNotificationInMinutes(Booking booking, int hrDifference,
                                           List<String> minArray, int minDifference, int sec) {
        if(minArray.contains(String.valueOf(minDifference)) && sec < 5) {
            if(minDifference == 1) showUpcomingBookingNotification(booking, minDifference, "minute");
            else showUpcomingBookingNotification(booking, minDifference, "minutes");
        }
        else if(hrDifference > 0 && minDifference == 0 && sec < 5) {
            showUpcomingBookingNotification(booking, 1, "hour");
        }
    }

    private boolean hasBookingToday(int bookingDay, int bookingMonth, int bookingYear) {
        return (bookingDay == calendarDay && bookingMonth == calendarMonth && bookingYear == calendarYear);
    }

    private boolean hasBookingTomorrow(int bookingDay, int bookingMonth, int bookingYear) {
        return (bookingDay == calendarDay + 1 && bookingMonth == calendarMonth && bookingYear == calendarYear);
    }

    private void getBooking() {
        Query booking1Query = firebaseDatabase.getReference("users").
                child(userId).child("bookingList").orderByChild("status").equalTo("Processing");

        booking1Query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList1.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Booking booking = new Booking(dataSnapshot);
                        bookingList1.add(booking);
                    }
                }
                success1 = true;
                finishLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();

                success1 = false;
                errorLoading(error.toString());
            }
        });

        Query booking2Query = firebaseDatabase.getReference("users").
                child(userId).child("bookingList").orderByChild("status").equalTo("Booked");

        success2 = false;
        booking2Query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList2.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Booking booking = new Booking(dataSnapshot);
                        bookingList2.add(booking);
                    }
                }
                success2 = true;
                finishLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();

                success2 = false;
                errorLoading(error.toString());
            }
        });
    }

    private void finishLoading() {
        if(success1 && success2) {
            Collections.reverse(bookingList1);
            Collections.reverse(bookingList2);
            startTimer();
        }
    }

    private void errorLoading(String error) {
        if(!(success1 && success2)) {
            bookingList1.clear();
            bookingList2.clear();
            startTimer();

            Toast.makeText(
                    myContext,
                    error,
                    Toast.LENGTH_LONG
            ).show();
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
            notificationChannel.setDescription("Booking Notification");
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
                        .setContentTitle("Clemenisle-EV Booking Reminder")
                        .setContentText("You only have less than " + value + " " + unit +
                        " before the schedule of your Booking (Id: " + booking.getId() +").")
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(false);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent = new Intent(myContext, RouteActivity.class);
        notificationIntent.putExtra("bookingId", booking.getId());
        notificationIntent.putExtra("startStationId", booking.getStartStation().getId());
        notificationIntent.putExtra("endStationId", booking.getEndStation().getId());

        PendingIntent pendingIntent = PendingIntent.getActivity(
                myContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        builder.setFullScreenIntent(pendingIntent, true);
        notificationManager.notify(1, builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(password != null) {
            if(!isCurrentPasswordValid() && !isPasswordUpdated) {
                firebaseAuth.signOut();
                sendLoginPreferences();

                Toast.makeText(
                        myContext,
                        "Your account has been logged out",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private boolean isCurrentPasswordValid() {
        if(password.length() >= 8) vCPWL = true;
        else vCPWL = false;

        if(password.matches(".*[A-Z].*"))vCPWU = true;
        else vCPWU = false;

        if(password.matches(".*[a-z].*"))vCPWLw = true;
        else vCPWLw = false;

        if(password.matches(".*[0-9].*")) vCPWN = true;
        else vCPWN = false;

        if(password.matches("[A-Za-z0-9]*")) vCPWS = true;
        else vCPWS = false;

        return vCPWL && vCPWU && vCPWLw && vCPWN && vCPWS;
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

        pwLengthCheckImage.setColorFilter(colorInitial);
        pwUpperCheckImage.setColorFilter(colorInitial);
        pwLowerCheckImage.setColorFilter(colorInitial);
        pwNumberCheckImage.setColorFilter(colorInitial);
        pwSymbolCheckImage.setColorFilter(colorInitial);

        vPWL = false; vPWU = false; vPWLw = false; vPWN = false; vPWS = false; vCPW = false;

        isPasswordUpdated = false;

        passwordDialog.show();
    }

    private void initPasswordDialog() {
        passwordDialog = new Dialog(myContext);
        passwordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        passwordDialog.setContentView(R.layout.dialog_update_password_layout);

        tvDialogTitle = passwordDialog.findViewById(R.id.tvDialogTitle);
        tvDialogCaption = passwordDialog.findViewById(R.id.tvDialogCaption);
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

        String dialogTitle = "Weak Password";
        String dialogCaption = "Please input a new password.";
        tvDialogTitle.setText(dialogTitle);
        tvDialogTitle.setTextColor(colorRed);
        tvDialogCaption.setText(dialogCaption);

        passwordDialog.setCanceledOnTouchOutside(false);
        passwordDialogCloseImage.setVisibility(View.GONE);

        passwordDialog.setOnDismissListener(dialogInterface -> finishAffinity());

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
        passwordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
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
                    pwLengthCheckImage.setColorFilter(colorGreen);
                    tvPWLength.setTextColor(colorGreen);
                    vPWL = true;
                }
                else {
                    pwLengthCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwLengthCheckImage.setColorFilter(colorRed);
                    tvPWLength.setTextColor(colorRed);
                    vPWL = false;
                }

                if(newPassword.matches(".*[A-Z].*")) {
                    pwUpperCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwUpperCheckImage.setColorFilter(colorGreen);
                    tvPWUpper.setTextColor(colorGreen);
                    vPWU = true;
                }
                else {
                    pwUpperCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwUpperCheckImage.setColorFilter(colorRed);
                    tvPWUpper.setTextColor(colorRed);
                    vPWU = false;
                }

                if(newPassword.matches(".*[a-z].*")) {
                    pwLowerCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwLowerCheckImage.setColorFilter(colorGreen);
                    tvPWLower.setTextColor(colorGreen);
                    vPWLw = true;
                }
                else {
                    pwLowerCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwLowerCheckImage.setColorFilter(colorRed);
                    tvPWLower.setTextColor(colorRed);
                    vPWLw = false;
                }

                if(newPassword.matches(".*[0-9].*")) {
                    pwNumberCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwNumberCheckImage.setColorFilter(colorGreen);
                    tvPWNumber.setTextColor(colorGreen);
                    vPWN = true;
                }
                else {
                    pwNumberCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwNumberCheckImage.setColorFilter(colorRed);
                    tvPWNumber.setTextColor(colorRed);
                    vPWN = false;
                }

                if(newPassword.matches("[A-Za-z0-9]*")) {
                    pwSymbolCheckImage.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    pwSymbolCheckImage.setColorFilter(colorGreen);
                    tvPWSymbol.setTextColor(colorGreen);
                    vPWS = true;
                }
                else {
                    pwSymbolCheckImage.setImageResource(R.drawable.ic_baseline_error_24);
                    pwSymbolCheckImage.setColorFilter(colorRed);
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
                            proceedToMainActivity();

                            Toast.makeText(
                                    myContext,
                                    "This operation is sensitive and requires recent authentication." +
                                            "Please log in again before trying this request.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        else errorPasswordUpdate();
                    }
                });
    }

    private void setPasswordDialogScreenEnabled(boolean value) {
        passwordUpdateButton.setEnabled(value);
        tlPassword.setEnabled(value);
        tlConfirmPassword.setEnabled(value);
    }

    private void finishPasswordUpdate() {
        isPasswordUpdated = true;
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

    private void proceedToMainActivity() {
        sendLoginPreferences();

        Intent intent = new Intent(myContext, MainActivity.class);
        myContext.startActivity(intent);
        ((Activity) myContext).finishAffinity();
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
}