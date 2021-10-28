package com.example.firebase_clemenisle_ev.Adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Classes.Chat;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    List<Chat> chats;
    List<User> users;
    String startPointId, passengerUserId, driverUserId, initialMessage, bookingTimestamp, taskTimestamp;
    boolean inDriverMode;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    int loadChatItemPosition = 10, incrementLoadedItems = 10;

    public ChatAdapter(Context context, List<Chat> chats, List<User> users, String startPointId,
                       String passengerUserId, String driverUserId, String initialMessage,
                       String bookingTimestamp, String taskTimestamp, boolean inDriverMode) {
        this.chats = chats;
        this.users = users;
        this.startPointId = startPointId;
        this.passengerUserId = passengerUserId;
        this.driverUserId = driverUserId;
        this.initialMessage = initialMessage;
        this.bookingTimestamp = bookingTimestamp;
        this.taskTimestamp = taskTimestamp;
        this.inDriverMode = inDriverMode;
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
        /*ConstraintLayout startPointMessageLayout = holder.startPointMessageLayout,
                endPointMessageLayout = holder.endPointMessageLayout;*/
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
        startPointLayout.setVisibility(View.VISIBLE);
        endPointLayout.setVisibility(View.VISIBLE);
        loadChatLayout.setVisibility(View.GONE);

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
            if(inDriverMode) {
                startPointLayout.setVisibility(View.GONE);
                endPointLayout.setVisibility(View.VISIBLE);

                String message = "<i>Loading…</i>";
                tvEndPointMessage.setText(fromHtml(message));

                getProfileImage(passengerUserId, endPointProfileImage);

                tvEndPointMessage.setText(initialMessage);
                tvEndPointTimestamp.setText(bookingTimestamp);
            }
            else {
                startPointLayout.setVisibility(View.VISIBLE);
                endPointLayout.setVisibility(View.GONE);

                String message = "<i>Loading…</i>";
                tvStartPointMessage.setText(fromHtml(message));

                getProfileImage(passengerUserId, startPointProfileImage);

                tvStartPointMessage.setText(initialMessage);
                tvStartPointTimestamp.setText(bookingTimestamp);
            }
        }
        else if(position == chats.size() && position < loadChatItemPosition) {
            if(inDriverMode) {
                startPointLayout.setVisibility(View.VISIBLE);
                endPointLayout.setVisibility(View.GONE);

                String message = "<i>Loading…</i>";
                tvStartPointMessage.setText(fromHtml(message));

                getProfileImage(driverUserId, startPointProfileImage);

                if(users.size() > 0) {
                    for(User user : users) {
                        String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                        if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();

                        message = "こんにちは (Hello), I am " + fullName + ", your assigned driver.";
                        tvStartPointMessage.setText(fromHtml(message));
                        tvStartPointTimestamp.setText(taskTimestamp);
                    }
                }
            }
            else {
                startPointLayout.setVisibility(View.GONE);
                endPointLayout.setVisibility(View.VISIBLE);

                String message = "<i>Loading…</i>";
                tvEndPointMessage.setText(fromHtml(message));

                getProfileImage(driverUserId, endPointProfileImage);

                if(users.size() > 0) {
                    for(User user : users) {
                        String fullName = "<b>" + user.getLastName() + "</b>, " + user.getFirstName();
                        if(user.getMiddleName().length() > 0) fullName += " " + user.getMiddleName();

                        message = "こんにちは (Hello), I am " + fullName + ", your assigned driver.";
                        tvEndPointMessage.setText(fromHtml(message));
                        tvEndPointTimestamp.setText(taskTimestamp);
                    }
                }
            }
        }
        else if(position < loadChatItemPosition && chats.size() != 0) {
            Chat chat = chats.get(position);
            String senderId = chat.getSenderId();
            String message = chat.getMessage() + " " + position;
            String timestamp = chat.getTimestamp();

            boolean state;
            if(position != chats.size() - 1)
                state = !(chats.get(position + 1).getSenderId().equals(senderId) &&
                        position + 1 < loadChatItemPosition);
            else
                state = !(senderId.equals(driverUserId) && position + 1 < loadChatItemPosition);

            if(senderId.equals(startPointId)) {
                startPointLayout.setVisibility(View.VISIBLE);
                endPointLayout.setVisibility(View.GONE);

                tvStartPointMessage.setText(message);
                tvStartPointTimestamp.setText(timestamp);

                if(state) {
                    startPointProfileImage.setVisibility(View.VISIBLE);
                    getProfileImage(senderId, startPointProfileImage);
                }
                else startPointProfileImage.setVisibility(View.INVISIBLE);
            }
            else {
                startPointLayout.setVisibility(View.GONE);
                endPointLayout.setVisibility(View.VISIBLE);

                tvEndPointMessage.setText(message);
                tvEndPointTimestamp.setText(timestamp);

                if(state) {
                    endPointProfileImage.setVisibility(View.VISIBLE);
                    getProfileImage(senderId, endPointProfileImage);
                }
                else endPointProfileImage.setVisibility(View.INVISIBLE);
            }

            tvStartPointMessage.setOnClickListener(view -> copyTextToClipboard(message));
            tvEndPointMessage.setOnClickListener(view -> copyTextToClipboard(message));
        }
        else if(position == loadChatItemPosition && position != chats.size() + additionalItemCount) {
            startPointLayout.setVisibility(View.GONE);
            endPointLayout.setVisibility(View.GONE);
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

    private void getProfileImage(String senderId, ImageView profileImage) {
        if(users.size() > 0) {
            for (User thisUser : users) {
                if(thisUser.getId().equals(senderId)) {
                    try {
                        Glide.with(myContext).load(thisUser.getProfileImage())
                                .placeholder(R.drawable.image_loading_placeholder)
                                .into(profileImage);
                    }
                    catch (Exception ignored) {}
                }
            }
        }
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
        }
    }

    public void setValues(String passengerUserId, String driverUserId, String initialMessage,
                                   String bookingTimestamp, String taskTimestamp) {
        this.passengerUserId = passengerUserId;
        this.driverUserId = driverUserId;
        this.initialMessage = initialMessage;
        this.bookingTimestamp = bookingTimestamp;
        this.taskTimestamp = taskTimestamp;
        notifyDataSetChanged();
    }
}
