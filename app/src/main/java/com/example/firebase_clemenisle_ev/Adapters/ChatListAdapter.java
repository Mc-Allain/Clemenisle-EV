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
import com.bumptech.glide.request.target.Target;
import com.example.firebase_clemenisle_ev.ChatActivity;
import com.example.firebase_clemenisle_ev.Classes.Chat;
import com.example.firebase_clemenisle_ev.Classes.DateTimeDifference;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    List<Chat> chatList;
    List<User> users;
    String userId;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    public ChatListAdapter(Context context, List<Chat> chatList, List<User> users,
                           String userId) {
        this.chatList = chatList;
        this.users = users;
        this.userId = userId;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_chat_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        ImageView profileImage = holder.profileImage;
        TextView tvEndPointFullName = holder.tvEndPointFullName, tvMessage = holder.tvMessage,
                tvBookingId = holder.tvBookingId, tvTimestamp = holder.tvTimestamp;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        Chat chat = chatList.get(position);

        String senderId = chat.getSenderId();
        String message = chat.getMessage();
        String taskId = chat.getTaskId();
        String endPointUserId = chat.getEndPointUserId();
        String driverUserId = chat.getDriverUserId();
        String timestamp = chat.getTimestamp();

        if(senderId.equals(userId)) message = "<b>You</b>: " + message;

        getEndPointInfo(endPointUserId, profileImage, tvEndPointFullName);

        tvMessage.setText(fromHtml(message));
        tvBookingId.setText(taskId);

        DateTimeDifference dateTimeDifference = new DateTimeDifference(timestamp);
        timestamp = dateTimeDifference.getResult();
        tvTimestamp.setText(timestamp);

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

        backgroundLayout.setOnClickListener(view -> {
            Intent intent = new Intent(myContext, ChatActivity.class);
            intent.putExtra("taskId", taskId);
            intent.putExtra("inDriverModule", !endPointUserId.equals(driverUserId));
            myContext.startActivity(intent);
        });
    }

    private void getEndPointInfo(String endPointUserId, ImageView profileImage, TextView tvFullName) {
        for(User user : users) {
            if(user.getId().equals(endPointUserId)) {
                try {
                    Glide.with(myContext).load(user.getProfileImage()).
                            placeholder(R.drawable.image_loading_placeholder).
                            override(Target.SIZE_ORIGINAL).into(profileImage);
                }
                catch (Exception ignored) {}

                String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();
                tvFullName.setText(fromHtml(fullName));

                return;
            }
        }
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
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
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        ImageView profileImage;
        TextView tvEndPointFullName, tvMessage, tvBookingId, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            profileImage = itemView.findViewById(R.id.profileImage);
            tvEndPointFullName = itemView.findViewById(R.id.tvEndPointFullName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);

            setIsRecyclable(false);
        }
    }
}
