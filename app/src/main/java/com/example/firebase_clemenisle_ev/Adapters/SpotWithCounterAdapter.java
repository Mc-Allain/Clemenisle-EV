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
import com.example.firebase_clemenisle_ev.Classes.Route;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class SpotWithCounterAdapter extends RecyclerView.Adapter<SpotWithCounterAdapter.ViewHolder> {

    List<Route> bookedSpots;
    int type;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    public SpotWithCounterAdapter(Context context, List<Route> bookedSpots, int type) {
        this.bookedSpots = bookedSpots;
        this.type = type;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public SpotWithCounterAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_spot_with_counter_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpotWithCounterAdapter.ViewHolder holder, int position) {
        ImageView thumbnail = holder.thumbnail;
        TextView tvName = holder.tvName, tvCounterBadge = holder.tvCounterBadge;
        ConstraintLayout backgroundLayout = holder.backgroundLayout;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        String id = bookedSpots.get(position).getId();
        String name = bookedSpots.get(position).getName();
        String img = bookedSpots.get(position).getImg();
        String count = "0";

        if(type == 0) {
            count = bookedSpots.get(position).getBooks() + "×";
        }
        else if(type == 1) {
            count = bookedSpots.get(position).getVisits() + "×";
        }

        try {
            Glide.with(myContext).load(img).
                    placeholder(R.drawable.image_loading_placeholder).
                    override(Target.SIZE_ORIGINAL).into(thumbnail);
        }
        catch (Exception ignored) {}
        tvName.setText(name);
        tvCounterBadge.setText(count);

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

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return bookedSpots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView tvName, tvCounterBadge;
        ConstraintLayout backgroundLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvName = itemView.findViewById(R.id.tvName);
            tvCounterBadge = itemView.findViewById(R.id.tvCounterBadge);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
        }
    }
}
