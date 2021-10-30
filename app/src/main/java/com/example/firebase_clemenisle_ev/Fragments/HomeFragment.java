package com.example.firebase_clemenisle_ev.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Adapters.TouristSpotAdapter;
import com.example.firebase_clemenisle_ev.Adapters.TouristSpotListAdapter;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.Comment;
import com.example.firebase_clemenisle_ev.Classes.DetailedTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.Route;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment implements
        TouristSpotListAdapter.OnLikeClickListener, TouristSpotAdapter.OnLikeClickListener {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout headerLayout, searchInputLayout, sortInputLayout;
    ImageView searchImage, sortImage, viewModeImage, cancelSearchImage, cancelSortImage, reloadImage;
    TextView tvAppTitle, tvLog;
    TextInputLayout tlSearch, tlSort;
    AutoCompleteTextView acSearch, acSort;
    RecyclerView touristSpotView;
    ProgressBar progressBar;

    Context myContext;
    Resources myResources;

    int colorBlack, colorInitial;

    TouristSpotAdapter touristSpotAdapter;
    TouristSpotListAdapter touristSpotListAdapter;
    List<DetailedTouristSpot> touristSpotList = new ArrayList<>(), copy = new ArrayList<>();
    List<SimpleTouristSpot> likedSpots = new ArrayList<>();

    int touristSpotCount = 0;

    String userId;
    User user;

    boolean listMode = true, isLoggedIn = false;
    boolean isResponseError = true;

    String defaultLogText = "No Record";
    String selectedSorting = "Default";

    ColorStateList cslInitial, cslBlue;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("viewMode", Context.MODE_PRIVATE);
        listMode = sharedPreferences.getBoolean("listMode", true);

        sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        sharedPreferences = myContext
                .getSharedPreferences("sortMode", Context.MODE_PRIVATE);
        selectedSorting = sharedPreferences.getString("sorting", "Default");
    }

    private void sendSharedPreferences(boolean value) {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("viewMode", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("listMode", value);
        editor.apply();
    }

    private void sendSortingPreferences(String value) {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("sortMode", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("sorting", value);
        editor.apply();
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

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        headerLayout = view.findViewById(R.id.headerLayout);
        searchImage = view.findViewById(R.id.searchImage);
        sortImage = view.findViewById(R.id.sortImage);
        viewModeImage = view.findViewById(R.id.viewModeImage);
        cancelSearchImage = view.findViewById(R.id.cancelSearchImage);
        cancelSortImage = view.findViewById(R.id.cancelSortImage);
        tvAppTitle = view.findViewById(R.id.tvAppTitle);
        searchInputLayout = view.findViewById(R.id.searchInputLayout);
        sortInputLayout = view.findViewById(R.id.sortInputLayout);
        tlSearch = view.findViewById(R.id.tlSearch);
        tlSort = view.findViewById(R.id.tlSort);
        acSearch = view.findViewById(R.id.acSearch);
        acSort = view.findViewById(R.id.acSort);
        touristSpotView = view.findViewById(R.id.touristSpotView);
        tvLog = view.findViewById(R.id.tvLog);
        progressBar = view.findViewById(R.id.progressBar);
        reloadImage = view.findViewById(R.id.reloadImage);

        myContext = getContext();
        myResources = getResources();

        colorBlack = myResources.getColor(R.color.black);
        colorInitial = myResources.getColor(R.color.initial);

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));

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

        acSort.setText(selectedSorting);

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        touristSpotView.setLayoutManager(linearLayout);

        selectAdapter();
        getTouristSpots();

        searchImage.setOnClickListener(view1 -> {
            if(!isResponseError) {
                openSearchLayout();
            }
        });

        sortImage.setOnClickListener(view1 -> {
            if(!isResponseError) {
                openSortLayout();
            }
        });

        viewModeImage.setOnClickListener(view1 -> {
            if(!isResponseError) {
                listMode = !listMode;
                selectAdapter();
                sendSharedPreferences(listMode);
            }
        });

        cancelSearchImage.setOnClickListener(view1 -> closeSearchLayout());

        cancelSortImage.setOnClickListener(view1 -> closeSortLayout());

        acSearch.setOnFocusChangeListener((view1, b) -> {
            if(b) {
                tlSearch.setStartIconTintList(cslBlue);
            }
            else {
                tlSearch.setStartIconTintList(cslInitial);
            }
        });

        acSort.setOnFocusChangeListener((view1, b) -> {
            if(b) {
                tlSort.setStartIconTintList(cslBlue);
            }
            else {
                tlSort.setStartIconTintList(cslInitial);
            }
        });

        acSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchTouristSpot();
            }
        });

        acSort.setOnItemClickListener((adapterView, view1, i, l) -> {
            selectedSorting = acSort.getText().toString();
            sortTouristSpots();
            sendSortingPreferences(selectedSorting);
        });

        return view;
    }

    private void setInputAdapter() {
        sortByNames();

        List<String> names = new ArrayList<>();
        for(int i=0; i<touristSpotList.size(); i++) {
            names.add(touristSpotList.get(i).getName());
        }

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(myContext, R.layout.simple_list_item_layout, names);
        acSearch.setAdapter(arrayAdapter);

        List<String> sortingName = Arrays.asList("Default", "Most Liked", "Most Visited", "Most Booked");

        arrayAdapter =
                new ArrayAdapter<>(myContext, R.layout.simple_list_item_layout, sortingName);
        acSort.setAdapter(arrayAdapter);
    }

    private void searchTouristSpot() {
        String value = acSearch.getText().toString().trim();
        List<DetailedTouristSpot> temp = new ArrayList<>();

        if(!value.isEmpty()) {
            for(DetailedTouristSpot touristSpot : copy) {
                if(touristSpot.getName().toLowerCase().contains(value.toLowerCase())) {
                    temp.add(touristSpot);
                }
            }
        }
        else {
            temp.addAll(copy);
        }

        touristSpotList.clear();
        touristSpotList.addAll(temp);
        sortTouristSpots();
        temp.clear();

        if(touristSpotList.size() == 0) {
            String caption;

            if(copy.size() == 0) {
                caption = defaultLogText;
            }
            else {
                caption = "Searching for \"" + value + "\"\n No Record Found";
            }

            tvLog.setText(caption);
            tvLog.setVisibility(View.VISIBLE);
            reloadImage.setVisibility(View.VISIBLE);
            touristSpotView.setVisibility(View.INVISIBLE);
        }
        else {
            tvLog.setVisibility(View.GONE);
            reloadImage.setVisibility(View.GONE);
            touristSpotView.setVisibility(View.VISIBLE);
        }
    }

    private void sortTouristSpots() {
        switch (selectedSorting) {
            case "Default":
                sortByNames();
                tlSort.setStartIconDrawable(R.drawable.ic_baseline_sort_by_alpha_24);
                break;
            case "Most Liked":
                sortByLikes();
                tlSort.setStartIconDrawable(R.drawable.ic_baseline_thumb_up_24);
                break;
            case "Most Visited":
                sortByVisits();
                tlSort.setStartIconDrawable(R.drawable.ic_baseline_person_pin_circle_24);
                break;
            case "Most Booked":
                sortByBooks();
                tlSort.setStartIconDrawable(R.drawable.ic_baseline_book_24);
                break;
        }
        acSort.clearFocus();
    }

    private void sortByNames() {
        Collections.sort(touristSpotList, (touristSpot, t1) ->
                touristSpot.getName().compareToIgnoreCase(t1.getName()));

        notifyChangesToAdapter();
    }

    private void sortByLikes() {
        Collections.sort(touristSpotList, new Comparator<DetailedTouristSpot>() {
            @Override
            public int compare(DetailedTouristSpot touristSpot, DetailedTouristSpot t1) {
                return touristSpot.getLikes() - t1.getLikes();
            }
        });

        Collections.reverse(touristSpotList);
        notifyChangesToAdapter();
    }

    private void sortByVisits() {
        Collections.sort(touristSpotList, new Comparator<DetailedTouristSpot>() {
            @Override
            public int compare(DetailedTouristSpot touristSpot, DetailedTouristSpot t1) {
                return touristSpot.getVisits() - t1.getVisits();
            }
        });

        Collections.reverse(touristSpotList);
        notifyChangesToAdapter();
    }

    private void sortByBooks() {
        Collections.sort(touristSpotList, new Comparator<DetailedTouristSpot>() {
            @Override
            public int compare(DetailedTouristSpot touristSpot, DetailedTouristSpot t1) {
                return touristSpot.getBooks() - t1.getBooks();
            }
        });

        Collections.reverse(touristSpotList);
        notifyChangesToAdapter();
    }

    private void openSortLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(headerLayout);

        constraintSet.clear(sortInputLayout.getId(), ConstraintSet.START);
        constraintSet.connect(sortInputLayout.getId(), ConstraintSet.START,
                tvAppTitle.getId(), ConstraintSet.START);
        constraintSet.connect(sortInputLayout.getId(), ConstraintSet.END,
                viewModeImage.getId(), ConstraintSet.END);

        setTransition(sortInputLayout, 1, 1);
        constraintSet.applyTo(headerLayout);

        setActionButtonEnabled(false);

        acSort.requestFocus();
    }

    private void closeSortLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(headerLayout);

        constraintSet.clear(sortInputLayout.getId(), ConstraintSet.START);
        constraintSet.clear(sortInputLayout.getId(), ConstraintSet.END);
        constraintSet.connect(sortInputLayout.getId(), ConstraintSet.START,
                headerLayout.getId(), ConstraintSet.END);

        setTransition(sortInputLayout, 1, 0);
        constraintSet.applyTo(headerLayout);

        setActionButtonEnabled(true);

        acSort.clearFocus();
    }

    private void openSearchLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(headerLayout);

        constraintSet.clear(searchInputLayout.getId(), ConstraintSet.START);
        constraintSet.connect(searchInputLayout.getId(), ConstraintSet.START,
                tvAppTitle.getId(), ConstraintSet.START);
        constraintSet.connect(searchInputLayout.getId(), ConstraintSet.END,
                viewModeImage.getId(), ConstraintSet.END);

        setTransition(searchInputLayout, 0, 1);
        constraintSet.applyTo(headerLayout);

        setActionButtonEnabled(false);

        acSearch.requestFocus();
    }

    private void closeSearchLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(headerLayout);

        constraintSet.clear(searchInputLayout.getId(), ConstraintSet.START);
        constraintSet.clear(searchInputLayout.getId(), ConstraintSet.END);
        constraintSet.connect(searchInputLayout.getId(), ConstraintSet.START,
                headerLayout.getId(), ConstraintSet.END);

        setTransition(searchInputLayout, 0, 0);
        constraintSet.applyTo(headerLayout);

        setActionButtonEnabled(true);

        acSearch.clearFocus();
    }

    private void setTransition(ConstraintLayout constraintLayout, int sender, int action) {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);

        TransitionManager.beginDelayedTransition(constraintLayout, transition);
    }

    private void getTouristSpots() {
        tvLog.setVisibility(View.GONE);
        reloadImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        touristSpotView.setVisibility(View.INVISIBLE);

        DatabaseReference usersRef = firebaseDatabase.getReference("users");

        Query touristSpotsQuery =
                firebaseDatabase.getReference("touristSpots")
                .orderByChild("deactivated").equalTo(false);
        touristSpotsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                touristSpotList.clear();

                if(snapshot.exists()) {
                    int index = 0;
                    touristSpotCount = (int) snapshot.getChildrenCount();

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DetailedTouristSpot touristSpot = new DetailedTouristSpot(dataSnapshot);
                        getStats(touristSpot, index, usersRef);
                        index++;
                    }
                }
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

    private void getStats(DetailedTouristSpot touristSpot, int index, DatabaseReference usersRef) {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int likes = 0, visits = 0, books = 0, comments = 0;

                if(snapshot.exists()) {
                    String id = touristSpot.getId();

                    for(DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                        if(dataSnapshot1.hasChildren()) {
                            User user = new User(dataSnapshot1);

                            List<SimpleTouristSpot> likedSpots = user.getLikedSpots();
                            for(SimpleTouristSpot likedSpot : likedSpots) {
                                if(likedSpot.getId().equals(id)) likes++;
                            }

                            List<Booking> bookingList = user.getBookingList();
                            for(Booking booking : bookingList) {
                                List<Route> routeList = booking.getRouteList();
                                for(Route route : routeList) {
                                    if(route.getId().equals(id)) {
                                        books++;
                                        if(route.isVisited()) visits++;
                                    }
                                }

                                if(booking.getDestinationSpot() != null) {
                                    if(booking.getDestinationSpot().getId().equals(id)) {
                                        books++;
                                        if(booking.getStatus().equals("Completed")) visits++;
                                    }
                                }
                            }

                            List<Comment> userComments = user.getComments();
                            for(Comment comment : userComments) {
                                if(comment.getId().equals(id)) comments++;
                            }
                        }
                    }
                }

                updateTouristSpotList(touristSpot, index, likes, visits, books, comments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                int books = 0, likes = 0, visits = 0, comments = 0;
                updateTouristSpotList(touristSpot, index, likes, visits, books, comments);
            }
        });
    }

    private void updateTouristSpotList(DetailedTouristSpot touristSpot, int index,
                                       int likes, int visits, int books, int comments) {
        touristSpot.setLikes(likes);
        touristSpot.setVisits(visits);
        touristSpot.setBooks(books);
        touristSpot.setComments(comments);

        if(touristSpotList.size() == touristSpotCount) touristSpotList.set(index, touristSpot);
        else touristSpotList.add(touristSpot);

        if(touristSpotList.size() == touristSpotCount) cloneTouristSpotList();
    }

    private void cloneTouristSpotList() {
        copy.clear();
        copy.addAll(touristSpotList);

        if(isLoggedIn) getCurrentUserLikedSpots();
        else finishLoading();
    }

    private void finishLoading() {
        setInputAdapter();
        searchTouristSpot();

        progressBar.setVisibility(View.GONE);
        touristSpotView.setVisibility(View.VISIBLE);

        isResponseError = false;

        setActionButtonEnabled(true);
    }

    private void setActionButtonEnabled(boolean value) {
        searchImage.setEnabled(value);
        sortImage.setEnabled(value);
        viewModeImage.setEnabled(value);

        if(value) {
            searchImage.getDrawable().setTint(colorBlack);
            sortImage.getDrawable().setTint(colorBlack);
            viewModeImage.getDrawable().setTint(colorBlack);
        }
        else {
            searchImage.getDrawable().setTint(colorInitial);
            sortImage.getDrawable().setTint(colorInitial);
            viewModeImage.getDrawable().setTint(colorInitial);
        }
    }

    private void errorLoading(String error) {
        touristSpotList.clear();
        copy.clear();
        notifyChangesToAdapter();

        setInputAdapter();

        tvLog.setText(error);
        tvLog.setVisibility(View.VISIBLE);
        reloadImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        touristSpotView.setVisibility(View.INVISIBLE);

        isResponseError = true;

        setActionButtonEnabled(false);
    }

    private void notifyChangesToAdapter() {
        if(listMode) {
            touristSpotListAdapter.notifyDataSetChanged();
        }
        else {
            touristSpotAdapter.notifyDataSetChanged();
        }
    }

    private void selectAdapter() {
        if (listMode) {
            viewModeImage.setImageResource(R.drawable.ic_baseline_dynamic_feed_24);
            touristSpotListAdapter = new TouristSpotListAdapter(
                    myContext, touristSpotList, likedSpots, isLoggedIn);
            touristSpotView.setAdapter(touristSpotListAdapter);
            touristSpotListAdapter.setOnLikeClickListener(this);
        } else {
            viewModeImage.setImageResource(R.drawable.ic_baseline_view_list_24);
            touristSpotAdapter = new TouristSpotAdapter(
                    myContext, touristSpotList, likedSpots, isLoggedIn);
            touristSpotView.setAdapter(touristSpotAdapter);
            touristSpotAdapter.setOnLikeClickListener(this);
        }
        viewModeImage.getDrawable().setTint(colorBlack);
    }

    private void getCurrentUserLikedSpots() {
        DatabaseReference usersRef = firebaseDatabase.getReference("users").child(userId);
        usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = null;
                        likedSpots.clear();
                        if(snapshot.exists()) {
                            user = new User(snapshot);
                            likedSpots.addAll(user.getLikedSpots());
                            finishLoading();
                        }
                        else errorLoading("Failed to get the current user");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        errorLoading(error.toString());
                    }
                });
    }

    @Override
    public void setProgressBarToVisible() {
        progressBar.setVisibility(View.VISIBLE);
    }
}