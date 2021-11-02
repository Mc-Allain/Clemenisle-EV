package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Adapters.ChatAdapter;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.Chat;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    DatabaseReference bookingListRef, taskListRef;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ValueEventListener usersValueEventListener, taskValueEventListener;

    ConstraintLayout userInfoLayout, driverInfoLayout, messageInputLayout, chatStatusLayout;
    ImageView profileImage, driverProfileImage, sendImage;
    TextView tvUserFullName, tvDriverFullName, tvPlateNumber, tvChatStatus;
    RecyclerView chatView;

    ImageView openImage, openImage2;
    TextView tvOpen, tvOpen2;

    EditText etMessage;

    Context myContext;
    Resources myResources;

    List<Booking> bookingList = new ArrayList<>();

    int colorBlue, colorInitial;

    String taskId, userId, passengerUserId, driverUserId, passengerProfileImg,
            driverProfileImg, driverFullName, initialMessage;
    boolean isLoggedIn = false, inDriverModule = false;

    ChatAdapter chatAdapter;
    List<Chat> chats = new ArrayList<>();
    String bookingTimestamp, taskTimestamp;

    String message;

    String defaultChatStatusText = "This chat is now disabled. <b>Status</b>: ";

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userInfoLayout = findViewById(R.id.userInfoLayout);
        driverInfoLayout = findViewById(R.id.driverInfoLayout);
        profileImage = findViewById(R.id.profileImage);
        driverProfileImage = findViewById(R.id.driverProfileImage);
        tvUserFullName = findViewById(R.id.tvUserFullName);
        tvDriverFullName = findViewById(R.id.tvDriverFullName);
        tvPlateNumber = findViewById(R.id.tvPlateNumber);
        chatView = findViewById(R.id.chatView);

        messageInputLayout = findViewById(R.id.messageInputLayout);
        etMessage = findViewById(R.id.etMessage);
        sendImage = findViewById(R.id.sendImage);

        chatStatusLayout = findViewById(R.id.chatStatusLayout);
        tvChatStatus = findViewById(R.id.tvChatStatus);

        tvOpen = findViewById(R.id.tvOpen);
        openImage = findViewById(R.id.openImage);
        tvOpen2 = findViewById(R.id.tvOpen2);
        openImage2 = findViewById(R.id.openImage2);

        myContext = ChatActivity.this;
        myResources = getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);

        initSharedPreferences();

        Intent intent = getIntent();
        taskId = intent.getStringExtra("taskId");
        inDriverModule = intent.getBooleanExtra("inDriverModule", false);

        sendImage.setEnabled(false);
        sendImage.getDrawable().setTint(colorInitial);

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
                if(inDriverModule) driverUserId = firebaseUser.getUid();
                else {
                    driverUserId = intent.getStringExtra("driverUserId");
                    passengerUserId = firebaseUser.getUid();
                }
                userId = firebaseUser.getUid();
            }
        }

        getUsers();

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, true);
        chatView.setLayoutManager(linearLayout);
        chatAdapter = new ChatAdapter(myContext, chats, userId, inDriverModule);
        chatView.setAdapter(chatAdapter);

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                message = etMessage.getText().toString();

                if(message.length() > 0) {
                    sendImage.setEnabled(true);
                    sendImage.getDrawable().setTint(colorBlue);
                }
                else {
                    sendImage.setEnabled(false);
                    sendImage.getDrawable().setTint(colorInitial);
                }
            }
        });

        sendImage.setOnClickListener(view -> sendMessage());
    }

    private void openItem(Booking booking) {
        boolean isOnTheSpot = booking.getBookingType().getId().equals("BT99");

        Intent intent;

        if(isOnTheSpot) intent = new Intent(myContext, OnTheSpotActivity.class);
        else intent = new Intent(myContext, RouteActivity.class);

        intent.putExtra("bookingId", booking.getId());
        intent.putExtra("inDriverModule", inDriverModule);
        if(inDriverModule) {
            intent.putExtra("isScanning", false);
            intent.putExtra("status", booking.getStatus());
            intent.putExtra("previousDriverUserId", booking.getPreviousDriverUserId());
            intent.putExtra("userId", passengerUserId);
        }
        else {
            if(!isOnTheSpot) {
                boolean isLatest = bookingList.get(0).getId().equals(booking.getId()) &&
                        booking.getStatus().equals("Completed") &&
                        !booking.getBookingType().getId().equals("BT99");

                intent.putExtra("isLatest", isLatest);
            }
        }
        myContext.startActivity(intent);
    }

    private void sendMessage() {
        String value = message;
        message = "";
        etMessage.setText(message);

        String chatIdSuffix = String.valueOf(chats.size() + 1);
        if(chatIdSuffix.length() == 1) chatIdSuffix = "0" + chatIdSuffix;
        String chatId = "C" + chatIdSuffix;

        String schedule = new DateTimeToString().getDateAndTime();
        Chat chat = new Chat(chatId, userId, value, schedule);

        taskListRef.child("chats").child(chatId).setValue(chat);

        if(inDriverModule) {
            bookingListRef.child("notified").setValue(false);
            bookingListRef.child("read").setValue(false);
        }
        else {
            taskListRef.child("notified").setValue(false);
            taskListRef.child("read").setValue(false);
        }
    }

    private void getUsers() {
        usersValueEventListener = usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear();
                if(snapshot.exists()) {
                    userInfoLayout.setVisibility(View.GONE);
                    driverInfoLayout.setVisibility(View.GONE);

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);

                        if(user.getId().equals(userId)) {
                            bookingList.addAll(user.getBookingList());
                            List<Booking> taskList = user.getTaskList();

                            if(inDriverModule) {
                                for(Booking task : taskList) {
                                    if(task.getId().equals(taskId)) {
                                        tvOpen.setOnClickListener(view -> openItem(task));
                                        tvOpen2.setOnClickListener(view -> openItem(task));
                                        openImage.setOnClickListener(view -> openItem(task));
                                        openImage2.setOnClickListener(view -> openItem(task));
                                        break;
                                    }
                                }
                            }
                            else {
                                for(Booking booking : bookingList) {
                                    if(booking.getId().equals(taskId)) {
                                        tvOpen.setOnClickListener(view -> openItem(booking));
                                        tvOpen2.setOnClickListener(view -> openItem(booking));
                                        openImage.setOnClickListener(view -> openItem(booking));
                                        openImage2.setOnClickListener(view -> openItem(booking));
                                        break;
                                    }
                                }
                            }
                        }

                        if(user.getId().equals(driverUserId)) {
                            List<Booking> taskList = user.getTaskList();
                            for(Booking task : taskList) {
                                if(task.getId().equals(taskId)) {
                                    String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                                    if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();

                                    if(!inDriverModule) {
                                        tvDriverFullName.setText(fromHtml(fullName));

                                        String plateNumber = "<b>Plate Number</b>: " + user.getPlateNumber();
                                        tvPlateNumber.setText(fromHtml(plateNumber));

                                        try {
                                            Glide.with(myContext).load(user.getProfileImage())
                                                    .placeholder(R.drawable.image_loading_placeholder)
                                                    .into(driverProfileImage);
                                        }
                                        catch (Exception ignored) {}

                                        driverInfoLayout.setVisibility(View.VISIBLE);
                                    }

                                    driverUserId = user.getId();
                                    driverProfileImg = user.getProfileImage();
                                    driverFullName = fullName;
                                    initialMessage = task.getMessage();
                                    taskTimestamp = task.getTimestamp();

                                    break;
                                }
                            }
                        }

                        List<Booking> bookingList = user.getBookingList();
                        for(Booking booking : bookingList) {
                            if(booking.getId().equals(taskId)) {
                                String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                                if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();

                                if(inDriverModule) {
                                    tvUserFullName.setText(fromHtml(fullName));

                                    try {
                                        Glide.with(myContext).load(user.getProfileImage())
                                                .placeholder(R.drawable.image_loading_placeholder)
                                                .into(profileImage);
                                    }
                                    catch (Exception ignored) {}

                                    userInfoLayout.setVisibility(View.VISIBLE);
                                }

                                passengerUserId = user.getId();
                                passengerProfileImg = user.getProfileImage();
                                initialMessage = booking.getMessage();
                                bookingTimestamp = booking.getTimestamp();

                                break;
                            }
                        }
                    }
                }
                getChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getChats() {
        bookingListRef = usersRef.child(passengerUserId).child("bookingList").child(taskId);
        taskListRef = usersRef.child(driverUserId).child("taskList").child(taskId);

        if(inDriverModule) taskListRef.child("read").setValue(true);
        else bookingListRef.child("read").setValue(true);

        taskValueEventListener = taskListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats.clear();
                if(snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);

                    DataSnapshot chatSnapshot = snapshot.child("chats");
                    for(DataSnapshot dataSnapshot : chatSnapshot.getChildren()) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        chats.add(chat);
                    }

                    messageInputLayout.setVisibility(View.GONE);
                    chatStatusLayout.setVisibility(View.GONE);

                    if(status != null && (status.equals("Booked") || status.equals("Request")))
                        messageInputLayout.setVisibility(View.VISIBLE);
                    else if(status != null) {
                        chatStatusLayout.setVisibility(View.VISIBLE);
                        String chatStatus = defaultChatStatusText + status;
                        tvChatStatus.setText(fromHtml(chatStatus));

                        int color = 0;

                        switch (status) {
                            case "Pending":
                                color = myResources.getColor(R.color.orange);
                                break;
                            case "Completed":
                                color = myResources.getColor(R.color.blue);
                                break;
                            case "Passed":
                            case "Cancelled":
                            case "Failed":
                                color = myResources.getColor(R.color.red);
                                break;
                        }

                        chatStatusLayout.setBackgroundColor(color);
                    }

                    Collections.reverse(chats);
                    chatAdapter.setValues(passengerUserId, driverUserId, passengerProfileImg, driverProfileImg,
                            driverFullName, initialMessage, bookingTimestamp, taskTimestamp, status);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        usersRef.removeEventListener(usersValueEventListener);
        taskListRef.removeEventListener(taskValueEventListener);
    }
}