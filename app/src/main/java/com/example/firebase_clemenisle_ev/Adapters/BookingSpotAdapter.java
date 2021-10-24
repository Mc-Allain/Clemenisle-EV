package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.firebase_clemenisle_ev.Classes.BookingTypeRoute;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class BookingSpotAdapter extends RecyclerView.Adapter<BookingSpotAdapter.ViewHolder> {

    List<SimpleTouristSpot> spots;
    BookingTypeRoute bookingTypeRoute;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    boolean isFromNearSpot = false;

    OnItemClickListener onItemClickListener;

    public BookingSpotAdapter(Context context, List<SimpleTouristSpot> spots, BookingTypeRoute bookingTypeRoute) {
        this.spots = spots;
        this.bookingTypeRoute = bookingTypeRoute;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_near_spot_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView thumbnail = holder.thumbnail;
        TextView tvName = holder.tvName;
        ConstraintLayout backgroundLayout = holder.backgroundLayout;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        SimpleTouristSpot spot = spots.get(position);

        String name = spot.getName();
        String img = spot.getImg();

        try {
            Glide.with(myContext).load(img).
                    placeholder(R.drawable.image_loading_placeholder).
                    override(Target.SIZE_ORIGINAL).into(thumbnail);
        }
        catch (Exception ignored) {}
        tvName.setText(name);

        int start = dpToPx(4), end = dpToPx(4);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == getItemCount();

        if(isFirstItem) {
            start = dpToPx(8);
        }
        if(isLastItem) {
            end = dpToPx(8);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMarginStart(start);
        layoutParams.setMarginEnd(end);
        backgroundLayout.setLayoutParams(layoutParams);

        backgroundLayout.setOnClickListener(view -> {
            if(onItemClickListener != null) {
                if(isFromNearSpot) onItemClickListener.addSpot(spot);
                else onItemClickListener.addRoute(spots, bookingTypeRoute);
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void addRoute(List<SimpleTouristSpot> spots, BookingTypeRoute bookingTypeRoute);
        void addSpot(SimpleTouristSpot spots);
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return spots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView tvName;
        ConstraintLayout backgroundLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvName = itemView.findViewById(R.id.tvName);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
        }
    }

    public void setBookingTypeRoute(BookingTypeRoute bookingTypeRoute) {
        this.bookingTypeRoute = bookingTypeRoute;
    }

    public void setSpots(List<SimpleTouristSpot> spots) {
        this.spots.clear();
        this.spots.addAll(spots);
        notifyDataSetChanged();
    }

    public void setFromNearSpot(boolean fromNearSpot) {
        isFromNearSpot = fromNearSpot;
        notifyDataSetChanged();
    }
}
