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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LoggedInBookingListFragment extends Fragment {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout constraintLayout,
            constraintLayout1, constraintLayout2, constraintLayout3, constraintLayout4, constraintLayout5;

    RecyclerView pendingView, bookedView, completedView, cancelledView, failedView;
    ConstraintLayout statusLayout1, statusLayout2, statusLayout3, statusLayout4, statusLayout5;
    ImageView pendingArrow, bookedArrow, completedArrow, cancelledArrow, failedArrow, reloadImage;
    TextView badgeText1, badgeText2, badgeText3, badgeText4, badgeText5;

    ProgressBar progressBar;

    int clickedIndex;
    TextView tvLog;

    Context myContext;
    Resources myResources;

    String defaultLogText = "No Record";

    BookingAdapter adapter1, adapter2, adapter3, adapter4, adapter5;

    List<Booking> bookingList1 = new ArrayList<>();
    List<Booking> bookingList2 = new ArrayList<>();
    List<Booking> bookingList3 = new ArrayList<>();
    List<Booking> bookingList4 = new ArrayList<>();
    List<Booking> bookingList5 = new ArrayList<>();

    boolean success1, success2, success3, success4, success5;

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
        View view = inflater.inflate(R.layout.fragment_logged_in_booking_list, container, false);

        constraintLayout = view.findViewById(R.id.constraintLayout);
        constraintLayout1 = view.findViewById(R.id.constraintLayout1);
        constraintLayout2 = view.findViewById(R.id.constraintLayout2);
        constraintLayout3 = view.findViewById(R.id.constraintLayout3);
        constraintLayout4 = view.findViewById(R.id.constraintLayout4);
        constraintLayout5 = view.findViewById(R.id.constraintLayout5);

        pendingView = view.findViewById(R.id.pendingView);
        statusLayout1 = view.findViewById(R.id.statusLayout1);
        pendingArrow = view.findViewById(R.id.pendingArrowImage);
        badgeText1 = view.findViewById(R.id.tvBadge1);

        bookedView = view.findViewById(R.id.bookedView);
        statusLayout2 = view.findViewById(R.id.statusLayout2);
        bookedArrow = view.findViewById(R.id.bookedArrowImage);
        badgeText2 = view.findViewById(R.id.tvBadge2);

        completedView = view.findViewById(R.id.completedView);
        statusLayout3 = view.findViewById(R.id.statusLayout3);
        completedArrow = view.findViewById(R.id.completedArrowImage);
        badgeText3 = view.findViewById(R.id.tvBadge3);

        cancelledView = view.findViewById(R.id.cancelledView);
        statusLayout4 = view.findViewById(R.id.statusLayout4);
        cancelledArrow = view.findViewById(R.id.cancelledArrowImage);
        badgeText4 = view.findViewById(R.id.tvBadge4);

        failedView = view.findViewById(R.id.failedView);
        statusLayout5 = view.findViewById(R.id.statusLayout5);
        failedArrow = view.findViewById(R.id.failedArrowImage);
        badgeText5 = view.findViewById(R.id.tvBadge5);

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
        pendingView.setLayoutManager(linearLayout1);
        adapter1 = new BookingAdapter(myContext, bookingList1);
        pendingView.setAdapter(adapter1);

        LinearLayoutManager linearLayout2 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        bookedView.setLayoutManager(linearLayout2);
        adapter2 = new BookingAdapter(myContext, bookingList2);
        bookedView.setAdapter(adapter2);

        LinearLayoutManager linearLayout3 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        completedView.setLayoutManager(linearLayout3);
        adapter3 = new BookingAdapter(myContext, bookingList3);
        completedView.setAdapter(adapter3);

        LinearLayoutManager linearLayout4 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        cancelledView.setLayoutManager(linearLayout4);
        adapter4 = new BookingAdapter(myContext, bookingList4);
        cancelledView.setAdapter(adapter4);

        LinearLayoutManager linearLayout5 = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        failedView.setLayoutManager(linearLayout5);
        adapter5 = new BookingAdapter(myContext, bookingList5);
        failedView.setAdapter(adapter5);

        getBookings();

        statusLayout1.setOnClickListener(view1 -> {
            if(pendingView.getVisibility() == View.VISIBLE) {
                setViewsToGone();
                resetConstraint();
            }
            else {
                setViewsToGone();
                pendingView.setVisibility(View.VISIBLE);
                pendingArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);

                clickedIndex = 1;
                resetConstraint();
                connectConstraintBottom();
            }
        });

        statusLayout2.setOnClickListener(view1 -> {
            if(bookedView.getVisibility() == View.VISIBLE) {
                setViewsToGone();
                resetConstraint();
            }
            else {
                setViewsToGone();
                bookedView.setVisibility(View.VISIBLE);
                bookedArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);

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
            if(cancelledView.getVisibility() == View.VISIBLE) {
                setViewsToGone();
                resetConstraint();
            }
            else {
                setViewsToGone();
                cancelledView.setVisibility(View.VISIBLE);
                cancelledArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);

                clickedIndex = 4;
                resetConstraint();
                connectConstraintBottom();
            }
        });

        statusLayout5.setOnClickListener(view1 -> {
            if(failedView.getVisibility() == View.VISIBLE) {
                setViewsToGone();
                resetConstraint();
            }
            else {
                setViewsToGone();
                failedView.setVisibility(View.VISIBLE);
                failedArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);

                clickedIndex = 5;
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
        }

        if(count == 0) {
            return;
        }

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        constraintSet.connect(constraintLayout5.getId(), ConstraintSet.BOTTOM,
                constraintLayout.getId(), ConstraintSet.BOTTOM);

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

        if(clickedIndex != 5) {
            int bottom = dpToPx(88);

            ConstraintLayout.LayoutParams layoutParams =
                    (ConstraintLayout.LayoutParams) constraintLayout5.getLayoutParams();
            layoutParams.setMargins(layoutParams.getMarginStart(), layoutParams.topMargin,
                    layoutParams.getMarginEnd(), bottom);
            constraintLayout5.setLayoutParams(layoutParams);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    private void resetConstraint() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

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

        pendingView.setVisibility(View.GONE);
        bookedView.setVisibility(View.GONE);
        completedView.setVisibility(View.GONE);
        cancelledView.setVisibility(View.GONE);
        failedView.setVisibility(View.GONE);

        pendingArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
        bookedArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
        completedArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
        cancelledArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
        failedArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
    }

    private void setViewsToVisible() {
        switch (clickedIndex) {
            case 1:
                pendingView.setVisibility(View.VISIBLE);
                break;
            case 2:
                bookedView.setVisibility(View.VISIBLE);
                break;
            case 3:
                completedView.setVisibility(View.VISIBLE);
                break;
            case 4:
                cancelledView.setVisibility(View.VISIBLE);
                break;
            case 5:
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
    }

    private void getBookings() {
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        setViewsToGone();
        resetConstraint();
        setScreenEnabled(false);

        Query booking1Query = usersRef.child(userId).child("bookingList").
                orderByChild("status").equalTo("Pending");

        success1 = false;
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
                        Toast.LENGTH_LONG
                ).show();

                success1 = false;
                errorLoading(error.toString());
            }
        });

        Query booking2Query = usersRef.child(userId).child("bookingList").
                orderByChild("status").equalTo("Booked");

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
                        Toast.LENGTH_LONG
                ).show();

                success2 = false;
                errorLoading(error.toString());
            }
        });

        Query booking3Query = usersRef.child(userId).child("bookingList").
                orderByChild("status").equalTo("Completed");

        success3 = false;
        booking3Query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList3.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Booking booking = new Booking(dataSnapshot);
                        bookingList3.add(booking);
                    }
                }
                success3 = true;
                Collections.reverse(bookingList3);
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

        Query booking4Query = usersRef.child(userId).child("bookingList").
                orderByChild("status").equalTo("Cancelled");

        success4 = false;
        booking4Query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList4.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Booking booking = new Booking(dataSnapshot);
                        bookingList4.add(booking);
                    }
                }
                success4 = true;
                Collections.reverse(bookingList4);
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

        Query booking5Query = usersRef.child(userId).child("bookingList").
                orderByChild("status").equalTo("Failed");

        success5 = false;
        booking5Query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList5.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Booking booking = new Booking(dataSnapshot);
                        bookingList5.add(booking);
                    }
                }
                success5 = true;
                Collections.reverse(bookingList5);
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
    }

    private void finishLoading() {
        if(success1 && success2 && success3 && success4 && success5) {
            updateAdapter();

            progressBar.setVisibility(View.GONE);

            setScreenEnabled(true);

            badgeText1.setText(String.valueOf(bookingList1.size()));
            badgeText2.setText(String.valueOf(bookingList2.size()));
            badgeText3.setText(String.valueOf(bookingList3.size()));
            badgeText4.setText(String.valueOf(bookingList4.size()));
            badgeText5.setText(String.valueOf(bookingList5.size()));

            if(bookingList1.size() + bookingList2.size() + bookingList3.size() +
            bookingList4.size() + bookingList5.size() == 0) {
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
        if(!(success1 && success2 && success3 && success4 && success5)) {
            bookingList1.clear();
            bookingList2.clear();
            bookingList3.clear();
            bookingList4.clear();
            bookingList5.clear();
            updateAdapter();

            tvLog.setText(error);
            tvLog.setVisibility(View.VISIBLE);
            reloadImage.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void updateAdapter() {
        adapter1.notifyDataSetChanged();
        adapter2.notifyDataSetChanged();
        adapter3.notifyDataSetChanged();
        adapter4.notifyDataSetChanged();
        adapter5.notifyDataSetChanged();
    }
}