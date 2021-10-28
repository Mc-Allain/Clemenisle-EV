package com.example.firebase_clemenisle_ev.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DriverTaskListFragment extends Fragment {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout constraintLayout,
            constraintLayout1, constraintLayout2, constraintLayout3, constraintLayout4;

    RecyclerView upcomingView, requestView, completedView, failedView;
    ConstraintLayout statusLayout1, statusLayout2, statusLayout3, statusLayout4;
    ImageView upcomingArrow, requestArrow, completedArrow, failedArrow, reloadImage;
    TextView badgeText1, badgeText2, badgeText3, badgeText4;

    ProgressBar progressBar;

    int clickedIndex;
    TextView tvLog;

    Context myContext;
    Resources myResources;

    String defaultLogText = "No Record";

    BookingAdapter adapter1, adapter2, adapter3, adapter4;

    List<Booking> taskList1 = new ArrayList<>();
    List<Booking> taskList2 = new ArrayList<>();
    List<Booking> taskList3 = new ArrayList<>();
    List<Booking> taskList4 = new ArrayList<>();

    boolean success1, success2, success3, success4;

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
        editor.putBoolean("remember", false);
        editor.putString("emailAddress", null);
        editor.putString("password", null);
        editor.apply();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_task_list, container, false);

        constraintLayout = view.findViewById(R.id.constraintLayout);
        constraintLayout1 = view.findViewById(R.id.constraintLayout1);
        constraintLayout2 = view.findViewById(R.id.constraintLayout2);
        constraintLayout3 = view.findViewById(R.id.constraintLayout3);
        constraintLayout4 = view.findViewById(R.id.constraintLayout4);

        upcomingView = view.findViewById(R.id.upcomingView);
        statusLayout1 = view.findViewById(R.id.statusLayout1);
        upcomingArrow = view.findViewById(R.id.upcomingArrowImage);
        badgeText1 = view.findViewById(R.id.tvBadge1);

        requestView = view.findViewById(R.id.requestView);
        statusLayout2 = view.findViewById(R.id.statusLayout2);
        requestArrow = view.findViewById(R.id.requestArrowImage);
        badgeText2 = view.findViewById(R.id.tvBadge2);

        completedView = view.findViewById(R.id.completedView);
        statusLayout3 = view.findViewById(R.id.statusLayout3);
        completedArrow = view.findViewById(R.id.completedArrowImage);
        badgeText3 = view.findViewById(R.id.tvBadge3);

        failedView = view.findViewById(R.id.failedView);
        statusLayout4 = view.findViewById(R.id.statusLayout4);
        failedArrow = view.findViewById(R.id.failedArrowImage);
        badgeText4 = view.findViewById(R.id.tvBadge4);

        tvLog = view.findViewById(R.id.tvLog);
        reloadImage = view.findViewById(R.id.reloadImage);
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
            else {
                userId = firebaseUser.getUid();
            }
        }

        try {
            Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage);
        }
        catch (Exception ignored) {}

        LinearLayoutManager linearLayout1 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        upcomingView.setLayoutManager(linearLayout1);
        adapter1 = new BookingAdapter(myContext, taskList1);
        upcomingView.setAdapter(adapter1);

        LinearLayoutManager linearLayout2 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        requestView.setLayoutManager(linearLayout2);
        adapter2 = new BookingAdapter(myContext, taskList2);
        requestView.setAdapter(adapter2);

        LinearLayoutManager linearLayout3 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        completedView.setLayoutManager(linearLayout3);
        adapter3 = new BookingAdapter(myContext, taskList3);
        completedView.setAdapter(adapter3);

        LinearLayoutManager linearLayout4 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        failedView.setLayoutManager(linearLayout4);
        adapter4 = new BookingAdapter(myContext, taskList4);
        failedView.setAdapter(adapter4);

        getBookings();

        statusLayout1.setOnClickListener(view1 -> {
            if(upcomingView.getVisibility() == View.VISIBLE) {
                setViewsToGone();
                resetConstraint();
            }
            else {
                setViewsToGone();
                upcomingView.setVisibility(View.VISIBLE);
                upcomingArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);

                clickedIndex = 1;
                resetConstraint();
                connectConstraintBottom();
            }
        });

        statusLayout2.setOnClickListener(view1 -> {
            if(requestView.getVisibility() == View.VISIBLE) {
                setViewsToGone();
                resetConstraint();
            }
            else {
                setViewsToGone();
                requestView.setVisibility(View.VISIBLE);
                requestArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);

                clickedIndex = 2;
                resetConstraint();
                connectConstraintBottom();
            }
        });

        statusLayout3.setOnClickListener(view1 -> {
            if(completedView.getVisibility() == View.VISIBLE) {
                setViewsToGone();
                resetConstraint();
            }
            else {
                setViewsToGone();
                completedView.setVisibility(View.VISIBLE);
                completedArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);

                clickedIndex = 3;
                resetConstraint();
                connectConstraintBottom();
            }
        });

        statusLayout4.setOnClickListener(view1 -> {
            if(failedView.getVisibility() == View.VISIBLE) {
                setViewsToGone();
                resetConstraint();
            }
            else {
                setViewsToGone();
                failedView.setVisibility(View.VISIBLE);
                failedArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);

                clickedIndex = 4;
                resetConstraint();
                connectConstraintBottom();
            }
        });

        return view;
    }

    private void connectConstraintBottom() {
        int count = 0;

        switch (clickedIndex) {
            case 1:
                count = Integer.parseInt(badgeText1.getText().toString());
                break;
            case 2:
                count = Integer.parseInt(badgeText2.getText().toString());
                break;
            case 3:
                count = Integer.parseInt(badgeText3.getText().toString());
                break;
            case 4:
                count = Integer.parseInt(badgeText4.getText().toString());
                break;
        }

        if(count == 0) {
            return;
        }

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        constraintSet.connect(constraintLayout4.getId(), ConstraintSet.BOTTOM,
                constraintLayout.getId(), ConstraintSet.BOTTOM);

        if(clickedIndex <= 3) {
            constraintSet.clear(constraintLayout4.getId(), ConstraintSet.TOP);

            constraintSet.connect(constraintLayout3.getId(), ConstraintSet.BOTTOM,
                    constraintLayout4.getId(), ConstraintSet.TOP);
        }

        if(clickedIndex <= 2) {
            constraintSet.clear(constraintLayout3.getId(), ConstraintSet.TOP);

            constraintSet.connect(constraintLayout2.getId(), ConstraintSet.BOTTOM,
                    constraintLayout3.getId(), ConstraintSet.TOP);
        }

        if(clickedIndex <= 1) {
            constraintSet.clear(constraintLayout2.getId(), ConstraintSet.TOP);

            constraintSet.connect(constraintLayout1.getId(), ConstraintSet.BOTTOM,
                    constraintLayout2.getId(), ConstraintSet.TOP);
        }

        setTransition();
        constraintSet.applyTo(constraintLayout);
    }

    private void resetConstraint() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        constraintSet.clear(constraintLayout4.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(constraintLayout4.getId(), ConstraintSet.TOP,
                constraintLayout3.getId(), ConstraintSet.BOTTOM);

        constraintSet.clear(constraintLayout3.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(constraintLayout3.getId(), ConstraintSet.TOP,
                constraintLayout2.getId(), ConstraintSet.BOTTOM);

        constraintSet.clear(constraintLayout2.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(constraintLayout2.getId(), ConstraintSet.TOP,
                constraintLayout1.getId(), ConstraintSet.BOTTOM);

        constraintSet.clear(constraintLayout1.getId(), ConstraintSet.BOTTOM);

        setTransition();
        constraintSet.applyTo(constraintLayout);
    }

    private void setTransition() {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);
        TransitionManager.beginDelayedTransition(constraintLayout, transition);
    }

    private void setViewsToGone() {
        clickedIndex = 0;

        upcomingView.setVisibility(View.GONE);
        requestView.setVisibility(View.GONE);
        completedView.setVisibility(View.GONE);
        failedView.setVisibility(View.GONE);

        upcomingArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
        requestArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
        completedArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
        failedArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
    }

    private void setViewsToVisible() {
        switch (clickedIndex) {
            case 1:
                upcomingView.setVisibility(View.VISIBLE);
                break;
            case 2:
                requestView.setVisibility(View.VISIBLE);
                break;
            case 3:
                completedView.setVisibility(View.VISIBLE);
                break;
            case 4:
                failedView.setVisibility(View.VISIBLE);
                break;
        }

        resetConstraint();
        connectConstraintBottom();
    }

    private void setScreenEnabled(boolean value) {
        statusLayout1.setEnabled(value);
        statusLayout2.setEnabled(value);
        statusLayout3.setEnabled(value);
        statusLayout4.setEnabled(value);
    }

    private void getBookings() {
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        setViewsToGone();
        resetConstraint();
        setScreenEnabled(false);

        Query task1Query = firebaseDatabase.getReference("users").
                child(userId).child("taskList").orderByChild("status").equalTo("Booked");

        success1 = false;
        task1Query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList1.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Booking task = new Booking(dataSnapshot);
                        taskList1.add(task);
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
                        Toast.LENGTH_LONG
                ).show();

                success1 = false;
                errorLoading(error.toString());
            }
        });

        Query task2Query = firebaseDatabase.getReference("users").
                child(userId).child("taskList").orderByChild("status").equalTo("Request");

        success2 = false;
        task2Query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Booking> taskList = new ArrayList<>();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Booking task = new Booking(dataSnapshot);
                        taskList.add(task);
                    }
                }

                Query driverQuery = firebaseDatabase.getReference("users").
                        orderByChild("driver").equalTo(true);

                driverQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        taskList2.clear();
                        taskList2.addAll(taskList);

                        if(snapshot.exists()) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                User user = new User(dataSnapshot);
                                List<Booking> userTaskList = user.getTaskList();

                                for(Booking task : userTaskList) {
                                    if(task.getStatus().equals("Request") &&
                                            !user.getId().equals(userId))
                                        taskList2.add(task);
                                }
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
                                Toast.LENGTH_LONG
                        ).show();

                        success2 = false;
                        errorLoading(error.toString());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                success2 = false;
                errorLoading(error.toString());
            }
        });

        Query task3Query = firebaseDatabase.getReference("users").
                child(userId).child("taskList").orderByChild("status").equalTo("Completed");

        success3 = false;
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
                success3 = true;
                Collections.reverse(taskList3);
                finishLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                success3 = false;
                errorLoading(error.toString());
            }
        });

        Query task4Query = firebaseDatabase.getReference("users").
                child(userId).child("taskList").orderByChild("status").equalTo("Failed");

        success4 = false;
        task4Query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList4.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Booking task = new Booking(dataSnapshot);
                        taskList4.add(task);
                    }
                }
                success4 = true;
                Collections.reverse(taskList4);
                finishLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                success4 = false;
                errorLoading(error.toString());
            }
        });
    }

    private void finishLoading() {
        if(success1 && success2 && success3 && success4) {
            updateAdapter();

            progressBar.setVisibility(View.GONE);

            setScreenEnabled(true);

            badgeText1.setText(String.valueOf(taskList1.size()));
            badgeText2.setText(String.valueOf(taskList2.size()));
            badgeText3.setText(String.valueOf(taskList3.size()));
            badgeText4.setText(String.valueOf(taskList4.size()));

            if(taskList1.size() + taskList2.size() +
                    taskList3.size() + taskList4.size() == 0) {
                tvLog.setText(defaultLogText);
                tvLog.setVisibility(View.VISIBLE);
                reloadImage.setVisibility(View.VISIBLE);
            }
            else {
                tvLog.setVisibility(View.GONE);
                reloadImage.setVisibility(View.GONE);
            }

            setViewsToVisible();
        }
    }

    private void errorLoading(String error) {
        if(!(success1 && success2 && success3 && success4)) {
            taskList1.clear();
            taskList2.clear();
            taskList3.clear();
            taskList4.clear();
            updateAdapter();

            tvLog.setText(error);
            tvLog.setVisibility(View.VISIBLE);
            reloadImage.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void updateAdapter() {
        adapter1.setInDriverMode(true);
        adapter2.setInDriverMode(true);
        adapter3.setInDriverMode(true);
        adapter4.setInDriverMode(true);
    }
}