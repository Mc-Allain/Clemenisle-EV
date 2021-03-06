package com.example.firebase_clemenisle_ev;

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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

import com.example.firebase_clemenisle_ev.Classes.AppMetaData;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.Chat;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.IWalletTransaction;
import com.example.firebase_clemenisle_ev.Classes.OnlinePayment;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
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
    List<Booking> taskList = new ArrayList<>();

    DateTimeToString dateTimeToString;

    CountDownTimer notificationTimer;

    DatabaseReference metaDataRef;

    AppMetaData appMetaData;

    List<String> statusPromptArray = Arrays.asList("Under Development", "Under Maintenance");

    boolean isAppStatusActivityShown = false, isAlertDialogShown = false;

    Dialog dialog;
    ImageView dialogCloseImage;
    Button updateAppButton;

    Dialog appVersionInfoDialog;
    ImageView appVersionInfoDialogCloseImage, preferencesImage;
    TextView tvAppVersionInfoDialogTitle, tvAppVersion, tvPreferences;
    ExpandableTextView extvNewlyAddedFeatures;

    boolean isShowAppVersionInfoEnabled;

    List<User> users = new ArrayList<>();

    Dialog reLoginDialog;
    ImageView reLoginDialogCloseImage;

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

        NotificationManager notificationManager = (NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
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

        initUpdateApplicationDialog();
        initAppVersionInfoDialog();
        initPasswordChangeLoginDialog();

        firebaseAuth = FirebaseAuth.getInstance();
        if(isLoggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) firebaseUser.reload();
            if(firebaseUser == null) {
                firebaseAuth.signOut();
                sendLoginPreferences();
                isLoggedIn = false;

                Toast.makeText(
                        myContext,
                        "Failed to get the current user. Account logged out.",
                        Toast.LENGTH_LONG
                ).show();
            }
            else userId = firebaseUser.getUid();
        }

        appMetaData = new AppMetaData();
        getAppMetaData();
        getUsers();

        if(userId != null) getTransactionList();

        if(password != null) {
            if(!isCurrentPasswordValid()) {
                initPasswordDialog();
                showPasswordDialog();
            }
        }

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if(navHostFragment != null) mainNavCtrlr = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(mainNav, mainNavCtrlr);

        fab = findViewById(R.id.floatingActionButton);
        fab.getDrawable().setTint(getResources().getColor(R.color.white));

        fab.setOnClickListener(view -> {
            Intent newIntent;
            if(isLoggedIn) newIntent = new Intent(myContext, BookingActivity.class);
            else newIntent = new Intent(myContext, LoginActivity.class);
            startActivity(newIntent);
        });
    }

    private void getTransactionList() {
        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                iWallet = 0;

                transactionList.clear();
                if(snapshot.exists()) {
                    User user = new User(snapshot);
                    transactionList.addAll(user.getTransactionList());
                    iWallet = user.getIWallet();
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
                        List<OnlinePayment> onlinePaymentList1 =
                                booking.getReferenceNumberList();

                        creditedAmount = 0;
                        for(OnlinePayment onlinePayment : onlinePaymentList1) {
                            if(onlinePayment != null)
                                creditedAmount += onlinePayment.getValue();
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

    private void refund(double refundAmount, double refundedAmount, String tId, String bookingId) {
        if(!isRefunded) return;
        isRefunded = false;

        IWalletTransaction transaction = new IWalletTransaction(tId,
                new DateTimeToString().getDateAndTime(), "Refund", refundAmount);
        transaction.setBookingId(bookingId);

        usersRef.child(userId).child("iwallet").setValue(iWallet + refundAmount);
        usersRef.child(userId).child("iWalletTransactionList").child(tId).setValue(transaction);
        usersRef.child(userId).child("bookingList").child(bookingId).child("refundedAmount").
                setValue(refundAmount + refundedAmount).addOnCompleteListener(task -> isRefunded = true);
    }

    private void getUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);
                        users.add(user);
                    }
                }
                getBooking();
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
            newIntent.putExtra("toUpdate", true);
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
            for(Booking booking : bookingList1)
                checkBooking(booking, false);

            for(Booking booking : bookingList2)
                checkBooking(booking, false);

            for(Booking task : taskList) {
                if(task.getStatus().equals("Booked") || task.getStatus().equals("Request"))
                    checkBooking(task, true);
            }

            for(IWalletTransaction transaction : transactionList) {
                boolean isNotified = transaction.isNotified();
                if(!isNotified) {
                    showSuccessfulTransactionNotification(transaction);
                }
            }
        }
    }

    private void setBookingStatusToFailed(Booking booking) {
        usersRef.child(userId).child("bookingList").child(booking.getId()).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            snapshot.getRef().child("status").setValue("Failed");
                            booking.setStatus("Failed");
                            showFailedBookingNotification(booking);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkBooking(Booking booking, boolean inDriverModule) {
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

        checkingForBookingNotification(booking, bookingDay, bookingMonth, bookingYear,
                maximumDaysInMonthOfYear, inDriverModule);
    }

    private void checkingForBookingNotification(Booking booking, int bookingDay, int bookingMonth,
                                                int bookingYear, int maximumDaysInMonthOfYear,
                                                boolean inDriverModule) {

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

            initNotificationInHours(booking, hourArray, minArray,
                    hrDifference + 1, minDifference, sec, inDriverModule);
        }

        if(booking.getStatus().equals("Booked") || booking.getStatus().equals("Request"))
            getEndPointInfo(booking, inDriverModule);

        if((booking.getStatus().equals("Pending") || booking.getStatus().equals("Booked")) &&
                !inDriverModule) {
            List<OnlinePayment> onlinePaymentList = booking.getReferenceNumberList();
            for(OnlinePayment onlinePayment : onlinePaymentList) {
                boolean isNotified = onlinePayment.isNotified();
                if(!isNotified) {
                    showCreditedRNNotification(booking, onlinePayment);
                }
            }
        }
    }

    private void getEndPointInfo(Booking booking, boolean inDriverModule) {
        for(User user : users) {
            if(inDriverModule) {
                List<Booking> bookingList = user.getBookingList();
                for(Booking booking1 : bookingList) {
                    if(booking1.getId().equals(booking.getId())) {
                        String fullName = user.getLastName() + ", " + user.getFirstName();
                        if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();

                        List<Chat> chats = booking.getChats();
                        String message = booking1.getMessage();
                        if(chats.size() > 0) message = chats.get(chats.size()-1).getMessage();
                        boolean notified = booking.isNotified();

                        if(!notified && message.length() > 0)
                            showChatNotification(booking, fullName, message, true);
                        else {
                            usersRef.child(userId).child("taskList").child(booking.getId()).
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
                for(Booking task : taskList) {
                    if(task.getId().equals(booking.getId())) {
                        String fullName = user.getLastName() + ", " + user.getFirstName();
                        if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();

                        List<Chat> chats = task.getChats();
                        String message = "??????????????? (Hello), I am " + fullName + ", your assigned driver.";
                        if(chats.size() > 0) message = chats.get(chats.size()-1).getMessage();
                        boolean notified = booking.isNotified();

                        if(!notified && message.length() > 0)
                            showChatNotification(booking, fullName, message, false);
                        else {
                            usersRef.child(userId).child("bookingList").child(booking.getId()).
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

    private void setTaskStatusToFailed(Booking targetTask) {
        for (User user : users) {
            List<Booking> taskList = user.getTaskList();

            for(Booking task : taskList) {
                if(task.getId().equals(targetTask.getId()) && !task.getStatus().equals("Passed")) {
                    usersRef.child(user.getId()).child("taskList").
                            child(task.getId()).child("status").setValue("Failed");
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
            else if(hrDifference == 1 && minDifference == 0 && sec < 5) {
                if(inDriverModule) showUpcomingTaskNotification(booking, hrDifference, "hour");
                else showUpcomingBookingNotification(booking, hrDifference, "hour");
            }
        }
    }

    private boolean isToday(int bookingDay, int bookingMonth, int bookingYear) {
        return (bookingDay == calendarDay && bookingMonth == calendarMonth && bookingYear == calendarYear);
    }

    private void getBooking() {
        /*usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList1.clear();
                bookingList2.clear();
                taskList.clear();

                if(snapshot.exists()) {
                    User user = new User(snapshot);
                    List<Booking> bookingList = user.getBookingList();
                    for(Booking booking : bookingList) {
                        if(booking.getStatus().equals("Pending"))
                            bookingList1.add(booking);
                        if(booking.getStatus().equals("Booked"))
                            bookingList2.add(booking);
                    }
                    taskList.addAll(user.getTaskList());
                }

                startTimer();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/

        bookingList1.clear();
        bookingList2.clear();
        taskList.clear();

        for(User user : users) {
            if(user.getId().equals(userId)) {
                List<Booking> bookingList = user.getBookingList();
                for(Booking booking : bookingList) {
                    if(booking.getStatus().equals("Pending"))
                        bookingList1.add(booking);
                    if(booking.getStatus().equals("Booked"))
                        bookingList2.add(booking);
                }
                taskList.addAll(user.getTaskList());
                break;
            }
        }

        startTimer();
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
                        " before the schedule of your Booking (ID: " + booking.getId() +").")
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(false);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent = new Intent(myContext, RouteActivity.class);
        notificationIntent.putExtra("notificationUserId", userId);
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
        notificationIntent.putExtra("notificationUserId", userId);
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
        notificationIntent.putExtra("notificationUserId", userId);
        notificationIntent.putExtra("taskId", task.getId());
        notificationIntent.putExtra("inDriverModule", inDriverModule);

        if(inDriverModule) {
            String passengerUserId = getPassengerUserId(task.getId());
            notificationIntent.putExtra("passengerUserId", passengerUserId);
        }
        else {
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

    private void showFailedBookingNotification(Booking booking) {
        NotificationManager notificationManager = getNotificationManager(booking.getId());
        Bitmap icon = BitmapFactory.decodeResource(myResources, R.drawable.front_icon);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(myContext, booking.getId())
                        .setSmallIcon(R.drawable.front_icon).setLargeIcon(icon)
                        .setContentTitle("Clemenisle-EV Booking Status")
                        .setContentText("You failed to go to your Booking (ID: " + booking.getId() +").")
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(false);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent;

        if(booking.getBookingType().getId().equals("BT99"))
            notificationIntent = new Intent(myContext, OnTheSpotActivity.class);
        else {
            notificationIntent = new Intent(myContext, RouteActivity.class);
            notificationIntent.putExtra("isLatest", false);
        }

        notificationIntent.putExtra("notificationUserId", userId);
        notificationIntent.putExtra("bookingId", booking.getId());
        notificationIntent.putExtra("inDriverModule", false);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                myContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        builder.setFullScreenIntent(pendingIntent, true);
        notificationManager.notify(1, builder.build());
    }

    private void showCreditedRNNotification(Booking booking, OnlinePayment onlinePayment) {
        NotificationManager notificationManager = getNotificationManager(booking.getId());
        Bitmap icon = BitmapFactory.decodeResource(myResources, R.drawable.front_icon);

        String value = "???" + onlinePayment.getValue();
        if(value.split("\\.")[1].length() == 1) value += 0;

        String referenceNumberValue = onlinePayment.getReferenceNumber();
        String content = value + " has been credited to #" + referenceNumberValue + ".";
        if(referenceNumberValue == null) content = "You paid " + value + " in your booking.";

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

        Intent notificationIntent = new Intent(myContext, OnlinePaymentActivity.class);
        notificationIntent.putExtra("notificationUserId", userId);
        notificationIntent.putExtra("bookingId", booking.getId());

        PendingIntent pendingIntent = PendingIntent.getActivity(
                myContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        builder.setFullScreenIntent(pendingIntent, true);
        notificationManager.notify(1, builder.build());

        usersRef.child(userId).child("bookingList").child(booking.getId()).
                child("onlinePaymentList").child(onlinePayment.getId()).
                child("notified").setValue(true);
    }

    private void showSuccessfulTransactionNotification(IWalletTransaction transaction) {
        NotificationManager notificationManager = getNotificationManager(transaction.getId());
        Bitmap icon = BitmapFactory.decodeResource(myResources, R.drawable.front_icon);

        String value = "???" + transaction.getValue();
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

        Intent notificationIntent = new Intent(myContext, IWalletActivity.class);
        notificationIntent.putExtra("notificationUserId", userId);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notificationTimer.cancel();
        if(password != null) {
            if(!isCurrentPasswordValid() && !isPasswordUpdated) {
                firebaseAuth.signOut();
                sendLoginPreferences();

                Toast.makeText(
                        myContext,
                        "Your account has been logged out. Account logged out.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
        isAppStatusActivityShown = true;
        dialog = null;
    }

    private boolean isCurrentPasswordValid() {
        vCPWL = password.length() >= 8;

        vCPWU = password.matches(".*[A-Z].*");

        vCPWLw = password.matches(".*[a-z].*");

        vCPWN = password.matches(".*[0-9].*");

        vCPWS = password.matches("[A-Za-z0-9]*");

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

        pwLengthCheckImage.getDrawable().setTint(colorInitial);
        pwUpperCheckImage.getDrawable().setTint(colorInitial);
        pwLowerCheckImage.getDrawable().setTint(colorInitial);
        pwNumberCheckImage.getDrawable().setTint(colorInitial);
        pwSymbolCheckImage.getDrawable().setTint(colorInitial);

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
        finishAffinity();
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