package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Classes.Comment;
import com.example.firebase_clemenisle_ev.Classes.DetailedTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.R;
import com.example.firebase_clemenisle_ev.SelectedSpotActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class UserProfileCommentAdapter extends RecyclerView.Adapter<UserProfileCommentAdapter.ViewHolder> {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");
    DatabaseReference touristSpotsRef = firebaseDatabase.getReference("touristSpots");

    List<Comment> comments;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    String defaultStatusText = "Foul comment", appealedtext = "(Appealed)",
            notActiveText = "This comment is not active";

    public UserProfileCommentAdapter(Context context, List<Comment> comments) {
        this.comments = comments;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_user_profile_comment_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout,
                headerLayout = holder.headerLayout, commentLayout = holder.commentLayout;
        TextView tvTouristSpot = holder.tvTouristSpot, tvUserFullName = holder.tvUserFullName,
                tvTimestamp = holder.tvTimestamp, tvCommentStatus = holder.tvCommentStatus;
        ExpandableTextView extvComment = holder.extvComment;
        ImageView profileImage = holder.profileImage;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        Comment comment = comments.get(position);
        String touristSpotId = comment.getId();
        String userId = comment.getUserId();
        String timestamp = comment.getTimestamp();
        String commentValue = comment.getValue();
        boolean isDeactivated = comment.isDeactivated();
        boolean isFouled = comment.isFouled();
        boolean isAppealed = comment.isAppealed();

        if(userId != null && userId.length() > 0) {
            profileImage.setVisibility(View.VISIBLE);
            tvUserFullName.setVisibility(View.VISIBLE);

            backgroundLayout.setPadding(0, 0, 0, dpToPx(12));
        }
        else {
            profileImage.setVisibility(View.GONE);
            tvUserFullName.setVisibility(View.GONE);

            backgroundLayout.setPadding(0, 0, 0, dpToPx(8));
        }

        tvTimestamp.setText(timestamp);
        extvComment.setText(commentValue);

        if(isFouled) {
            tvCommentStatus.setVisibility(View.VISIBLE);
            tvCommentStatus.setText(defaultStatusText);

            extvComment.setVisibility(View.GONE);

            String status = defaultStatusText;
            if(isAppealed) status = defaultStatusText + " " + appealedtext;

            tvCommentStatus.setText(status);
        }
        else {
            tvCommentStatus.setVisibility(View.GONE);

            if(isDeactivated) {
                tvCommentStatus.setVisibility(View.VISIBLE);
                tvCommentStatus.setText(notActiveText);

                extvComment.setVisibility(View.GONE);
            }
            else extvComment.setVisibility(View.VISIBLE);
        }

        getTouristSpot(touristSpotId, tvTouristSpot);
        getUser(userId, tvUserFullName, profileImage);

        int top = dpToPx(2), bottom = dpToPx(2);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == getItemCount();

        if(isFirstItem) {
            top = dpToPx(0);
        }
        if(isLastItem) {
            bottom = dpToPx(0);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, bottom);
        backgroundLayout.setLayoutParams(layoutParams);

        backgroundLayout.setOnClickListener(view -> {
            Intent intent = new Intent(myContext, SelectedSpotActivity.class);
            intent.putExtra("id", touristSpotId);
            intent.putExtra("isLoggedIn", true);
            intent.putExtra("toComment", true);
            myContext.startActivity(intent);
        });
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    private void getTouristSpot(String touristSpotId, TextView tvTouristSpot) {
        touristSpotsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DetailedTouristSpot touristSpot = new DetailedTouristSpot(dataSnapshot);
                        if(touristSpot.getId().equals(touristSpotId)) {
                            tvTouristSpot.setText(touristSpot.getName());
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUser(String userId, TextView tvUserFullName, ImageView profileImage) {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);
                        if(user.getId().equals(userId)) {

                            try {
                                Glide.with(myContext).load(user.getProfileImage())
                                        .placeholder(R.drawable.image_loading_placeholder)
                                        .into(profileImage);
                            }
                            catch (Exception ignored) {}

                            String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                            if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
                            tvUserFullName.setText(fromHtml(fullName));

                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        if(html == null){
            return new SpannableString("");
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        }
        else {
            return Html.fromHtml(html);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout, headerLayout, commentLayout;
        TextView tvTouristSpot, tvUserFullName, tvTimestamp, tvCommentStatus;
        ExpandableTextView extvComment;
        ImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            headerLayout = itemView.findViewById(R.id.headerLayout);
            commentLayout = itemView.findViewById(R.id.backgroundLayout);
            tvTouristSpot = itemView.findViewById(R.id.tvTouristSpot);
            tvUserFullName = itemView.findViewById(R.id.tvUserFullName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvCommentStatus = itemView.findViewById(R.id.tvCommentStatus);
            extvComment = itemView.findViewById(R.id.extvComment);
            profileImage = itemView.findViewById(R.id.profileImage);

            setIsRecyclable(false);
        }
    }
}
