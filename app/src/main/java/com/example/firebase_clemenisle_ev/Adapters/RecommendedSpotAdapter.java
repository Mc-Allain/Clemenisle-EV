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

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class RecommendedSpotAdapter extends RecyclerView.Adapter<RecommendedSpotAdapter.ViewHolder> {

    List<SimpleTouristSpot> recommendedSpots, selectedSpots,
            touristSpotList, hiddenSpots = new ArrayList<>();
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    OnButtonClickListener onButtonClickListener;

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    public interface OnButtonClickListener{
        void addSpot(SimpleTouristSpot recommendedSpot);
        void viewAll();
    }

    public RecommendedSpotAdapter(Context context, List<SimpleTouristSpot> recommendedSpots,
                                  List<SimpleTouristSpot> selectedSpots,
                                  List<SimpleTouristSpot> touristSpotList) {
        this.recommendedSpots = recommendedSpots;
        this.selectedSpots = selectedSpots;
        this.touristSpotList = touristSpotList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_recommended_spot_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView thumbnail = holder.thumbnail, viewAllThumbnail = holder.viewAllThumbnail;
        TextView tvName = holder.tvName, tvSpotCount = holder.tvSpotCount;
        Button addButton = holder.addButton, viewButton = holder.viewButton;
        ConstraintLayout backgroundLayout = holder.backgroundLayout,
                viewAllLayout = holder.viewAllLayout, touristSpotLayout = holder.touristSpotLayout;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        if(position == 0) {
            touristSpotLayout.setVisibility(View.GONE);

            hiddenSpots.clear();
            for(SimpleTouristSpot touristSpot : touristSpotList) {
                if(!isInRecommendedSpots(touristSpot)) {
                    hiddenSpots.add(touristSpot);
                }
            }

            if(hiddenSpots.size() > 0) {
                viewAllLayout.setVisibility(View.VISIBLE);

                String countText =  "+" + hiddenSpots.size();
                String img = hiddenSpots.get(position).getImg();

                Glide.with(myContext).load(img).override(Target.SIZE_ORIGINAL).into(viewAllThumbnail);
                tvSpotCount.setText(countText);

                viewButton.setOnClickListener(view -> onButtonClickListener.viewAll());
            }
            else {
                viewAllLayout.setVisibility(View.GONE);
            }
        }
        else {
            touristSpotLayout.setVisibility(View.VISIBLE);
            viewAllLayout.setVisibility(View.GONE);

            SimpleTouristSpot recommendedSpot = recommendedSpots.get(position - 1);

            String name = recommendedSpot.getName();
            String img = recommendedSpot.getImg();

            Glide.with(myContext).load(img).override(Target.SIZE_ORIGINAL).into(thumbnail);
            tvName.setText(name);

            String buttonText;
            if(isInSelectedSpots(recommendedSpot)) {
                buttonText = "Added";
                addButton.setText(buttonText);
                addButton.setEnabled(false);
            }
            else {
                buttonText = "Add";
                addButton.setText(buttonText);
                addButton.setEnabled(true);
            }

            addButton.setOnClickListener(view -> {
                onButtonClickListener.addSpot(recommendedSpot);
            });
        }

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
    }

    private boolean isInSelectedSpots(SimpleTouristSpot targetSpot) {
        for(SimpleTouristSpot selectedSpot : selectedSpots) {
            if(selectedSpot.getId().equals(targetSpot.getId())) {
                return true;
            }
        }
        return false;
    }


    private boolean isInRecommendedSpots(SimpleTouristSpot targetSpot) {
        if(recommendedSpots.size() > 0) {
            for(SimpleTouristSpot recommendedSpot : recommendedSpots) {
                if(recommendedSpot.getId().equals(targetSpot.getId())) {
                    return true;
                }
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
        return recommendedSpots.size() + 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail, viewAllThumbnail;
        TextView tvName, tvSpotCount;
        Button addButton, viewButton;
        ConstraintLayout backgroundLayout, touristSpotLayout, viewAllLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);

            viewAllThumbnail = itemView.findViewById(R.id.viewAllThumbnail);
            tvSpotCount = itemView.findViewById(R.id.tvSpotCount);
            viewButton = itemView.findViewById(R.id.viewButton);
            viewAllLayout = itemView.findViewById(R.id.viewAllLayout);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvName = itemView.findViewById(R.id.tvName);
            addButton = itemView.findViewById(R.id.addButton);
            touristSpotLayout = itemView.findViewById(R.id.touristSpotLayout);
        }
    }

    public void setSelectedSpots(List<SimpleTouristSpot> selectedSpots) {
        this.selectedSpots.clear();
        this.selectedSpots.addAll(selectedSpots);
        notifyDataSetChanged();
    }
}
