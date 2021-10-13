package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class SelectedSpotAdapter extends RecyclerView.Adapter<SelectedSpotAdapter.ViewHolder> {

    List<SimpleTouristSpot> spots;
    int columnCount;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    OnRemoveClickListener onRemoveClickListener;

    public void setOnRemoveClickListener(OnRemoveClickListener onRemoveClickListener) {
        this.onRemoveClickListener = onRemoveClickListener;
    }

    public interface OnRemoveClickListener{
        void removeSpot(SimpleTouristSpot spot);
    }

    public SelectedSpotAdapter(Context context, List<SimpleTouristSpot> spots, int columnCount) {
        this.spots = spots;
        this.columnCount = columnCount;
        this.inflater =  LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_selected_spot_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView spotThumbnail = holder.spotThumbnail;
        TextView name = holder.name;
        Button removeButton = holder.removeButton;
        ConstraintLayout backgroundLayout = holder.backgroundLayout;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        SimpleTouristSpot spot = spots.get(position);

        Glide.with(myContext).load(spot.getImg())
                .placeholder(R.drawable.image_loading_placeholder)
                .into(spotThumbnail);

        name.setText(spot.getName());

        int start = dpToPx(4), top = dpToPx(4), end = dpToPx(4), bottom = dpToPx(4);

        boolean isFirstRow = position + 1 <= columnCount, isLastRow = position >= getItemCount() - columnCount;
        boolean isLeftSide = (position + 1) % columnCount == 1, isRightSide = (position + 1) % columnCount == 0;

        if(isFirstRow) {
            top = dpToPx(8);
        }
        if(isLastRow) {
            bottom = dpToPx(8);
        }
        if(isLeftSide) {
            start = dpToPx(8);
        }
        if(isRightSide) {
            end = dpToPx(8);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, bottom);
        layoutParams.setMarginStart(start);
        layoutParams.setMarginEnd(end);
        backgroundLayout.setLayoutParams(layoutParams);

        removeButton.setOnClickListener(view -> onRemoveClickListener.removeSpot(spot));
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
        ImageView spotThumbnail;
        TextView name;
        Button removeButton;
        ConstraintLayout backgroundLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            spotThumbnail = itemView.findViewById(R.id.spotThumbnail);
            name = itemView.findViewById(R.id.tvName);
            removeButton = itemView.findViewById(R.id.removeButton);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
        }
    }

    public void setSpots(List<SimpleTouristSpot> spots) {
        this.spots.clear();
        this.spots.addAll(spots);
        notifyDataSetChanged();
    }
}
