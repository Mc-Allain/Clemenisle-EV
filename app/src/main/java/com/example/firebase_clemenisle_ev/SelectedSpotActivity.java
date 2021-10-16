package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.firebase_clemenisle_ev.Adapters.NearSpotAdapter;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.DetailedTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.Route;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.Station;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SelectedSpotActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ImageView thumbnail, likeImage, visitImage, bookImage, moreImage, i360Image, locateImage, homeImage;
    TextView tvName, tvStation, tvLikes, tvVisits, tvBooks, tvNearSpot, tvLiked, tvOption,
            tv360Image, tvLocate;
    ExpandableTextView extvDescription;
    ConstraintLayout backgroundLayout, buttonLayout, connectingLayout;
    RecyclerView nearSpotView;

    Context myContext;

    String userId;

    boolean loggedIn = false;

    String id, name, description, img;
    int likes, visits, books;
    double lat, lng;
    List<SimpleTouristSpot> nearSpots = new ArrayList<>();
    List<Station> nearStations;
    boolean deactivated;
    StringBuilder stations;

    NearSpotAdapter nearSpotAdapter;

    List<SimpleTouristSpot> likedSpots = new ArrayList<>();
    SimpleTouristSpot selectedSpot;

    boolean onScreen = false;
    String liked;

    Handler optionHandler = new Handler();
    Runnable optionRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_spot);

        thumbnail = findViewById(R.id.thumbnail);
        tvName = findViewById(R.id.tvName);
        tvStation = findViewById(R.id.tvStartStation2);
        extvDescription = findViewById(R.id.extvDescription);
        likeImage = findViewById(R.id.likeImage);
        visitImage = findViewById(R.id.visitImage);
        bookImage = findViewById(R.id.bookImage);
        tvLikes = findViewById(R.id.tvLikes);
        tvVisits = findViewById(R.id.tvVisits);
        tvBooks = findViewById(R.id.tvBooks);
        moreImage = findViewById(R.id.moreImage);
        backgroundLayout = findViewById(R.id.backgroundLayout);
        buttonLayout = findViewById(R.id.buttonLayout);
        tvNearSpot = findViewById(R.id.tvNearSpot);
        nearSpotView = findViewById(R.id.nearSpotView);
        tvLiked = findViewById(R.id.tvLiked);
        tvOption = findViewById(R.id.tvOption);
        i360Image = findViewById(R.id.i360Image);
        locateImage = findViewById(R.id.locateImage);
        tv360Image = findViewById(R.id.tv360Image);
        tvLocate = findViewById(R.id.tvLocate);
        connectingLayout = findViewById(R.id.connectingLayout);
        homeImage = findViewById(R.id.homeImage);

        myContext = SelectedSpotActivity.this;

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        loggedIn = intent.getBooleanExtra("loggedIn", false);

        onScreen = true;

        firebaseAuth = FirebaseAuth.getInstance();
        if(loggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) {
                firebaseUser.reload();
                userId = firebaseUser.getUid();
                getLikedSpots();
            }
        }

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false);
        nearSpotView.setLayoutManager(linearLayout);
        nearSpotAdapter = new NearSpotAdapter(myContext, nearSpots, loggedIn);
        nearSpotView.setAdapter(nearSpotAdapter);

        getTouristSpots();

        likeImage.setOnClickListener(view -> {
            if(loggedIn) {
                if(firebaseUser != null) {
                    likeImage.setEnabled(false);
                    if(liked.equals("false")) {
                        likeSpot(selectedSpot);
                    }
                    else{
                        unlikeSpot();
                    }
                }
            }
            else {
                Intent newIntent = new Intent(myContext, LoginActivity.class);
                myContext.startActivity(newIntent);
            }
        });

        likeImage.setOnLongClickListener(view -> {
            Toast.makeText(myContext,
                    "Likes: " + tvLikes.getText(),
                    Toast.LENGTH_SHORT).show();
            return false;
        });

        visitImage.setOnLongClickListener(view -> {
            Toast.makeText(myContext,
                    "Visits: " + tvVisits.getText(),
                    Toast.LENGTH_SHORT).show();
            return false;
        });

        bookImage.setOnLongClickListener(view -> {
            Toast.makeText(myContext,
                    "Books: " + tvBooks.getText(),
                    Toast.LENGTH_SHORT).show();
            return false;
        });

        moreImage.setOnClickListener(view -> {
            if(tvOption.getText().equals("false")) {
                openOption();
            }
            else {
                closeOption();
            }
        });

        homeImage.setOnClickListener(view -> backToHome());

        thumbnail.setOnClickListener(view -> openStreetView(id));

        tv360Image.setOnClickListener(view -> openStreetView(id));

        i360Image.setOnClickListener(view -> openStreetView(id));

        tvLocate.setOnClickListener(view -> openMap(id, lat, lng, name, 0));

        locateImage.setOnClickListener(view -> openMap(id, lat, lng, name, 0));
    }

    private void likeSpot(SimpleTouristSpot touristSpot) {
        DatabaseReference usersRef = firebaseDatabase.getReference("users")
                .child(userId).child("likedSpots");
        usersRef.child(id).setValue(touristSpot);
    }

    private void unlikeSpot() {
        DatabaseReference usersRef = firebaseDatabase.getReference("users")
                .child(userId).child("likedSpots");
        usersRef.child(id).setValue(null);
    }

    private boolean isInLikedSpots(SimpleTouristSpot targetSpot) {
        for(SimpleTouristSpot likedSpot : likedSpots) {
            if(likedSpot.getId().equals(targetSpot.getId())) {
                return true;
            }
        }
        return false;
    }

    private void backToHome() {
        Intent newIntent = new Intent(myContext, MainActivity.class);
        startActivity(newIntent);
        finishAffinity();
    }

    private void openMap(String id, double lat, double lng, String name, int type) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        intent.putExtra("name", name);
        intent.putExtra("type", type);
        myContext.startActivity(intent);
    }

    private void openStreetView(String id) {
        DatabaseReference vriRef = firebaseDatabase.getReference("touristSpots")
                .child(id).child("vri");
        vriRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Intent intent = new Intent(myContext, StreetWebView.class);
                    intent.putExtra("id", id);
                    myContext.startActivity(intent);
                }
                else {
                    Toast.makeText(
                            myContext,
                            "No Street View Record",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void openOption() {
        moreImage.setEnabled(false);

        optionHandler.removeCallbacks(optionRunnable);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(connectingLayout);

        constraintSet.clear(buttonLayout.getId(), ConstraintSet.TOP);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.TOP,
                moreImage.getId(), ConstraintSet.TOP);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.BOTTOM,
                moreImage.getId(), ConstraintSet.BOTTOM);

        setTransition(buttonLayout);
        constraintSet.applyTo(connectingLayout);

        tvOption.setText("true");
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_close_24);
        moreImage.setColorFilter(myContext.getResources().getColor(R.color.red));

        optionRunnable = () -> closeOption();

        optionHandler.postDelayed(optionRunnable, 3000);
    }

    private void closeOption() {
        moreImage.setEnabled(false);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(connectingLayout);

        constraintSet.clear(buttonLayout.getId(), ConstraintSet.TOP);
        constraintSet.clear(buttonLayout.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.TOP,
                connectingLayout.getId(), ConstraintSet.BOTTOM);

        setTransition(buttonLayout);
        constraintSet.applyTo(connectingLayout);

        tvOption.setText("false");
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_more_horiz_24);
        moreImage.setColorFilter(myContext.getResources().getColor(R.color.black));
    }

    private void setTransition(ConstraintLayout constraintLayout) {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);
        TransitionManager.beginDelayedTransition(constraintLayout, transition);
    }

    private void getTouristSpots() {
        DatabaseReference touristSpotsRef = firebaseDatabase.getReference("touristSpots")
                .child(id);
        touristSpotsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nearSpots.clear();
                if(snapshot.exists()) {
                    DetailedTouristSpot touristSpot = new DetailedTouristSpot(snapshot);
                    getStats(touristSpot);
                    updateNearSpots(touristSpot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void updateNearSpots(DetailedTouristSpot touristSpot) {
        nearSpots.addAll(touristSpot.getNearSpots());

        if(nearSpots.size() == 0) {
            tvNearSpot.setVisibility(View.GONE);
            nearSpotView.setVisibility(View.GONE);
        }
        else {
            tvNearSpot.setVisibility(View.VISIBLE);
            nearSpotView.setVisibility(View.VISIBLE);
        }

        nearSpotAdapter.notifyDataSetChanged();
    }


    private void getStats(DetailedTouristSpot touristSpot) {
        DatabaseReference usersRef = firebaseDatabase.getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int books = 0, likes = 0, visits = 0;

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User thisUser = new User(dataSnapshot);

                        List<SimpleTouristSpot> userLikedSpots = thisUser.getLikedSpots();
                        for(SimpleTouristSpot likedSpot : userLikedSpots) {
                            if(likedSpot.getId().equals(id)) {
                                likes++;
                            }
                        }

                        List<Booking> userBookingList = thisUser.getBookingList();
                        for(Booking booking : userBookingList) {
                            List<Route> routeList = booking.getRouteList();
                            for(Route route : routeList) {
                                if(route.getId().equals(id)) {
                                    books++;
                                    if(route.isVisited()) {
                                        visits++;
                                    }
                                }
                            }
                        }
                    }
                }
                touristSpot.setBooks(books);
                touristSpot.setLikes(likes);
                touristSpot.setVisits(visits);
                setInfo(touristSpot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();

                touristSpot.setBooks(0);
                touristSpot.setLikes(0);
                touristSpot.setVisits(0);
                setInfo(touristSpot);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onScreen = false;
    }

    private void setInfo(DetailedTouristSpot touristSpot) {
        name = touristSpot.getName();
        description = touristSpot.getDescription();
        img = touristSpot.getImg();
        likes = touristSpot.getLikes();
        visits = touristSpot.getVisits();
        books = touristSpot.getBooks();
        lat = touristSpot.getLat();
        lng = touristSpot.getLng();
        nearStations = touristSpot.getNearStations();
        deactivated = touristSpot.isDeactivated();
        stations = new StringBuilder();

        selectedSpot = new SimpleTouristSpot(deactivated, id, img, name);

        boolean isFirst = true;
        for(Station nearStation : nearStations) {
            if(isFirst) {
                stations.append(nearStation.getName());
                isFirst = false;
            }
            else {
                stations.append(", ").append(nearStation.getName());
            }
        }

        if(onScreen) {
            updateInfo();
        }
    }

    private void updateInfo() {
        Glide.with(myContext).load(img).placeholder(R.drawable.image_loading_placeholder).
                override(Target.SIZE_ORIGINAL).into(thumbnail);
        tvName.setText(name);
        extvDescription.setText(description);
        tvStation.setText(stations);
        tvLikes.setText(String.valueOf(likes));
        tvVisits.setText(String.valueOf(visits));
        tvBooks.setText(String.valueOf(books));

        tvLiked.setText(String.valueOf(
                isInLikedSpots(selectedSpot)
        ));
        liked = tvLiked.getText().toString();

        int color;
        if(liked.equals("false")) {
            color = myContext.getResources().getColor(R.color.black);
        }
        else {
            color = myContext.getResources().getColor(R.color.blue);
        }
        likeImage.setColorFilter(color);

        likeImage.setEnabled(true);
    }

    private void getLikedSpots() {
        DatabaseReference usersRef = firebaseDatabase.getReference("users").child(userId);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likedSpots.clear();
                if(snapshot.exists()) {
                    User user = new User(snapshot);
                    likedSpots.addAll(user.getLikedSpots());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}