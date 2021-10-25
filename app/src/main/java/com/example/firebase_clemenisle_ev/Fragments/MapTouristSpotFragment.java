package com.example.firebase_clemenisle_ev.Fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Adapters.MapPlaceAdapter;
import com.example.firebase_clemenisle_ev.Classes.DetailedTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.Place;
import com.example.firebase_clemenisle_ev.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MapTouristSpotFragment extends Fragment implements MapPlaceAdapter.OnItemClickListener {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);

    CheckBox cbTouristSpot;
    RecyclerView mapTouristSpotView;
    ProgressBar progressBar;

    Context myContext;

    List<Place> placeList = new ArrayList<>();
    MapPlaceAdapter mapPlaceAdapter;

    String id = null;
    String name = "";

    OnComboBoxClickListener onComboBoxClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_tourist_spot, container, false);

        cbTouristSpot = view.findViewById(R.id.cbTouristSpot);
        mapTouristSpotView = view.findViewById(R.id.mapTouristSpotView);
        progressBar = view.findViewById(R.id.progressBar);

        myContext = inflater.getContext();

        Bundle bundle = getArguments();
        if(bundle != null) {
            id = bundle.getString("id");
            name = bundle.getString("name");
        }

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        mapTouristSpotView.setLayoutManager(linearLayout);
        mapPlaceAdapter = new MapPlaceAdapter(myContext, placeList, id, 0);
        mapTouristSpotView.setAdapter(mapPlaceAdapter);
        mapPlaceAdapter.setOnItemClickListener(this);

        getTouristSpots();

        cbTouristSpot.setOnClickListener(view1 -> {
            if(cbTouristSpot.isChecked()) {
                mapPlaceAdapter.selectAllPlaces();
            }
            else {
                mapPlaceAdapter.deselectAllPlaces();
            }
        });

        return view;
    }

    public interface OnComboBoxClickListener {
        void sendSelectedTouristSpots(List<Place> places, boolean isSelectAction);
        void sendSelectedTouristSpot(Place selectedPlace);
    }

    @Override
    public void sendDataSet(List<Place> places, int type, boolean isSelectAction) {
        if(type == 0) {
            onComboBoxClickListener.sendSelectedTouristSpots(places, isSelectAction);

            cbTouristSpot.setChecked(places.size() == placeList.size());
        }
    }

    @Override
    public void sendSelectedPlace(Place selectedPlace, int type) {
        if(type == 0) {
            onComboBoxClickListener.sendSelectedTouristSpot(selectedPlace);
        }
    }

    private void getTouristSpots() {
        progressBar.setVisibility(View.VISIBLE);

        Query touristSpotsQuery =
                firebaseDatabase.getReference("touristSpots")
                        .orderByChild("deactivated").equalTo(false);
        touristSpotsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                placeList.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DetailedTouristSpot touristSpot = new DetailedTouristSpot(dataSnapshot);

                        String id = touristSpot.getId();
                        String name = touristSpot.getName();
                        double lat = touristSpot.getLat();
                        double lng = touristSpot.getLng();

                        Place place = new Place(id, name, lat, lng);

                        if(!touristSpot.isDeactivated()) {
                            placeList.add(place);
                        }

                        mapPlaceAdapter.sortByNames();
                    }
                }
                else {
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                placeList.clear();
                mapPlaceAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            onComboBoxClickListener = (OnComboBoxClickListener) activity;
        }
        catch(Exception exception) {
            Toast.makeText(
                    context,
                    "Interface error",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}