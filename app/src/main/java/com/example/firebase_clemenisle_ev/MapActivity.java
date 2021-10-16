package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.firebase_clemenisle_ev.Adapters.MapTabFragmentAdapter;
import com.example.firebase_clemenisle_ev.Classes.MapCoordinates;
import com.example.firebase_clemenisle_ev.Classes.Place;
import com.example.firebase_clemenisle_ev.Fragments.MapFragment;
import com.example.firebase_clemenisle_ev.Fragments.MapSettingsFragment;
import com.example.firebase_clemenisle_ev.Fragments.MapStationFragment;
import com.example.firebase_clemenisle_ev.Fragments.MapTouristSpotFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

public class MapActivity extends AppCompatActivity
implements MapTouristSpotFragment.OnComboBoxClickListener,
        MapStationFragment.OnComboBoxClickListener,
        MapSettingsFragment.OnMapSettingChangeListener {

    MapCoordinates mapCoordinates = new MapCoordinates();

    FrameLayout mapLayout;
    FloatingActionButton fab;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    ConstraintLayout constraintLayout, placeViewLayout, fabLayout;

    Context myContext;
    Resources myResources;

    MapFragment mapFragment = new MapFragment();
    MapTabFragmentAdapter tabAdapter;

    String id;
    int type;
    double lat, lng;
    String name;

    int bottom;
    boolean placeViewIsShow = false;

    ColorStateList cslRed, cslBlue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapLayout = findViewById(R.id.mapLayout);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        constraintLayout = findViewById(R.id.constraintLayout);
        placeViewLayout = findViewById(R.id.placeViewLayout);
        fabLayout = findViewById(R.id.fabLayout);

        myContext = MapActivity.this;
        myResources = getResources();

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        lat = intent.getDoubleExtra("lat", mapCoordinates.getInitialLatLng().latitude);
        lng = intent.getDoubleExtra("lng", mapCoordinates.getInitialLatLng().longitude);
        name = intent.getStringExtra("name");
        type = intent.getIntExtra("type", 0);

        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putDouble("lat", lat);
        bundle.putDouble("lng", lng);
        bundle.putString("name", name);
        bundle.putInt("type", type);
        mapFragment.setArguments(bundle);

        fab = findViewById(R.id.floatingActionButton);
        fab.setColorFilter(myResources.getColor(R.color.white));

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().replace(mapLayout.getId(), mapFragment).commit();

        tabAdapter = new MapTabFragmentAdapter(fragmentManager, getLifecycle(), id, type, name);
        viewPager.setAdapter(tabAdapter);

        cslRed = ColorStateList.valueOf(myResources.getColor(R.color.red));
        cslBlue = ColorStateList.valueOf(myResources.getColor(R.color.blue));

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        Typeface typeface = ResourcesCompat.getFont(myContext, R.font.ubuntu);

        ViewGroup viewGroup = (ViewGroup) tabLayout.getChildAt(0);
        int tabCount = viewGroup.getChildCount();

        for(int i = 0; i < tabCount; i++) {
            ViewGroup viewGroup1 = (ViewGroup) viewGroup.getChildAt(i);
            int tabChildrenCount = viewGroup1.getChildCount();

            for(int j = 0; j < tabChildrenCount; j++) {
                View view = viewGroup1.getChildAt(j);
                if(view instanceof TextView) {
                    ((TextView) view).setTypeface(typeface);
                }
            }
        }

        fab.setOnClickListener(view -> {
            fab.setEnabled(false);
            bottom = 0;
            constraintSet.clear(fabLayout.getId(), ConstraintSet.BOTTOM);
            constraintSet.clear(mapLayout.getId(), ConstraintSet.BOTTOM);

            transition1(constraintSet);
            transition2(constraintSet);
            transition3(constraintSet);

            fab.setColorFilter(myResources.getColor(R.color.white));
            placeViewIsShow = !placeViewIsShow;
            fab.setEnabled(true);
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    private void transition1(ConstraintSet constraintSet) {
        if(placeViewIsShow) {
            constraintSet.clear(placeViewLayout.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(placeViewLayout.getId(), ConstraintSet.TOP,
                    constraintLayout.getId(), ConstraintSet.BOTTOM);
        }
        else {
            constraintSet.clear(placeViewLayout.getId(), ConstraintSet.TOP);
            constraintSet.connect(placeViewLayout.getId(), ConstraintSet.BOTTOM,
                    constraintLayout.getId(), ConstraintSet.BOTTOM);
        }

        setTransition1();
        constraintSet.applyTo(constraintLayout);
    }

    private void transition2(ConstraintSet constraintSet) {
        if(placeViewIsShow) {
            constraintSet.connect(fabLayout.getId(), ConstraintSet.BOTTOM,
                    constraintLayout.getId(), ConstraintSet.BOTTOM);

            fab.setImageResource(R.drawable.ic_baseline_location_on_24);
            fab.setBackgroundTintList(cslBlue);

            bottom = dpToPx(32);
        }
        else {
            constraintSet.connect(fabLayout.getId(), ConstraintSet.BOTTOM,
                    placeViewLayout.getId(), ConstraintSet.TOP);

            fab.setImageResource(R.drawable.ic_baseline_close_24);
            fab.setBackgroundTintList(cslRed);

            bottom = dpToPx(8);
        }

        setTransition2();
        constraintSet.applyTo(constraintLayout);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) fab.getLayoutParams();
        layoutParams.setMargins(dpToPx(0), dpToPx(0), dpToPx(0), bottom);
        fab.setLayoutParams(layoutParams);
    }

    private void transition3(ConstraintSet constraintSet) {
        if(placeViewIsShow) {
            constraintSet.connect(mapLayout.getId(), ConstraintSet.BOTTOM,
                    constraintLayout.getId(), ConstraintSet.BOTTOM);
        }
        else {
            constraintSet.connect(mapLayout.getId(), ConstraintSet.BOTTOM,
                    placeViewLayout.getId(), ConstraintSet.TOP);
        }

        setTransition3();
        constraintSet.applyTo(constraintLayout);
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    private void setTransition1() {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);
        TransitionManager.beginDelayedTransition(placeViewLayout, transition);
    }

    private void setTransition2() {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);
        TransitionManager.beginDelayedTransition(fabLayout, transition);
    }

    private void setTransition3() {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);
        TransitionManager.beginDelayedTransition(mapLayout, transition);
    }

    @Override
    public void sendSelectedTouristSpots(List<Place> places, boolean selectAction) {
        if(selectAction) {
            mapFragment.markTouristSpot(places);
        }
        else {
            mapFragment.unmarkTouristSpot(places);
        }
    }

    @Override
    public void sendSelectedTouristSpot(Place selectedPlace) {
        mapFragment.selectPlace(selectedPlace, 0);
    }

    @Override
    public void sendSelectedStations(List<Place> places, boolean selectAction) {
        if(selectAction) {
            mapFragment.markStation(places);
        }
        else {
            mapFragment.unmarkStations(places);
        }
    }

    @Override
    public void sendSelectedStation(Place selectedPlace) {
        mapFragment.selectPlace(selectedPlace, 1);
    }

    @Override
    public void sendMarkColor(int color, int type) {
        mapFragment.changeMarkColor(color, type);
    }

    @Override
    public void sendMarkIcon(int icon, int type) {
        mapFragment.changeMarkIcon(icon, type);
    }

    @Override
    public void sendMapType(int type) {
        mapFragment.changeMapType(type);
    }

    @Override
    public void sendMapAutoFocus(boolean value) {
        mapFragment.changeMapAutoFocus(value);
    }
}