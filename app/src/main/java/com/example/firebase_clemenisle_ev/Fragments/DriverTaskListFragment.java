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

public class DriverTaskListFragment extends Fragment implements BookingAdapter.OnActionClickListener {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout constraintLayout,
            constraintLayout1, constraintLayout2, constraintLayout3, constraintLayout4, constraintLayout5, constraintLayout6;

    RecyclerView upcomingView, requestView, ongoingView, completedView, passedView, failedView;
    ConstraintLayout statusLayout1, statusLayout2, statusLayout3, statusLayout4, statusLayout5, statusLayout6;
    ImageView upcomingArrow, requestArrow, ongoingArrow, completedArrow, passedArrow, failedArrow, reloadImage;
    TextView badgeText1, badgeText2, badgeText3, badgeText4, badgeText5, badgeText6;

    ProgressBar progressBar;

    int clickedIndex;
    TextView tvLog;

    Context myContext;
    Resources myResources;

    String defaultLogText = "No Record";

    BookingAdapter adapter1, adapter2, adapter3, adapter4, adapter5, adapter6;

    List<Booking> taskList1 = new ArrayList<>();
    List<Booking> taskList2 = new ArrayList<>();
    List<Booking> taskList3 = new ArrayList<>();
    List<Booking> taskList4 = new ArrayList<>();
    List<Booking> taskList5 = new ArrayList<>();
    List<Booking> taskList6 = new ArrayList<>();

    boolean success1, success2, success3, success4, success5, success6;

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

        View view = inflater.inflate(R.layout.fragment_driver_task_list, container, false);

        constraintLayout = view.findViewById(R.id.constraintLayout);
        constraintLayout1 = view.findViewById(R.id.constraintLayout1);
        constraintLayout2 = view.findViewById(R.id.constraintLayout2);
        constraintLayout3 = view.findViewById(R.id.constraintLayout3);
        constraintLayout4 = view.findViewById(R.id.constraintLayout4);
        constraintLayout5 = view.findViewById(R.id.constraintLayout5);
        constraintLayout6 = view.findViewById(R.id.constraintLayout6);

        upcomingView = view.findViewById(R.id.upcomingView);
        statusLayout1 = view.findViewById(R.id.statusLayout1);
        upcomingArrow = view.findViewById(R.id.upcomingArrowImage);
        badgeText1 = view.findViewById(R.id.tvBadge1);

        requestView = view.findViewById(R.id.requestView);
        statusLayout2 = view.findViewById(R.id.statusLayout2);
        requestArrow = view.findViewById(R.id.requestArrowImage);
        badgeText2 = view.findViewById(R.id.tvBadge2);

        ongoingView = view.findViewById(R.id.ongoingView);
        statusLayout3 = view.findViewById(R.id.statusLayout3);
        ongoingArrow = view.findViewById(R.id.ongoingArrowImage);
        badgeText3 = view.findViewById(R.id.tvBadge3);

        completedView = view.findViewById(R.id.completedView);
        statusLayout4 = view.findViewById(R.id.statusLayout4);
        completedArrow = view.findViewById(R.id.completedArrowImage);
        badgeText4 = view.findViewById(R.id.tvBadge4);

        passedView = view.findViewById(R.id.passedView);
        statusLayout5 = view.findViewById(R.id.statusLayout5);
        passedArrow = view.findViewById(R.id.passedArrowImage);
        badgeText5 = view.findViewById(R.id.tvBadge5);

