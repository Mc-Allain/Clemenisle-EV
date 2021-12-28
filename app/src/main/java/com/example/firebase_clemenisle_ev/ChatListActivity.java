package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Adapters.ChatListAdapter;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatListActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    RecyclerView chatListView;
    TextView tvLog;
    ImageView reloadImage;
    ProgressBar progressBar;

    Context myContext;
    Resources myResources;

    List<User> users = new ArrayList<>();
    List<Booking> bookingList = new ArrayList<>();
    List<Booking> taskList = new ArrayList<>();
    List<Chat> chatList = new ArrayList<>();
    ChatListAdapter chatListAdapter;

    String userId;
    StringBuilder userFullName;

    boolean isLoggedIn = false;

    String defaultLogText = "No Chat Record";

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
        setContentView(R.layout.activity_chat_list);

        chatListView = findViewById(R.id.chatListView);
        tvLog = findViewById(R.id.tvLog);
        progressBar = findViewById(R.id.progressBar);
        reloadImage = findViewById(R.id.reloadImage);

        myContext = ChatListActivity.this;
        myResources = getResources();

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
                        "Failed to get the current user. Account logged out.",
                        Toast.LENGTH_LONG
                ).show();
            }
            else userId = firebaseUser.getUid();
        }

        try {
            Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage);
        }
        catch (Exception ignored) {}

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        chatListView.setLayoutManager(linearLayout);
        chatListAdapter = new ChatListAdapter(myContext, chatList, users, bookingList, userId);
        chatListView.setAdapter(chatListAdapter);

        getUsers();
    }

    private void getUsers() {
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        chatListView.setVisibility(View.INVISIBLE);

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
                getBookingList();
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

    private void getBookingList() {
        bookingList.clear();
        taskList.clear();

        for(User user : users) {
            if(user.getId().equals(userId)) {
                bookingList.addAll(user.getBookingList());
                taskList.addAll(user.getTaskList());

                userFullName = new StringBuilder(user.getLastName() + ", " + user.getFirstName());
                if(user.getMiddleName().length() > 0)
                    userFullName.append(" ").append(user.getMiddleName());
            }
        }
        getChatList();
    }

    private void getChatList() {
        List<Chat> bookingChatList = new ArrayList<>();
        for(Booking booking : bookingList) {
            for(User user : users) {
                List<Booking> taskList = user.getTaskList();
                for(Booking task : taskList) {
                    if(booking.getId().equals(task.getId())) {
                        List<Chat> chats = task.getChats();
                        Chat chat;

                        if(chats.size() > 0) chat = chats.get(chats.size() - 1);
                        else {
                            String fullName = user.getLastName() + ", " + user.getFirstName();
                            if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
                            String message = "こんにちは (Hello), I am " + fullName + ", your assigned driver.";

                            chat = new Chat("C01", user.getId(), message, task.getTimestamp());
                        }

                        chat.setEndPointUserId(user.getId());
                        chat.setDriverUserId(user.getId());
                        chat.setStatus(task.getStatus());
                        chat.setBooking(booking);

                        bookingChatList.add(chat);
                        break;
                    }
                }
            }
        }

        chatList.clear();
        chatList.addAll(bookingChatList);
        for(Booking task : taskList) {
            for(User user : users) {
                List<Booking> bookingList = user.getBookingList();
                for(Booking booking : bookingList) {
                    if(task.getId().equals(booking.getId())) {
                        List<Chat> chats = task.getChats();
                        Chat chat;

                        if(chats.size() > 0) chat = chats.get(chats.size() - 1);
                        else {
                            String message = "こんにちは (Hello), I am " + userFullName.toString() +
                                    ", your assigned driver.";
                            chat = new Chat("C01", userId, message, task.getTimestamp());
                        }

                        chat.setEndPointUserId(user.getId());
                        chat.setDriverUserId(userId);
                        chat.setStatus(task.getStatus());
                        chat.setBooking(task);

                        chatList.add(chat);
                        break;
                    }
                }
            }
        }

        Collections.sort(chatList, (chat, t1) -> {
            DateTimeToString dateTimeToString = new DateTimeToString();
            dateTimeToString.setFormattedSchedule(chat.getTimestamp());
            String chatTS = dateTimeToString.getDateNo(true) + " " +
                    dateTimeToString.getTime(true);
            dateTimeToString.setFormattedSchedule(t1.getTimestamp());
            String chatTS1 = dateTimeToString.getDateNo(true) + " " +
                    dateTimeToString.getTime(true);

            return chatTS1.compareToIgnoreCase(chatTS);
        });

        if(chatList.size() > 0) finishLoading();
        else errorLoading(defaultLogText);
    }

    private void finishLoading() {
        chatListAdapter.notifyDataSetChanged();

        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        chatListView.setVisibility(View.VISIBLE);
    }

    private void errorLoading(String error) {
        chatList.clear();
        chatListAdapter.notifyDataSetChanged();

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        chatListView.setVisibility(View.INVISIBLE);
    }
}