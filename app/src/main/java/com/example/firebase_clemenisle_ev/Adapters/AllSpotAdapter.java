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
import com.bumptech.glide.request.target.Target;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class AllSpotAdapter extends RecyclerView.Adapter<AllSpotAdapter.ViewHolder> {

    List<SimpleTouristSpot> touristSpotList, selectedSpots;
    int columnCount;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    OnButtonClickListener onButtonClickListener;

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    public interface OnButtonClickListener{
        void addSpot(SimpleTouristSpot spot);
    }

    public AllSpotAdapter(Context context, List<SimpleTouristSpot> touristSpotList,
                          List<SimpleTouristSpot> selectedSpots, int columnCount) {
        this.touristSpotList = touristSpotList;
        this.columnCount = columnCount;
        this.selectedSpots = selectedSpots;
        this.inflater =  LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_all_spot_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView thumbnail = holder.thumbnail;
        TextView tvName = holder.tvName;
        Button addButton = holder.addButton;
        ConstraintLayout backgroundLayout = holder.backgroundLayout;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        SimpleTouristSpot touristSpot = touristSpotList.get(position);

        String name = touristSpot.getName();
        String img = touristSpot.getImg();

        try {
            Glide.with(myContext).load(img).
                    placeholder(R.drawable.image_loading_placeholder).
                    override(Target.SIZE_ORIGINAL).into(thumbnail);
        }
        catch (Exception ignored) {}
        tvName.setText(name);

        String buttonText;
        if(isInSelectedSpots(touristSpot)) {
            buttonText = "Added";
            addButton.setText(buttonText);
            addButton.setEnabled(false);
        }
        else {
            buttonText = "Add";
            addButton.setText(buttonText);
            addButton.setEnabled(true);
        }

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

        addButton.setOnClickListener(view -> onButtonClickListener.addSpot(touristSpot));
    }

    private boolean isInSelectedSpots(SimpleTouristSpot targetSpot) {
        for(SimpleTouristSpot selectedSpot : selectedSpots) {
            if(selectedSpot.getId().equals(targetSpot.getId())) {
                return true;
            }
        }
        return false;
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return touristSpotList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView tvName;
        Button addButton;
        ConstraintLayout backgroundLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvName = itemView.findViewById(R.id.tvName);
            addButton = itemView.findViewById(R.id.addButton);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
        }
    }

    public void setSelectedSpots(List<SimpleTouristSpot> selectedSpots) {
        this.selectedSpots.clear();
        this.selectedSpots.addAll(selectedSpots);
        notifyDataSetChanged();
    }
}
