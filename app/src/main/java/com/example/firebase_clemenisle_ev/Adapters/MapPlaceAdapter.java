package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.example.firebase_clemenisle_ev.Classes.Place;
import com.example.firebase_clemenisle_ev.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MapPlaceAdapter extends RecyclerView.Adapter<MapPlaceAdapter.ViewHolder> {

    List<Place> placeList;
    String initialId;
    int type;

    List<Place> selectedPlaces = new ArrayList<>();

    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    OnItemClickListener onItemClickListener;

    int colorBlue, colorInitial;

    public MapPlaceAdapter(Context context, List<Place> placeList, String initialId, int type) {
        this.placeList = placeList;
        this.initialId = initialId;
        this.type = type;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_map_place_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CheckBox cbName = holder.cbName;
        ImageView locateImage = holder.locateImage;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        String id = placeList.get(position).getId();
        String name = placeList.get(position).getName();
        double lat = placeList.get(position).getLat();
        double lng = placeList.get(position).getLng();

        Place place = new Place(id, name, lat , lng);

        cbName.setText(name);

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);

        if(id.equals(initialId) && !inSelectedPlaces(place)) {
            selectedPlaces.add(place);
            cbName.setChecked(true);
            locateImage.setEnabled(true);
            locateImage.getDrawable().setTint(colorBlue);
        }
        else {
            if(inSelectedPlaces(place)) {
                cbName.setChecked(true);
                locateImage.setEnabled(true);
                locateImage.getDrawable().setTint(colorBlue);
            }
            else {
                cbName.setChecked(false);
                locateImage.setEnabled(false);
                locateImage.getDrawable().setTint(colorInitial);
            }
        }

        cbName.setOnClickListener(view -> {
            if(cbName.isChecked()) {
                selectedPlaces.add(place);
                locateImage.setEnabled(true);
                locateImage.getDrawable().setTint(colorBlue);
            }
            else {
                if(id.equals(initialId)) initialId = null;
                removeSelectedPlace(place);
                locateImage.setEnabled(false);
                locateImage.getDrawable().setTint(colorInitial);
            }

            onItemClickListener.sendDataSet(selectedPlaces, type, cbName.isChecked());
        });

        locateImage.setOnClickListener(view -> onItemClickListener.sendSelectedPlace(place, type));
    }

    private void removeSelectedPlace(Place targetPlace) {
        List<Place> temp = new ArrayList<>();

        for(Place place : selectedPlaces) {
            if(!(place.getId().equals(targetPlace.getId()))) {
                temp.add(place);
            }
        }

        selectedPlaces.clear();
        selectedPlaces.addAll(temp);
    }

    private boolean inSelectedPlaces(Place targetPlace) {
        for(Place place : selectedPlaces) {
            if(place.getId().equals(targetPlace.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbName;
        ImageView locateImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cbName = itemView.findViewById(R.id.cbName);
            locateImage = itemView.findViewById(R.id.locateImage);
        }
    }

    public void sortByNames() {
        Collections.sort(placeList, (place, t1) ->
                place.getName().compareToIgnoreCase(t1.getName()));

        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void sendDataSet(List<Place> places, int type, boolean isSelectAction);
        void sendSelectedPlace(Place selectedPlace, int type);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void selectAllPlaces() {
        selectedPlaces.clear();
        for(Place place : placeList) {
            if(!inSelectedPlaces(place)) {
                selectedPlaces.add(place);
            }
        }
        notifyDataSetChanged();

        onItemClickListener.sendDataSet(selectedPlaces, type, true);
    }

    public void deselectAllPlaces() {
        initialId = null;
        selectedPlaces.clear();
        notifyDataSetChanged();

        onItemClickListener.sendDataSet(selectedPlaces, type, false);
    }
}
