package com.example.firebase_clemenisle_ev.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
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
import com.example.firebase_clemenisle_ev.Classes.Booking;
import com.example.firebase_clemenisle_ev.Classes.Chat;
import com.example.firebase_clemenisle_ev.Classes.DateTimeDifference;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.OnTheSpotActivity;
import com.example.firebase_clemenisle_ev.R;
import com.example.firebase_clemenisle_ev.RouteActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    List<Chat> chatList;
    List<User> users;
    List<Booking> bookingList;
    String userId;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    int colorBlack, colorInitial;

    public ChatListAdapter(Context context, List<Chat> chatList, List<User> users,
                           List<Booking> bookingList, String userId) {
        this.chatList = chatList;
        this.users = users;
        this.bookingList = bookingList;
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

        colorBlack = myResources.getColor(R.color.black);
        colorInitial = myResources.getColor(R.color.initial);

        Chat chat = chatList.get(position);

        String senderId = chat.getSenderId();
        String message = chat.getMessage();
        String timestamp = chat.getTimestamp();

        String endPointUserId = chat.getEndPointUserId();
        String driverUserId = chat.getDriverUserId();
        String status = chat.getStatus();

        Booking booking = chat.getBooking();
        String taskId = booking.getId();
        boolean read = booking.isRead();

        boolean inDriverModule = !endPointUserId.equals(driverUserId);
        getEndPointInfo(endPointUserId, driverUserId, profileImage, tvEndPointFullName);

        if(!read) {
            message = "<b>" + message + "</b>";
            tvMessage.setTextColor(colorBlack);
        }
        else tvMessage.setTextColor(colorInitial);
        if(senderId.equals(userId)) message = "<b>You</b>: " + message;
        tvMessage.setText(fromHtml(message));

        String taskIdText = taskId;
        if(!read) {
            taskIdText = "<b>" + taskId + "</b>";
            tvBookingId.setTextColor(colorBlack);
        }
        else tvBookingId.setTextColor(colorInitial);
        tvBookingId.setText(fromHtml(taskIdText));

        DateTimeDifference dateTimeDifference = new DateTimeDifference(timestamp);
        timestamp = dateTimeDifference.getResult();
        if(!read) {
            timestamp = "<b>" + timestamp + "</b>";
            tvTimestamp.setTextColor(colorBlack);
        }
        else tvTimestamp.setTextColor(colorInitial);
        tvTimestamp.setText(fromHtml(timestamp));

        Drawable rippleDrawable = null;

        switch (status) {
            case "Pending":
                rippleDrawable = ResourcesCompat.getDrawable(myResources,
                        R.drawable.custom_orange_ripple_effect, null);
                break;
            case "Request":
            case "Booked":
                rippleDrawable = ResourcesCompat.getDrawable(myResources,
                        R.drawable.custom_green_ripple_effect, null);
                break;
            case "Completed":
                rippleDrawable = ResourcesCompat.getDrawable(myResources,
                        R.drawable.custom_blue_ripple_effect, null);
                break;
            case "Passed":
            case "Cancelled":
            case "Failed":
                rippleDrawable = ResourcesCompat.getDrawable(myResources,
                        R.drawable.custom_red_ripple_effect, null);
                break;
        }

        backgroundLayout.setBackground(rippleDrawable);

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

        tvBookingId.setOnClickListener(view -> openItem(booking, inDriverModule, endPointUserId));

        backgroundLayout.setOnClickListener(view -> {
            Intent intent = new Intent(myContext, ChatActivity.class);
            intent.putExtra("taskId", taskId);
            intent.putExtra("inDriverModule", inDriverModule);
            if(!inDriverModule) intent.putExtra("driverUserId", driverUserId);
            myContext.startActivity(intent);
        });
    }

    private void openItem(Booking booking, boolean inDriverModule, String passengerId) {
        boolean isOnTheSpot = booking.getBookingType().getId().equals("BT99");

        Intent intent;

        if(isOnTheSpot)
            intent = new Intent(myContext, OnTheSpotActivity.class);
        else
            intent = new Intent(myContext, RouteActivity.class);

        intent.putExtra("bookingId", booking.getId());
        intent.putExtra("inDriverModule", inDriverModule);
        if(inDriverModule) {
            intent.putExtra("isScanning", false);
            intent.putExtra("status", booking.getStatus());
            intent.putExtra("previousDriverUserId", booking.getPreviousDriverUserId());
            intent.putExtra("userId", passengerId);
        }
        else {
            if(!isOnTheSpot) {
                boolean isLatest = bookingList.get(0).getId().equals(booking.getId()) &&
                        booking.getStatus().equals("Completed") &&
                        !booking.getBookingType().getId().equals("BT99");

                intent.putExtra("isLatest", isLatest);
            }
        }
        myContext.startActivity(intent);
    }

    private void getEndPointInfo(String endPointUserId, String driverUserId, ImageView profileImage, TextView tvFullName) {
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
                if(endPointUserId.equals(driverUserId)) fullName += " (Driver)";
                else fullName += " (Passenger)";
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
