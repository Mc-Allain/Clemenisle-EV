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
import com.example.firebase_clemenisle_ev.LoginActivity;
import com.example.firebase_clemenisle_ev.MapActivity;
import com.example.firebase_clemenisle_ev.R;
import com.example.firebase_clemenisle_ev.SelectedSpotActivity;
import com.example.firebase_clemenisle_ev.StreetWebView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

public class TouristSpotListAdapter extends RecyclerView.Adapter<TouristSpotListAdapter.ViewHolder> {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    List<DetailedTouristSpot> touristSpots;
    List<SimpleTouristSpot> likedSpots;
    LayoutInflater inflater;

    boolean isLoggedIn;
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

    public TouristSpotListAdapter(Context context, List<DetailedTouristSpot> touristSpots,
                                  List<SimpleTouristSpot> likedSpots, boolean isLoggedIn) {
        this.touristSpots = touristSpots;
        this.likedSpots = likedSpots;
        this.inflater = LayoutInflater.from(context);
        this.isLoggedIn = isLoggedIn;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_tourist_spot_layout_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView thumbnail = holder.thumbnail,
                likeImage = holder.likeImage, visitImage = holder.visitImage,
                bookImage = holder.bookImage, commentImage = holder.commentImage,
                moreImage = holder.moreImage, openImage = holder.openImage, i360Image = holder.i360Image, locateImage = holder.locateImage;
        TextView tvName = holder.tvName, tvLikes = holder.tvLikes, tvVisits = holder.tvVisits,
                tvBooks = holder.tvBooks, tvComments = holder.tvComments,
                tvOption = holder.tvOption, tvOpen = holder.tvOpen, tv360Image = holder.tv360Image, tvLocate = holder.tvLocate;
        ConstraintLayout backgroundLayout = holder.backgroundLayout,
                buttonLayout = holder.buttonLayout;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        firebaseAuth = FirebaseAuth.getInstance();
        if(isLoggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) {
                firebaseUser.reload();
                userId = firebaseUser.getUid();
            }
        }

        Handler optionHandler = new Handler();
        Runnable optionRunnable = () -> closeOption(buttonLayout, backgroundLayout, moreImage, tvOption);

        String id = touristSpots.get(position).getId();
        String name = touristSpots.get(position).getName();
        String img = touristSpots.get(position).getImg();
        int likes = touristSpots.get(position).getLikes();
        int visits = touristSpots.get(position).getVisits();
        int books = touristSpots.get(position).getBooks();
        int comments = touristSpots.get(position).getComments();
        double lat = touristSpots.get(position).getLat();
        double lng = touristSpots.get(position).getLng();
        boolean deactivated = touristSpots.get(position).isDeactivated();

        SimpleTouristSpot touristSpot = new SimpleTouristSpot(deactivated, id, img, name);

        Glide.with(myContext).load(img).placeholder(R.drawable.image_loading_placeholder).
                override(Target.SIZE_ORIGINAL).into(thumbnail);
        tvName.setText(name);
        tvLikes.setText(String.valueOf(likes));
        tvVisits.setText(String.valueOf(visits));
        tvBooks.setText(String.valueOf(books));
        tvComments.setText(String.valueOf(comments));

        boolean isLiked = isInLikedSpots(touristSpot);

        int color;
        if(!isLiked) color = myResources.getColor(R.color.black);
        else color = myResources.getColor(R.color.blue);
        likeImage.setColorFilter(color);

        closeOption(buttonLayout, backgroundLayout, moreImage, tvOption);

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
            if(isLoggedIn) {
                if(firebaseUser != null) {
                    likeImage.setEnabled(false);
                    onLikeClickListener.setProgressBarToVisible();

                    if(!isLiked) likeSpot(touristSpot);
                    else unlikeSpot(id);
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

        commentImage.setOnClickListener(view -> openItem(id, true));

        commentImage.setOnLongClickListener(view -> {
            Toast.makeText(myContext,
                    "Comments: " + tvComments.getText(),
                    Toast.LENGTH_SHORT).show();
            return false;
        });

        moreImage.setOnClickListener(view -> {
            if(tvOption.getText().equals("false")) {
                openOption(buttonLayout, backgroundLayout, moreImage,
                        optionHandler, optionRunnable, tvOption);
            }
            else {
                closeOption(buttonLayout, backgroundLayout, moreImage, tvOption);
            }
        });

        tvOpen.setOnClickListener(view -> openItem(id, false));

        openImage.setOnClickListener(view -> openItem(id, false));

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
        usersRef.child(spotId).removeValue();
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
                            "No Street View Record",
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

    private void openItem(String id, boolean toComment) {
        Intent intent = new Intent(myContext, SelectedSpotActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("isLoggedIn", isLoggedIn);
        intent.putExtra("toComment", toComment);
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
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.TOP,
                moreImage.getId(), ConstraintSet.TOP);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.BOTTOM,
                moreImage.getId(), ConstraintSet.BOTTOM);

        setTransition(buttonLayout);
        constraintSet.applyTo(backgroundLayout);

        tvOption.setText("true");
        moreImage.setEnabled(true);
        moreImage.setImageResource(R.drawable.ic_baseline_close_24);
        moreImage.setColorFilter(myResources.getColor(R.color.red));

        optionHandler.postDelayed(optionRunnable, 3000);
    }

    private void closeOption(ConstraintLayout buttonLayout, ConstraintLayout backgroundLayout,
                             ImageView moreImage, TextView tvOption) {
        moreImage.setEnabled(false);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(backgroundLayout);

        constraintSet.clear(buttonLayout.getId(), ConstraintSet.TOP);
        constraintSet.clear(buttonLayout.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(buttonLayout.getId(), ConstraintSet.TOP,
                backgroundLayout.getId(), ConstraintSet.BOTTOM);

        setTransition(buttonLayout);
        constraintSet.applyTo(backgroundLayout);

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

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView thumbnail, likeImage, visitImage, bookImage, commentImage,
                moreImage, openImage, i360Image, locateImage;
        TextView tvName, tvLikes, tvVisits, tvBooks, tvComments,
                tvOption, tvOpen, tv360Image, tvLocate;
        ConstraintLayout backgroundLayout, buttonLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvName = itemView.findViewById(R.id.tvName);
            likeImage = itemView.findViewById(R.id.likeImage);
            visitImage = itemView.findViewById(R.id.visitImage);
            bookImage = itemView.findViewById(R.id.bookImage);
            commentImage = itemView.findViewById(R.id.commentImage);

            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvVisits = itemView.findViewById(R.id.tvVisits);
            tvBooks = itemView.findViewById(R.id.tvBooks);
            tvComments = itemView.findViewById(R.id.tvComments);

            moreImage = itemView.findViewById(R.id.moreImage);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            buttonLayout = itemView.findViewById(R.id.buttonLayout);
            tvOption = itemView.findViewById(R.id.tvOption);
            openImage = itemView.findViewById(R.id.openImage);
            i360Image = itemView.findViewById(R.id.i360Image);
            locateImage = itemView.findViewById(R.id.locateImage);
            tvOpen = itemView.findViewById(R.id.tvOpen);
            tv360Image = itemView.findViewById(R.id.tv360Image);
            tvLocate = itemView.findViewById(R.id.tvLocate);

        }
    }
}
