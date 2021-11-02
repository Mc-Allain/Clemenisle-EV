package com.example.firebase_clemenisle_ev.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.ChatListActivity;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class BookingListFragment extends Fragment {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ImageView chatImage;
    TextView tvBadge;

    Context myContext;

    boolean isLoggedIn = false;
    String userId;
    int newChats = 0;

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

    private void sendSharedPreferences() {
        SharedPreferences sharedPreferences =
                myContext.getSharedPreferences("nav", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("mode", 1);
        editor.apply();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_booking_list, container, false);
        chatImage = view.findViewById(R.id.chatImage);
        tvBadge = view.findViewById(R.id.tvBadge);

        myContext = inflater.getContext();

        initSharedPreferences();
        sendSharedPreferences();

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

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction;

        if(isLoggedIn) {
            fragmentTransaction = fragmentManager.beginTransaction().
                    replace(R.id.bookingListFragmentContainer, new LoggedInBookingListFragment(), null);
            chatImage.setVisibility(View.VISIBLE);
            tvBadge.setVisibility(View.VISIBLE);
            if(userId != null) getUsers();
        }
        else {
            fragmentTransaction = fragmentManager.beginTransaction().
                    replace(R.id.bookingListFragmentContainer, new LoginPromptFragment(), null);
            chatImage.setVisibility(View.GONE);
            tvBadge.setVisibility(View.GONE);
        }
        fragmentTransaction.commit();

        chatImage.setOnClickListener(view1 -> {
            Intent intent = new Intent(myContext, ChatListActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void getUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();
                newChats = 0;

                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);
                        users.add(user);
                    }
                }
                getBookingList(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getBookingList(List<User> users) {
        List<Booking> bookingList = new ArrayList<>(), taskList = new ArrayList<>();

        for(User user : users) {
            if(user.getId().equals(userId)) {
                bookingList.addAll(user.getBookingList());
                taskList.addAll(user.getTaskList());
            }
        }
        getChatList(bookingList, taskList, users);
    }

    private void getChatList(List<Booking> bookingList, List<Booking> taskList,
                             List<User> users) {
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
}