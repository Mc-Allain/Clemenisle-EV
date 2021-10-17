package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
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

import com.example.firebase_clemenisle_ev.Classes.Comment;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.R;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    List<User> users;
    String spotId, userId;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    int colorBlue, colorInitial, colorBlack, colorRed;

    String defaultStatusText = "Foul comment", appealedtext = "(Appealed)",
            reportedStatus = "Reported", notActiveText = "This comment is not active";

    OnActionButtonClicked onActionButtonClickedListener;

    public CommentAdapter(Context context, List<User> users, String spotId, String userId) {
        this.users = users;
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
        ConstraintLayout backgroundLayout = holder.backgroundLayout,
                voteLayout = holder.voteLayout;
        TextView tvUserFullName = holder.tvUserFullName, tvCommentStatus = holder.tvCommentStatus,
                tvUpVotes = holder.tvUpVotes, tvDownVotes = holder.tvDownVotes;
        ExpandableTextView extvComment = holder.extvComment;
        ImageView profileImage  = holder.profileImage,
                editImage = holder.editImage,
                appealImage = holder.appealImage,
                deactivateImage = holder.deactivateImage,
                reportImage = holder.reportImage,
                upVoteImage = holder.upVoteImage,
                downVoteImage = holder.downVoteImage;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);
        colorBlack = myResources.getColor(R.color.black);
        colorRed = myResources.getColor(R.color.red);

        User user = users.get(position);

        List<Comment> comments = user.getComments();

        Comment commentRecord = null;
        for(Comment thisComment : comments) {
            if(thisComment.getId().equals(spotId)) {
                commentRecord = thisComment;
                break;
            }
        }
        Comment finalCommentRecord = commentRecord;

        String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
        if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
        tvUserFullName.setText(fromHtml(fullName));

        boolean upVoted = false, downVoted = false;

        if(commentRecord != null) {
            String commentValue = commentRecord.getValue();
            boolean fouled = commentRecord.isFouled();
            boolean appealed = commentRecord.isAppealed();

            extvComment.setText(commentValue);
            backgroundLayout.setVisibility(View.VISIBLE);

            if(fouled) {
                tvCommentStatus.setVisibility(View.VISIBLE);
                tvCommentStatus.setText(defaultStatusText);

                extvComment.setVisibility(View.GONE);
                voteLayout.setVisibility(View.GONE);

                if(user.getId().equals(userId))  {
                    appealImage.setEnabled(true);
                    appealImage.setVisibility(View.VISIBLE);

                    String status = defaultStatusText;
                    if(appealed) {
                        appealImage.setEnabled(false);
                        appealImage.setColorFilter(colorInitial);
                        status = defaultStatusText + " " + appealedtext;
                    }
                    else appealImage.setColorFilter(colorBlue);

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

                if(commentRecord.isDeactivated()) {
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

            if(user.getId().equals(userId)) {
                editImage.setVisibility(View.VISIBLE);

                upVoteImage.setEnabled(false);
                downVoteImage.setEnabled(false);
                upVoteImage.getDrawable().setTint(colorInitial);
                downVoteImage.getDrawable().setTint(colorInitial);

                String formattedFullName = fullName + " (You)";
                tvUserFullName.setText(fromHtml(formattedFullName));
            }
            else {
                editImage.setVisibility(View.GONE);

                upVoteImage.setEnabled(true);
                downVoteImage.setEnabled(true);
                upVoteImage.getDrawable().setTint(colorBlack);
                downVoteImage.getDrawable().setTint(colorBlack);
            }
        }
        else backgroundLayout.setVisibility(View.GONE);

        reportImage.setEnabled(true);
        reportImage.setColorFilter(colorRed);

        int upVotes = 0, downVotes = 0;
        for(User user1 : users) {
            List<Comment> upVotedComments = user1.getUpVotedComments();
            for(Comment comment : upVotedComments) {
                if(comment.getId().equals(spotId) && comment.getUserId().equals(user.getId())) {
                    upVotes++;

                    if(user1.getId().equals(userId)) {
                        upVoteImage.getDrawable().setTint(colorBlue);
                        upVoted = true;
                    }
                }
            }

            List<Comment> downVotedComments = user1.getDownVotedComments();
            for(Comment comment : downVotedComments) {
                if(comment.getId().equals(spotId) && comment.getUserId().equals(user.getId())) {
                    downVotes++;

                    if(user1.getId().equals(userId)) {
                        downVoteImage.getDrawable().setTint(colorRed);
                        downVoted = true;
                    }
                }
            }

            List<Comment> reportedComments = user1.getReportedComments();
            for(Comment comment : reportedComments) {
                if(comment.getId().equals(spotId) && comment.getUserId().equals(user.getId())) {
                    reportImage.setEnabled(false);
                    reportImage.setColorFilter(colorInitial);

                    tvCommentStatus.setVisibility(View.VISIBLE);

                    String status = reportedStatus;

                    if(commentRecord.isFouled()) status = defaultStatusText;
                    else if(commentRecord.isDeactivated()) status = notActiveText + " | " + reportedStatus;

                    tvCommentStatus.setText(status);
                }
            }
        }
        boolean finalUpVoted = upVoted, finalDownVoted = downVoted;

        tvUpVotes.setText(String.valueOf(upVotes));
        tvDownVotes.setText(String.valueOf(downVotes));

        int top = dpToPx(4), bottom = dpToPx(4);

        boolean isLastItem = position + 1 == getItemCount();

        if(isLastItem) {
            bottom = dpToPx(8);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, top, layoutParams.rightMargin, bottom);
        backgroundLayout.setLayoutParams(layoutParams);

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

        upVoteImage.setOnClickListener(view ->
                onActionButtonClickedListener.
                        upVoteImageOnClick(spotId, user.getId(), finalCommentRecord,
                                finalUpVoted, finalDownVoted)
        );

        downVoteImage.setOnClickListener(view ->
                onActionButtonClickedListener.
                        downVoteImageOnClick(spotId, user.getId(), finalCommentRecord,
                                finalUpVoted, finalDownVoted)
        );

        editImage.setOnClickListener(view -> onActionButtonClickedListener.editImageOnClick());

        appealImage.setOnClickListener(view -> onActionButtonClickedListener.appealImageOnClick());

        deactivateImage.setOnClickListener(view -> onActionButtonClickedListener.deactivateImageOnClick());

        reportImage.setOnClickListener(view ->
                onActionButtonClickedListener.
                        reportImageOnClick(spotId, user.getId(), finalCommentRecord)
        );
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
        void reportImageOnClick(String spotId, String senderUserId, Comment comment);
        void upVoteImageOnClick(String spotId, String senderUserId, Comment comment,
                                boolean upVoted, boolean downVoted);
        void downVoteImageOnClick(String spotId, String senderUserId, Comment comment,
                                  boolean upVoted, boolean downVoted);
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
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout, voteLayout;
        TextView tvUserFullName, tvCommentStatus, tvUpVotes, tvDownVotes;
        ExpandableTextView extvComment;
        ImageView profileImage, editImage, appealImage, deactivateImage, reportImage, upVoteImage, downVoteImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            voteLayout = itemView.findViewById(R.id.voteLayout);
            tvUserFullName = itemView.findViewById(R.id.tvUserFullName);
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
        }
    }
}
