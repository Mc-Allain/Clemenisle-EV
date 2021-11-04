package com.example.firebase_clemenisle_ev.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Adapters.BookingAdapter;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PendingListFragment extends Fragment implements BookingAdapter.OnActionClickListener {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    TextView tvLog;
    ImageView reloadImage;
    RecyclerView bookingView;
    ProgressBar progressBar;

    Context myContext;
    Resources myResources;

    String defaultLogText = "No Pending Booking Record";

    BookingAdapter bookingAdapter;

    List<Booking> pendingBookingList = new ArrayList<>(),
            isNotOnTheSpotBookingList = new ArrayList<>(),
            isNotPaidBookingList = new ArrayList<>(),
            taskList3 = new ArrayList<>();

    String userId;
    boolean isLoggedIn = false;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pending_list, container, false);

        tvLog = view.findViewById(R.id.tvLog);
        reloadImage = view.findViewById(R.id.reloadImage);
        bookingView = view.findViewById(R.id.bookingView);
        progressBar = view.findViewById(R.id.progressBar);

        myContext = getContext();
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
                        "Failed to get the current user",
                        Toast.LENGTH_LONG
                ).show();
            }
            else userId = firebaseUser.getUid();
        }

        try {
            Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage);
        }
        catch (Exception ignored) {}

        LinearLayoutManager linearLayout1 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        bookingView.setLayoutManager(linearLayout1);
        bookingAdapter = new BookingAdapter(myContext, pendingBookingList);
        bookingView.setAdapter(bookingAdapter);
        bookingAdapter.setOnLikeClickListener(this);

        getPendingBooking();

        return view;
    }

    private void getPendingBooking() {
        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference usersRef = firebaseDatabase.getReference("users");

        Query task3Query = usersRef.child(userId).child("taskList").
                orderByChild("status").equalTo("Ongoing");

        task3Query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList3.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Booking task = new Booking(dataSnapshot);
                        taskList3.add(task);
                    }
                }
                bookingAdapter.setOnGoingTaskList(taskList3);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                taskList3.clear();
                bookingAdapter.setOnGoingTaskList(taskList3);
            }
        });

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pendingBookingList.clear();
                isNotOnTheSpotBookingList.clear();
                isNotPaidBookingList.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);
                        List<Booking> bookingList = user.getBookingList();

                        for(Booking booking : bookingList) {
                            if(booking.getStatus().equals("Pending"))
                                if(booking.getBookingType().getId().equals("BT99"))
                                    pendingBookingList.add(booking);
                                else if(booking.isPaid()) isNotOnTheSpotBookingList.add(booking);
                                else isNotPaidBookingList.add(booking);
                        }
                    }
                }

                Collections.sort(isNotPaidBookingList, (booking, t1) ->
                        booking.getId().compareToIgnoreCase(t1.getId()));

                Collections.sort(isNotOnTheSpotBookingList, (booking, t1) ->
                        booking.getId().compareToIgnoreCase(t1.getId()));

                Collections.sort(pendingBookingList, (booking, t1) ->
                        booking.getId().compareToIgnoreCase(t1.getId()));

                pendingBookingList.addAll(isNotOnTheSpotBookingList);
                pendingBookingList.addAll(isNotPaidBookingList);

                if(pendingBookingList.size() > 0) finishLoading();
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

    private void finishLoading() {
        bookingAdapter.setInDriverMode(true);

        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        bookingView.setVisibility(View.VISIBLE);
    }

    private void errorLoading(String error) {
        pendingBookingList.clear();
        isNotOnTheSpotBookingList.clear();
        isNotPaidBookingList.clear();
        bookingAdapter.notifyDataSetChanged();

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        bookingView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setProgressBarToVisible(boolean value) {
        if(value) progressBar.setVisibility(View.VISIBLE);
        else progressBar.setVisibility(View.GONE);
    }
}