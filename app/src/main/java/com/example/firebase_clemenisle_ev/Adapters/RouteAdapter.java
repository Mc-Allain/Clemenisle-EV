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
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.Route;
import com.example.firebase_clemenisle_ev.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    List<Route> routeList;
    int columnCount;
    String bookingId, status;
    boolean isLatest;
    LayoutInflater inflater;

    boolean isLoggedIn, inDriverMode;
    String userId;

    Context myContext;
    Resources myResources;

    OnVisitClickListener onVisitClickListener;

    public void setOnVisitClickListener(OnVisitClickListener onVisitClickListener) {
        this.onVisitClickListener = onVisitClickListener;
    }

    public interface OnVisitClickListener{
        void setProgressBarToVisible();
    }

    public RouteAdapter(Context context, List<Route> routeList, int columnCount, String bookingId,
                        String status, boolean isLatest, boolean isLoggedIn, boolean inDriverMode) {
        this.routeList = routeList;
        this.columnCount = columnCount;
        this.bookingId = bookingId;
        this.status = status;
        this.isLatest = isLatest;
        this.isLoggedIn = isLoggedIn;
        this.inDriverMode = inDriverMode;
        this.inflater =  LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_route_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView routeThumbnail = holder.routeThumbnail;
        TextView tvName = holder.tvName, tvRouteSpotNoBadge = holder.tvRouteSpotNoBadge;
        Button visitButton = holder.visitButton;
        ConstraintLayout backgroundLayout = holder.backgroundLayout;

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

        String spotId = routeList.get(position).getRouteId();

        try {
            Glide.with(myContext).load(routeList.get(position).getImg())
                    .placeholder(R.drawable.image_loading_placeholder)
                    .into(routeThumbnail);
        }
        catch (Exception ignored) {}

        tvName.setText(routeList.get(position).getName());
        tvRouteSpotNoBadge.setText(String.valueOf(position + 1));
        
        int colorBlue = myResources.getColor(R.color.blue);
        int colorRed = myResources.getColor(R.color.red);

        if(!routeList.get(position).isVisited()) {
            String visitButtonText = "Mark as visited";
            visitButton.setText(visitButtonText);
            visitButton.setBackgroundColor(colorBlue);
        }
        else {
            String unvisitButtonText = "Mark as unvisited";
            visitButton.setText(unvisitButtonText);
            visitButton.setBackgroundColor(colorRed);
        }

        visitButton.setEnabled(isLatest);

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) routeThumbnail.getLayoutParams();
        if(!status.equals("Completed") || inDriverMode) {
            visitButton.setVisibility(View.GONE);
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                    layoutParams.rightMargin, dpToPx(8));
        }
        else {
            visitButton.setVisibility(View.VISIBLE);
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                    layoutParams.rightMargin, dpToPx(0));
        }
        routeThumbnail.setLayoutParams(layoutParams);

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

        layoutParams = (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, bottom);
        layoutParams.setMarginStart(start);
        layoutParams.setMarginEnd(end);
        backgroundLayout.setLayoutParams(layoutParams);

        visitButton.setOnClickListener(view -> {
            visitButton.setEnabled(false);
            onVisitClickListener.setProgressBarToVisible();

            if(visitButton.getText().equals("Mark as visited")) {
                visitSpot(spotId);
            }
            else {
                unvisitSpot(spotId);
            }
        });
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView routeThumbnail;
        TextView tvName, tvRouteSpotNoBadge;
        Button visitButton;
        ConstraintLayout backgroundLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            routeThumbnail = itemView.findViewById(R.id.routeThumbnail);
            tvName = itemView.findViewById(R.id.tvName);
            tvRouteSpotNoBadge = itemView.findViewById(R.id.tvRouteSpotNoBadge);
            visitButton = itemView.findViewById(R.id.visitButton);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
        }
    }

    private void visitSpot(String spotId) {
        DatabaseReference usersRef = firebaseDatabase.getReference("users").child(userId)
                .child("bookingList").child(bookingId).child("routeSpots").child(spotId);
        usersRef.child("visited").setValue(true);
    }

    private void unvisitSpot(String spotId) {
        DatabaseReference usersRef = firebaseDatabase.getReference("users").child(userId)
                .child("bookingList").child(bookingId).child("routeSpots").child(spotId);
        usersRef.child("visited").setValue(false);
    }

    public void setStatus(String status) {
        this.status = status;
        notifyDataSetChanged();
    }
}
