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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Classes.Comment;
import com.example.firebase_clemenisle_ev.Classes.DateTimeDifference;
import com.example.firebase_clemenisle_ev.Classes.DetailedTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.OtherComment;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
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
    List<OtherComment> otherComments;
    User user;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    String defaultStatusText = "Foul comment", appealedtext = "(Appealed)",
            reportedStatus = "Reported", notActiveText = "This comment is not active";

    public UserProfileCommentAdapter(Context context, List<Comment> comments, List<OtherComment> otherComments) {
        this.comments = comments;
        this.otherComments = otherComments;
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
                headerLayout = holder.headerLayout, commentLayout = holder.commentLayout,
                badgeLayout = holder.badgeLayout;
        TextView tvTouristSpot = holder.tvTouristSpot, tvUserFullName = holder.tvUserFullName,
                tvTimestamp = holder.tvTimestamp, tvCommentStatus = holder.tvCommentStatus;
        ExpandableTextView extvComment = holder.extvComment;
        ImageView profileImage = holder.profileImage, ownerImage = holder.ownerImage, developerImage = holder.developerImage,
                adminImage = holder.adminImage, driverImage = holder.driverImage, likerImage = holder.likerImage;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        if(comments.size() > 0) {
            Comment comment = comments.get(position);
            String spotId = comment.getId();
            String timestamp = comment.getTimestamp();
            String value = comment.getValue();
            boolean isDeactivated = comment.isDeactivated();
            boolean isFouled = comment.isFouled();
            boolean isAppealed = comment.isAppealed();

            profileImage.setVisibility(View.GONE);
            tvUserFullName.setVisibility(View.GONE);

            backgroundLayout.setPadding(0, 0, 0, dpToPx(8));

            DateTimeDifference dateTimeDifference = new DateTimeDifference(timestamp);
            timestamp = dateTimeDifference.getResult();
            tvTimestamp.setText(timestamp);
            extvComment.setText(value);

            getTouristSpot(spotId, tvTouristSpot);

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

            backgroundLayout.setOnClickListener(view -> {
                Intent intent = new Intent(myContext, SelectedSpotActivity.class);
                intent.putExtra("id", spotId);
                intent.putExtra("isLoggedIn", true);
                intent.putExtra("toComment", true);
                myContext.startActivity(intent);
            });
        }
        else if(otherComments.size() > 0) {
            OtherComment otherComment = otherComments.get(position);
            String spotId = otherComment.getSpotId();
            String senderId = otherComment.getSenderUserId();

            profileImage.setVisibility(View.VISIBLE);
            tvUserFullName.setVisibility(View.VISIBLE);

            backgroundLayout.setPadding(0, 0, 0, dpToPx(12));

            getTouristSpot(spotId, tvTouristSpot);
            getUser(senderId, tvUserFullName, profileImage, spotId, badgeLayout, ownerImage, developerImage,
                    adminImage, driverImage, likerImage, tvTimestamp, extvComment, tvCommentStatus);

            backgroundLayout.setOnClickListener(view -> {
                Intent intent = new Intent(myContext, SelectedSpotActivity.class);
                intent.putExtra("id", spotId);
                intent.putExtra("isLoggedIn", true);
                intent.putExtra("toComment", true);
                intent.putExtra("selectedSenderId", senderId);
                myContext.startActivity(intent);
            });
        }

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
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    private void getTouristSpot(String spotId, TextView tvTouristSpot) {
        touristSpotsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DetailedTouristSpot touristSpot = new DetailedTouristSpot(dataSnapshot);
                        if(touristSpot.getId().equals(spotId)) {
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

    private void getUser(String senderId, TextView tvUserFullName, ImageView profileImage,
                         String spotId,  ConstraintLayout badgeLayout, ImageView ownerImage,
                         ImageView developerImage, ImageView adminImage, ImageView driverImage,
                         ImageView likerImage, TextView tvTimestamp, ExpandableTextView extvComment,
                         TextView tvCommentStatus) {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User sender = new User(dataSnapshot);
                        if(sender.getId().equals(senderId)) {

                            try {
                                Glide.with(myContext).load(sender.getProfileImage())
                                        .placeholder(R.drawable.image_loading_placeholder)
                                        .into(profileImage);
                            }
                            catch (Exception ignored) {}

                            String fullName = "<b>" + sender.getLastName() + "</b>, " + sender.getFirstName();
                            if(sender.getMiddleName().length() > 0) fullName += " " + sender.getMiddleName();
                            tvUserFullName.setText(fromHtml(fullName));

                            badgeLayout.setVisibility(View.GONE);
                            ownerImage.setVisibility(View.GONE);
                            developerImage.setVisibility(View.GONE);
                            adminImage.setVisibility(View.GONE);
                            driverImage.setVisibility(View.GONE);
                            likerImage.setVisibility(View.GONE);

                            if(sender.getRole().isOwner()) {
                                badgeLayout.setVisibility(View.VISIBLE);
                                ownerImage.setVisibility(View.VISIBLE);
                                ownerImage.setOnLongClickListener(view -> {
                                    Toast.makeText(
                                            myContext,
                                            "Owner",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return false;
                                });
                            }
                            if(sender.getRole().isDeveloper()) {
                                badgeLayout.setVisibility(View.VISIBLE);
                                developerImage.setVisibility(View.VISIBLE);
                                developerImage.setOnLongClickListener(view -> {
                                    Toast.makeText(
                                            myContext,
                                            "Developer",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return false;
                                });
                            }
                            if(sender.getRole().isAdmin()) {
                                badgeLayout.setVisibility(View.VISIBLE);
                                adminImage.setVisibility(View.VISIBLE);
                                adminImage.setOnLongClickListener(view -> {
                                    Toast.makeText(
                                            myContext,
                                            "Admin",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return false;
                                });
                            }
                            if(sender.getRole().isDriver()) {
                                badgeLayout.setVisibility(View.VISIBLE);
                                driverImage.setVisibility(View.VISIBLE);
                                driverImage.setOnLongClickListener(view -> {
                                    Toast.makeText(
                                            myContext,
                                            "Driver",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return false;
                                });
                            }

                            List<SimpleTouristSpot> likedSpots = sender.getLikedSpots();
                            for(SimpleTouristSpot likedSpot : likedSpots) {
                                if(likedSpot.getId().equals(spotId)) {
                                    badgeLayout.setVisibility(View.VISIBLE);
                                    likerImage.setVisibility(View.VISIBLE);
                                    likerImage.setOnLongClickListener(view -> {
                                        Toast.makeText(
                                                myContext,
                                                "Liker",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        return false;
                                    });
                                    break;
                                }
                            }

                            String value = null, timestamp = null;
                            boolean isDeactivated = false, isFouled = false, isAppealed = false;

                            for(Comment comment : sender.getComments()) {
                                if(comment.getId().equals(spotId)) {
                                    isDeactivated = comment.isDeactivated();
                                    isFouled = comment.isFouled();
                                    isAppealed = comment.isAppealed();
                                    timestamp = comment.getTimestamp();
                                    value = comment.getValue();
                                    break;
                                }
                            }

                            DateTimeDifference dateTimeDifference = new DateTimeDifference(timestamp);
                            timestamp = dateTimeDifference.getResult();
                            tvTimestamp.setText(timestamp);
                            extvComment.setText(value);

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

                            if(user != null) {
                                List<OtherComment> reportedComments = user.getReportedComments();
                                for(OtherComment otherComment : reportedComments) {
                                    if(otherComment.getSpotId().equals(spotId) && otherComment.getSenderUserId().equals(sender.getId())) {
                                        tvCommentStatus.setVisibility(View.VISIBLE);

                                        String status = reportedStatus;

                                        if(isFouled) status = defaultStatusText;
                                        else if(isDeactivated) status = notActiveText + " | " + reportedStatus;

                                        tvCommentStatus.setText(status);
                                        break;
                                    }
                                }
                            }

                            return;
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

    public void setUser(User user) {
        this.user = user;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return Math.max(comments.size(), otherComments.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout, headerLayout, commentLayout, badgeLayout;
        TextView tvTouristSpot, tvUserFullName, tvTimestamp, tvCommentStatus;
        ExpandableTextView extvComment;
        ImageView profileImage, ownerImage, developerImage, adminImage, driverImage, likerImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            headerLayout = itemView.findViewById(R.id.headerLayout);
            commentLayout = itemView.findViewById(R.id.backgroundLayout);
            tvTouristSpot = itemView.findViewById(R.id.tvTouristSpot);
            tvUserFullName = itemView.findViewById(R.id.tvUserFullName);
            badgeLayout = itemView.findViewById(R.id.badgeLayout);
            ownerImage = itemView.findViewById(R.id.ownerImage);
            developerImage = itemView.findViewById(R.id.developerImage);
            adminImage = itemView.findViewById(R.id.adminImage);
            driverImage = itemView.findViewById(R.id.driverImage);
            likerImage = itemView.findViewById(R.id.likerImage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvCommentStatus = itemView.findViewById(R.id.tvCommentStatus);
            extvComment = itemView.findViewById(R.id.extvComment);
            profileImage = itemView.findViewById(R.id.profileImage);

            setIsRecyclable(false);
        }
    }
}
