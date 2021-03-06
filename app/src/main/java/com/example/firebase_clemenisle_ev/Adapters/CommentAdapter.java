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
import com.example.firebase_clemenisle_ev.Classes.OtherComment;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.LoginActivity;
import com.example.firebase_clemenisle_ev.R;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    List<User> users, commentedUsers;
    String spotId, userId;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    int colorBlue, colorInitial, colorBlack, colorRed;

    String defaultStatusText = "Foul comment", appealedText = "(Appealed)",
            reportedStatus = "Reported", notActiveText = "This comment is not active";

    int loadCommentItemPosition = 5, incrementLoadedItems = 5;

    OnActionButtonClicked onActionButtonClickedListener;

    public CommentAdapter(Context context, List<User> users, List<User> commentedUsers, String spotId, String userId) {
        this.users = users;
        this.commentedUsers = commentedUsers;
        this.spotId = spotId;
        this.userId = userId;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_comment_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout, commentLayout = holder.commentLayout,
                voteLayout = holder.voteLayout, loadCommentLayout = holder.loadCommentLayout,
                badgeLayout = holder.badgeLayout;
        TextView tvUserFullName = holder.tvUserFullName, tvTimestamp = holder.tvTimestamp,
                tvCommentStatus = holder.tvCommentStatus, tvUpVotes = holder.tvUpVotes,
                tvDownVotes = holder.tvDownVotes, tvLoadComment = holder.tvLoadComment;
        ExpandableTextView extvComment = holder.extvComment;
        ImageView profileImage  = holder.profileImage,
                editImage = holder.editImage,
                appealImage = holder.appealImage,
                deactivateImage = holder.deactivateImage,
                reportImage = holder.reportImage,
                upVoteImage = holder.upVoteImage,
                downVoteImage = holder.downVoteImage,
                ownerImage = holder.ownerImage,
                developerImage = holder.developerImage,
                adminImage = holder.adminImage,
                driverImage = holder.driverImage,
                likerImage = holder.likerImage;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);
        colorBlack = myResources.getColor(R.color.black);
        colorRed = myResources.getColor(R.color.red);

        if(loadCommentItemPosition < incrementLoadedItems) loadCommentItemPosition = incrementLoadedItems;

        if(commentedUsers.size() < loadCommentItemPosition && commentedUsers.size() != 0)
            loadCommentItemPosition = commentedUsers.size();

        if(position < loadCommentItemPosition && commentedUsers.size() != 0) {
            backgroundLayout.setVisibility(View.VISIBLE);
            commentLayout.setVisibility(View.VISIBLE);
            loadCommentLayout.setVisibility(View.GONE);

            User user = commentedUsers.get(position);

            List<Comment> comments = user.getComments();

            Comment commentRecord = null;
            for(Comment thisComment : comments) {
                if(thisComment.getId().equals(spotId)) {
                    commentRecord = thisComment;
                    break;
                }
            }

            setOnScreenEnabled(true, reportImage, deactivateImage, appealImage, editImage,
                    upVoteImage, downVoteImage);
            editImage.getDrawable().setTint(colorBlue);

            try {
                Glide.with(myContext).load(user.getProfileImage())
                        .placeholder(R.drawable.image_loading_placeholder)
                        .into(profileImage);
            }
            catch (Exception ignored) {}

            String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
            if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
            tvUserFullName.setText(fromHtml(fullName));

            boolean isUpVoted = false, isDownVoted = false, isReported = false;

            if(commentRecord != null) {
                String commentValue = commentRecord.getValue();
                boolean isDeactivated = commentRecord.isDeactivated();
                boolean isFouled = commentRecord.isFouled();
                boolean isAppealed = commentRecord.isAppealed();

                extvComment.setText(commentValue);
                backgroundLayout.setVisibility(View.VISIBLE);

                String timestamp = commentRecord.getTimestamp();
                DateTimeDifference dateTimeDifference = new DateTimeDifference(timestamp);
                timestamp = dateTimeDifference.getResult();
                tvTimestamp.setText(timestamp);

                if(isFouled) {
                    tvCommentStatus.setVisibility(View.VISIBLE);
                    tvCommentStatus.setText(defaultStatusText);

                    extvComment.setVisibility(View.GONE);
                    voteLayout.setVisibility(View.GONE);

                    if(user.getId().equals(userId))  {
                        appealImage.setEnabled(true);
                        appealImage.setVisibility(View.VISIBLE);

                        String status = defaultStatusText;
                        if(isAppealed) {
                            appealImage.setEnabled(false);
                            appealImage.getDrawable().setTint(colorInitial);
                            status = defaultStatusText + " " + appealedText;
                        }
                        else appealImage.getDrawable().setTint(colorBlue);

                        tvCommentStatus.setText(status);
                    }
                    else appealImage.setVisibility(View.GONE);

                    deactivateImage.setVisibility(View.GONE);
                    reportImage.setVisibility(View.GONE);

                    backgroundLayout.setPadding(0, 0, 0, dpToPx(12));
                }
                else {
                    tvCommentStatus.setVisibility(View.GONE);

                    appealImage.setVisibility(View.GONE);

                    if(isDeactivated) {
                        tvCommentStatus.setVisibility(View.VISIBLE);
                        tvCommentStatus.setText(notActiveText);

                        deactivateImage.setImageResource(R.drawable.ic_baseline_comment_24);
                        deactivateImage.getDrawable().setTint(colorBlue);

                        extvComment.setVisibility(View.GONE);
                        voteLayout.setVisibility(View.GONE);

                        backgroundLayout.setPadding(0, 0, 0, dpToPx(12));
                    }
                    else {
                        extvComment.setVisibility(View.VISIBLE);
                        voteLayout.setVisibility(View.VISIBLE);

                        deactivateImage.setImageResource(R.drawable.ic_baseline_comments_disabled_24);
                        deactivateImage.getDrawable().setTint(colorRed);

                        backgroundLayout.setPadding(0, 0, 0, 0);
                    }

                    if(user.getId().equals(userId)) deactivateImage.setVisibility(View.VISIBLE);
                    else deactivateImage.setVisibility(View.GONE);

                    if(user.getId().equals(userId)) reportImage.setVisibility(View.GONE);
                    else reportImage.setVisibility(View.VISIBLE);
                }

                badgeLayout.setVisibility(View.GONE);
                ownerImage.setVisibility(View.GONE);
                developerImage.setVisibility(View.GONE);
                adminImage.setVisibility(View.GONE);
                driverImage.setVisibility(View.GONE);
                likerImage.setVisibility(View.GONE);

                if(user.getRole().isOwner()) {
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
                if(user.getRole().isDeveloper()) {
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
                if(user.getRole().isAdmin()) {
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
                if(user.getRole().isDriver()) {
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

                List<SimpleTouristSpot> likedSpots = user.getLikedSpots();
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

                if(user.getId().equals(userId)) {
                    editImage.setVisibility(View.VISIBLE);

                    upVoteImage.setEnabled(false);
                    downVoteImage.setEnabled(false);
                    upVoteImage.getDrawable().setTint(colorInitial);
                    downVoteImage.getDrawable().setTint(colorInitial);

                    fullName += " <b>(You)<b/>";
                    tvUserFullName.setText(fromHtml(fullName));
                }
                else {
                    editImage.setVisibility(View.GONE);

                    upVoteImage.setEnabled(true);
                    downVoteImage.setEnabled(true);
                    upVoteImage.getDrawable().setTint(colorBlack);
                    downVoteImage.getDrawable().setTint(colorBlack);

                    commentLayout.setOnClickListener(view -> onActionButtonClickedListener.commentOnClick(user.getId()));
                }
            }
            else backgroundLayout.setVisibility(View.GONE);

            reportImage.setEnabled(true);
            reportImage.getDrawable().setTint(colorRed);

            int upVotes = 0, downVotes = 0;
            for(User user1 : users) {
                List<OtherComment> upVotedComments = user1.getUpVotedComments();
                for(OtherComment otherComment : upVotedComments) {
                    if(otherComment.getSpotId().equals(spotId) && otherComment.getSenderUserId().equals(user.getId())) {
                        upVotes++;

                        if(user1.getId().equals(userId)) {
                            upVoteImage.getDrawable().setTint(colorBlue);
                            isUpVoted = true;
                        }
                        break;
                    }
                }

                List<OtherComment> downVotedComments = user1.getDownVotedComments();
                for(OtherComment otherComment : downVotedComments) {
                    if(otherComment.getSpotId().equals(spotId) && otherComment.getSenderUserId().equals(user.getId())) {
                        downVotes++;

                        if(user1.getId().equals(userId)) {
                            downVoteImage.getDrawable().setTint(colorRed);
                            isDownVoted = true;
                        }
                        break;
                    }
                }

                if(user1.getId().equals(userId)) {
                    List<OtherComment> reportedComments = user1.getReportedComments();
                    for(OtherComment otherComment : reportedComments) {
                        if(otherComment.getSpotId().equals(spotId) && otherComment.getSenderUserId().equals(user.getId())) {
                            reportImage.setEnabled(false);
                            reportImage.getDrawable().setTint(colorInitial);
                            upVoteImage.setEnabled(false);
                            upVoteImage.getDrawable().setTint(colorInitial);
                            isReported = true;

                            tvCommentStatus.setVisibility(View.VISIBLE);

                            String status = reportedStatus;

                            if(commentRecord.isFouled()) status = defaultStatusText;
                            else if(commentRecord.isDeactivated()) status = notActiveText + " | " + reportedStatus;

                            tvCommentStatus.setText(status);
                            break;
                        }
                    }
                }
            }
            boolean finalUpVoted = isUpVoted, finalDownVoted = isDownVoted,
                    finalReported = isReported;

            tvUpVotes.setText(String.valueOf(upVotes));
            tvDownVotes.setText(String.valueOf(downVotes));

            upVoteImage.setOnLongClickListener(view -> {
                onActionButtonClickedListener.upVoteImageOnLongClick();
                return false;
            });

            downVoteImage.setOnLongClickListener(view -> {
                onActionButtonClickedListener.downVoteImageOnLongClick();
                return false;
            });

            editImage.setOnLongClickListener(view -> {
                onActionButtonClickedListener.editImageOnLongClick();
                return false;
            });

            appealImage.setOnLongClickListener(view -> {
                onActionButtonClickedListener.appealImageOnLongClick();
                return false;
            });

            deactivateImage.setOnLongClickListener(view -> {
                onActionButtonClickedListener.deactivateImageOnLongClick();
                return false;
            });

            reportImage.setOnLongClickListener(view -> {
                onActionButtonClickedListener.reportImageOnLongClick();
                return false;
            });

            upVoteImage.setOnClickListener(view -> {
                if(userId == null) loginPrompt();
                else {
                    setOnScreenEnabled(false, reportImage, deactivateImage, appealImage, editImage,
                            upVoteImage, downVoteImage);
                    onActionButtonClickedListener.
                            upVoteImageOnClick(spotId, user.getId(), finalUpVoted, finalDownVoted);
                }
            });

            downVoteImage.setOnClickListener(view -> {
                if(userId == null) loginPrompt();
                else {
                    if(finalReported) {
                        Toast.makeText(
                                myContext,
                                "You cannot remove your down vote in the comment that you reported",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    else {
                        setOnScreenEnabled(false, reportImage, deactivateImage, appealImage, editImage,
                                upVoteImage, downVoteImage);
                        onActionButtonClickedListener.
                                downVoteImageOnClick(spotId, user.getId(), finalUpVoted, finalDownVoted);
                    }
                }
            });

            editImage.setOnClickListener(view -> onActionButtonClickedListener.editImageOnClick());

            appealImage.setOnClickListener(view -> {
                setOnScreenEnabled(false, reportImage, deactivateImage, appealImage, editImage,
                        upVoteImage, downVoteImage);
                onActionButtonClickedListener.appealImageOnClick();
            });

            deactivateImage.setOnClickListener(view -> onActionButtonClickedListener.deactivateImageOnClick());

            reportImage.setOnClickListener(view -> {
                if(userId == null) loginPrompt();
                else {
                    setOnScreenEnabled(false, reportImage, deactivateImage, appealImage, editImage,
                            upVoteImage, downVoteImage);
                    onActionButtonClickedListener.reportImageOnClick(spotId, user.getId());
                }
            });
        }
        else if(position == loadCommentItemPosition && position != commentedUsers.size()) {
            backgroundLayout.setVisibility(View.VISIBLE);
            commentLayout.setVisibility(View.GONE);
            loadCommentLayout.setVisibility(View.VISIBLE);

            int itemsCountToIncrement = incrementLoadedItems;
            if(loadCommentItemPosition + incrementLoadedItems > commentedUsers.size())
                itemsCountToIncrement = commentedUsers.size() - loadCommentItemPosition;

            String loadCommentText = "Load " + itemsCountToIncrement + " more ";
            loadCommentText += itemsCountToIncrement == 1  ? "comment" : "comments";
            tvLoadComment.setText(loadCommentText);

            loadCommentLayout.setOnClickListener(view -> {
                loadCommentItemPosition += incrementLoadedItems;
                notifyDataSetChanged();
            });
        }
        else backgroundLayout.setVisibility(View.GONE);

        int top = dpToPx(1), bottom = dpToPx(1);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == getItemCount();

        if(isFirstItem) {
            top = dpToPx(8);
        }
        if(isLastItem) {
            bottom = dpToPx(8);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, bottom);
        backgroundLayout.setLayoutParams(layoutParams);
    }

    private void setOnScreenEnabled(boolean value,
                                           ImageView reportImage, ImageView deactivateImage,
                                    ImageView appealImage, ImageView editImage,
                                    ImageView upVoteImage, ImageView downVoteImage) {

        reportImage.setEnabled(value);
        deactivateImage.setEnabled(value);
        appealImage.setEnabled(value);
        editImage.setEnabled(value);
        upVoteImage.setEnabled(value);
        downVoteImage.setEnabled(value);

        if(!value) {
            reportImage.getDrawable().setTint(colorInitial);
            deactivateImage.getDrawable().setTint(colorInitial);
            appealImage.getDrawable().setTint(colorInitial);
            editImage.getDrawable().setTint(colorInitial);
            upVoteImage.getDrawable().setTint(colorInitial);
            downVoteImage.getDrawable().setTint(colorInitial);
        }
    }

    private void loginPrompt() {
        Intent intent = new Intent(myContext, LoginActivity.class);
        myContext.startActivity(intent);
    }

    public interface OnActionButtonClicked {
        void editImageOnLongClick();
        void reportImageOnLongClick();
        void deactivateImageOnLongClick();
        void appealImageOnLongClick();
        void upVoteImageOnLongClick();
        void downVoteImageOnLongClick();

        void editImageOnClick();
        void appealImageOnClick();
        void deactivateImageOnClick();
        void reportImageOnClick(String spotId, String senderUserId);
        void upVoteImageOnClick(String spotId, String senderUserId, boolean isUpVoted, boolean isDownVoted);
        void downVoteImageOnClick(String spotId, String senderUserId, boolean isUpVoted, boolean isDownVoted);

        void commentOnClick(String selectedSenderId);
    }

    public void setOnActionButtonClickedListener(OnActionButtonClicked onActionButtonClickedListener) {
        this.onActionButtonClickedListener = onActionButtonClickedListener;
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

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return commentedUsers.size() + 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout, voteLayout, commentLayout, loadCommentLayout, badgeLayout;
        TextView tvUserFullName, tvTimestamp, tvCommentStatus, tvUpVotes, tvDownVotes, tvLoadComment;
        ExpandableTextView extvComment;
        ImageView profileImage, editImage, appealImage, deactivateImage, reportImage,
                upVoteImage, downVoteImage, developerImage, adminImage, driverImage, likerImage, ownerImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            voteLayout = itemView.findViewById(R.id.voteLayout);
            tvUserFullName = itemView.findViewById(R.id.tvUserFullName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvCommentStatus = itemView.findViewById(R.id.tvCommentStatus);
            tvUpVotes = itemView.findViewById(R.id.tvUpVotes);
            tvDownVotes = itemView.findViewById(R.id.tvDownVotes);
            extvComment = itemView.findViewById(R.id.extvComment);
            profileImage = itemView.findViewById(R.id.profileImage);

            editImage = itemView.findViewById(R.id.editImage);
            appealImage = itemView.findViewById(R.id.appealImage);
            deactivateImage = itemView.findViewById(R.id.deactivateImage);
            reportImage = itemView.findViewById(R.id.reportImage);
            deactivateImage = itemView.findViewById(R.id.deactivateImage);
            upVoteImage = itemView.findViewById(R.id.upVoteImage);
            downVoteImage = itemView.findViewById(R.id.downVoteImage);

            badgeLayout = itemView.findViewById(R.id.badgeLayout);
            ownerImage = itemView.findViewById(R.id.ownerImage);
            developerImage = itemView.findViewById(R.id.developerImage);
            adminImage = itemView.findViewById(R.id.adminImage);
            driverImage = itemView.findViewById(R.id.driverImage);
            likerImage = itemView.findViewById(R.id.likerImage);

            commentLayout = itemView.findViewById(R.id.commentLayout);
            loadCommentLayout = itemView.findViewById(R.id.loadCommentLayout);
            tvLoadComment = itemView.findViewById(R.id.tvLoadComment);

            setIsRecyclable(false);
        }
    }
}