        failedView = view.findViewById(R.id.failedView);
        statusLayout6 = view.findViewById(R.id.statusLayout6);
        failedArrow = view.findViewById(R.id.failedArrowImage);
        badgeText6 = view.findViewById(R.id.tvBadge6);

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
            else userId = firebaseUser.getUid();
        }

        try {
            Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage);
        }
        catch (Exception ignored) {}

        LinearLayoutManager linearLayout1 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        upcomingView.setLayoutManager(linearLayout1);
        adapter1 = new BookingAdapter(myContext, taskList1);
        upcomingView.setAdapter(adapter1);
        adapter1.setOnLikeClickListener(this);

        LinearLayoutManager linearLayout2 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        requestView.setLayoutManager(linearLayout2);
        adapter2 = new BookingAdapter(myContext, taskList2);
        requestView.setAdapter(adapter2);
        adapter2.setOnLikeClickListener(this);
        adapter2.setOngoingTaskList(taskList3);

        LinearLayoutManager linearLayout3 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        ongoingView.setLayoutManager(linearLayout3);
        adapter3 = new BookingAdapter(myContext, taskList3);
        ongoingView.setAdapter(adapter3);
        adapter3.setOnLikeClickListener(this);

        LinearLayoutManager linearLayout4 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        completedView.setLayoutManager(linearLayout4);
        adapter4 = new BookingAdapter(myContext, taskList4);
        completedView.setAdapter(adapter4);
        adapter4.setOnLikeClickListener(this);

        LinearLayoutManager linearLayout5 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        passedView.setLayoutManager(linearLayout5);
        adapter5 = new BookingAdapter(myContext, taskList5);
        passedView.setAdapter(adapter5);
        adapter5.setOnLikeClickListener(this);

        LinearLayoutManager linearLayout6 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        failedView.setLayoutManager(linearLayout6);
        adapter6 = new BookingAdapter(myContext, taskList6);
        failedView.setAdapter(adapter6);
        adapter6.setOnLikeClickListener(this);

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
            if(ongoingView.getVisibility() == View.VISIBLE) {
                setViewsToGone();
                resetConstraint();
            }
            else {
                setViewsToGone();
                ongoingView.setVisibility(View.VISIBLE);
                ongoingArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);

                clickedIndex = 3;
                resetConstraint();
                connectConstraintBottom();
            }
        });

        statusLayout4.setOnClickListener(view1 -> {
            if(completedView.getVisibility() == View.VISIBLE) {
                setViewsToGone();
                resetConstraint();
            }
            else {
                setViewsToGone();
                completedView.setVisibility(View.VISIBLE);
                completedArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);

                clickedIndex = 4;
                resetConstraint();
                connectConstraintBottom();
            }
        });

        statusLayout5.setOnClickListener(view1 -> {
            if(passedView.getVisibility() == View.VISIBLE) {
                setViewsToGone();
                resetConstraint();
            }
            else {
                setViewsToGone();
                passedView.setVisibility(View.VISIBLE);
                passedArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);

                clickedIndex = 5;
                resetConstraint();
                connectConstraintBottom();
            }
        });

        statusLayout6.setOnClickListener(view1 -> {
            if(failedView.getVisibility() == View.VISIBLE) {
                setViewsToGone();
                resetConstraint();
            }
            else {
                setViewsToGone();
                failedView.setVisibility(View.VISIBLE);
                failedArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);

                clickedIndex = 6;
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
            case 5:
                count = Integer.parseInt(badgeText5.getText().toString());
                break;
            case 6:
                count = Integer.parseInt(badgeText6.getText().toString());
                break;
        }

        if(count == 0) {
            return;
        }

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        constraintSet.connect(constraintLayout6.getId(), ConstraintSet.BOTTOM,
                constraintLayout.getId(), ConstraintSet.BOTTOM);

        if(clickedIndex <= 5) {
            constraintSet.clear(constraintLayout6.getId(), ConstraintSet.TOP);

            constraintSet.connect(constraintLayout5.getId(), ConstraintSet.BOTTOM,
                    constraintLayout6.getId(), ConstraintSet.TOP);
        }

        if(clickedIndex <= 4) {
            constraintSet.clear(constraintLayout5.getId(), ConstraintSet.TOP);

            constraintSet.connect(constraintLayout4.getId(), ConstraintSet.BOTTOM,
                    constraintLayout5.getId(), ConstraintSet.TOP);
        }

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

        constraintSet.clear(constraintLayout6.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(constraintLayout6.getId(), ConstraintSet.TOP,
                constraintLayout5.getId(), ConstraintSet.BOTTOM);

        constraintSet.clear(constraintLayout5.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(constraintLayout5.getId(), ConstraintSet.TOP,
                constraintLayout4.getId(), ConstraintSet.BOTTOM);

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
        ongoingView.setVisibility(View.GONE);
        completedView.setVisibility(View.GONE);
        passedView.setVisibility(View.GONE);
        failedView.setVisibility(View.GONE);

        upcomingArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
        requestArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
        ongoingArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
        completedArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
        passedArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
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
                ongoingView.setVisibility(View.VISIBLE);
            case 4:
                completedView.setVisibility(View.VISIBLE);
                break;
            case 5:
                passedView.setVisibility(View.VISIBLE);
                break;
            case 6:
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
        statusLayout5.setEnabled(value);
        statusLayout6.setEnabled(value);
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
                child(userId).child("taskList").orderByChild("status").equalTo("Ongoing");

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
                child(userId).child("taskList").orderByChild("status").equalTo("Completed");

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

        Query task5Query = firebaseDatabase.getReference("users").
                child(userId).child("taskList").orderByChild("status").equalTo("Passed");

        success5 = false;
        task5Query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList5.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Booking task = new Booking(dataSnapshot);
                        taskList5.add(task);
                    }
                }
                success5 = true;
                Collections.reverse(taskList5);
                finishLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                success5 = false;
                errorLoading(error.toString());
            }
        });

        Query task6Query = firebaseDatabase.getReference("users").
                child(userId).child("taskList").orderByChild("status").equalTo("Failed");

        success6 = false;
        task6Query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList6.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Booking task = new Booking(dataSnapshot);
                        taskList6.add(task);
                    }
                }
                success6 = true;
                Collections.reverse(taskList6);
                finishLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                success6 = false;
                errorLoading(error.toString());
            }
        });
    }

    private void finishLoading() {
        if(success1 && success2 && success3 && success4 && success5 && success6) {
            updateAdapter();

            progressBar.setVisibility(View.GONE);

            setScreenEnabled(true);

            badgeText1.setText(String.valueOf(taskList1.size()));
            badgeText2.setText(String.valueOf(taskList2.size()));
            badgeText3.setText(String.valueOf(taskList3.size()));
            badgeText4.setText(String.valueOf(taskList4.size()));
            badgeText5.setText(String.valueOf(taskList5.size()));
            badgeText6.setText(String.valueOf(taskList6.size()));

            if(taskList1.size() + taskList2.size() + taskList3.size() +
                    taskList4.size() + taskList5.size() + taskList6.size() == 0) {
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
        if(!(success1 && success2 && success3 && success4 && success5 && success6)) {
            taskList1.clear();
            taskList2.clear();
            taskList3.clear();
            taskList4.clear();
            taskList5.clear();
            taskList6.clear();
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
        adapter5.setInDriverMode(true);
        adapter6.setInDriverMode(true);
    }

    @Override
    public void setProgressBarToVisible(boolean value) {
        if(value) progressBar.setVisibility(View.VISIBLE);
        else progressBar.setVisibility(View.GONE);
    }
}