package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.BookingType;
import com.example.firebase_clemenisle_ev.Classes.Route;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.Station;
import com.example.firebase_clemenisle_ev.MapActivity;
import com.example.firebase_clemenisle_ev.OnTheSpotActivity;
import com.example.firebase_clemenisle_ev.R;
import com.example.firebase_clemenisle_ev.RouteActivity;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    List<Booking> bookingList;
    LayoutInflater inflater;

    Context myContext;

    String startStationText = "Start Station", endStationText = "End Station";
    String destinationSpotText = "Destination Spot", originLocationText = "Origin Location";

    String locateStartStationText = "Locate Start Station",
            locateEndStationText = "Locate End Station";
    String
            locateOriginLocationText = "Locate Origin Location",
            locateDestinationSpotText = "Locate Destination Spot";

    public BookingAdapter(Context context, List<Booking> bookingList) {
        this.bookingList = bookingList;
        this.inflater = LayoutInflater.from(context);
        Collections.reverse(this.bookingList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_booking_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView thumbnail = holder.thumbnail, moreImage = holder.moreImage,
                openImage = holder.openImage, locateImage = holder.locateImage,
                locateEndImage = holder.locateEndImage, paidImage = holder.paidImage;
        TextView tvBookingId = holder.tvBookingId, tvSchedule = holder.tvSchedule,
                tvTypeName = holder.tvTypeName, tvPrice = holder.tvPrice,
                tvStartStation = holder.tvStartStation, tvEndStation = holder.tvEndStation,
                tvStartStation2 = holder.tvStartStation2, tvEndStation2 = holder.tvEndStation2,
                tvOption = holder.tvOption, tvOpen = holder.tvOpen,
                tvLocate = holder.tvLocate, tvLocateEnd = holder.tvLocateEnd;
        ConstraintLayout backgroundLayout = holder.backgroundLayout, buttonLayout = holder.buttonLayout;

        myContext = inflater.getContext();

        Handler optionHandler = new Handler();
        Runnable optionRunnable = () -> closeOption(buttonLayout, backgroundLayout, moreImage, tvOption);

        Booking booking = bookingList.get(position);

        String status = booking.getStatus();

        String bookingId = booking.getId();
        String schedule = booking.getSchedule();

        BookingType bookingType = booking.getBookingType();
        String typeName = bookingType.getName();
        String price = "₱" + bookingType.getPrice();
        if(price.split("\\.")[1].length() == 1) price += 0;

        tvBookingId.setText(bookingId);
        tvSchedule.setText(schedule);
        tvPrice.setText(price);
        tvTypeName.setText(typeName);

        Resources resources = myContext.getResources();
        int color = 0;
        Drawable backgroundDrawable = resources.getDrawable(R.color.blue);

        switch (status) {
            case "Processing":
                color = resources.getColor(R.color.orange);
                backgroundDrawable = resources.getDrawable(R.color.orange);
                break;
            case "Booked":
                color = resources.getColor(R.color.green);
                backgroundDrawable = resources.getDrawable(R.color.green);
                break;
            case "Completed":
                color = resources.getColor(R.color.blue);
                backgroundDrawable = resources.getDrawable(R.color.blue);
                break;
            case "Cancelled":
            case "Failed":
                color = resources.getColor(R.color.red);
                backgroundDrawable = resources.getDrawable(R.color.red);
                break;
        }

        tvBookingId.setBackground(backgroundDrawable);
        tvPrice.setTextColor(color);

        if(!bookingType.getId().equals("BT99")) {
            Station startStation = booking.getStartStation();
            Station endStation = booking.getEndStation();

            String img;
            List<Route> routeList = bookingList.get(position).getRouteList();
            if(routeList.size() > 0) {
                 img = routeList.get(0).getImg();
                 Glide.with(myContext).load(img).placeholder(R.drawable.image_loading_placeholder).
                         override(Target.SIZE_ORIGINAL).into(thumbnail);
            }

            tvStartStation.setText(startStationText);
            tvEndStation.setText(endStationText);

            tvStartStation2.setText(startStation.getName());
            tvEndStation2.setText(endStation.getName());

            tvLocate.setText(locateStartStationText);
            tvLocateEnd.setText(locateEndStationText);

            boolean isPaid = booking.isPaid();

            if(isPaid) paidImage.setVisibility(View.VISIBLE);
            else paidImage.setVisibility(View.GONE);

            paidImage.setColorFilter(color);

            tvLocate.setOnClickListener(view -> openMap(startStation));

            locateImage.setOnClickListener(view -> openMap(startStation));

            tvLocateEnd.setOnClickListener(view -> openMap(endStation));

            locateEndImage.setOnClickListener(view -> openMap(endStation));

            paidImage.setOnLongClickListener(view -> {
                Toast.makeText(
                        myContext,
                        "Paid",
                        Toast.LENGTH_SHORT
                ).show();
                return false;
            });
        }
        else {
            SimpleTouristSpot destinationSpot = booking.getDestinationSpot();

            String img = destinationSpot.getImg();
            Glide.with(myContext).load(img).placeholder(R.drawable.image_loading_placeholder).
                    override(Target.SIZE_ORIGINAL).into(thumbnail);

            double originLat = booking.getOriginLat(), originLng = booking.getOriginLng();
            String originLocation = "Latitude: " + originLat + "\nLongitude: " + originLng;

            tvStartStation.setText(originLocationText);
            tvEndStation.setText(destinationSpotText);

            tvStartStation2.setText(originLocation);
            tvEndStation2.setText(destinationSpot.getName());

            tvLocate.setText(locateOriginLocationText);
            tvLocateEnd.setText(locateDestinationSpotText);

            tvLocate.setOnClickListener(view -> openMap2(originLat, originLng));

            locateImage.setOnClickListener(view -> openMap2(originLat, originLng));

            tvLocateEnd.setOnClickListener(view -> openMap3(destinationSpot));

            locateEndImage.setOnClickListener(view -> openMap3(destinationSpot));
        }

        moreImage.setOnClickListener(view -> {
            if(tvOption.getText().equals("false")) {
                openOption(buttonLayout, backgroundLayout, moreImage,
                        optionHandler, optionRunnable, tvOption);
            }
            else {
                closeOption(buttonLayout, backgroundLayout, moreImage, tvOption);
            }
        });

        tvOpen.setOnClickListener(view -> openItem(booking));

        openImage.setOnClickListener(view -> openItem(booking));

        int top = dpToPx(4), bottom = dpToPx(4);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == getItemCount();

        if(isFirstItem) {
            top = dpToPx(8);
        }
        if(isLastItem) {
            if(status.equals("Failed")) {
                bottom = dpToPx(88);
            }
            else {
                bottom = dpToPx(8);
            }
        }

        ConstraintLayout.LayoutParams layoutParams = (
                ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, bottom);
        backgroundLayout.setLayoutParams(layoutParams);
    }

    private void openMap(Station startStation) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", startStation.getId());
        intent.putExtra("lat", startStation.getLat());
        intent.putExtra("lng", startStation.getLng());
        intent.putExtra("name", startStation.getName());
        intent.putExtra("type", 1);
        myContext.startActivity(intent);
    }

    private void openMap2(double originLat, double originLng) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", 0);
        intent.putExtra("lat", originLat);
        intent.putExtra("lng", originLng);
        intent.putExtra("name", "Your Location");
        intent.putExtra("type", 2);
        myContext.startActivity(intent);
    }

    private void openMap3(SimpleTouristSpot destinationSpot) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", destinationSpot.getId());
        intent.putExtra("lat", destinationSpot.getLat());
        intent.putExtra("lng", destinationSpot.getLng());
        intent.putExtra("name", destinationSpot.getName());
        intent.putExtra("type", 0);
        myContext.startActivity(intent);
    }

    private void openItem(Booking booking) {
        Intent intent;

        if(booking.getBookingType().getId().equals("BT99")) {
            intent = new Intent(myContext, OnTheSpotActivity.class);
        }
        else {
            intent = new Intent(myContext, RouteActivity.class);
        }

        intent.putExtra("bookingId", booking.getId());
        intent.putExtra("isLatest",
                bookingList.get(0).getId().equals(booking.getId()) &&
                        booking.getStatus().equals("Completed") &&
                        !booking.getBookingType().getId().equals("BT99"));

        myContext.startActivity(intent);
    }

    private void openOption(ConstraintLayout buttonLayout, ConstraintLayout backgroundLayout,
                            ImageView moreImage, Handler optionHandler, Runnable optionRunnable,
                            TextView tvOption) {
        moreImage.setEnabled(false);

        optionHandler.removeCallbacks(optionRunnable);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(backgroundLayout);

        constraintSet.clear(buttonLayout.getId(), ConstraintSet.TOP);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.BOTTOM,
                moreImage.getId(), ConstraintSet.BOTTOM);

        setTransition(buttonLayout);
        constraintSet.applyTo(backgroundLayout);

        tvOption.setText("true");
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_close_24);
        moreImage.setColorFilter(myContext.getResources().getColor(R.color.red));

        optionHandler.postDelayed(optionRunnable, 3000);
    }

    private void closeOption(ConstraintLayout buttonLayout, ConstraintLayout backgroundLayout,
                             ImageView moreImage, TextView tvOption) {
        moreImage.setEnabled(false);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(backgroundLayout);

        constraintSet.clear(buttonLayout.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.TOP,
                backgroundLayout.getId(), ConstraintSet.BOTTOM);

        setTransition(buttonLayout);
        constraintSet.applyTo(backgroundLayout);

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

    private int dpToPx(int dp) {
        float px = dp * myContext.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail, moreImage, openImage, locateImage, locateEndImage, paidImage;
        TextView tvBookingId, tvSchedule, tvTypeName, tvPrice,
                tvStartStation, tvStartStation2, tvEndStation, tvEndStation2,
                tvOption, tvOpen, tvLocate, tvLocateEnd;
        ConstraintLayout backgroundLayout, buttonLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            tvTypeName = itemView.findViewById(R.id.tvTypeName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStartStation = itemView.findViewById(R.id.tvStartStation);
            tvStartStation2 = itemView.findViewById(R.id.tvStartStation2);
            tvEndStation = itemView.findViewById(R.id.tvEndStation);
            tvEndStation2 = itemView.findViewById(R.id.tvEndStation2);
            tvOption = itemView.findViewById(R.id.tvOption);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            buttonLayout = itemView.findViewById(R.id.buttonLayout);
            moreImage = itemView.findViewById(R.id.moreImage);
            tvOpen = itemView.findViewById(R.id.tvOpen);
            openImage = itemView.findViewById(R.id.openImage);
            tvLocate = itemView.findViewById(R.id.tvLocate);
            locateImage = itemView.findViewById(R.id.locateImage);
            tvLocateEnd = itemView.findViewById(R.id.tvLocateEnd);
            locateEndImage = itemView.findViewById(R.id.locateEndImage);
            paidImage = itemView.findViewById(R.id.paidImage);
        }
    }
}
