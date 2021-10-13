package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import com.example.firebase_clemenisle_ev.Classes.DetailedTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.Station;
import com.example.firebase_clemenisle_ev.LoginActivity;
import com.example.firebase_clemenisle_ev.MapActivity;
import com.example.firebase_clemenisle_ev.R;
import com.example.firebase_clemenisle_ev.StreetWebView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TouristSpotAdapter extends RecyclerView.Adapter<TouristSpotAdapter.ViewHolder> {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    List<DetailedTouristSpot> touristSpots;
    List<SimpleTouristSpot> likedSpots;
    LayoutInflater inflater;

    boolean loggedIn;
    String userId;

    Context myContext;
    Resources myResources;

    OnLikeClickListener onLikeClickListener;

    public void setOnLikeClickListener(OnLikeClickListener onLikeClickListener) {
        this.onLikeClickListener = onLikeClickListener;
    }

    public interface OnLikeClickListener{
        void setProgressBarToVisible();
    }

    public TouristSpotAdapter(Context context, List<DetailedTouristSpot> touristSpots,
                              List<SimpleTouristSpot> likedSpots, boolean loggedIn) {
        this.touristSpots = touristSpots;
        this.likedSpots = likedSpots;
        this.inflater = LayoutInflater.from(context);
        this.loggedIn = loggedIn;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_tourist_spot_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView thumbnail = holder.thumbnail,
                likeImage = holder.likeImage, visitImage = holder.visitImage,
                bookImage = holder.bookImage, moreImage = holder.moreImage,
                i360Image = holder.i360Image, locateImage = holder.locateImage;
        TextView tvName = holder.tvName, tvStation = holder.tvStation,
                tvLikes = holder.tvLikes, tvVisits = holder.tvVisits,
                tvBooks = holder.tvBooks, tvNearSpot = holder.tvNearSpot,
                tvLiked = holder.tvLiked, tvOption = holder.tvOption,
                tv360Image = holder.tv360Image, tvLocate = holder.tvLocate;
        ExpandableTextView extvDescription = holder.extvDescription;
        ConstraintLayout backgroundLayout = holder.backgroundLayout, buttonLayout = holder.buttonLayout,
                connectingLayout = holder.connectingLayout;
        RecyclerView nearSpotView = holder.nearSpotView;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        firebaseAuth = FirebaseAuth.getInstance();
        if(loggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) {
                firebaseUser.reload();
                userId = firebaseUser.getUid();
            }
        }

        Handler optionHandler = new Handler();
        Runnable optionRunnable = () -> closeOption(buttonLayout, connectingLayout, moreImage, tvOption);

        String id = touristSpots.get(position).getId();
        String name = touristSpots.get(position).getName();
        String description = touristSpots.get(position).getDescription();
        String img = touristSpots.get(position).getImg();
        int likes = touristSpots.get(position).getLikes();
        int visits = touristSpots.get(position).getVisits();
        int books = touristSpots.get(position).getBooks();
        double lat = touristSpots.get(position).getLat();
        double lng = touristSpots.get(position).getLng();
        boolean deactivated = touristSpots.get(position).isDeactivated();
        List<SimpleTouristSpot> nearSpots = new ArrayList<>(touristSpots.get(position).getNearSpots());
        List<Station> nearStations = new ArrayList<>(touristSpots.get(position).getNearStations());
        StringBuilder stations = new StringBuilder();

        SimpleTouristSpot touristSpot = new SimpleTouristSpot(deactivated, id, img, name);

        boolean isFirst = true;
        for(Station nearStation : nearStations) {
            if(isFirst) {
                stations.append(nearStation.getName());
                isFirst = false;
            }
            else {
                stations.append(", ").append(nearStation.getName());
            }
        }

        Glide.with(myContext).load(img).placeholder(R.drawable.image_loading_placeholder).
                override(Target.SIZE_ORIGINAL).into(thumbnail);
        tvName.setText(name);
        extvDescription.setText(description);
        tvStation.setText(stations.toString());
        tvLikes.setText(String.valueOf(likes));
        tvVisits.setText(String.valueOf(visits));
        tvBooks.setText(String.valueOf(books));

        if(nearSpots.size() == 0) {
            tvNearSpot.setVisibility(View.GONE);
            nearSpotView.setVisibility(View.GONE);
        }
        else {
            tvNearSpot.setVisibility(View.VISIBLE);
            nearSpotView.setVisibility(View.VISIBLE);
        }

        if(nearSpotView.getAdapter() == null) {
            LinearLayoutManager linearLayout =
                    new LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false);
            nearSpotView.setLayoutManager(linearLayout);
            NearSpotAdapter nearSpotAdapter = new NearSpotAdapter(myContext, nearSpots, loggedIn);
            nearSpotView.setAdapter(nearSpotAdapter);
        }
        else {
            NearSpotAdapter nearSpotAdapter = (NearSpotAdapter) nearSpotView.getAdapter();
            nearSpotAdapter.setNearSpots(nearSpots);
        }

        tvLiked.setText(String.valueOf(
                isInLikedSpots(touristSpot)
        ));
        String liked = tvLiked.getText().toString();

        int color;
        if(liked.equals("false")) {
            color = myResources.getColor(R.color.black);
        }
        else {
            color = myResources.getColor(R.color.blue);
        }
        likeImage.setColorFilter(color);

        closeOption(buttonLayout, connectingLayout, moreImage, tvOption);

        int top = dpToPx(4), bottom = dpToPx(4);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == getItemCount();

        if(isFirstItem) {
            top = dpToPx(8);
        }
        if(isLastItem) {
            bottom = dpToPx(88);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, bottom);
        backgroundLayout.setLayoutParams(layoutParams);

        likeImage.setEnabled(true);
        likeImage.setOnClickListener(view -> {
            if(loggedIn) {
                if(firebaseUser != null) {
                    likeImage.setEnabled(false);
                    onLikeClickListener.setProgressBarToVisible();

                    if(liked.equals("false")) {
                        likeSpot(touristSpot);
                    }
                    else{
                        unlikeSpot(id);
                    }
                }
            }
            else {
                Intent intent = new Intent(myContext, LoginActivity.class);
                myContext.startActivity(intent);
            }
        });

        likeImage.setOnLongClickListener(view -> {
            Toast.makeText(myContext,
                    "Likes: " + tvLikes.getText(),
                    Toast.LENGTH_SHORT).show();
            return false;
        });

        visitImage.setOnLongClickListener(view -> {
            Toast.makeText(myContext,
                    "Visits: " + tvVisits.getText(),
                    Toast.LENGTH_SHORT).show();
            return false;
        });

        bookImage.setOnLongClickListener(view -> {
            Toast.makeText(myContext,
                    "Books: " + tvBooks.getText(),
                    Toast.LENGTH_SHORT).show();
            return false;
        });

        moreImage.setOnClickListener(view -> {
            if(tvOption.getText().equals("false")) {
                openOption(buttonLayout, connectingLayout, moreImage,
                        optionHandler, optionRunnable, tvOption);
            }
            else {
                closeOption(buttonLayout, connectingLayout, moreImage, tvOption);
            }
        });

        thumbnail.setOnClickListener(view -> openStreetView(id));

        tv360Image.setOnClickListener(view -> openStreetView(id));

        i360Image.setOnClickListener(view -> openStreetView(id));

        tvLocate.setOnClickListener(view -> openMap(id, lat, lng, name));

        locateImage.setOnClickListener(view -> openMap(id, lat, lng, name));
    }

    private void likeSpot(SimpleTouristSpot touristSpot) {
        String spotId = touristSpot.getId();

        DatabaseReference usersRef = firebaseDatabase.getReference("users")
                .child(userId).child("likedSpots");
        usersRef.child(spotId).setValue(touristSpot);
    }

    private void unlikeSpot(String spotId) {
        DatabaseReference usersRef = firebaseDatabase.getReference("users")
                .child(userId).child("likedSpots");
        usersRef.child(spotId).setValue(null);
    }

    private boolean isInLikedSpots(SimpleTouristSpot targetSpot) {
        for(SimpleTouristSpot likedSpot : likedSpots) {
            if(likedSpot.getId().equals(targetSpot.getId())) {
                return true;
            }
        }
        return false;
    }

    private void openMap(String id, double lat, double lng, String name) {
        Intent intent = new Intent(myContext, MapActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        intent.putExtra("name", name);
        intent.putExtra("type", 0);
        myContext.startActivity(intent);
    }

    private void openStreetView(String id) {
        DatabaseReference touristSpotsRef = firebaseDatabase.getReference("touristSpots")
                .child(id).child("vri");
        touristSpotsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Intent intent = new Intent(myContext, StreetWebView.class);
                    intent.putExtra("id", id);
                    myContext.startActivity(intent);
                }
                else {
                    Toast.makeText(
                            myContext,
                            "No Virtual Reality Image",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void openOption(ConstraintLayout buttonLayout, ConstraintLayout connectingLayout,
                            ImageView moreImage, Handler optionHandler, Runnable optionRunnable,
                            TextView tvOption) {
        moreImage.setEnabled(false);

        optionHandler.removeCallbacks(optionRunnable);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(connectingLayout);

        constraintSet.clear(buttonLayout.getId(), ConstraintSet.TOP);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.TOP,
                moreImage.getId(), ConstraintSet.TOP);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.BOTTOM,
                moreImage.getId(), ConstraintSet.BOTTOM);

        setTransition(buttonLayout);
        constraintSet.applyTo(connectingLayout);

        tvOption.setText("true");
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_close_24);
        moreImage.setColorFilter(myResources.getColor(R.color.red));

        optionHandler.postDelayed(optionRunnable, 3000);
    }

    private void closeOption(ConstraintLayout buttonLayout, ConstraintLayout connectingLayout,
                             ImageView moreImage, TextView tvOption) {
        moreImage.setEnabled(false);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(connectingLayout);

        constraintSet.clear(buttonLayout.getId(), ConstraintSet.TOP);
        constraintSet.clear(buttonLayout.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.TOP,
                connectingLayout.getId(), ConstraintSet.BOTTOM);

        setTransition(buttonLayout);
        constraintSet.applyTo(connectingLayout);

        tvOption.setText("false");
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_more_horiz_24);
        moreImage.setColorFilter(myResources.getColor(R.color.black));
    }

    private void setTransition(ConstraintLayout constraintLayout) {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);
        TransitionManager.beginDelayedTransition(constraintLayout, transition);
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
        ImageView thumbnail, likeImage, visitImage, bookImage, moreImage, i360Image, locateImage;
        TextView tvName, tvStation, tvLikes, tvVisits, tvBooks, tvNearSpot, tvLiked, tvOption,
        tv360Image, tvLocate;
        ExpandableTextView extvDescription;
        ConstraintLayout backgroundLayout, buttonLayout, connectingLayout;
        RecyclerView nearSpotView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvName = itemView.findViewById(R.id.tvName);
            tvStation = itemView.findViewById(R.id.tvStartStation2);
            extvDescription = itemView.findViewById(R.id.extvDescription);
            likeImage = itemView.findViewById(R.id.likeImage);
            visitImage = itemView.findViewById(R.id.visitImage);
            bookImage = itemView.findViewById(R.id.bookImage);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvVisits = itemView.findViewById(R.id.tvVisits);
            tvBooks = itemView.findViewById(R.id.tvBooks);
            moreImage = itemView.findViewById(R.id.moreImage);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            buttonLayout = itemView.findViewById(R.id.buttonLayout);
            tvNearSpot = itemView.findViewById(R.id.tvNearSpot);
            nearSpotView = itemView.findViewById(R.id.nearSpotView);
            tvLiked = itemView.findViewById(R.id.tvLiked);
            tvOption = itemView.findViewById(R.id.tvOption);
            i360Image = itemView.findViewById(R.id.i360Image);
            locateImage = itemView.findViewById(R.id.locateImage);
            tv360Image = itemView.findViewById(R.id.tv360Image);
            tvLocate = itemView.findViewById(R.id.tvLocate);
            connectingLayout = itemView.findViewById(R.id.connectingLayout);
        }
    }
}
