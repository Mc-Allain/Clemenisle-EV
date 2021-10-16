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

    int colorBlue, colorInitial, colorBlack;

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
        TextView tvUserFullName = holder.tvUserFullName, tvFoulComment = holder.tvFoulComment,
                tvUpVotes = holder.tvUpVotes, tvDownVotes = holder.tvDownVotes;
        ExpandableTextView extvComment = holder.extvComment;
        ImageView profileImage  = holder.profileImage,
                editImage = holder.editImage,
                deactivateImage = holder.deactivateImage,
                reportImage = holder.reportImage,
                upVoteImage = holder.upVoteImage,
                downVoteImage = holder.downVoteImage;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);
        colorBlack = myResources.getColor(R.color.black);

        User user = users.get(position);

        List<Comment> comments = user.getComments();

        Comment commentRecord = null;
        for(Comment comment : comments) {
            if(comment.getId().equals(spotId) && !comment.isDeactivated()) {
                commentRecord = comment;
                break;
            }
        }

        String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
        if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
        tvUserFullName.setText(fromHtml(fullName));

        if(commentRecord != null) {
            String commentValue = commentRecord.getValue();
            boolean fouled = commentRecord.isFouled();

            extvComment.setText(commentValue);
            backgroundLayout.setVisibility(View.VISIBLE);

            if(fouled) {
                tvUserFullName.setPadding(0, 0, dpToPx(106), 0);
                tvFoulComment.setVisibility(View.VISIBLE);
                extvComment.setVisibility(View.GONE);
                voteLayout.setVisibility(View.GONE);
            }
            else {
                tvUserFullName.setPadding(0, 0, 0, 0);
                tvFoulComment.setVisibility(View.GONE);
                extvComment.setVisibility(View.VISIBLE);
                voteLayout.setVisibility(View.VISIBLE);
            }
        }
        else {
            backgroundLayout.setVisibility(View.GONE);
            return;
        }

        if(commentRecord.isDeactivated()) {
            backgroundLayout.setVisibility(View.GONE);
            return;
        }
        else backgroundLayout.setVisibility(View.VISIBLE);

        if(user.getId().equals(userId)) {
            editImage.setVisibility(View.VISIBLE);
            deactivateImage.setVisibility(View.VISIBLE);
            reportImage.setVisibility(View.GONE);

            upVoteImage.setEnabled(false);
            downVoteImage.setEnabled(false);
            upVoteImage.getDrawable().setTint(colorInitial);
            downVoteImage.getDrawable().setTint(colorInitial);

            String formattedFullName = fullName + " (You)";
            tvUserFullName.setText(fromHtml(formattedFullName));
        }
        else {
            editImage.setVisibility(View.GONE);
            deactivateImage.setVisibility(View.GONE);
            reportImage.setVisibility(View.VISIBLE);

            upVoteImage.setEnabled(true);
            downVoteImage.setEnabled(true);
            upVoteImage.getDrawable().setTint(colorBlack);
            downVoteImage.getDrawable().setTint(colorBlack);
        }

        int upVotes = 0, downVotes = 0;
        for(User user1 : users) {
            List<Comment> upVotedComments = user1.getUpVotedComments();
            for(Comment comment : upVotedComments) {
                if(comment.getId().equals(spotId) && comment.getUserId().equals(userId)) {
                    upVotes++;
                }
            }

            List<Comment> downVotedComments = user1.getDownVotedComments();
            for(Comment comment : downVotedComments) {
                if(comment.getId().equals(spotId) && comment.getUserId().equals(userId)) {
                    downVotes++;
                }
            }
        }

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
        TextView tvUserFullName, tvFoulComment, tvUpVotes, tvDownVotes;
        ExpandableTextView extvComment;
        ImageView profileImage, editImage, deactivateImage, reportImage, upVoteImage, downVoteImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            voteLayout = itemView.findViewById(R.id.voteLayout);
            tvUserFullName = itemView.findViewById(R.id.tvUserFullName);
            tvFoulComment = itemView.findViewById(R.id.tvFoulComment);
            tvUpVotes = itemView.findViewById(R.id.tvUpVotes);
            tvDownVotes = itemView.findViewById(R.id.tvDownVotes);
            extvComment = itemView.findViewById(R.id.extvComment);
            profileImage = itemView.findViewById(R.id.profileImage);
            editImage = itemView.findViewById(R.id.editImage);
            deactivateImage = itemView.findViewById(R.id.deactivateImage);
            reportImage = itemView.findViewById(R.id.reportImage);
            deactivateImage = itemView.findViewById(R.id.deactivateImage);
            upVoteImage = itemView.findViewById(R.id.upVoteImage);
            downVoteImage = itemView.findViewById(R.id.downVoteImage);
        }
    }
}
