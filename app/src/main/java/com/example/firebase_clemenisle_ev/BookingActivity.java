package com.example.firebase_clemenisle_ev;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Adapters.AllSpotAdapter;
import com.example.firebase_clemenisle_ev.Adapters.BookingRouteAdapter;
import com.example.firebase_clemenisle_ev.Adapters.BookingSpotAdapter;
import com.example.firebase_clemenisle_ev.Adapters.BookingStationAdapter;
import com.example.firebase_clemenisle_ev.Adapters.BookingTypeAdapter;
import com.example.firebase_clemenisle_ev.Adapters.RecommendedSpotAdapter;
import com.example.firebase_clemenisle_ev.Adapters.ScheduleTimeAdapter;
import com.example.firebase_clemenisle_ev.Adapters.SelectedSpotAdapter;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.BookingType;
import com.example.firebase_clemenisle_ev.Classes.BookingTypeRoute;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.Classes.DetailedTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.Route;
import com.example.firebase_clemenisle_ev.Classes.ScheduleTime;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.Station;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BookingActivity extends AppCompatActivity implements
        BookingTypeAdapter.OnItemClickListener, BookingStationAdapter.OnItemClickListener,
        BookingRouteAdapter.OnItemClickListener, BookingSpotAdapter.OnItemClickListener,
        SelectedSpotAdapter.OnRemoveClickListener, RecommendedSpotAdapter.OnButtonClickListener,
        AllSpotAdapter.OnButtonClickListener, ScheduleTimeAdapter.OnItemClickListener {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout buttonLayout;
    TextView tvActivityName, tvCaption, tvSteps, tvBookingInfo;
    ImageView infoImage;

    ConstraintLayout firstConstraint;
    TextView tvLog1;
    ImageView reloadImage1;
    ProgressBar progressBar1;
    RecyclerView bookingTypeView;

    ConstraintLayout secondConstraint;
    TextView tvLog2;
    ImageView reloadImage2;
    ProgressBar progressBar2;
    RecyclerView bookingStationView;

    ConstraintLayout thirdConstraint, customRouteLayout, nearSpotsTextLayout, nearSpotViewLayout,
        customizeButtonLayout, recommendedRouteLayout;
    TextView tvLog3, tvNearSpots, tvShowNearSpots, tvLocateStartingLocation;
    ImageView showImage, reloadImage3, locateStartingStationImage;
    ProgressBar progressBar3, progressBar3p1;
    RecyclerView recommendedRouteView, nearSpotView;

    ConstraintLayout fourthConstraint , recommendedSpotLayout, recommendedSpotsTextLayout,
            recommendedSpotViewLayout, endStationLayout, selectedSpotLayout;
    TextView tvLog4, tvRecommendedSpots, tvShowRecommendedSpots;
    TextInputLayout tlEndStation;
    AutoCompleteTextView acEndStation;
    ImageView showImage2, reloadImage4;
    ProgressBar progressBar4, progressBar4p1;
    RecyclerView selectedSpotView, recommendedSpotView;

    ConstraintLayout fifthConstraint;
    TextView tvLog5, tvScheduleDate2, tvChangeBookingDate;
    ImageView changeImage, reloadImage5;
    ProgressBar progressBar5;
    RecyclerView scheduleTimeView;

    ConstraintLayout sixthConstraint;
    TextInputLayout tlMessage;
    EditText etMessage;

    Button continueButton, backButton, customizeButton;

    Context myContext;
    Resources myResources;

    int colorBlack, colorRed, colorBlue, colorInitial;
    ColorStateList cslInitial, cslBlue;

    String defaultCaptionText = "Please select one by tapping your desired item.",
            routeSpotsCaptionText = "You can add another spots to your desired route.",
            threeSpotsCaptionText = "You must select at least three (3) Tourist Spots to your Route.",
            bookingScheduleCaptionText = "Please select your booking schedule.",
            bookingScheduleInvalidDateCaptionText = "Date must be at least two (2) days from now.",
            messageCaptionText = "You can enter a message for this booking record.";

    String bookingTypeActivityText = "Booking Type", stationActivityText = "Starting E-Vehicle Station";
    String recommendedRouteActivityText = "Recommended Routes", listOfRouteActivityText = "Route Spots";
    String bookingScheduleActivityText = "Booking Schedule", messageActivityText = "Message";

    String defaultLogText = "No Records", noRouteLogText = "No Recommended Routes",
            noSelectedSpotText = "No Selected Spot";

    String defaultNearSpotsText = "Tourist Spot(s) Near ", noNearSpotsText = "No Tourist Spots Near ",
            failedNearSpotsText = "Failed to get Near Tourist Spots";

    String defaultRecommendedSpotsText = "Recommended Tourist Spots Near ",
            noRecommendedSpotsText = "No Recommended Tourist Spots Near ",
            failedRecommendedSpotsText = "Failed to get Recommended Tourist Spots";

    String showText = "Show", hideText = "Hide";

    BookingTypeAdapter bookingTypeAdapter;
    List<BookingType> bookingTypeList = new ArrayList<>();
    BookingType bookingType = null;

    BookingStationAdapter bookingStationAdapter;
    List<Station> stationList = new ArrayList<>();
    Station station = null;

    BookingRouteAdapter bookingRouteAdapter;
    List<BookingTypeRoute> bookingTypeRouteList = new ArrayList<>();
    BookingTypeRoute bookingTypeRoute = null;

    BookingSpotAdapter nearSpotAdapter;
    List<SimpleTouristSpot> nearSpots = new ArrayList<>();

    int selectedSpotColumnCount = 2;
    List<SimpleTouristSpot> spots = new ArrayList<>();

    RecommendedSpotAdapter recommendedSpotAdapter;
    List<SimpleTouristSpot> recommendedSpots = new ArrayList<>();
    List<SimpleTouristSpot> list1 = new ArrayList<>(), list2 = new ArrayList<>();

    List<String> endStationsText = new ArrayList<>();
    String endStationText;
    List<Station> endStations = new ArrayList<>();
    Station endStation = null;

    int allSpotColumnCount = 2;
    AllSpotAdapter allSpotAdapter;
    List<SimpleTouristSpot> touristSpotList = new ArrayList<>(), copy = new ArrayList<>();

    DateTimeToString dateTimeToString;

    String rawScheduleDate = "";

    Calendar calendar = Calendar.getInstance();
    int calendarYear, calendarMonth, calendarDay;

    int scheduleDateAllowance = 2;
    boolean vSD = false;

    int scheduleTimeListColumnCount = 3;
    ScheduleTimeAdapter scheduleTimeAdapter;
    List<ScheduleTime> scheduleTimeList = new ArrayList<>();
    ScheduleTime scheduleTime = null;
    String bookingScheduleText;

    String message = "";

    int currentStep = 1, endStep = 6;

    Dialog dialog;

    ConstraintLayout contentLayout;

    RecyclerView spotView;
    ScrollView scrollView;
    ImageView dialogCloseImage;

    ConstraintLayout bookingTypeLayout;
    TextView tvBookingType2, tvPrice;

    ConstraintLayout startingStationLayout;
    ImageView locateImage;
    TextView  tvStartingStation2, tvLocate;

    ConstraintLayout routeSpotsLayout;
    ImageView locateImage2;
    TextView tvEndStation2, tvSpotCount, tvLocate2;

    ConstraintLayout bookingScheduleLayout;
    TextView tvBookingSchedule2;

    ConstraintLayout messageLayout;
    ExpandableTextView extvMessage;

    ConstraintLayout buttonLayout2;
    Button submitButton;

    ProgressBar dialogProgressBar;

    Dialog dialog2;

    RecyclerView touristSpotView;
    ImageView dialogCloseImage2;
    TextInputLayout tlSearch;
    AutoCompleteTextView acSearch;

    TextView tvLog4p3;
    ImageView reloadImage4p3;
    ProgressBar progressBar4p3;

    String defaultSpotCountText = "Number of Tourist Spot(s): ";

    String userId;

    boolean isLoggedIn = false;

    DatabaseReference usersRef;
    boolean isGeneratingBookingId = false;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        buttonLayout = findViewById(R.id.buttonLayout);

        customRouteLayout = findViewById(R.id.customRouteLayout);
        nearSpotsTextLayout = findViewById(R.id.nearSpotsTextLayout);
        nearSpotViewLayout = findViewById(R.id.nearSpotViewLayout);
        customizeButtonLayout = findViewById(R.id.customizeButtonLayout);
        recommendedRouteLayout = findViewById(R.id.recommendedRouteLayout);

        recommendedSpotLayout = findViewById(R.id.recommendedSpotLayout);
        recommendedSpotsTextLayout = findViewById(R.id.recommendedSpotsTextLayout);
        recommendedSpotViewLayout = findViewById(R.id.recommendedSpotViewLayout);
        endStationLayout = findViewById(R.id.endStationLayout);
        selectedSpotLayout = findViewById(R.id.selectedSpotLayout);

        firstConstraint = findViewById(R.id.firstConstraint);
        secondConstraint = findViewById(R.id.secondConstraint);
        thirdConstraint = findViewById(R.id.thirdConstraint);
        fourthConstraint = findViewById(R.id.fourthConstraint);
        fifthConstraint = findViewById(R.id.fifthConstraint);
        sixthConstraint = findViewById(R.id.sixthConstraint);

        tvActivityName = findViewById(R.id.tvActivityName);
        tvCaption = findViewById(R.id.tvCaption);
        tvSteps = findViewById(R.id.tvSteps);

        tvLog1 = findViewById(R.id.tvLog1);
        bookingTypeView = findViewById(R.id.bookingTypeView);
        progressBar1 = findViewById(R.id.progressBar1);
        reloadImage1 = findViewById(R.id.reloadImage1);

        tvLog2 = findViewById(R.id.tvLog2);
        bookingStationView = findViewById(R.id.bookingStationView);
        progressBar2 = findViewById(R.id.progressBar2);
        reloadImage2 = findViewById(R.id.reloadImage2);

        tvLog3 = findViewById(R.id.tvLog3);
        recommendedRouteView = findViewById(R.id.recommendedRouteView);
        progressBar3 = findViewById(R.id.progressBar3);
        reloadImage3 = findViewById(R.id.reloadImage3);

        nearSpotView = findViewById(R.id.nearSpotView);
        progressBar3p1 = findViewById(R.id.progressBar3p1);
        tvNearSpots = findViewById(R.id.tvNearSpots);
        customizeButton = findViewById(R.id.customizeButton);

        tvShowNearSpots = findViewById(R.id.tvShowNearSpots);
        showImage = findViewById(R.id.showImage);
        tvLocateStartingLocation = findViewById(R.id.tvLocateStartingLocation);
        locateStartingStationImage = findViewById(R.id.locateStartingStationImage);

        tvLog4 = findViewById(R.id.tvLog4);
        selectedSpotView = findViewById(R.id.selectedSpotView);
        progressBar4 = findViewById(R.id.progressBar4);
        reloadImage4 = findViewById(R.id.reloadImage4);

        recommendedSpotView = findViewById(R.id.recommendedSpotView);
        progressBar4p1 = findViewById(R.id.progressBar4p1);
        tvRecommendedSpots = findViewById(R.id.tvRecommendedSpots);

        tvShowRecommendedSpots = findViewById(R.id.tvShowRecommendedSpots);
        showImage2 = findViewById(R.id.showImage2);

        tlEndStation = findViewById(R.id.tlEndStation);
        acEndStation = findViewById(R.id.acEndStation);

        tvLog5 = findViewById(R.id.tvLog5);
        scheduleTimeView = findViewById(R.id.scheduleTimeView);
        progressBar5 = findViewById(R.id.progressBar5);
        reloadImage5 = findViewById(R.id.reloadImage5);

        tvScheduleDate2 = findViewById(R.id.tvScheduleDate2);
        tvChangeBookingDate = findViewById(R.id.tvChangeBookingDate);
        changeImage = findViewById(R.id.changeImage);

        tlMessage = findViewById(R.id.tlMessage);
        etMessage = findViewById(R.id.etMessage);

        continueButton = findViewById(R.id.continueButton);
        backButton = findViewById(R.id.backButton);

        infoImage = findViewById(R.id.infoImage);
        tvBookingInfo = findViewById(R.id.tvBookingInfo);

        myContext = BookingActivity.this;
        myResources = myContext.getResources();

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

                onBackPressed();
            }
            else {
                userId = firebaseUser.getUid();
            }
        }

        cslInitial = ColorStateList.valueOf(myResources.getColor(R.color.initial));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));

        colorBlack = myResources.getColor(R.color.black);
        colorRed = myResources.getColor(R.color.red);
        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);

        tvSteps.setText(getStepText());

        initBookingInformationDialog();
        initAllSpotsDialog();

        Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage1);
        Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage2);
        Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage3);
        Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage4);
        Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage4p3);
        Glide.with(myContext).load(R.drawable.magnify_4s_256px).into(reloadImage5);

        firstConstraint.setVisibility(View.VISIBLE);
        secondConstraint.setVisibility(View.GONE);
        thirdConstraint.setVisibility(View.GONE);
        fourthConstraint.setVisibility(View.GONE);
        fifthConstraint.setVisibility(View.GONE);

        dateTimeToString = new DateTimeToString();

        LinearLayoutManager linearLayout1 =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        bookingTypeView.setLayoutManager(linearLayout1);
        bookingTypeAdapter = new BookingTypeAdapter(myContext, bookingTypeList);
        bookingTypeView.setAdapter(bookingTypeAdapter);
        bookingTypeAdapter.setOnItemClickListener(this);

        getBookingTypes();

        LinearLayoutManager linearLayout2 =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        bookingStationView.setLayoutManager(linearLayout2);
        bookingStationAdapter = new BookingStationAdapter(myContext, stationList);
        bookingStationView.setAdapter(bookingStationAdapter);
        bookingStationAdapter.setOnItemClickListener(this);

        getStations();

        LinearLayoutManager linearLayout3 =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        recommendedRouteView.setLayoutManager(linearLayout3);
        bookingRouteAdapter = new BookingRouteAdapter(myContext, bookingTypeRouteList);
        recommendedRouteView.setAdapter(bookingRouteAdapter);
        bookingRouteAdapter.setOnItemClickListener(this);

        LinearLayoutManager linearLayout3p1 =
                new LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false);
        nearSpotView.setLayoutManager(linearLayout3p1);
        nearSpotAdapter = new BookingSpotAdapter(myContext, nearSpots, bookingTypeRoute);
        nearSpotView.setAdapter(nearSpotAdapter);
        nearSpotAdapter.setFromNearSpot(true);
        nearSpotAdapter.setOnItemClickListener(this);

        LinearLayoutManager linearLayout4p1 =
                new LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false);
        recommendedSpotView.setLayoutManager(linearLayout4p1);
        recommendedSpotAdapter = new RecommendedSpotAdapter(myContext, recommendedSpots, spots, touristSpotList);
        recommendedSpotView.setAdapter(recommendedSpotAdapter);
        recommendedSpotAdapter.setOnButtonClickListener(this);

        GridLayoutManager gridLayoutManagerD1 =
                new GridLayoutManager(myContext, allSpotColumnCount, GridLayoutManager.VERTICAL, false);
        touristSpotView.setLayoutManager(gridLayoutManagerD1);
        allSpotAdapter = new AllSpotAdapter(myContext, touristSpotList, spots, allSpotColumnCount);
        touristSpotView.setAdapter(allSpotAdapter);
        allSpotAdapter.setOnButtonClickListener(this);

        getTouristSpots();

        GridLayoutManager gridLayoutManager2 =
                new GridLayoutManager(myContext, scheduleTimeListColumnCount, GridLayoutManager.VERTICAL, false);
        scheduleTimeView.setLayoutManager(gridLayoutManager2);
        scheduleTimeAdapter = new ScheduleTimeAdapter(myContext, scheduleTimeList, scheduleTimeListColumnCount);
        scheduleTimeView.setAdapter(scheduleTimeAdapter);
        scheduleTimeAdapter.setOnItemClickListener(this);

        getScheduleTime();

        continueButton.setOnClickListener(view -> {
            if(!bookingType.getId().equals("BT99")) {
                if(currentStep == 1) {
                    if(bookingStationAdapter.getBookingType() == null) {
                        bookingStationAdapter.setBookingType(bookingType);
                        checkStationContinueButton();
                    }
                    else {
                        if(bookingStationAdapter.getBookingType() != bookingType) {
                            bookingStationAdapter.setBookingType(bookingType);
                            continueButton.setEnabled(false);
                        }
                        else {
                            checkStationContinueButton();
                        }
                    }

                    firstConstraint.setVisibility(View.GONE);
                    secondConstraint.setVisibility(View.VISIBLE);

                    bookingTypeLayout.setVisibility(View.VISIBLE);
                    tvBookingType2.setText(bookingType.getName());
                    String price = "₱" + bookingType.getPrice();
                    if(price.split("\\.")[1].length() == 1) price += 0;
                    tvPrice.setText(price);

                    tvActivityName.setText(stationActivityText);

                    backButton.setVisibility(View.VISIBLE);
                    infoImage.setVisibility(View.VISIBLE);
                    tvBookingInfo.setVisibility(View.VISIBLE);
                }
                else if(currentStep == 2) {
                    getBookingRoutes();
                    getNearSpots();

                    if(bookingRouteAdapter.getStation() == null) {
                        bookingRouteAdapter.setStation(station);
                        checkRouteContinueButton();
                    }
                    else {
                        if(bookingRouteAdapter.getStation() != station) {
                            bookingRouteAdapter.setStation(station);
                            continueButton.setEnabled(false);
                        }
                        else {
                            checkRouteContinueButton();
                        }
                    }

                    secondConstraint.setVisibility(View.GONE);
                    thirdConstraint.setVisibility(View.VISIBLE);

                    startingStationLayout.setVisibility(View.VISIBLE);
                    tvStartingStation2.setText(station.getName());

                    tvActivityName.setText(recommendedRouteActivityText);

                    checkRouteContinueButton();
                }
                else if(currentStep == 3) {
                    spots = new ArrayList<>(bookingTypeRoute.getSpots());
                    goToStep4FromStep3();
                }
                else if(currentStep == 4) {
                    fourthConstraint.setVisibility(View.GONE);
                    fifthConstraint.setVisibility(View.VISIBLE);

                    if(rawScheduleDate.length() == 0) {
                        calendarYear = calendar.get(Calendar.YEAR);
                        calendarMonth = calendar.get(Calendar.MONTH);
                        calendarDay = calendar.get(Calendar.DAY_OF_MONTH);
                        rawScheduleDate = calendarYear + "-" + calendarMonth + "-" + calendarDay;

                        dateTimeToString.setDateToSplit(rawScheduleDate);
                        tvScheduleDate2.setText(dateTimeToString.getDate());
                        tvScheduleDate2.setTextColor(colorRed);
                        vSD = false;

                        tvCaption.setText(bookingScheduleInvalidDateCaptionText);
                        tvCaption.setTextColor(colorRed);
                    }
                    else tvCaption.setText(bookingScheduleCaptionText);

                    tvActivityName.setText(bookingScheduleActivityText);

                    checkScheduleContinueButton();
                }
                else if(currentStep == 5) {
                    fifthConstraint.setVisibility(View.GONE);
                    sixthConstraint.setVisibility(View.VISIBLE);

                    bookingScheduleLayout.setVisibility(View.VISIBLE);
                    tvBookingSchedule2.setText(bookingScheduleText);

                    ConstraintLayout.LayoutParams layoutParams =
                            (ConstraintLayout.LayoutParams) contentLayout.getLayoutParams();

                    if(message.length() > 256) {
                        layoutParams.height = dpToPx(0);
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                    }
                    else {
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                    }

                    contentLayout.setLayoutParams(layoutParams);

                    if(message.length() > 0) messageLayout.setVisibility(View.VISIBLE);
                    else messageLayout.setVisibility(View.GONE);
                    buttonLayout2.setVisibility(View.VISIBLE);

                    tvActivityName.setText(messageActivityText);
                    tvCaption.setText(messageCaptionText);
                }
                else if(currentStep == 6) dialog.show();
            }
            else {
                if(currentStep == 1) {
                    firstConstraint.setVisibility(View.GONE);
                    continueButton.setEnabled(false);
                    backButton.setVisibility(View.VISIBLE);
                }
            }

                if(currentStep < endStep) {
                    currentStep++;
                    tvSteps.setText(getStepText());
                }
        });

        backButton.setOnClickListener(view -> {
            if(!bookingType.getId().equals("BT99")) {
                if(currentStep == 2) {
                    secondConstraint.setVisibility(View.GONE);
                    firstConstraint.setVisibility(View.VISIBLE);

                    bookingTypeLayout.setVisibility(View.GONE);

                    tvActivityName.setText(bookingTypeActivityText);

                    checkBookingTypeContinueButton();
                    backButton.setVisibility(View.GONE);
                    infoImage.setVisibility(View.GONE);
                    tvBookingInfo.setVisibility(View.GONE);
                }
                else if(currentStep == 3) {
                    thirdConstraint.setVisibility(View.GONE);
                    secondConstraint.setVisibility(View.VISIBLE);

                    if(tvShowNearSpots.getText().equals(hideText))
                        transition1();

                    startingStationLayout.setVisibility(View.GONE);

                    tvActivityName.setText(stationActivityText);

                    checkStationContinueButton();
                }
                else if(currentStep == 4) {
                    fourthConstraint.setVisibility(View.GONE);
                    thirdConstraint.setVisibility(View.VISIBLE);

                    spots.clear();

                    if(tvShowRecommendedSpots.getText().equals(hideText))
                        transition4();

                    routeSpotsLayout.setVisibility(View.GONE);

                    tvActivityName.setText(recommendedRouteActivityText);
                    tvCaption.setText(defaultCaptionText);
                    tvCaption.setTextColor(colorBlack);

                    checkRouteContinueButton();
                }
                else if(currentStep == 5) {
                    fifthConstraint.setVisibility(View.GONE);
                    fourthConstraint.setVisibility(View.VISIBLE);

                    tvActivityName.setText(listOfRouteActivityText);
                    tvCaption.setText(routeSpotsCaptionText);

                    checkSelectedSpotContinueButton();
                }
                else if(currentStep == 6) {
                    sixthConstraint.setVisibility(View.GONE);
                    fifthConstraint.setVisibility(View.VISIBLE);

                    bookingScheduleLayout.setVisibility(View.GONE);

                    ConstraintLayout.LayoutParams layoutParams =
                            (ConstraintLayout.LayoutParams) contentLayout.getLayoutParams();
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    contentLayout.setLayoutParams(layoutParams);

                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    messageLayout.setVisibility(View.GONE);
                    buttonLayout2.setVisibility(View.GONE);

                    tvActivityName.setText(bookingScheduleActivityText);
                    tvCaption.setText(bookingScheduleCaptionText);

                    checkScheduleContinueButton();
                }
            }
            else {
                if(currentStep == 2) {
                    firstConstraint.setVisibility(View.VISIBLE);

                    bookingTypeLayout.setVisibility(View.GONE);

                    tvActivityName.setText(bookingTypeActivityText);

                    checkBookingTypeContinueButton();
                    backButton.setVisibility(View.GONE);
                    infoImage.setVisibility(View.GONE);
                    tvBookingInfo.setVisibility(View.GONE);
                }
            }

            if(currentStep > 1) {
                currentStep--;
                tvSteps.setText(getStepText());
            }
        });

        infoImage.setOnClickListener(view -> openBookingInfo());
        tvBookingInfo.setOnClickListener(view -> openBookingInfo());

        tvShowNearSpots.setOnClickListener(view -> showNearSpots());
        showImage.setOnClickListener(view -> showNearSpots());

        tvLocateStartingLocation.setOnClickListener(view -> openMap());
        locateStartingStationImage.setOnClickListener(view -> openMap());

        customizeButton.setOnClickListener(view -> {
            spots.clear();
            goToStep4FromStep3();

            if(currentStep < endStep) {
                currentStep++;
                tvSteps.setText(getStepText());
            }
        });

        tvShowRecommendedSpots.setOnClickListener(view -> showRecommendedSpots());
        showImage2.setOnClickListener(view -> showRecommendedSpots());

        selectedSpotView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(tvShowRecommendedSpots.getText().equals(hideText) && showImage2.isEnabled()) {
                    tvShowRecommendedSpots.setEnabled(false);
                    showImage2.setEnabled(false);
                    transition4();
                }
            }
        });

        acEndStation.setOnFocusChangeListener((view1, b) -> {
            if(b) {
                tlEndStation.setStartIconTintList(cslBlue);
            }
            else {
                tlEndStation.setStartIconTintList(cslInitial);
            }
        });

        acEndStation.setOnItemClickListener((adapterView, view, i, l) -> {
            endStationText = acEndStation.getText().toString();
            endStation = endStations.get(i);
            tvEndStation2.setText(endStationText);
            acEndStation.clearFocus();
        });

        tvChangeBookingDate.setOnClickListener(view -> showDatePickerDialog());
        changeImage.setOnClickListener(view -> showDatePickerDialog());

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

                if(message.length() > 0) messageLayout.setVisibility(View.VISIBLE);
                else messageLayout.setVisibility(View.GONE);

                ConstraintLayout.LayoutParams layoutParams =
                        (ConstraintLayout.LayoutParams) contentLayout.getLayoutParams();

                if(message.length() > 256) {
                    layoutParams.height = dpToPx(0);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                }
                else {
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                }

                contentLayout.setLayoutParams(layoutParams);

                extvMessage.setText(message);
            }
        });
    }

    private String getStepText() {
        return "Step " + currentStep + (currentStep == 1 ? "" : " out of " +  endStep);
    }

    private void goToStep4FromStep3() {
        thirdConstraint.setVisibility(View.GONE);
        fourthConstraint.setVisibility(View.VISIBLE);

        LinearLayoutManager linearLayoutD1 =
                new LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false);
        spotView.setLayoutManager(linearLayoutD1);
        BookingSpotAdapter bookingSpotAdapter = new BookingSpotAdapter(myContext, spots, bookingTypeRoute);
        spotView.setAdapter(bookingSpotAdapter);

        GridLayoutManager gridLayoutManager =
                new GridLayoutManager(myContext, selectedSpotColumnCount, GridLayoutManager.VERTICAL, false);
        selectedSpotView.setLayoutManager(gridLayoutManager);
        SelectedSpotAdapter selectedSpotAdapter = new SelectedSpotAdapter(myContext, spots, selectedSpotColumnCount);
        selectedSpotView.setAdapter(selectedSpotAdapter);
        selectedSpotAdapter.setOnRemoveClickListener(this);

        if(spots.size() == 0) {
            tvLog4.setText(noSelectedSpotText);
            tvLog4.setVisibility(View.VISIBLE);
            reloadImage4.setVisibility(View.VISIBLE);
            selectedSpotView.setVisibility(View.INVISIBLE);

            routeSpotsLayout.setVisibility(View.GONE);
            endStationLayout.setVisibility(View.GONE);
        }
        else {
            tvLog4.setVisibility(View.GONE);
            reloadImage4.setVisibility(View.GONE);
            selectedSpotView.setVisibility(View.VISIBLE);

            routeSpotsLayout.setVisibility(View.VISIBLE);
            getNearStations();
        }
        progressBar4.setVisibility(View.GONE);

        String spotCountText = defaultSpotCountText + spots.size();
        tvSpotCount.setText(spotCountText);

        tvActivityName.setText(listOfRouteActivityText);

        getRecommendedSpots();
        checkSelectedSpotContinueButton();
    }

    private void showDatePickerDialog() {
        calendarYear = calendar.get(Calendar.YEAR);
        calendarMonth = calendar.get(Calendar.MONTH);
        calendarDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(myContext,
                (datePicker, i, i1, i2) -> {
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth();
                    int day = datePicker.getDayOfMonth();
                    rawScheduleDate = year + "-" + month + "-" + day;

                    dateTimeToString.setDateToSplit(rawScheduleDate);
                    tvScheduleDate2.setText(dateTimeToString.getDate());
                    if(year < calendarYear ||
                            (month < calendarMonth && year == calendarYear) ||
                            (day < calendarDay + scheduleDateAllowance && month == calendarMonth &&
                                    year == calendarYear)) {
                        tvScheduleDate2.setTextColor(colorRed);
                        tvCaption.setText(bookingScheduleInvalidDateCaptionText);
                        tvCaption.setTextColor(colorRed);
                        vSD = false;
                    }
                    else{
                        tvScheduleDate2.setTextColor(colorBlack);
                        tvCaption.setText(bookingScheduleCaptionText);
                        tvCaption.setTextColor(colorBlack);
                        vSD = true;
                    }

                    if(scheduleTime != null)
                        bookingScheduleText =
                                dateTimeToString.getDate() + " | " + scheduleTime.getTime();

                    checkScheduleContinueButton();
                }, calendarYear, calendarMonth, calendarDay );

        datePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        if(currentStep > 1) {
            backButton.performClick();
        }
        else {
            super.onBackPressed();
        }
    }

    private void showNearSpots() {
        tvShowNearSpots.setEnabled(false);
        showImage.setEnabled(false);

        if(tvShowNearSpots.getText().equals(showText)) transition1();
        else if(tvShowNearSpots.getText().equals(hideText)) transition1();
    }

    private void transition1() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(nearSpotViewLayout);

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) nearSpotViewLayout.getLayoutParams();

        if(tvShowNearSpots.getText().equals(showText)) {
            constraintSet.clear(nearSpotView.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(nearSpotView.getId(), ConstraintSet.TOP,
                    nearSpotViewLayout.getId(), ConstraintSet.TOP);

            layoutParams.setMargins(layoutParams.leftMargin, dpToPx(8),
                    layoutParams.rightMargin, layoutParams.bottomMargin);
        }
        else if(tvShowNearSpots.getText().equals(hideText)) {
            constraintSet.clear(nearSpotView.getId(), ConstraintSet.TOP);
            constraintSet.connect(nearSpotView.getId(), ConstraintSet.BOTTOM,
                    nearSpotViewLayout.getId(), ConstraintSet.TOP);

            layoutParams.height = dpToPx(100);
        }

        nearSpotViewLayout.setLayoutParams(layoutParams);

        setTransition1();
        if(tvShowNearSpots.getText().equals(showText)) transition2();
        constraintSet.applyTo(nearSpotViewLayout);
    }

    private void transition2() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(thirdConstraint);

        if(tvShowNearSpots.getText().equals(hideText)) {
            ConstraintLayout.LayoutParams layoutParams =
                    (ConstraintLayout.LayoutParams) nearSpotViewLayout.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.setMargins(layoutParams.leftMargin, dpToPx(0),
                    layoutParams.rightMargin, layoutParams.bottomMargin);
            nearSpotViewLayout.setLayoutParams(layoutParams);
        }

        constraintSet.clear(customizeButtonLayout.getId(), ConstraintSet.TOP);
        constraintSet.connect(customizeButtonLayout.getId(), ConstraintSet.TOP,
                customRouteLayout.getId(), ConstraintSet.BOTTOM);

        setTransition2();
        transition3();
        constraintSet.applyTo(thirdConstraint);
    }

    private void transition3() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(thirdConstraint);

        constraintSet.clear(recommendedRouteLayout.getId(), ConstraintSet.TOP);
        constraintSet.connect(recommendedRouteLayout.getId(), ConstraintSet.TOP,
                customizeButtonLayout.getId(), ConstraintSet.BOTTOM);

        setTransition3();
        constraintSet.applyTo(thirdConstraint);
    }

    private void setTransition1() {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);

        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                if(tvShowNearSpots.getText().equals(hideText))
                    transition2();
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        TransitionManager.beginDelayedTransition(nearSpotView, transition);
    }

    private void setTransition2() {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);

        TransitionManager.beginDelayedTransition(customizeButtonLayout, transition);
    }

    private void setTransition3() {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);

        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                if(tvShowNearSpots.getText().equals(showText)) {
                    tvShowNearSpots.setText(hideText);
                    showImage.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                }
                else if(tvShowNearSpots.getText().equals(hideText)) {
                    tvShowNearSpots.setText(showText);
                    showImage.setImageResource(R.drawable.ic_baseline_visibility_24);
                }

                tvShowNearSpots.setEnabled(true);
                showImage.setEnabled(true);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        TransitionManager.beginDelayedTransition(recommendedRouteLayout, transition);
    }

    private void showRecommendedSpots() {
        tvShowRecommendedSpots.setEnabled(false);
        showImage2.setEnabled(false);

        if(tvShowRecommendedSpots.getText().equals(showText)) transition4();
        else if(tvShowRecommendedSpots.getText().equals(hideText)) transition4();
    }

    private void transition4() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(recommendedSpotViewLayout);

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) recommendedSpotViewLayout.getLayoutParams();

        if(tvShowRecommendedSpots.getText().equals(showText)) {
            constraintSet.clear(recommendedSpotView.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(recommendedSpotView.getId(), ConstraintSet.TOP,
                    recommendedSpotViewLayout.getId(), ConstraintSet.TOP);

            layoutParams.setMargins(layoutParams.leftMargin, dpToPx(8),
                    layoutParams.rightMargin, layoutParams.bottomMargin);
        }
        else if(tvShowRecommendedSpots.getText().equals(hideText)) {
            constraintSet.clear(recommendedSpotView.getId(), ConstraintSet.TOP);
            constraintSet.connect(recommendedSpotView.getId(), ConstraintSet.BOTTOM,
                    recommendedSpotViewLayout.getId(), ConstraintSet.TOP);

            layoutParams.height = dpToPx(156);
        }

        recommendedSpotViewLayout.setLayoutParams(layoutParams);

        setTransition4();
        if(tvShowRecommendedSpots.getText().equals(showText)) transition5();
        constraintSet.applyTo(recommendedSpotViewLayout);
    }

    private void transition5() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(fourthConstraint);

        if(tvShowRecommendedSpots.getText().equals(hideText)) {
            ConstraintLayout.LayoutParams layoutParams =
                    (ConstraintLayout.LayoutParams) recommendedSpotViewLayout.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.setMargins(layoutParams.leftMargin, dpToPx(0),
                    layoutParams.rightMargin, layoutParams.bottomMargin);
            recommendedSpotViewLayout.setLayoutParams(layoutParams);
        }

        constraintSet.clear(selectedSpotLayout.getId(), ConstraintSet.TOP);
        constraintSet.connect(selectedSpotLayout.getId(), ConstraintSet.TOP,
                recommendedSpotLayout.getId(), ConstraintSet.BOTTOM);

        setTransition5();
        constraintSet.applyTo(fourthConstraint);
    }

    private void setTransition4() {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);

        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                if(tvShowRecommendedSpots.getText().equals(hideText))
                    transition5();
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        TransitionManager.beginDelayedTransition(recommendedSpotView, transition);
    }

    private void setTransition5() {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);

        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                if(tvShowRecommendedSpots.getText().equals(showText)) {
                    tvShowRecommendedSpots.setText(hideText);
                    showImage2.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                }
                else if(tvShowRecommendedSpots.getText().equals(hideText)) {
                    tvShowRecommendedSpots.setText(showText);
                    showImage2.setImageResource(R.drawable.ic_baseline_visibility_24);
                }

                tvShowRecommendedSpots.setEnabled(true);
                showImage2.setEnabled(true);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        TransitionManager.beginDelayedTransition(selectedSpotLayout, transition);
    }

    private void openBookingInfo() {
        if(dialog != null) {
            scrollView.scrollTo(0, 0);
            dialog.show();
        }
    }

    private void initBookingInformationDialog() {
        dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_booking_information_layout);

        dialogCloseImage = dialog.findViewById(R.id.dialogCloseImage);
        scrollView = dialog.findViewById(R.id.scrollView);
        contentLayout = dialog.findViewById(R.id.contentLayout);

        bookingTypeLayout = dialog.findViewById(R.id.bookingTypeLayout);
        tvBookingType2 = dialog.findViewById(R.id.tvBookingType2);
        tvPrice = dialog.findViewById(R.id.tvPrice);

        startingStationLayout = dialog.findViewById(R.id.startingStationLayout);
        tvStartingStation2 = dialog.findViewById(R.id.tvStartingStation2);
        locateImage = dialog.findViewById(R.id.locateImage);
        tvLocate = dialog.findViewById(R.id.tvLocate);

        routeSpotsLayout = dialog.findViewById(R.id.routeSpotsLayout);
        tvEndStation2 = dialog.findViewById(R.id.tvEndStation2);
        tvSpotCount = dialog.findViewById(R.id.tvSpotCount);
        locateImage2 = dialog.findViewById(R.id.locateImage2);
        tvLocate2 = dialog.findViewById(R.id.tvLocate2);
        spotView = dialog.findViewById(R.id.spotView);

        bookingScheduleLayout = dialog.findViewById(R.id.bookingScheduleLayout);
        tvBookingSchedule2 = dialog.findViewById(R.id.tvBookingSchedule2);

        messageLayout = dialog.findViewById(R.id.messageLayout);
        extvMessage = dialog.findViewById(R.id.extvMessage);

        buttonLayout2 = dialog.findViewById(R.id.buttonLayout);
        submitButton = dialog.findViewById(R.id.submitButton);

        dialogProgressBar = dialog.findViewById(R.id.dialogProgressBar);

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) contentLayout.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        contentLayout.setLayoutParams(layoutParams);

        tvLocate.setOnClickListener(view -> openMap());
        locateImage.setOnClickListener(view -> openMap());

        tvLocate2.setOnClickListener(view -> openMap2());
        locateImage2.setOnClickListener(view -> openMap2());

        submitButton.setOnClickListener(view -> generateBookingId());

        dialogCloseImage.setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void generateBookingId() {
        setDialogScreenEnabled(false);
        dialogProgressBar.setVisibility(View.VISIBLE);

        isGeneratingBookingId = false;
        usersRef = firebaseDatabase.getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!isGeneratingBookingId) {
                    isGeneratingBookingId = true;
                    usersRef = null;

                    String yearId = dateTimeToString.getYear2Suffix();
                    int month = Integer.parseInt(dateTimeToString.getMonthNo()) + 1;
                    String monthId = String.valueOf(month);
                    if(monthId.length() == 1) monthId = "0" + monthId;
                    String dayId = dateTimeToString.getDay();
                    if(dayId.length() == 1) dayId = "0" + dayId;

                    String bookingId = "B" + yearId + "-" + monthId + dayId;

                    int suffixCount = 0;

                    if(snapshot.exists()) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = new User(dataSnapshot);

                            List<Booking> bookingList = user.getBookingList();
                            if(bookingList.size() > 0) {
                                for(Booking booking : bookingList) {
                                    if(booking.getId().startsWith(bookingId)) {
                                        suffixCount++;
                                    }
                                }
                            }
                        }
                    }
                    String idSuffix = String.valueOf(suffixCount);
                    if(idSuffix.length() == 1) idSuffix = "0" + idSuffix;

                    bookingId += "-" + idSuffix;

                    addBooking(bookingId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        "Failed to book a tour. Please try again.",
                        Toast.LENGTH_LONG
                ).show();

                isGeneratingBookingId = false;
                usersRef = null;

                setDialogScreenEnabled(true);
                dialogProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private List<Route> generateRouteList() {
        List<Route> resultList = new ArrayList<>();

        int index = 1;
        for(SimpleTouristSpot spot : spots) {
            String keySuffix = String.valueOf(index);
            if(keySuffix.length() == 1) keySuffix = "0" + keySuffix;

            Route route = new Route(false, spot.getId(), spot.getImg(), spot.getName(),
                   "BR" + keySuffix , false);
            resultList.add(route);

            index++;
        }

        return resultList;
    }

    private void addBooking(String bookingId) {
        List<Route> bookingRouteList = generateRouteList();
        bookingType.setRouteList(null);

        Booking booking =
                new Booking(bookingType, endStation, bookingId, message,
                        bookingScheduleText, station, "Processing");

        DatabaseReference bookingListRef = firebaseDatabase.getReference("users")
                .child(userId).child("bookingList").child(bookingId);
        bookingListRef.setValue(booking).addOnCompleteListener(task -> {

            if(task.isSuccessful()) addBookingRoute(bookingId, bookingRouteList, bookingListRef);
            else {
                Toast.makeText(
                        myContext,
                        "Failed to book a tour. Please try again.",
                        Toast.LENGTH_LONG
                ).show();

                setDialogScreenEnabled(true);
                dialogProgressBar.setVisibility(View.GONE);
            }
        });
    }
    
    private void addBookingRoute(String bookingId, List<Route> bookingRouteList,
                                 DatabaseReference bookingListRef) {
        int index = 1;
        for(Route route : bookingRouteList) {
            boolean isLastItem;
            if(index == bookingRouteList.size()) isLastItem = true;
            else isLastItem = false;

            DatabaseReference routeSpotsRef =
                    bookingListRef.child("routeSpots").child(route.getRouteId());
            routeSpotsRef.setValue(route).addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            if(isLastItem) proceedToNextActivity(bookingId);
                        }
                        else {
                            Toast.makeText(
                                    myContext,
                                    "Failed to book a tour. Please try again.",
                                    Toast.LENGTH_LONG
                            ).show();

                            setDialogScreenEnabled(true);
                            dialogProgressBar.setVisibility(View.GONE);
                        }
                    });
            index++;
        }
    }

    private void proceedToNextActivity(String bookingId) {
        Intent intent = new Intent(myContext, MainActivity.class);

        startActivity(intent);
        finishAffinity();

        intent = new Intent(myContext, RouteActivity.class);
        intent.putExtra("bookingId",bookingId);
        intent.putExtra("isLatest", false);

        startActivity(intent);

        Toast.makeText(
                myContext,
                "Successfully booked a tour",
                Toast.LENGTH_LONG
        ).show();
    }

    private void setDialogScreenEnabled(boolean value) {
        dialog.setCanceledOnTouchOutside(value);
        dialogCloseImage.setEnabled(value);
        submitButton.setEnabled(value);

        if(value) {
            dialogCloseImage.setColorFilter(colorRed);
            locateImage.setColorFilter(colorBlue);
            locateImage2.setColorFilter(colorBlue);
            tvLocate.setTextColor(colorBlue);
            tvLocate2.setTextColor(colorBlue);
        }
        else {
            dialogCloseImage.setColorFilter(colorInitial);
            locateImage.setColorFilter(colorInitial);
            locateImage2.setColorFilter(colorInitial);
            tvLocate.setTextColor(colorInitial);
            tvLocate2.setTextColor(colorInitial);
        }
    }

    private void openMap() {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", station.getId());
        intent.putExtra("lat", station.getLat());
        intent.putExtra("lng", station.getLng());
        intent.putExtra("name", station.getName());
        intent.putExtra("type", 1);
        myContext.startActivity(intent);
    }

    private void openMap2() {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", endStation.getId());
        intent.putExtra("lat", endStation.getLat());
        intent.putExtra("lng", endStation.getLng());
        intent.putExtra("name", endStation.getName());
        intent.putExtra("type", 1);
        myContext.startActivity(intent);
    }

    private void initAllSpotsDialog() {
        dialog2 = new Dialog(myContext);
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog2.setContentView(R.layout.dialog_all_spot_layout);

        dialogCloseImage2 = dialog2.findViewById(R.id.dialogCloseImage);
        tlSearch = dialog2.findViewById(R.id.tlSearch);
        acSearch = dialog2.findViewById(R.id.acSearch);

        tvLog4p3 = dialog2.findViewById(R.id.tvLog4p3);
        progressBar4p3 = dialog2.findViewById(R.id.progressBar4p3);
        reloadImage4p3 = dialog2.findViewById(R.id.reloadImage4p3);

        touristSpotView = dialog2.findViewById(R.id.touristSpotView);

        dialogCloseImage2.setOnClickListener(view -> dialog2.dismiss());

        acSearch.setOnFocusChangeListener((view1, b) -> {
            if(b) {
                tlSearch.setStartIconTintList(cslBlue);
            }
            else {
                tlSearch.setStartIconTintList(cslInitial);
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

        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog2.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog2.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void getTouristSpots() {
        tlSearch.setVisibility(View.GONE);
        tvLog4p3.setVisibility(View.GONE);
        reloadImage4p3.setVisibility(View.GONE);
        progressBar4p3.setVisibility(View.VISIBLE);
        touristSpotView.setVisibility(View.INVISIBLE);

        Query touristSpotsQuery = firebaseDatabase.getReference("touristSpots")
                .orderByChild("deactivated").equalTo(false);
        touristSpotsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                touristSpotList.clear();
                copy.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        SimpleTouristSpot touristSpot = new SimpleTouristSpot(dataSnapshot);
                        touristSpotList.add(touristSpot);
                    }

                    copy.addAll(touristSpotList);

                    if(touristSpotList.size() > 0) finishLoading4p3();
                    else setLogText4p3(noRouteLogText);
                }
                else {
                    setLogText4p3(noRouteLogText);
                    if(currentStep >= 4) errorLoading4p3();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();

                setLogText4p3(error.toString());
                if(currentStep >= 4) errorLoading4p3();
            }
        });
    }

    private void searchTouristSpot() {
        String value = acSearch.getText().toString().trim();
        List<SimpleTouristSpot> temp = new ArrayList<>();

        if(!value.isEmpty()) {
            for(SimpleTouristSpot touristSpot : copy) {
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
        sortByNames();
        temp.clear();

        if(touristSpotList.size() == 0) {
            String caption;

            if(copy.size() == 0) {
                caption = defaultLogText;
            }
            else {
                caption = "Searching for \"" + value + "\"\n No Record Found";
            }

            tvLog4p3.setText(caption);
            tvLog4p3.setVisibility(View.VISIBLE);
            reloadImage4p3.setVisibility(View.VISIBLE);
            touristSpotView.setVisibility(View.INVISIBLE);
        }
        else {
            tvLog4p3.setVisibility(View.GONE);
            reloadImage4p3.setVisibility(View.GONE);
            touristSpotView.setVisibility(View.VISIBLE);
        }
    }

    private void sortByNames() {
        Collections.sort(touristSpotList, (touristSpot, t1) ->
                touristSpot.getName().compareToIgnoreCase(t1.getName()));

        allSpotAdapter.notifyDataSetChanged();
    }

    private void getBookingTypes() {
        tvLog1.setVisibility(View.GONE);
        reloadImage1.setVisibility(View.GONE);
        progressBar1.setVisibility(View.VISIBLE);

        Query bookingTypesQuery = firebaseDatabase.getReference("bookingTypeList")
                .orderByChild("deactivated").equalTo(false);
        bookingTypesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingTypeList.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        BookingType bookingType = new BookingType(dataSnapshot);
                        bookingTypeList.add(bookingType);
                    }
                    finishLoading1();
                }
                else {
                    setLogText1(defaultLogText);
                    if(currentStep >= 1) errorLoading1();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();

                setLogText1(error.toString());
                if(currentStep >= 1) errorLoading1();
            }
        });
    }

    @Override
    public void sendBookingType(BookingType bookingType) {
        this.bookingType = bookingType;
        checkBookingTypeContinueButton();
    }

    private void checkBookingTypeContinueButton() {
        if(bookingType != null) {
            continueButton.setEnabled(bookingType.getId() != null);
        }
        else {
            continueButton.setEnabled(false);
        }
    }

    private void getStations() {
        tvLog2.setVisibility(View.GONE);
        reloadImage2.setVisibility(View.GONE);
        progressBar2.setVisibility(View.VISIBLE);

        Query stationsQuery = firebaseDatabase.getReference("stations")
                .orderByChild("deactivated").equalTo(false);
        stationsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stationList.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Station station = dataSnapshot.getValue(Station.class);
                        stationList.add(station);
                    }
                    finishLoading2();
                }
                else {
                    setLogText2(defaultLogText);
                    if(currentStep >= 2) errorLoading2();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();

                setLogText2(error.toString());
                if(currentStep >= 2) errorLoading2();
            }
        });
    }

    @Override
    public void sendStation(Station station) {
        this.station = station;
        checkStationContinueButton();
    }

    private void checkStationContinueButton() {
        if(station != null) {
            continueButton.setEnabled(station.getId() != null);
        }
        else {
            continueButton.setEnabled(false);
        }
    }

    private void getBookingRoutes() {
        tvLog3.setVisibility(View.GONE);
        reloadImage3.setVisibility(View.GONE);
        progressBar3.setVisibility(View.VISIBLE);

        DatabaseReference routesRef = firebaseDatabase.getReference("bookingTypeList")
                .child(bookingType.getId()).child("routeList");
        routesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingTypeRouteList.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        BookingTypeRoute bookingTypeRoute = new BookingTypeRoute(dataSnapshot);
                        if(bookingTypeRoute.getStartStation().getId().equals(station.getId())) {
                            bookingTypeRouteList.add(bookingTypeRoute);
                        }
                    }

                    if(bookingTypeRouteList.size() > 0) finishLoading3();
                    else {
                        setLogText3(noRouteLogText);
                        if(currentStep >= 3) errorLoading3();
                    }
                }
                else {
                    setLogText3(noRouteLogText);
                    if(currentStep >= 3) errorLoading3();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();
                
                setLogText3(error.toString());
                if(currentStep >= 3) errorLoading3();
            }
        });
    }

    @Override
    public void sendRoute(BookingTypeRoute bookingTypeRoute) {
        this.bookingTypeRoute = bookingTypeRoute;
        checkRouteContinueButton();
    }

    private void checkRouteContinueButton() {
        if(bookingTypeRoute != null) {
            continueButton.setEnabled(bookingTypeRoute.getId() != null);
        }
        else {
            continueButton.setEnabled(false);
        }
    }

    private void getNearSpots() {
        customizeButton.setEnabled(false);
        progressBar3p1.setVisibility(View.VISIBLE);

        Query touristSpotsQuery = firebaseDatabase.getReference("touristSpots")
                .orderByChild("deactivated").equalTo(false);
        touristSpotsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nearSpots.clear();

                if(spots.size() == 0)
                    recommendedSpots.clear();

                if(station == null) {
                    setLogText3p1(failedNearSpotsText);
                    if(currentStep >= 3) customizeButton.setEnabled(false);
                    return;
                }

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DetailedTouristSpot touristSpot = new DetailedTouristSpot(dataSnapshot);
                        List<Station> nearStations = touristSpot.getNearStations();

                        for(Station nearStation : nearStations) {
                            if(nearStation.getId().equals(station.getId())) {
                                SimpleTouristSpot nearSpot = new SimpleTouristSpot(dataSnapshot);
                                nearSpots.add(nearSpot);
                            }
                        }
                    }

                    if(spots.size() == 0)
                        recommendedSpots.addAll(nearSpots);

                    if(nearSpots.size() > 0) finishLoading3p1();
                    else {
                        String logText = noNearSpotsText + station.getName();
                        setLogText3p1(logText);
                    }
                }
                else setLogText3p1(defaultLogText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();

                String logText = failedNearSpotsText + station.getName();
                setLogText3p1(logText);
                if(currentStep >= 3) customizeButton.setEnabled(false);
            }
        });
    }

    @Override
    public void addRoute(List<SimpleTouristSpot> spots, BookingTypeRoute bookingTypeRoute) {

    }

    @Override
    public void removeSpot(SimpleTouristSpot spot) {
        List<SimpleTouristSpot> newSpots = new ArrayList<>();

        for(SimpleTouristSpot newSpot : spots) {
            if(!(newSpot.getId().equals(spot.getId()))) {
                newSpots.add(newSpot);
            }
        }
        updateSelectedSpots(newSpots);
    }

    private void checkSelectedSpotContinueButton() {
        if(spots.size() > 2) {
            continueButton.setEnabled(true);
            tvCaption.setText(routeSpotsCaptionText);
            tvCaption.setTextColor(colorBlack);
        }
        else {
            continueButton.setEnabled(false);
            tvCaption.setText(threeSpotsCaptionText);
            tvCaption.setTextColor(colorRed);
        }
    }

    private void getRecommendedSpots() {
        progressBar4p1.setVisibility(View.VISIBLE);

        if(spots.size() == 0) {
            recommendedSpots.clear();
            recommendedSpots.addAll(nearSpots);
            finishLoading4p1();
        }
        else {
            DatabaseReference touristSpotsRef = firebaseDatabase.getReference("touristSpots")
                    .child(spots.get(spots.size() - 1).getId());
            touristSpotsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list1.clear(); list2.clear();
                    recommendedSpots.clear();

                    if(station == null) {
                        setLogText4p1(failedRecommendedSpotsText);
                        if(currentStep >= 4) errorLoading3();
                        return;
                    }

                    if(snapshot.exists()) {
                        DetailedTouristSpot touristSpot = new DetailedTouristSpot(snapshot);
                        for(SimpleTouristSpot nearSpot : touristSpot.getNearSpots()) {
                            if(isInSelectedSpots(nearSpot)) {
                                list2.add(nearSpot);
                            }
                            else {
                                list1.add(nearSpot);
                            }
                        }

                        recommendedSpots.addAll(list1);
                        recommendedSpots.addAll(list2);

                        if(recommendedSpots.size() > 0) finishLoading4p1();
                        else {
                            String logText = noRecommendedSpotsText +
                                    spots.get(spots.size() - 1).getName();
                            setLogText4p1(logText);
                        }
                    }
                    else setLogText4p1(defaultLogText);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(
                            myContext,
                            error.toString(),
                            Toast.LENGTH_SHORT
                    ).show();


                    String logText;
                    if(spots.size() == 0) {
                        logText = failedRecommendedSpotsText + station.getName();
                    }
                    else {
                        logText = failedRecommendedSpotsText + spots.get(spots.size() - 1).getName();
                    }
                    setLogText4p1(logText);
                    if(currentStep >= 3) errorLoading4p1();
                }
            });
        }
    }

    @Override
    public void addSpot(SimpleTouristSpot spot) {
        List<SimpleTouristSpot> newSpots = new ArrayList<>(spots);
        newSpots.add(spot);
        updateSelectedSpots(newSpots);

        if(currentStep != 4) {
            goToStep4FromStep3();

            if(currentStep < endStep) {
                currentStep++;
                tvSteps.setText(getStepText());
            }
        }
    }

    @Override
    public void viewAll() {
        if(dialog2 != null) dialog2.show();
    }

    private void updateSelectedSpots(List<SimpleTouristSpot> newSpots) {
        spots = new ArrayList<>(newSpots);

        BookingSpotAdapter bookingSpotAdapter = (BookingSpotAdapter) spotView.getAdapter();
        if(bookingSpotAdapter != null) bookingSpotAdapter.setSpots(spots);

        SelectedSpotAdapter selectedSpotAdapter = (SelectedSpotAdapter) selectedSpotView.getAdapter();
        if(selectedSpotAdapter != null) selectedSpotAdapter.setSpots(spots);

        if(spots.size() == 0) {
            tvLog4.setText(noSelectedSpotText);
            tvLog4.setVisibility(View.VISIBLE);
            reloadImage4.setVisibility(View.VISIBLE);
            selectedSpotView.setVisibility(View.INVISIBLE);

            routeSpotsLayout.setVisibility(View.GONE);
            endStationLayout.setVisibility(View.GONE);
        }
        else {
            tvLog4.setVisibility(View.GONE);
            reloadImage4.setVisibility(View.GONE);
            selectedSpotView.setVisibility(View.VISIBLE);

            routeSpotsLayout.setVisibility(View.VISIBLE);
            getNearStations();
        }
        progressBar4.setVisibility(View.GONE);

        String spotCountText = defaultSpotCountText + spots.size();
        tvSpotCount.setText(spotCountText);

        getRecommendedSpots();
        checkSelectedSpotContinueButton();
    }

    private void getNearStations() {
        DatabaseReference touristSpotsRef = firebaseDatabase.getReference("touristSpots")
                .child(spots.get(spots.size() - 1).getId());
        touristSpotsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                endStations.clear();
                endStationsText.clear();

                if(snapshot.exists()) {
                    DetailedTouristSpot touristSpot = new DetailedTouristSpot(snapshot);
                    List<Station> nearStations = touristSpot.getNearStations();

                    for(Station nearStation : nearStations) {
                        endStations.add(nearStation);
                        endStationsText.add(nearStation.getName());
                    }

                    if(endStations.size() > 0) finishLoading4p2();
                    else errorLoading4p2();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();

                errorLoading4p2();
            }
        });
    }

    private void getScheduleTime() {
        tvLog5.setVisibility(View.GONE);
        reloadImage5.setVisibility(View.GONE);
        progressBar5.setVisibility(View.VISIBLE);
        scheduleTimeView.setVisibility(View.INVISIBLE);

        DatabaseReference scheduleTimeListRef = firebaseDatabase.getReference("scheduleTimeList");
        scheduleTimeListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                scheduleTimeList.clear();

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ScheduleTime scheduleTime = dataSnapshot.getValue(ScheduleTime.class);
                        scheduleTimeList.add(scheduleTime);
                    }

                    finishLoading5();
                }
                else {
                    setLogText5(defaultLogText);
                    if(currentStep >= 5) errorLoading5();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();

                setLogText5(error.toString());
                if(currentStep >= 5) errorLoading5();
            }
        });
    }

    @Override
    public void sendScheduleTime(ScheduleTime scheduleTime) {
        this.scheduleTime = scheduleTime;
        bookingScheduleText = dateTimeToString.getDate() + " | " + scheduleTime.getTime();

        checkScheduleContinueButton();
    }

    private void checkScheduleContinueButton() {
        continueButton.setEnabled((vSD && !(scheduleTime == null)));
    }

    private void rebootStep(int targetStep) {
        firstConstraint.setVisibility(View.GONE);
        secondConstraint.setVisibility(View.GONE);
        thirdConstraint.setVisibility(View.GONE);
        fourthConstraint.setVisibility(View.GONE);

        tvCaption.setText(defaultCaptionText);

        switch (targetStep) {
            case 1:
                tvActivityName.setText(bookingTypeActivityText);
                firstConstraint.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.GONE);
                break;
            case 2:
                tvActivityName.setText(stationActivityText);
                secondConstraint.setVisibility(View.VISIBLE);
                break;
            case 3:
                tvActivityName.setText(recommendedRouteActivityText);
                thirdConstraint.setVisibility(View.VISIBLE);
                break;
            case 4:
                tvActivityName.setText(listOfRouteActivityText);
                fourthConstraint.setVisibility(View.VISIBLE);
                if(bookingTypeRoute.getSpots().size() > 0) tvCaption.setText(routeSpotsCaptionText);
                else tvCaption.setText(defaultCaptionText);
                break;
        }

        currentStep = targetStep;

        Toast.makeText(
                myContext,
                "There are changes in the database " +
                        "that might affect your booking.",
                Toast.LENGTH_LONG
        ).show();
    }

    private void finishLoading1() {
        bookingTypeAdapter.notifyDataSetChanged();

        int targetStep = 1;
        if(!isInBookingTypes()) {
            bookingType = null;
            bookingTypeAdapter.setBookingTypeId(null);
            checkBookingTypeContinueButton();
            if(currentStep > targetStep) rebootStep(targetStep);
        }

        progressBar1.setVisibility(View.GONE);
        bookingTypeView.setVisibility(View.VISIBLE);
    }

    private boolean isInBookingTypes() {
        if(bookingType != null && bookingTypeList.size() > 0) {
            for(BookingType bookingType1 : bookingTypeList) {
                if(bookingType1.getId().equals(bookingType.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setLogText1(String value) {
        bookingTypeList.clear();
        bookingTypeAdapter.notifyDataSetChanged();

        tvLog1.setText(value);
        tvLog1.setVisibility(View.VISIBLE);
        reloadImage1.setVisibility(View.VISIBLE);
        progressBar1.setVisibility(View.GONE);
        bookingTypeView.setVisibility(View.INVISIBLE);
    }

    private void errorLoading1() {
        int targetStep = 1;
        bookingType = null;
        bookingTypeAdapter.setBookingTypeId(null);
        checkBookingTypeContinueButton();
        if(currentStep > targetStep) rebootStep(targetStep);
    }

    private void finishLoading2() {
        bookingStationAdapter.notifyDataSetChanged();

        int targetStep = 2;
        if(!isInStations()) {
            station = null;
            bookingStationAdapter.setStationId(null);
            if(currentStep != 1) checkStationContinueButton();
            if(currentStep > targetStep) rebootStep(targetStep);
        }

        progressBar2.setVisibility(View.GONE);
        bookingStationView.setVisibility(View.VISIBLE);
    }

    private boolean isInStations() {
        if(station != null && stationList.size() > 0) {
            for(Station station1 : stationList) {
                if(station1.getId().equals(station.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setLogText2(String value) {
        stationList.clear();
        bookingStationAdapter.notifyDataSetChanged();

        tvLog2.setText(value);
        tvLog2.setVisibility(View.VISIBLE);
        reloadImage2.setVisibility(View.VISIBLE);
        progressBar2.setVisibility(View.GONE);
        bookingStationView.setVisibility(View.INVISIBLE);
    }

    private void errorLoading2() {
        int targetStep = 2;
        station = null;
        bookingStationAdapter.setStationId(null);
        if(currentStep != 1) checkStationContinueButton();
        if(currentStep > targetStep) rebootStep(targetStep);
    }

    private void finishLoading3() {
        bookingRouteAdapter.notifyDataSetChanged();

        int targetStep = 3;
        if(!isInBookingRoutes()) {
            bookingTypeRoute = null;
            bookingRouteAdapter.setRouteId(null);
            if(currentStep != 1) checkRouteContinueButton();
            if(currentStep > targetStep) rebootStep(targetStep);
        }

        progressBar3.setVisibility(View.GONE);
        recommendedRouteView.setVisibility(View.VISIBLE);
    }

    private boolean isInBookingRoutes() {
        if(bookingTypeRoute != null && bookingTypeRouteList.size() > 0) {
            for(BookingTypeRoute bookingTypeRoute1 : bookingTypeRouteList) {
                if(bookingTypeRoute1.getId().equals(bookingTypeRoute.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setLogText3(String value) {
        bookingTypeRouteList.clear();
        bookingRouteAdapter.notifyDataSetChanged();

        tvLog3.setText(value);
        tvLog3.setVisibility(View.VISIBLE);
        reloadImage3.setVisibility(View.VISIBLE);
        progressBar3.setVisibility(View.GONE);
        recommendedRouteView.setVisibility(View.INVISIBLE);
    }

    private void errorLoading3() {
        int targetStep = 3;
        bookingTypeRoute = null;
        bookingRouteAdapter.setRouteId(null);
        if(currentStep != 1) checkRouteContinueButton();
        if(currentStep > targetStep) rebootStep(targetStep);
    }

    private void finishLoading3p1() {
        nearSpotAdapter.notifyDataSetChanged();

        showImage.setVisibility(View.VISIBLE);
        tvShowNearSpots.setVisibility(View.VISIBLE);
        tvNearSpots.setPadding(0, 0, dpToPx(88), 0);

        String nearSpotsText = nearSpots.size() + " " + defaultNearSpotsText + station.getName();
        tvNearSpots.setText(nearSpotsText);
        customizeButton.setEnabled(true);
        progressBar3p1.setVisibility(View.GONE);
    }

    private void setLogText3p1(String value) {
        nearSpots.clear();
        nearSpotAdapter.notifyDataSetChanged();

        showImage.setVisibility(View.GONE);
        tvShowNearSpots.setVisibility(View.GONE);

        if(tvShowNearSpots.getText().equals(hideText)){
            tvShowNearSpots.setEnabled(false);
            showImage.setEnabled(false);
            transition1();
        }

        tvNearSpots.setPadding(0, 0, 0, 0);

        tvNearSpots.setText(value);
        customizeButton.setEnabled(true);
        progressBar3p1.setVisibility(View.GONE);
    }

    private boolean isInSelectedSpots(SimpleTouristSpot targetSpot) {
        for(SimpleTouristSpot selectedSpot : spots) {
            if(selectedSpot.getId().equals(targetSpot.getId())) {
                return true;
            }
        }
        return false;
    }

    private void finishLoading4p1() {
        recommendedSpotAdapter.setSelectedSpots(spots);
        allSpotAdapter.setSelectedSpots(spots);

        showImage2.setVisibility(View.VISIBLE);
        tvShowRecommendedSpots.setVisibility(View.VISIBLE);
        tvRecommendedSpots.setPadding(0, 0, dpToPx(88), 0);

        String recommendedSpotsText;
        if(spots.size() == 0) {
            recommendedSpotsText =
                    recommendedSpots.size() + " " + defaultRecommendedSpotsText + station.getName();
        }
        else {
            recommendedSpotsText =
                    recommendedSpots.size() + " " + defaultRecommendedSpotsText +
                            spots.get(spots.size() - 1).getName();
        }

        if(spots.size() < 3 && tvShowRecommendedSpots.getText().equals(showText) &&
                showImage2.isEnabled()) {

            tvShowRecommendedSpots.setEnabled(false);
            showImage2.setEnabled(false);
            new Handler().postDelayed(() -> transition4(), 300);
        }

        tvRecommendedSpots.setText(recommendedSpotsText);
        progressBar4p1.setVisibility(View.GONE);
    }

    private void setLogText4p1(String value) {
        recommendedSpots.clear();
        recommendedSpotAdapter.notifyDataSetChanged();

        showImage2.setVisibility(View.GONE);
        tvShowRecommendedSpots.setVisibility(View.GONE);

        if(tvShowRecommendedSpots.getText().equals(hideText)) {
            tvShowRecommendedSpots.setEnabled(false);
            showImage2.setEnabled(false);
            transition4();
        }

        tvRecommendedSpots.setPadding(0, 0, 0, 0);

        tvRecommendedSpots.setText(value);
        progressBar4p1.setVisibility(View.GONE);
    }

    private void errorLoading4p1() {
        int targetStep = 4;
        if(currentStep != 1) checkSelectedSpotContinueButton();
        if(currentStep > targetStep) rebootStep(targetStep);
    }

    private void finishLoading4p2() {
        endStation = endStations.get(0);
        endStationText = endStationsText.get(0);
        acEndStation.setText(endStationText);
        tvEndStation2.setText(endStationText);

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(myContext, R.layout.simple_list_item_layout, endStationsText);
        acEndStation.setAdapter(arrayAdapter);
        acEndStation.clearFocus();

        endStationLayout.setVisibility(View.VISIBLE);
    }

    private void errorLoading4p2() {
        endStations.clear();
        endStationsText.clear();

        endStation = null;
        endStationText = "Station Near " + spots.get(spots.size() - 1).getName();
        tvEndStation2.setText(endStationText);

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(myContext, R.layout.simple_list_item_layout, endStationsText);
        acEndStation.setAdapter(arrayAdapter);
        acEndStation.clearFocus();

        endStationLayout.setVisibility(View.GONE);
    }

    private void finishLoading4p3() {
        searchTouristSpot();

        int targetStep = 4;
        if(!areSelectedSpotsExisting()) {
            if(currentStep != 1) checkSelectedSpotContinueButton();
            if(currentStep > targetStep) rebootStep(targetStep);
        }

        List<String> touristSpotsText = new ArrayList<>();
        for(SimpleTouristSpot touristSpot : touristSpotList) {
            touristSpotsText.add(touristSpot.getName());
        }

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(myContext, R.layout.simple_list_item_layout, touristSpotsText);
        acSearch.setAdapter(arrayAdapter);

        tlSearch.setVisibility(View.VISIBLE);
        tvLog4p3.setVisibility(View.GONE);
        reloadImage4p3.setVisibility(View.GONE);
        progressBar4p3.setVisibility(View.GONE);
        touristSpotView.setVisibility(View.VISIBLE);
    }

    private boolean areSelectedSpotsExisting() {
        boolean result = true;

        if(spots.size() > 0) {
            for(SimpleTouristSpot selectedSpot : spots) {
                if(!isInTouristSpots(selectedSpot)) {
                    removeSpot(selectedSpot);
                    result = false;
                }
            }
        }
        return result;
    }

    private boolean isInTouristSpots(SimpleTouristSpot targetSpot) {
        for (SimpleTouristSpot touristSpot : touristSpotList) {
            if (touristSpot.getId().equals(targetSpot.getId())) {
                return true;
            }
        }
        return false;
    }

    private void setLogText4p3(String value) {
        touristSpotList.clear();
        copy.clear();
        allSpotAdapter.notifyDataSetChanged();

        List<String> touristSpotsText = new ArrayList<>();
        for(SimpleTouristSpot touristSpot : touristSpotList) {
            touristSpotsText.add(touristSpot.getName());
        }

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(myContext, R.layout.simple_list_item_layout, touristSpotsText);
        acSearch.setAdapter(arrayAdapter);

        tlSearch.setVisibility(View.GONE);
        tvLog4p3.setText(value);
        tvLog4p3.setVisibility(View.VISIBLE);
        reloadImage4p3.setVisibility(View.VISIBLE);
        progressBar4p3.setVisibility(View.GONE);
        touristSpotView.setVisibility(View.INVISIBLE);
    }

    private void errorLoading4p3() {
        int targetStep = 4;
        removeAllSelectedSpots();
        if(currentStep != 1) checkSelectedSpotContinueButton();
        if(currentStep > targetStep) rebootStep(targetStep);
    }

    private void removeAllSelectedSpots() {
        if(spots.size() > 0) {
            for(SimpleTouristSpot selectedSpot : spots) {
                removeSpot(selectedSpot);
            }
        }
    }

    private void finishLoading5() {
        scheduleTimeAdapter.notifyDataSetChanged();

        int targetStep = 5;
        if(!isInScheduleTime()) {
            scheduleTime = null;
            scheduleTimeAdapter.setScheduleTimeId(null);
            if(currentStep != 1) checkScheduleContinueButton();
            if(currentStep > targetStep) rebootStep(targetStep);
        }

        progressBar5.setVisibility(View.GONE);
        scheduleTimeView.setVisibility(View.VISIBLE);
    }

    private boolean isInScheduleTime() {
        if(scheduleTime != null && scheduleTimeList.size() > 0) {
            for(ScheduleTime scheduleTime1 : scheduleTimeList) {
                if(scheduleTime1.getId().equals(scheduleTime.getId()) && !scheduleTime1.isDeactivated()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setLogText5(String value) {
        scheduleTimeList.clear();
        scheduleTimeAdapter.notifyDataSetChanged();

        tvLog5.setText(value);
        tvLog5.setVisibility(View.VISIBLE);
        reloadImage5.setVisibility(View.VISIBLE);
        progressBar5.setVisibility(View.GONE);
        scheduleTimeView.setVisibility(View.INVISIBLE);
    }

    private void errorLoading5() {
        int targetStep = 5;
        scheduleTime = null;
        scheduleTimeAdapter.setScheduleTimeId(null);
        if(currentStep != 1) checkScheduleContinueButton();
        if(currentStep > targetStep) rebootStep(targetStep);
    }

    private int dpToPx(int dp) {
        float px = dp * myContext.getResources().getDisplayMetrics().density;
        return (int) px;
    }
}