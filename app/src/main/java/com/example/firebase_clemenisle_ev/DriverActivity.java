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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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
import com.example.firebase_clemenisle_ev.Classes.IWalletTransaction;
import com.example.firebase_clemenisle_ev.Classes.ReferenceNumber;
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
import androidx.appcompat.content.res.AppCompatResources;
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
    ImageView chatImage;
    TextView tvBadge;

    BottomNavigationView driverNav;
    NavController driverNavCtrlr;
    NavHostFragment navHostFragment;

    Context myContext;
    Resources myResources;

    List<Booking> pendingList = new ArrayList<>();
    List<Booking> bookingList1 = new ArrayList<>();
    List<Booking> bookingList2 = new ArrayList<>();
    List<Booking> driverTaskList = new ArrayList<>(), taskList = new ArrayList<>();

    DateTimeToString dateTimeToString;

    Calendar calendar = Calendar.getInstance();
    int calendarYear, calendarMonth, calendarDay;

    CountDownTimer notificationTimer;

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

    int newChats = 0;

    int transactionListCount;
    double iWallet;
    boolean isRefunded = true;

    List<IWalletTransaction> transactionList = new ArrayList<>();

    boolean isGeneratingTransactionId = false;

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
        chatImage = findViewById(R.id.chatImage);
        tvBadge = findViewById(R.id.tvBadge);
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

        if(userId != null) getTransactionList();

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if(navHostFragment != null) driverNavCtrlr = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(driverNav, driverNavCtrlr);

        driverNavCtrlr.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(driverNav.getSelectedItemId() == R.id.settingsFragment2)
                headerLayout.setVisibility(View.GONE);
            else headerLayout.setVisibility(View.VISIBLE);
        });

        chatImage.setOnClickListener(view -> {
            Intent intent = new Intent(myContext, ChatListActivity.class);
            startActivity(intent);
        });
    }

    private void getTransactionList() {
        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionListCount = 0;
                iWallet = 0;

                transactionList.clear();
                if(snapshot.exists()) {
                    User user = new User(snapshot);
                    transactionList.addAll(user.getTransactionList());
                    iWallet = user.getIWallet();

                    DataSnapshot dataSnapshot = snapshot.child("iWalletTransactionList");
                    transactionListCount = (int) dataSnapshot.getChildrenCount();
                }

                new Handler().postDelayed(() -> getReferenceNumber(), 3000);
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

    private void getReferenceNumber() {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double price, creditedAmount, balance, refundAmount = 0, refundedAmount;
                String status;

                if(snapshot.exists()) {
                    User user = new User(snapshot);
                    for(Booking booking : user.getBookingList()) {
                        List<ReferenceNumber> referenceNumberList1 =
                                booking.getReferenceNumberList();

                        creditedAmount = 0;
                        for(ReferenceNumber referenceNumber : referenceNumberList1) {
                            if(referenceNumber != null)
                                creditedAmount += referenceNumber.getValue();
                        }

                        price = booking.getBookingType().getPrice();
                        refundedAmount = booking.getRefundedAmount();
                        status = booking.getStatus();

                        balance = price - creditedAmount;
                        if(balance < 0) {
                            refundAmount = balance * -1;
                            refundAmount -= refundedAmount;
                        }

                        if(status.equals("Cancelled") || status.equals("Failed"))
                            refundAmount = creditedAmount - refundedAmount;

                        if(refundAmount > 0) generateTransactionId(refundAmount, refundedAmount, booking.getId());
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

    private void generateTransactionId(double refundAmount, double refundedAmount, String bookingId) {
        isGeneratingTransactionId = false;
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!isGeneratingTransactionId) {
                    isGeneratingTransactionId = true;

                    DateTimeToString dateTimeToString = new DateTimeToString();
                    String yearId = dateTimeToString.getYear2Suffix();
                    int month = Integer.parseInt(dateTimeToString.getMonthNo()) + 1;
                    String monthId = String.valueOf(month);
                    if(monthId.length() == 1) monthId = "0" + monthId;
                    String dayId = dateTimeToString.getDay();
                    if(dayId.length() == 1) dayId = "0" + dayId;

                    String transactionId = "T" + yearId + "-" + monthId + dayId;

                    int suffixCount = 0;

                    if(snapshot.exists()) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = new User(dataSnapshot);

                            List<IWalletTransaction> transactionList = user.getTransactionList();
                            if(transactionList.size() > 0) {
                                for(IWalletTransaction transaction : transactionList) {
                                    if(transaction.getId().startsWith(transactionId)) {
                                        suffixCount++;
                                    }
                                }
                            }
                        }
                    }
                    String idSuffix = String.valueOf(suffixCount);
                    if(idSuffix.length() == 1) idSuffix = "0" + idSuffix;

                    transactionId += "-" + idSuffix;

                    refund(refundAmount, refundedAmount, transactionId, bookingId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                isGeneratingTransactionId = false;
            }
        });
    }

    private void refund(double refundAmount, double refundedAmount, String wtId, String bookingId) {
        if(!isRefunded) return;
        isRefunded = false;

        IWalletTransaction transaction = new IWalletTransaction(wtId,
                new DateTimeToString().getDateAndTime(), "Refund", refundAmount);
        transaction.setBookingId(bookingId);

        usersRef.child(userId).child("iWallet").setValue(iWallet + refundAmount);
        usersRef.child(userId).child("iWalletTransactionList").child(wtId).setValue(transaction);
        usersRef.child(userId).child("bookingList").child(bookingId).child("refundedAmount").
                setValue(refundAmount + refundedAmount).addOnCompleteListener(task -> isRefunded = true);
    }

    private void getBookingList() {
        List<Booking> bookingList = new ArrayList<>(), taskList = new ArrayList<>();

        for(User user : users) {
            if(user.getId().equals(userId)) {
                bookingList.addAll(user.getBookingList());
                taskList.addAll(user.getTaskList());
            }
        }
        getChatList(bookingList, taskList);
    }

    private void getChatList(List<Booking> bookingList, List<Booking> taskList) {
        for(Booking booking : bookingList) {
            for(User user : users) {
                List<Booking> taskList1 = user.getTaskList();
                for(Booking task : taskList1) {
                    if(booking.getId().equals(task.getId())) {
                        if(!booking.isRead()) newChats++;
                        break;
                    }
                }
            }
        }

        for(Booking task : taskList) {
            for(User user : users) {
                List<Booking> bookingList1 = user.getBookingList();
                for(Booking booking : bookingList1) {
                    if(task.getId().equals(booking.getId())) {
                        if(!task.isRead()) newChats++;
                        break;
                    }
                }
            }
        }

        if(newChats > 0) {
            tvBadge.setText(String.valueOf(newChats));
            tvBadge.setVisibility(View.VISIBLE);
        }
        else tvBadge.setVisibility(View.GONE);
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
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
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
        appVersionInfoDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
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
        bookingList1.clear();
        bookingList2.clear();
        pendingList.clear();
        driverTaskList.clear();
        taskList.clear();

        for(User user : users) {
            List<Booking> bookingList = user.getBookingList();

            if(user.getId().equals(userId)) {
                for(Booking booking : bookingList) {
                    if(booking.getStatus().equals("Pending"))
                        bookingList1.add(booking);
                    if(booking.getStatus().equals("Booked"))
                        bookingList2.add(booking);
                }
                driverTaskList.addAll(user.getTaskList());
            }
            else {
                for(Booking booking : bookingList) {
                    if(booking.getStatus().equals("Pending"))
                        pendingList.add(booking);
                }
                taskList.addAll(user.getTaskList());
            }
        }

        Collections.sort(pendingList, (booking, t1) ->
                booking.getId().compareToIgnoreCase(t1.getId()));

        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(notificationTimer != null) notificationTimer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(notificationTimer != null) notificationTimer.start();
    }

    private void startTimer() {
        if(notificationTimer != null) notificationTimer.cancel();
        notificationTimer = new CountDownTimer(28000, 28000) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                notificationTimerFunction();
                start();
            }
        }.start();
        notificationTimerFunction();
    }

    private void notificationTimerFunction() {
        if(userId != null) {
            for(Booking booking : pendingList)
                checkBooking(booking, false, true);

            for(Booking booking : bookingList1)
                checkBooking(booking, true, false);

            for(Booking booking : bookingList2)
                checkBooking(booking, true, false);

            for(Booking task : driverTaskList) {
                if(task.getStatus().equals("Booked") || task.getStatus().equals("Request"))
                    checkBooking(task, true, true);
            }

            for(Booking task : taskList) {
                if(task.getStatus().equals("Booked") || task.getStatus().equals("Request"))
                    checkBooking(task, false, true);
            }

            for(IWalletTransaction transaction : transactionList) {
                boolean isNotified = transaction.isNotified();
                if(!isNotified) {
                    showSuccessfulTransactionNotification(transaction);
                }
            }
        }
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

    private void checkBooking(Booking booking, boolean isNotifiable, boolean inDriverModule) {
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

        checkingForTaskNotification(booking, bookingDay, bookingMonth, bookingYear,
                    maximumDaysInMonthOfYear, inDriverModule, isNotifiable);
    }

    private void checkingForTaskNotification(Booking booking, int bookingDay, int bookingMonth,
                                                int bookingYear, int maximumDaysInMonthOfYear,
                                             boolean inDriverModule, boolean isNotifiable) {

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

            if((booking.getStatus().equals("Booked") || booking.getStatus().equals("Request")) &&
                    isNotifiable) {
                initNotificationInHours(booking, hourArray, minArray,
                        hrDifference + 1, minDifference, sec, inDriverModule);
                initNotificationInMinutes(booking, minArray, hrDifference, minDifference, sec, inDriverModule);
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

            if(isNotifiable)
                initNotificationInHours(booking, hourArray, minArray,
                        hrDifference + 1, minDifference, sec, inDriverModule);
        }

        if((booking.getStatus().equals("Booked") || booking.getStatus().equals("Request")) &&
                isNotifiable) getEndPointInfo(booking, inDriverModule);

        if((booking.getStatus().equals("Pending") || booking.getStatus().equals("Booked")) &&
                !inDriverModule && isNotifiable) {
            List<ReferenceNumber> referenceNumberList = booking.getReferenceNumberList();
            for(ReferenceNumber referenceNumber : referenceNumberList) {
                boolean isNotified = referenceNumber.isNotified();
                if(!isNotified) {
                    showCreditedRNNotification(booking, referenceNumber);
                }
            }
        }
    }

    private void getEndPointInfo(Booking task, boolean inDriverModule) {
        for(User user : users) {
            if(inDriverModule) {
                List<Booking> bookingList = user.getBookingList();
                for(Booking booking : bookingList) {
                    if(booking.getId().equals(task.getId())) {
                        String fullName = user.getLastName() + ", " + user.getFirstName();
                        if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();

                        List<Chat> chats = task.getChats();
                        String message = booking.getMessage();
                        if(chats.size() > 0) message = chats.get(chats.size()-1).getMessage();
                        boolean notified = task.isNotified();

                        if(!notified && message.length() > 0)
                            showChatNotification(task, fullName, message, true);
                        else {
                            usersRef.child(userId).child("taskList").child(task.getId()).
                                    addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists())
                                                snapshot.getRef().child("notified").setValue(true);
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
                        break;
                    }
                }
            }
            else {
                List<Booking> taskList = user.getTaskList();
                for(Booking task1 : taskList) {
                    if(task1.getId().equals(task.getId())) {
                        String fullName = user.getLastName() + ", " + user.getFirstName();
                        if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();

                        List<Chat> chats = task1.getChats();
                        String message = "こんにちは (Hello), I am " + fullName + ", your assigned driver.";
                        if(chats.size() > 0) message = chats.get(chats.size()-1).getMessage();
                        boolean notified = task.isNotified();

                        if(!notified && message.length() > 0)
                            showChatNotification(task, fullName, message, false);
                        else {
                            usersRef.child(userId).child("bookingList").child(task.getId()).
                                    addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists())
                                                snapshot.getRef().child("notified").setValue(true);
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
                        break;
                    }
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
                if(task.getId().equals(booking.getId()) && !task.getStatus().equals("Passed")) {
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

    private void initNotificationInHours(Booking booking, List<Integer> hourArray, List<Integer> minArray,
                                         int hrDifference, int minDifference, int sec, boolean inDriverModule) {
        if(!booking.getBookingType().getId().equals("BT99")) {
            if(hourArray.contains(hrDifference) &&
                    (minArray.contains(minDifference) || minDifference == 0) && sec < 5) {
                if(inDriverModule) showUpcomingTaskNotification(booking, hrDifference, "hours");
                else showUpcomingBookingNotification(booking, hrDifference, "hours");
            }
            else if(hrDifference % 24 == 0) {
                if((minArray.contains(minDifference) || minDifference == 0) && sec < 5) {
                    int day = hrDifference/24;
                    if(day == 1) {
                        if(inDriverModule) showUpcomingTaskNotification(booking, day, "day");
                        else showUpcomingBookingNotification(booking, day, "day");
                    }
                    else {
                        if(inDriverModule) showUpcomingTaskNotification(booking, day, "days");
                        else showUpcomingBookingNotification(booking, day, "days");
                    }
                }
            }
        }
    }

    private void initNotificationInMinutes(Booking booking, List<Integer> minArray,
                                           int hrDifference, int minDifference, int sec,
                                           boolean inDriverModule) {
        if(!booking.getBookingType().getId().equals("BT99")) {
            if(hrDifference == 0 && minArray.contains(minDifference) && sec < 5) {
                if(minDifference == 1) {
                    if(inDriverModule) showUpcomingTaskNotification(booking, minDifference, "minute");
                    else showUpcomingBookingNotification(booking, minDifference, "minute");
                }
                else {
                    if(inDriverModule) showUpcomingTaskNotification(booking, minDifference, "minutes");
                    else showUpcomingBookingNotification(booking, minDifference, "minutes");
                }
            }
            else if(hrDifference == 1 && minDifference == 0 && sec < 5)
                if(inDriverModule) showUpcomingTaskNotification(booking, hrDifference, "hour");
                else showUpcomingBookingNotification(booking, hrDifference, "hour");
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
                        .setContentTitle("Clemenisle-EV Booking Reminder")
                        .setContentText("You only have less than " + value + " " + unit +
                                " before the schedule of your Booking (ID: " + booking.getId() +").")
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(false);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent = new Intent(myContext, RouteActivity.class);
        notificationIntent.putExtra("bookingId", booking.getId());
        notificationIntent.putExtra("inDriverModule", false);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                myContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        builder.setFullScreenIntent(pendingIntent, true);
        notificationManager.notify(1, builder.build());
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

    private void showChatNotification(Booking task, String fullName, String message,
                                      boolean inDriverModule) {
        NotificationManager notificationManager = getNotificationManager(task.getId());

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(myContext, task.getId())
                        .setSmallIcon(R.drawable.front_icon)
                        .setContentTitle("Clemenisle-EV Chat: " + task.getId())
                        .setContentText(fullName + ": " + message)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(false);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent = new Intent(myContext, ChatActivity.class);
        notificationIntent.putExtra("taskId", task.getId());
        notificationIntent.putExtra("inDriverModule", inDriverModule);

        if(!inDriverModule) {
            String taskDriverUserId = getDriverUserId(task.getId());
            notificationIntent.putExtra("driverUserId", taskDriverUserId);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                myContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        builder.setFullScreenIntent(pendingIntent, true);
        notificationManager.notify(1, builder.build());

        if(inDriverModule) {
            usersRef.child(userId).child("taskList").child(task.getId()).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists())
                                snapshot.getRef().child("notified").setValue(true);
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
        else {
            usersRef.child(userId).child("bookingList").child(task.getId()).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists())
                                snapshot.getRef().child("notified").setValue(true);
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
    }

    private void showFailedTaskNotification(Booking task) {
        NotificationManager notificationManager = getNotificationManager(task.getId());
        Bitmap icon = BitmapFactory.decodeResource(myResources, R.drawable.front_icon);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(myContext, task.getId())
                        .setSmallIcon(R.drawable.front_icon).setLargeIcon(icon)
                        .setContentTitle("Clemenisle-EV Booking Status")
                        .setContentText("You failed to perform your Task (ID: " + task.getId() +").")
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

    private void showCreditedRNNotification(Booking booking, ReferenceNumber referenceNumber) {
        NotificationManager notificationManager = getNotificationManager(booking.getId());
        Bitmap icon = BitmapFactory.decodeResource(myResources, R.drawable.front_icon);

        String value = "₱" + referenceNumber.getValue();
        if(value.split("\\.")[1].length() == 1) value += 0;

        String referenceNumberValue = referenceNumber.getReferenceNumber();
        String content = value + " has been credited to #" + ".";
        if(referenceNumberValue == null) content = "You pay " + value + " in your booking.";

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(myContext, booking.getId())
                        .setSmallIcon(R.drawable.front_icon).setLargeIcon(icon)
                        .setContentTitle("Clemenisle-EV Online Payment Status")
                        .setContentText(content)
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(false);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent;
        notificationIntent = new Intent(myContext, OnlinePaymentActivity.class);
        notificationIntent.putExtra("bookingId", booking.getId());

        PendingIntent pendingIntent = PendingIntent.getActivity(
                myContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        builder.setFullScreenIntent(pendingIntent, true);
        notificationManager.notify(1, builder.build());

        usersRef.child(userId).child("bookingList").child(booking.getId()).
                child("referenceNumberList").child(referenceNumber.getId()).
                child("notified").setValue(true);
    }

    private void showSuccessfulTransactionNotification(IWalletTransaction transaction) {
        NotificationManager notificationManager = getNotificationManager(transaction.getId());
        Bitmap icon = BitmapFactory.decodeResource(myResources, R.drawable.front_icon);

        String value = "₱" + transaction.getValue();
        if(value.split("\\.")[1].length() == 1) value += 0;

        String category = transaction.getCategory();

        String content = "";
        if(category.equals("Top-up")) {
            if(transaction.getValue() > 0) content = value + " has been added to your iWallet.";
        }
        else if(category.equals("Transfer")) {
            if(transaction.getValue() > 0) content = value + " has been transferred to your GCash." +
                    "Mobile Number: " + transaction.getMobileNumber();
        }
        else return;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(myContext, transaction.getId())
                        .setSmallIcon(R.drawable.front_icon).setLargeIcon(icon)
                        .setContentTitle("Clemenisle-EV Online Payment Status")
                        .setContentText(content)
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(false);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent;
        notificationIntent = new Intent(myContext, IWalletActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                myContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        builder.setFullScreenIntent(pendingIntent, true);
        notificationManager.notify(1, builder.build());

        usersRef.child(userId).child("iWalletTransactionList").child(transaction.getId()).
                child("notified").setValue(true);
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

    private String getDriverUserId(String bookingId) {
        for(User user : users) {
            List<Booking> taskList = user.getTaskList();
            for(Booking task : taskList) {
                if(task.getId().equals(bookingId) && !task.getStatus().equals("Passed")) {
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
                newChats = 0;

                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);
                        users.add(user);
                    }
                }

                getPendingList();
                getBookingList();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notificationTimer.cancel();
    }
}