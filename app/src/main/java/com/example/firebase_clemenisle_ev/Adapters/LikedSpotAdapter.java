package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class LikedSpotAdapter extends RecyclerView.Adapter<LikedSpotAdapter.ViewHolder> {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);

    List<SimpleTouristSpot> likedSpots;
    String userId;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    long unlikePressedTime;
    Toast unlikeToast;
    boolean isUnliked = true;

    public LikedSpotAdapter(Context context, List<SimpleTouristSpot> likedSpots, String userId) {
        this.likedSpots = likedSpots;
        this.userId = userId;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_liked_spot_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView thumbnail = holder.thumbnail;
        TextView tvName = holder.tvName;
        Button unlikeButton = holder.unlikeButton;
        ConstraintLayout backgroundLayout = holder.backgroundLayout;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        SimpleTouristSpot likedSpot = likedSpots.get(position);

        String id = likedSpot.getId();
        String name = likedSpot.getName();
        String img = likedSpot.getImg();

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
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                layoutParams.rightMargin, layoutParams.bottomMargin);
        layoutParams.setMarginStart(start);
        layoutParams.setMarginEnd(end);
        backgroundLayout.setLayoutParams(layoutParams);

        unlikeButton.setOnClickListener(view -> {
            if(unlikePressedTime + 2500 > System.currentTimeMillis() && !isUnliked) {
                unlikeToast.cancel();

                DatabaseReference usersRef = firebaseDatabase.getReference("users")
                        .child(userId).child("likedSpots");
                usersRef.child(id).removeValue();

                isUnliked = true;
            }
            else {
                unlikeToast = Toast.makeText(myContext,
                        "Press again to unlike", Toast.LENGTH_SHORT);
                unlikeToast.show();

                unlikePressedTime = System.currentTimeMillis();

                isUnliked = false;
            }
        });
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return likedSpots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView tvName;
        Button unlikeButton;
        ConstraintLayout backgroundLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvName = itemView.findViewById(R.id.tvName);
            unlikeButton = itemView.findViewById(R.id.unlikeButton);
        }
    }
}
