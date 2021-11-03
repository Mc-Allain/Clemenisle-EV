package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebase_clemenisle_ev.Classes.BookingType;
import com.example.firebase_clemenisle_ev.Classes.BookingTypeRoute;
import com.example.firebase_clemenisle_ev.Classes.Station;
import com.example.firebase_clemenisle_ev.MapActivity;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class BookingStationAdapter extends RecyclerView.Adapter<BookingStationAdapter.ViewHolder> {

    List<Station> stationList;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    int colorBlue, colorWhite, colorBlack, colorInitial;

    String defaultRouteCountText = "Recommended Route(s): ";

    String stationId = null;
    BookingType bookingType = null;

    boolean isZeroCountDisable = false;

    OnItemClickListener onItemClickListener;

    public BookingStationAdapter(Context context, List<Station> stationList) {
        this.stationList = stationList;
        this.inflater = LayoutInflater.from(context);
    }

    public void setBookingType(BookingType bookingType) {
        if(this.bookingType != null) {
            if(!(this.bookingType.getId().equals(bookingType.getId()))) {
                stationId = null;
            }
        }
        this.bookingType = bookingType;
        notifyDataSetChanged();
    }

    public BookingType getBookingType() {
        return bookingType;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_booking_station_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvStationName = holder.tvStationName, tvLocate = holder.tvLocate,
                tvRouteCount = holder.tvRouteCount;
        ImageView locateImage = holder.locateImage;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorWhite = myResources.getColor(R.color.white);
        colorBlack = myResources.getColor(R.color.black);
        colorInitial = myResources.getColor(R.color.initial);

        Station station = stationList.get(position);

        String id = stationList.get(position).getId();
        String name = stationList.get(position).getName();

        int initCount = 0;
        if(bookingType != null) {
            List<BookingTypeRoute> bookingTypeRouteList = bookingType.getRouteList();
            for(BookingTypeRoute bookingTypeRoute : bookingTypeRouteList) {
                if(bookingTypeRoute.getStartStation().getId().equals(id)) {
                    initCount++;
                }
            }
        }
        int routeCount = initCount;
        String routeCountText = defaultRouteCountText + routeCount;

        tvStationName.setText(name);
        tvRouteCount.setText(routeCountText);

        boolean isSelected = false;
        if(stationId != null)
            isSelected = stationId.equals(id);

        if(routeCount > 0 || !isZeroCountDisable) {
            if(isSelected) {
                backgroundLayout.setBackgroundColor(colorBlue);
                tvStationName.setTextColor(colorWhite);
                tvRouteCount.setTextColor(colorWhite);
                tvLocate.setTextColor(colorWhite);
                locateImage.getDrawable().setTint(colorWhite);
            }
            else {
                backgroundLayout.setBackgroundColor(colorWhite);
                tvStationName.setTextColor(colorBlack);
                tvRouteCount.setTextColor(colorBlack);
                tvLocate.setTextColor(colorBlue);
                locateImage.getDrawable().setTint(colorBlue);
            }
        }
        else {
            backgroundLayout.setBackgroundColor(colorWhite);
            tvStationName.setTextColor(colorInitial);
            tvRouteCount.setTextColor(colorInitial);
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
            if(routeCount > 0 || !isZeroCountDisable) {
                stationId = id;
                onItemClickListener.sendStation(station);
                notifyDataSetChanged();
            }
        });

        locateImage.setOnClickListener(view -> openMap(station));

        tvLocate.setOnClickListener(view -> openMap(station));
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

    public interface OnItemClickListener {
        void sendStation(Station station);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        TextView tvStationName, tvRouteCount, tvLocate;
        ImageView locateImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvStationName = itemView.findViewById(R.id.tvStationName);
            tvRouteCount = itemView.findViewById(R.id.tvRouteCount);
            tvLocate = itemView.findViewById(R.id.tvLocate);
            locateImage = itemView.findViewById(R.id.locateImage);

            setIsRecyclable(false);
        }
    }
}
