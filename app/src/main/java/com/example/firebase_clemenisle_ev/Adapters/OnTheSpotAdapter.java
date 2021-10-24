package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.firebase_clemenisle_ev.Classes.DetailedTouristSpot;
import com.example.firebase_clemenisle_ev.MapActivity;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class OnTheSpotAdapter extends RecyclerView.Adapter<OnTheSpotAdapter.ViewHolder> {

    List<DetailedTouristSpot> touristSpots;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    int colorBlue, colorWhite, colorBlack;

    String spotId = null;

    OnItemClickListener onItemClickListener;

    public OnTheSpotAdapter(Context context, List<DetailedTouristSpot> touristSpots) {
        this.touristSpots = touristSpots;
        this.inflater = LayoutInflater.from(context);
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_on_the_spot_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView thumbnail = holder.thumbnail, locateImage = holder.locateImage;
        TextView tvName = holder.tvName, tvLocate = holder.tvLocate;
        ConstraintLayout backgroundLayout = holder.backgroundLayout;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorWhite = myResources.getColor(R.color.white);
        colorBlack = myResources.getColor(R.color.black);

        DetailedTouristSpot touristSpot = touristSpots.get(position);

        String id = touristSpot.getId();
        String name = touristSpot.getName();
        String img = touristSpot.getImg();

        try {
            Glide.with(myContext).load(img).
                    placeholder(R.drawable.image_loading_placeholder).
                    override(Target.SIZE_ORIGINAL).into(thumbnail);
        }
        catch (Exception ignored) {}
        tvName.setText(name);

        boolean isSelected = false;
        if(spotId != null)
            isSelected = spotId.equals(id);

        if(isSelected) {
            backgroundLayout.setBackgroundColor(colorBlue);
            tvName.setTextColor(colorWhite);
            tvLocate.setTextColor(colorWhite);
            locateImage.setColorFilter(colorWhite);
        }
        else {
            backgroundLayout.setBackgroundColor(colorWhite);
            tvName.setTextColor(colorBlack);
            tvLocate.setTextColor(colorBlue);
            locateImage.setColorFilter(colorBlue);
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
            spotId = id;
            onItemClickListener.sendTouristSpot(touristSpot);
            notifyDataSetChanged();
        });

        locateImage.setOnClickListener(view -> openMap(touristSpot));

        tvLocate.setOnClickListener(view -> openMap(touristSpot));
    }

    private void openMap(DetailedTouristSpot touristSpot) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", touristSpot.getId());
        intent.putExtra("lat", touristSpot.getLat());
        intent.putExtra("lng", touristSpot.getLng());
        intent.putExtra("name", touristSpot.getName());
        intent.putExtra("type", 0);
        myContext.startActivity(intent);
    }

    public interface OnItemClickListener {
        void sendTouristSpot(DetailedTouristSpot touristSpot);
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
        return touristSpots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail, locateImage;
        TextView tvName, tvLocate;
        ConstraintLayout backgroundLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            locateImage = itemView.findViewById(R.id.locateImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocate = itemView.findViewById(R.id.tvLocate);
        }
    }
}
