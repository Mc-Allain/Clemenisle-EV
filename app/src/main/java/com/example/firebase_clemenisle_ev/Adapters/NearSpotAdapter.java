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
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.R;
import com.example.firebase_clemenisle_ev.SelectedSpotActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class NearSpotAdapter extends RecyclerView.Adapter<NearSpotAdapter.ViewHolder>{

    List<SimpleTouristSpot> nearSpots;
    LayoutInflater inflater;
    boolean isLoggedIn;

    Context myContext;
    Resources myResources;

    public NearSpotAdapter(Context context, List<SimpleTouristSpot> nearSpots, boolean isLoggedIn) {
        this.nearSpots = nearSpots;
        this.inflater = LayoutInflater.from(context);
        this.isLoggedIn = isLoggedIn;
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

        String id = nearSpots.get(position).getId();
        String name = nearSpots.get(position).getName();
        String img = nearSpots.get(position).getImg();

        Glide.with(myContext).load(img).placeholder(R.drawable.image_loading_placeholder).override(Target.SIZE_ORIGINAL).into(thumbnail);
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

        thumbnail.setOnClickListener(view -> {
            Intent intent = new Intent(myContext, SelectedSpotActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("isLoggedIn", isLoggedIn);
            myContext.startActivity(intent);
        });
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return nearSpots.size();
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

    public void setNearSpots(List<SimpleTouristSpot> nearSpots) {
        this.nearSpots.clear();
        this.nearSpots.addAll(nearSpots);
        notifyDataSetChanged();
    }
}
