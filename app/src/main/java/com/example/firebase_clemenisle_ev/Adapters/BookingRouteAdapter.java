package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebase_clemenisle_ev.Classes.BookingTypeRoute;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.Station;
import com.example.firebase_clemenisle_ev.MapActivity;
import com.example.firebase_clemenisle_ev.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BookingRouteAdapter extends RecyclerView.Adapter<BookingRouteAdapter.ViewHolder>
    implements BookingSpotAdapter.OnItemClickListener {

    List<BookingTypeRoute> bookingRouteList;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    int colorBlue, colorWhite, colorBlack, colorInitial;

    String defaultSpotCountText = "Number of Tourist Spot(s): ", defaultEndStationText = "End Station: ";

    String routeId = null;
    Station station = null;

    boolean zeroCountDisable = false;

    OnItemClickListener onItemClickListener;

    public BookingRouteAdapter(Context context, List<BookingTypeRoute> bookingRouteList) {
        this.bookingRouteList = bookingRouteList;
        this.inflater = LayoutInflater.from(context);
    }

    public void setStation(Station station) {
        if(this.station != null) {
            if(!(this.station.getId().equals(station.getId()))) {
                routeId = null;
            }
        }
        this.station = station;
        notifyDataSetChanged();
    }

    public Station getStation() {
        return station;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    @NonNull
    @Override
    public BookingRouteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_booking_route_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingRouteAdapter.ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvEndStationName = holder.tvEndStationName, tvLocate = holder.tvLocate,
                tvSpotCount = holder.tvSpotCount;
        ImageView locateImage = holder.locateImage;
        RecyclerView recommendedSpotsView = holder.recommendedSpotsView;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorWhite = myResources.getColor(R.color.white);
        colorBlack = myResources.getColor(R.color.black);
        colorInitial = myResources.getColor(R.color.initial);

        BookingTypeRoute bookingTypeRoute = bookingRouteList.get(position);
        Station endStation = bookingTypeRoute.getEndStation();
        List<SimpleTouristSpot> spots = new ArrayList<>(bookingTypeRoute.getSpots());

        String id = bookingRouteList.get(position).getId();
        String name = endStation.getName();

        String spotCountText = defaultSpotCountText + spots.size();

        String endStationText = defaultEndStationText + name;
        tvEndStationName.setText(endStationText);
        tvSpotCount.setText(spotCountText);

        if(recommendedSpotsView.getAdapter() == null) {
            LinearLayoutManager linearLayout =
                    new LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false);
            recommendedSpotsView.setLayoutManager(linearLayout);
            BookingSpotAdapter bookingSpotAdapter = new BookingSpotAdapter(myContext, spots, bookingTypeRoute);
            recommendedSpotsView.setAdapter(bookingSpotAdapter);
            bookingSpotAdapter.setOnItemClickListener(this);
        }
        else {
            BookingSpotAdapter bookingSpotAdapter = (BookingSpotAdapter) recommendedSpotsView.getAdapter();
            bookingSpotAdapter.setBookingTypeRoute(bookingTypeRoute);
            bookingSpotAdapter.setSpots(spots);
        }

        boolean isSelected = false;
        if(routeId != null)
            isSelected = routeId.equals(id);

        if(spots.size() > 0 || !zeroCountDisable) {
            if(isSelected) {
                backgroundLayout.setBackgroundColor(colorBlue);
                tvEndStationName.setTextColor(colorWhite);
                tvSpotCount.setTextColor(colorWhite);
                tvLocate.setTextColor(colorWhite);
                locateImage.setColorFilter(colorWhite);
            }
            else {
                backgroundLayout.setBackgroundColor(colorWhite);
                tvEndStationName.setTextColor(colorBlack);
                tvSpotCount.setTextColor(colorBlack);
                tvLocate.setTextColor(colorBlue);
                locateImage.setColorFilter(colorBlue);
            }
        }
        else {
            backgroundLayout.setBackgroundColor(colorWhite);
            tvEndStationName.setTextColor(colorInitial);
            tvSpotCount.setTextColor(colorInitial);
        }

        int top = dpToPx(0), bottom = dpToPx(0);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == getItemCount();

        if(isFirstItem) {
            top = dpToPx(8);
        }
        if(isLastItem) {
            bottom = dpToPx(8);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, bottom);
        backgroundLayout.setLayoutParams(layoutParams);

        backgroundLayout.setOnClickListener(view -> {
            if(spots.size() > 0 || !zeroCountDisable) {
                routeId = id;
                onItemClickListener.sendRoute(bookingTypeRoute);
                notifyDataSetChanged();
            }
        });

        locateImage.setOnClickListener(view -> openMap(endStation));

        tvLocate.setOnClickListener(view -> openMap(endStation));
    }

    private void openMap(Station station) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", station.getId());
        intent.putExtra("lat", station.getLat());
        intent.putExtra("lng", station.getLng());
        intent.putExtra("name", station.getName());
        intent.putExtra("type", 1);
        myContext.startActivity(intent);
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public void itemClicked(List<SimpleTouristSpot> spots, BookingTypeRoute bookingTypeRoute) {
        if(spots.size() > 0 || !zeroCountDisable) {
            routeId = bookingTypeRoute.getId();
            onItemClickListener.sendRoute(bookingTypeRoute);
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener {
        void sendRoute(BookingTypeRoute bookingTypeRoute);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return bookingRouteList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        TextView tvEndStationName, tvSpotCount, tvLocate;
        ImageView locateImage;
        RecyclerView recommendedSpotsView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvEndStationName = itemView.findViewById(R.id.tvEndStationName);
            tvSpotCount = itemView.findViewById(R.id.tvSpotCount);
            tvLocate = itemView.findViewById(R.id.tvLocate);
            locateImage = itemView.findViewById(R.id.locateImage);
            recommendedSpotsView = itemView.findViewById(R.id.recommendedSpotsView);
        }
    }
}
