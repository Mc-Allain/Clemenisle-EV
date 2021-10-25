package com.example.firebase_clemenisle_ev.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.firebase_clemenisle_ev.Classes.MapCoordinates;
import com.example.firebase_clemenisle_ev.Classes.Place;
import com.example.firebase_clemenisle_ev.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class MapFragment extends Fragment {

    MapCoordinates mapCoordinates = new MapCoordinates();

    Button resetButton;

    String id;
    int type;
    double lat, lng;
    String name;

    GoogleMap myGoogleMap;

    List<Place> selectedTouristSpots = new ArrayList<>(), selectedStations = new ArrayList<>();
    Place selectedPlace;
    int selectedType;

    Context myContext;
    Resources myResources;

    LatLng defaultLatLng = mapCoordinates.getInitialLatLng();
    float defaultZoom = 16;
    float currentZoom = defaultZoom;

    int tsMarkColor, sMarkColor, tsMarkIcon, sMarkIcon, mapType;
    boolean mapAutoFocus, locateFocus = false;

    boolean fromBooking, fromBookingRecord;
    BookingMapListener bookingMapListener;

    LatLng currentLocation;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("mapSetting", Context.MODE_PRIVATE);

        int tsColorCode = sharedPreferences.getInt("tsColorCode", 1);

        switch (tsColorCode) {
            case 1:
                tsMarkColor = myResources.getColor(R.color.blue);
                break;
            case 2:
                tsMarkColor = myResources.getColor(R.color.red);
                break;
            case 3:
                tsMarkColor = myResources.getColor(R.color.green);
                break;
            case 4:
                tsMarkColor = myResources.getColor(R.color.orange);
                break;
            case 5:
                tsMarkColor = myResources.getColor(R.color.dark_violet);
                break;
            case 6:
                tsMarkColor = myResources.getColor(R.color.black);
                break;
        }

        int sColorCode = sharedPreferences.getInt("sColorCode", 1);

        switch (sColorCode) {
            case 1:
                sMarkColor = myResources.getColor(R.color.blue);
                break;
            case 2:
                sMarkColor = myResources.getColor(R.color.red);
                break;
            case 3:
                sMarkColor = myResources.getColor(R.color.green);
                break;
            case 4:
                sMarkColor = myResources.getColor(R.color.orange);
                break;
            case 5:
                sMarkColor = myResources.getColor(R.color.dark_violet);
                break;
            case 6:
                sMarkColor = myResources.getColor(R.color.black);
                break;
        }

        tsMarkIcon = sharedPreferences.getInt("tsMarkIcon", R.drawable.ic_baseline_tour_24);
        sMarkIcon = sharedPreferences.getInt("sMarkIcon", R.drawable.ic_baseline_ev_station_24);
        mapType = sharedPreferences.getInt("mapType", 1);
        mapAutoFocus = sharedPreferences.getBoolean("mapAutoFocus", true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        resetButton = view.findViewById(R.id.resetButton);

        myContext = getContext();
        myResources = getResources();

        initSharedPreferences();

        Bundle bundle = getArguments();
        if(bundle != null) {
            id = bundle.getString("id");
            lat = bundle.getDouble("lat", defaultLatLng.latitude);
            lng = bundle.getDouble("lng", defaultLatLng.longitude);
            name = bundle.getString("name");
            type = bundle.getInt("type", 0);

            fromBooking = bundle.getBoolean("fromBooking", false);
            if(fromBooking) mapAutoFocus = true;

            fromBookingRecord = bundle.getBoolean("fromBookingRecord", false);
        }

        if(type == 2) {
            currentLocation = new LatLng(lat, lng);
        }
        else {
            Place place = new Place(id, name, lat, lng);

            if(type == 0) {
                selectedTouristSpots.add(place);
            }
            else if(type == 1) {
                selectedStations.add(place);
            }
            selectedPlace = place;
        }
        selectedType = type;

        SupportMapFragment supportMapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

        supportMapFragment.getMapAsync(googleMap -> {
            googleMap.setMapType(mapType);
            googleMap.setMinZoomPreference(10);

            setButtonEnabled(false);

            LatLng latLng = new LatLng(lat, lng), zoomLatLng;

            if(mapAutoFocus) zoomLatLng = new LatLng(lat, lng);
            else zoomLatLng = mapCoordinates.getInitialLatLng();

            if(bookingMapListener == null) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(name);
                markerOptions.icon(bitmapDescriptor(type));
                googleMap.addMarker(markerOptions).showInfoWindow();

                googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(zoomLatLng, currentZoom),
                        new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                setButtonEnabled(true);
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
            }
            else bookingMapListener.currentLocationOnClick();

            googleMap.setOnCameraMoveListener(() -> {
                currentZoom = googleMap.getCameraPosition().zoom;
                resetButton.setVisibility(View.VISIBLE);
            });

            googleMap.setOnCameraMoveCanceledListener(() -> setButtonEnabled(true));

            googleMap.setOnMapClickListener(latLng1 -> {
                if(fromBooking) {
                    getUserCurrentLocation(latLng1, "Your Location");
                    bookingMapListener.sendManualClicked();
                }
            });

            googleMap.setOnMarkerClickListener(marker -> {
                String name = marker.getTitle();
                double lat = marker.getPosition().latitude;
                double lng = marker.getPosition().longitude;

                getMarkedPlace(name, lat, lng);
                return false;
            });

            myGoogleMap = googleMap;

            if(fromBookingRecord) locateOnTheSpot();
        });

        resetButton.setOnClickListener(view1 -> {
            setButtonEnabled(false);

            currentZoom = defaultZoom;
            myGoogleMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLatLng, currentZoom),
                    new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            resetButton.setVisibility(View.GONE);
                            setButtonEnabled(true);
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
        });

        return view;
    }

    public void mapSettingsRequestResult() {
        initSharedPreferences();
        myGoogleMap.setMapType(mapType);
        if(selectedType == 0) locateOnTheSpot();
        if(selectedType == 2) getUserCurrentLocation(currentLocation, "Your Location");
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }

    private void setButtonEnabled(boolean value) {
        resetButton.setEnabled(value);
        if(bookingMapListener != null) bookingMapListener.setCurrentLocationEnabled(value);
    }

    public void setBookingMapListener(BookingMapListener bookingMapListener) {
        this.bookingMapListener = bookingMapListener;
    }

    public interface BookingMapListener {
        void setCurrentLocationEnabled(boolean value);
        void currentLocationOnClick();
        void sendCurrentLocation(LatLng currentLocation);
        void sendManualClicked();
    }

    private void markCurrentLocation(LatLng latLng, String locationName) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(locationName);
        markerOptions.icon(bitmapDescriptor(2));

        myGoogleMap.addMarker(markerOptions).showInfoWindow();
    }

    public void getUserCurrentLocation(LatLng latLng, String locationName) {
        myGoogleMap.clear();
        selectedType = 2;

        LatLng onTheSpotLatLng = new LatLng(selectedPlace.getLat(), selectedPlace.getLng());
        markOnTheSpot(onTheSpotLatLng);

        markCurrentLocation(latLng, locationName);

        setButtonEnabled(false);

        currentZoom = defaultZoom;
        myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, currentZoom),
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        setButtonEnabled(true);
                    }

                    @Override
                    public void onCancel() {

                    }
                });

        currentLocation = latLng;
        if(bookingMapListener != null) bookingMapListener.sendCurrentLocation(latLng);
    }

    private void markOnTheSpot(LatLng latLng) {
        String placeName = selectedPlace.getName();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(placeName);
        markerOptions.icon(bitmapDescriptor(0));

        myGoogleMap.addMarker(markerOptions).showInfoWindow();
    }

    public void locateOnTheSpot() {
        myGoogleMap.clear();
        selectedType = 0;

        markCurrentLocation(currentLocation, "Your Location");

        LatLng onTheSpotLatLng = new LatLng(selectedPlace.getLat(), selectedPlace.getLng());
        markOnTheSpot(onTheSpotLatLng);

        setButtonEnabled(false);

        myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(onTheSpotLatLng, currentZoom),
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        setButtonEnabled(true);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    private void getMarkedPlace(String name, double lat, double lng) {
        for(Place place : selectedTouristSpots) {
            if(place.getName().equals(name) && place.getLat() == lat && place.getLng() == lng) {
                selectedPlace = place;
                selectedType = 0;
                return;
            }
        }

        for(Place place : selectedStations) {
            if(place.getName().equals(name) && place.getLat() == lat && place.getLng() == lng) {
                selectedPlace = place;
                selectedType = 1;
                return;
            }
        }

        selectedType = 2;
    }

    private boolean isInPlaceList(Place targetPlace, List<Place> placeList) {
        if(placeList.size() > 0) {
            for(Place place : placeList) {
                if(place.getId().equals(targetPlace.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void changeMapAutoFocus(boolean value) {
        mapAutoFocus = value;
    }

    public void changeMapType(int type) {
        myGoogleMap.setMapType(type);
    }

    public void changeMarkIcon(int icon, int type) {
        if(type == 0) {
            tsMarkIcon = icon;
        }
        else if(type == 1) {
            sMarkIcon = icon;
        }
        replaceMarks(selectedPlace);
    }

    public void changeMarkColor(int color, int type) {
        if(type == 0) {
            tsMarkColor = color;
        }
        else if(type == 1) {
            sMarkColor = color;
        }
        replaceMarks(selectedPlace);
    }

    public void selectPlace(Place selectedPlace, int type) {
        selectedType = type;
        locateFocus = true;
        replaceMarks(selectedPlace);
    }

    public void markTouristSpot(List<Place> placeList) {
        selectedType = 0;
        locateFocus = false;

        Place lastItem = placeList.get(placeList.size() - 1);
        for(Place place : placeList) {
            if(!isInPlaceList(place, selectedTouristSpots)) {
                selectedTouristSpots.add(place);
                placeMark(place, 0, lastItem);
            }
        }
    }

    public void markStation(List<Place> placeList) {
        selectedType = 1;
        locateFocus = false;

        Place lastItem = placeList.get(placeList.size() - 1);
        for(Place place : placeList) {
            if(!isInPlaceList(place, selectedStations)) {
                selectedStations.add(place);
                placeMark(place, 1, lastItem);
            }
        }
    }

    public void unmarkTouristSpot(List<Place> placeList){
        selectedType = 0;
        locateFocus = false;

        selectedTouristSpots.clear();
        selectedTouristSpots.addAll(placeList);
        replaceMarks(null);
    }

    public void unmarkStations(List<Place> placeList){
        selectedType = 1;
        locateFocus = false;

        selectedStations.clear();
        selectedStations.addAll(placeList);
        replaceMarks(null);
    }

    public void replaceMarks(Place selectedPlace) {
        int flType = 0, slType = 0;
        List<Place> firstList = new ArrayList<>();
        List<Place> secondList = new ArrayList<>();

        myGoogleMap.clear();

        if(selectedType == 0) {
            flType = 1;
            firstList.addAll(selectedStations);
            secondList.addAll(selectedTouristSpots);
        }
        else if(selectedType == 1) {
            slType = 1;
            firstList.addAll(selectedTouristSpots);
            secondList.addAll(selectedStations);
        }
        else if(selectedType == 2) {
            firstList.addAll(selectedTouristSpots);
            secondList.addAll(selectedStations);
            selectedPlace = null;
        }

        if(!fromBooking && currentLocation != null)
            markCurrentLocation(currentLocation, "Your Location");

        for(Place place : firstList) {
            placeMark(place, flType, null);
        }

        for(Place place : secondList) {
            placeMark(place, slType, selectedPlace);
        }
    }

    public void placeMark(Place currentItem, int type, Place lastItem) {
        String id = currentItem.getId();
        LatLng latLng = new LatLng(currentItem.getLat(), currentItem.getLng());
        String name = currentItem.getName();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(name);
        markerOptions.icon(bitmapDescriptor(type));

        if(lastItem == null) {
            myGoogleMap.addMarker(markerOptions);
            selectedPlace = null;
        }
        else {
            if(id.equals(lastItem.getId())) {
                selectedPlace = lastItem;
                myGoogleMap.addMarker(markerOptions).showInfoWindow();

                if(mapAutoFocus || locateFocus) {
                    myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, currentZoom));
                }
            }
            else {
                myGoogleMap.addMarker(markerOptions);
            }
        }
    }

    private BitmapDescriptor bitmapDescriptor(int type) {
        int iconId = 0;
        int iconColor = 0;

        if(type == 0) {
            iconId = tsMarkIcon;
            iconColor = tsMarkColor;
        }
        else if(type == 1) {
            iconId = sMarkIcon;
            iconColor = sMarkColor;
        }
        else if(type == 2) {
            iconId = R.drawable.ic_baseline_emoji_people_24;
            iconColor = sMarkColor;
        }

        Drawable drawable = ContextCompat.getDrawable(myContext, iconId);
        drawable.setTint(iconColor);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}