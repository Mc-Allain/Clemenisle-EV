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
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout userInfoLayout, driverInfoLayout;
    ImageView profileImage, driverProfileImage, sendImage;
    TextView tvUserFullName, tvDriverFullName, tvDriverPlateNo;
    RecyclerView chatView;

    EditText etMessage;

    Context myContext;
    Resources myResources;

    int colorBlue, colorInitial;

    String bookingId, userId, passengerUserId, driverUserId, passengerProfileImg,
            driverProfileImg, driverFullName, initialMessage;
    boolean isLoggedIn = false, inDriverModule = false;

    ChatAdapter chatAdapter;
    List<Chat> chats = new ArrayList<>();
    String bookingTimestamp, taskTimestamp;

    String message;


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
        tvDriverPlateNo = findViewById(R.id.tvDriverPlateNo);
        chatView = findViewById(R.id.chatView);

        etMessage = findViewById(R.id.etMessage);
        sendImage = findViewById(R.id.sendImage);

        myContext = ChatActivity.this;
        myResources = getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);

        initSharedPreferences();

        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");
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
                else passengerUserId = firebaseUser.getUid();
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

    private void sendMessage() {
        String value = message;
        message = "";
        etMessage.setText(message);

        String chatIdSuffix = String.valueOf(chats.size() + 1);
        if(chatIdSuffix.length() == 1) chatIdSuffix = "0" + chatIdSuffix;
        String chatId = "C" + chatIdSuffix;

        String schedule = new DateTimeToString().getDateAndTime();
        Chat chat = new Chat(chatId, userId, value, schedule);

        DatabaseReference bookingListRef = usersRef.child(passengerUserId).child("bookingList").child(bookingId),
        taskListRef = usersRef.child(driverUserId).child("taskList").child(bookingId);

        bookingListRef.child("chats").child(chatId).setValue(chat);

        if(inDriverModule) bookingListRef.child("notified").setValue(false);
        else taskListRef.child("notified").setValue(false);
    }

    private void getUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);

                        if(!inDriverModule) {
                            List<Booking> taskList = user.getTaskList();
                            for(Booking booking : taskList) {
                                if(booking.getId().equals(bookingId)) {
                                    String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                                    if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
                                    tvDriverFullName.setText(fromHtml(fullName));

                                    String plateNo = "<b>Plate Number</b>: " + user.getPlateNumber();
                                    tvDriverPlateNo.setText(fromHtml(plateNo));

                                    try {
                                        Glide.with(myContext).load(user.getProfileImage())
                                                .placeholder(R.drawable.image_loading_placeholder)
                                                .into(driverProfileImage);
                                    }
                                    catch (Exception ignored) {}

                                    driverInfoLayout.setVisibility(View.VISIBLE);
                                    userInfoLayout.setVisibility(View.GONE);

                                    driverUserId = user.getId();
                                    driverProfileImg = user.getProfileImage();
                                    driverFullName = fullName;
                                    initialMessage = booking.getMessage();
                                    taskTimestamp = booking.getTimestamp();

                                    break;
                                }
                            }
                        }

                        if(inDriverModule) {
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

                                    userInfoLayout.setVisibility(View.VISIBLE);
                                    driverInfoLayout.setVisibility(View.GONE);

                                    passengerUserId = user.getId();
                                    passengerProfileImg = user.getProfileImage();
                                    initialMessage = booking.getMessage();
                                    bookingTimestamp = booking.getTimestamp();

                                    break;
                                }
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
        usersRef.child(passengerUserId).child("bookingList").child(bookingId).child("chats")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        chats.add(chat);
                    }
                }

                Collections.reverse(chats);
                chatAdapter.setValues(passengerUserId, driverUserId, passengerProfileImg, driverProfileImg,
                        driverFullName, initialMessage, bookingTimestamp, taskTimestamp);
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
}