package com.example.firebase_clemenisle_ev.Adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
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
import com.example.firebase_clemenisle_ev.Classes.Chat;
import com.example.firebase_clemenisle_ev.Classes.DateTimeDifference;
import com.example.firebase_clemenisle_ev.Classes.DateTimeToString;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    List<Chat> chats;
    String startPointId, passengerUserId, driverUserId, passengerProfileImg, driverProfileImg,
            driverFullName, initialMessage, bookingTimestamp, taskTimestamp, status;
    boolean inDriverModule;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    int loadChatItemPosition = 10, incrementLoadedItems = 10;

    DateTimeToString dateTimeToString;

    public ChatAdapter(Context context, List<Chat> chats, String startPointId, boolean inDriverModule) {
        this.chats = chats;
        this.startPointId = startPointId;
        this.inDriverModule = inDriverModule;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_chat_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout,
                startPointLayout = holder.startPointLayout,
                endPointLayout = holder.endPointLayout,
                loadChatLayout = holder.loadChatLayout;
        TextView tvStartPointMessage = holder.tvStartPointMessage,
                tvEndPointMessage = holder.tvEndPointMessage,
                tvStartPointTimestamp = holder.tvStartPointTimestamp,
                tvEndPointTimestamp = holder.tvEndPointTimestamp,
                tvLoadChat = holder.tvLoadChat;
        ImageView startPointProfileImage = holder.startPointProfileImage,
                endPointProfileImage = holder.endPointProfileImage;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        if(loadChatItemPosition < incrementLoadedItems) loadChatItemPosition = incrementLoadedItems;

        int additionalItemCount = initialMessage == null || initialMessage.length() == 0 ? 1 : 2;

        if(chats.size() + additionalItemCount < loadChatItemPosition && chats.size() != 0)
            loadChatItemPosition = chats.size() + additionalItemCount;

        backgroundLayout.setVisibility(View.VISIBLE);
        startPointLayout.setVisibility(View.GONE);
        endPointLayout.setVisibility(View.GONE);
        loadChatLayout.setVisibility(View.GONE);

        if(status != null) {
            int color = 0;

            switch (status) {
                case "Pending":
                    color = myResources.getColor(R.color.orange);
                    break;
                case "Request":
                case "Booked":
                    color = myResources.getColor(R.color.green);
                    break;
                case "Completed":
                    color = myResources.getColor(R.color.blue);
                    break;
                case "Passed":
                case "Cancelled":
                case "Failed":
                    color = myResources.getColor(R.color.red);
                    break;
            }

            tvStartPointMessage.setBackgroundTintList(ColorStateList.valueOf(color));
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        if(position == 0) {
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                    layoutParams.rightMargin, dpToPx(16));
        }
        else {
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                    layoutParams.rightMargin, 0);
        }
        backgroundLayout.setLayoutParams(layoutParams);

        if(position == chats.size() + 1 && position < loadChatItemPosition && additionalItemCount == 2) {
            String profileImg = passengerProfileImg;

            if(bookingTimestamp == null) return;

            if(inDriverModule) {
                endPointLayout.setVisibility(View.VISIBLE);

                try {
                    Glide.with(myContext).load(profileImg)
                            .placeholder(R.drawable.image_loading_placeholder)
                            .into(endPointProfileImage);
                }
                catch (Exception ignored) {}

                tvEndPointMessage.setText(initialMessage);

                DateTimeDifference dateTimeDifference = new DateTimeDifference(bookingTimestamp);
                bookingTimestamp = dateTimeDifference.getResult();
                tvEndPointTimestamp.setText(bookingTimestamp);

                tvEndPointMessage.setOnClickListener(view -> copyTextToClipboard(initialMessage));
            }
            else {
                startPointLayout.setVisibility(View.VISIBLE);

                try {
                    Glide.with(myContext).load(profileImg)
                            .placeholder(R.drawable.image_loading_placeholder)
                            .into(startPointProfileImage);
                }
                catch (Exception ignored) {}

                tvStartPointMessage.setText(initialMessage);

                DateTimeDifference dateTimeDifference = new DateTimeDifference(bookingTimestamp);
                bookingTimestamp = dateTimeDifference.getResult();
                tvStartPointTimestamp.setText(bookingTimestamp);

                tvStartPointMessage.setOnClickListener(view -> copyTextToClipboard(initialMessage));
            }
        }
        else if(position == chats.size() && position < loadChatItemPosition) {
            String profileImg = driverProfileImg;

            if(taskTimestamp == null) return;

            if(inDriverModule) {
                startPointLayout.setVisibility(View.VISIBLE);

                try {
                    Glide.with(myContext).load(profileImg)
                            .placeholder(R.drawable.image_loading_placeholder)
                            .into(startPointProfileImage);
                }
                catch (Exception ignored) {}

                String message = "こんにちは (Hello), I am " + driverFullName + ", your assigned driver.";
                tvStartPointMessage.setText(fromHtml(message));

                DateTimeDifference dateTimeDifference = new DateTimeDifference(taskTimestamp);
                taskTimestamp = dateTimeDifference.getResult();
                tvStartPointTimestamp.setText(taskTimestamp);

                tvStartPointMessage.setOnClickListener(view -> copyTextToClipboard(message));
            }
            else {
                endPointLayout.setVisibility(View.VISIBLE);

                try {
                    Glide.with(myContext).load(profileImg)
                            .placeholder(R.drawable.image_loading_placeholder)
                            .into(endPointProfileImage);
                }
                catch (Exception ignored) {}

                String message = "こんにちは (Hello), I am " + driverFullName + ", your assigned driver.";
                tvEndPointMessage.setText(fromHtml(message));

                DateTimeDifference dateTimeDifference = new DateTimeDifference(taskTimestamp);
                taskTimestamp = dateTimeDifference.getResult();
                tvEndPointTimestamp.setText(taskTimestamp);

                tvEndPointMessage.setOnClickListener(view -> copyTextToClipboard(message));
            }
        }
        else if(position < loadChatItemPosition && chats.size() != 0) {
            Chat chat = chats.get(position);
            String senderId = chat.getSenderId();
            String message = chat.getMessage();
            String timestamp = chat.getTimestamp();
            String profileImg;

            DateTimeDifference dateTimeDifference = new DateTimeDifference(timestamp);

            if(senderId.equals(passengerUserId)) profileImg = passengerProfileImg;
            else profileImg = driverProfileImg;

            boolean state;
            if(position != chats.size() - 1)
                state = !(chats.get(position + 1).getSenderId().equals(senderId) &&
                        position + 1 < loadChatItemPosition);
            else state = !(senderId.equals(driverUserId) && position + 1 < loadChatItemPosition);

            if(senderId.equals(startPointId)) {
                if(position != 0)
                    checkTimestamp(timestamp, chats.get(position - 1).getTimestamp(),
                            tvStartPointTimestamp);
                timestamp = dateTimeDifference.getResult();

                startPointLayout.setVisibility(View.VISIBLE);
                tvStartPointMessage.setText(message);
                tvStartPointTimestamp.setText(timestamp);

                tvStartPointMessage.setOnClickListener(view -> copyTextToClipboard(message));

                if(state) {
                    startPointProfileImage.setVisibility(View.VISIBLE);
                    try {
                        Glide.with(myContext).load(profileImg)
                                .placeholder(R.drawable.image_loading_placeholder)
                                .into(startPointProfileImage);
                    }
                    catch (Exception ignored) {}
                }
                else startPointProfileImage.setVisibility(View.INVISIBLE);
            }
            else {
                if(position != 0)
                    checkTimestamp(timestamp, chats.get(position - 1).getTimestamp(),
                            tvEndPointTimestamp);
                timestamp = dateTimeDifference.getResult();

                endPointLayout.setVisibility(View.VISIBLE);
                tvEndPointMessage.setText(message);
                tvEndPointTimestamp.setText(timestamp);

                tvEndPointMessage.setOnClickListener(view -> copyTextToClipboard(message));

                if(state) {
                    endPointProfileImage.setVisibility(View.VISIBLE);
                    try {
                        Glide.with(myContext).load(profileImg)
                                .placeholder(R.drawable.image_loading_placeholder)
                                .into(endPointProfileImage);
                    }
                    catch (Exception ignored) {}
                }
                else endPointProfileImage.setVisibility(View.INVISIBLE);
            }
        }
        else if(position == loadChatItemPosition && position != chats.size() + additionalItemCount) {
            loadChatLayout.setVisibility(View.VISIBLE);

            int itemsCountToIncrement = incrementLoadedItems;
            if(loadChatItemPosition + incrementLoadedItems > chats.size() + additionalItemCount)
                itemsCountToIncrement = chats.size() + additionalItemCount - loadChatItemPosition;

            String loadChatText = "Load " + itemsCountToIncrement + " more ";
            loadChatText += itemsCountToIncrement == 1  ? "message" : "messages";
            tvLoadChat.setText(loadChatText);

            loadChatLayout.setOnClickListener(view -> {
                loadChatItemPosition += incrementLoadedItems;
                notifyDataSetChanged();
            });
        }
        else backgroundLayout.setVisibility(View.GONE);
    }

    private void checkTimestamp(String timestamp, String timestamp1, TextView tvTimestamp) {
        dateTimeToString = new DateTimeToString();

        dateTimeToString.setFormattedSchedule(timestamp);
        int stampYear = Integer.parseInt(dateTimeToString.getYear());
        int stampMonth = Integer.parseInt(dateTimeToString.getMonthNo());
        int stampDay = Integer.parseInt(dateTimeToString.getDay());

        int stampHour = Integer.parseInt(dateTimeToString.getRawHour());
        int stampMin = Integer.parseInt(dateTimeToString.getMin());

        dateTimeToString.setFormattedSchedule(timestamp1);
        int stampYear1 = Integer.parseInt(dateTimeToString.getYear());
        int stampMonth1 = Integer.parseInt(dateTimeToString.getMonthNo());
        int stampDay1 = Integer.parseInt(dateTimeToString.getDay());

        int stampHour1 = Integer.parseInt(dateTimeToString.getRawHour());
        int stampMin1 = Integer.parseInt(dateTimeToString.getMin());

        int yearDifference, monthDifference, dayDifference, hrDifference, minDifference;

        yearDifference = stampYear1 - stampYear;
        monthDifference = stampMonth1 - stampMonth;
        dayDifference = stampDay1 - stampDay;
        hrDifference = stampHour1 - stampHour;
        minDifference = stampMin1 - stampMin;

        boolean state = yearDifference > 1 || monthDifference > 1 || dayDifference > 1 ||
                hrDifference > 1 || minDifference > 5;

        if(state) tvTimestamp.setVisibility(View.VISIBLE);
        else tvTimestamp.setVisibility(View.GONE);
    }

    private void copyTextToClipboard(String value) {
        ClipboardManager clipboard =
                (ClipboardManager) myContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Value", value);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(
                myContext,
                "Text Copied",
                Toast.LENGTH_SHORT
        ).show();
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
        return chats.size() + 3;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout, startPointLayout, endPointLayout,
                startPointMessageLayout, endPointMessageLayout, loadChatLayout;
        TextView tvStartPointMessage, tvEndPointMessage,
                tvStartPointTimestamp, tvEndPointTimestamp, tvLoadChat;
        ImageView startPointProfileImage, endPointProfileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            loadChatLayout = itemView.findViewById(R.id.loadChatLayout);
            tvLoadChat = itemView.findViewById(R.id.tvLoadChat);

            startPointLayout = itemView.findViewById(R.id.startPointLayout);
            endPointLayout = itemView.findViewById(R.id.endPointLayout);
            tvStartPointMessage = itemView.findViewById(R.id.tvStartPointMessage);
            tvEndPointMessage = itemView.findViewById(R.id.tvEndPointMessage);
            startPointProfileImage = itemView.findViewById(R.id.startPointProfileImage);
            endPointProfileImage = itemView.findViewById(R.id.endPointProfileImage);

            startPointMessageLayout = itemView.findViewById(R.id.startPointMessageLayout);
            tvStartPointTimestamp = itemView.findViewById(R.id.tvStartPointTimestamp);
            endPointMessageLayout = itemView.findViewById(R.id.endPointMessageLayout);
            tvEndPointTimestamp = itemView.findViewById(R.id.tvEndPointTimestamp);

            setIsRecyclable(false);
        }
    }

    public void setValues(String passengerUserId, String driverUserId, String passengerProfileImg,
                          String driverProfileImg, String driverFullName, String initialMessage,
                          String bookingTimestamp, String taskTimestamp, String status) {
        this.passengerUserId = passengerUserId;
        this.driverUserId = driverUserId;
        this.passengerProfileImg = passengerProfileImg;
        this.driverProfileImg = driverProfileImg;
        this.driverFullName = driverFullName;
        this.initialMessage = initialMessage;
        this.bookingTimestamp = bookingTimestamp;
        this.taskTimestamp = taskTimestamp;
        this.status = status;
        notifyDataSetChanged();
    }
}
