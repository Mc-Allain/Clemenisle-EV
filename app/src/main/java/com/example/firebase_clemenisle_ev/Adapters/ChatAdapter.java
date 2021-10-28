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
                startPointMessageLayout = holder.startPointMessageLayout,
                endPointMessageLayout = holder.endPointMessageLayout;
        TextView tvStartPointMessage = holder.tvStartPointMessage,
                tvEndPointMessage = holder.tvEndPointMessage,
                tvStartPointTimestamp = holder.tvStartPointTimestamp,
                tvEndPointTimestamp = holder.tvEndPointTimestamp;
        ImageView startPointProfileImage = holder.startPointProfileImage,
                endPointProfileImage = holder.endPointProfileImage;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        if(position == 0) {
            ConstraintLayout.LayoutParams layoutParams =
                    (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                    layoutParams.rightMargin, dpToPx(16));
            backgroundLayout.setLayoutParams(layoutParams);
        }

        if(position == chats.size() + 1) {
            if(inDriverMode) {
                startPointLayout.setVisibility(View.GONE);
                endPointLayout.setVisibility(View.VISIBLE);

                String message = "<i>Loading…</i>";
                tvEndPointMessage.setText(fromHtml(message));

                getProfileImage(passengerUserId, endPointProfileImage, tvEndPointMessage,
                        endPointLayout, tvEndPointTimestamp, position);
            }
            else {
                startPointLayout.setVisibility(View.VISIBLE);
                endPointLayout.setVisibility(View.GONE);

                String message = "<i>Loading…</i>";
                tvStartPointMessage.setText(fromHtml(message));

                getProfileImage(passengerUserId, startPointProfileImage, tvStartPointMessage,
                        startPointLayout, tvStartPointTimestamp, position);
            }
        }
        else if(position == chats.size()) {
            if(inDriverMode) {
                startPointLayout.setVisibility(View.VISIBLE);
                endPointLayout.setVisibility(View.GONE);

                getProfileImage(driverUserId, startPointProfileImage, tvStartPointMessage,
                        startPointLayout, tvStartPointTimestamp, position);
            }
            else {
                startPointLayout.setVisibility(View.GONE);
                endPointLayout.setVisibility(View.VISIBLE);

                getProfileImage(driverUserId, endPointProfileImage, tvEndPointMessage,
                        endPointLayout, tvEndPointTimestamp, position);
            }
        }
        else {
            Chat chat = chats.get(position);
            String senderId = chat.getSenderId();
            String message = chat.getMessage();
            String timestamp = chat.getTimestamp();

            boolean state;
            if(position != chats.size() - 1)
                state = !chats.get(position + 1).getSenderId().equals(senderId);
            else state = !senderId.equals(driverUserId);

            if(senderId.equals(startPointId)) {
                startPointLayout.setVisibility(View.VISIBLE);
                endPointLayout.setVisibility(View.GONE);

                tvStartPointMessage.setText(message);
                tvStartPointTimestamp.setText(timestamp);

                if(state) {
                    startPointProfileImage.setVisibility(View.VISIBLE);
                    getProfileImage(senderId, startPointProfileImage, tvStartPointMessage,
                            startPointLayout, tvStartPointTimestamp, position);
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
                    getProfileImage(senderId, endPointProfileImage, tvEndPointMessage,
                            endPointLayout, tvEndPointTimestamp, position);
                }
                else endPointProfileImage.setVisibility(View.INVISIBLE);
            }

            tvStartPointMessage.setOnClickListener(view -> copyTextToClipboard(message));
            tvEndPointMessage.setOnClickListener(view -> copyTextToClipboard(message));
        }
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

    private void getProfileImage(String senderId, ImageView profileImage,
                                 TextView tvMessage, ConstraintLayout messageLayout,
                                 TextView tvTimestamp, int position) {
        if(users.size() > 0) {
            for (User thisUser : users) {
                if(thisUser.getId().equals(senderId)) {
                    try {
                        Glide.with(myContext).load(thisUser.getProfileImage())
                                .placeholder(R.drawable.image_loading_placeholder)
                                .into(profileImage);
                    }
                    catch (Exception ignored) {}

                    profileImage.setVisibility(View.VISIBLE);
                    messageLayout.setVisibility(View.VISIBLE);
                    tvTimestamp.setVisibility(View.VISIBLE);

                    if(position == chats.size() + 1) {
                        if(initialMessage == null || initialMessage.length() == 0)
                            messageLayout.setVisibility(View.GONE);
                        else {
                            messageLayout.setVisibility(View.VISIBLE);
                            tvMessage.setText(initialMessage);
                            tvTimestamp.setText(bookingTimestamp);
                        }
                    }
                    if(position == chats.size()) {
                        messageLayout.setVisibility(View.VISIBLE);
                        String fullName = "<b>" + thisUser.getLastName() + "</b>, " + thisUser.getFirstName();
                        if(thisUser.getMiddleName().length() > 0) fullName += " " + thisUser.getMiddleName();

                        String message = "こんにちは (Hello), I am " + fullName + ", your assigned driver.";
                        tvMessage.setText(fromHtml(message));
                        tvTimestamp.setText(taskTimestamp);
                    }

                    tvMessage.setOnClickListener(view ->
                            copyTextToClipboard(tvMessage.getText().toString()));
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
        return chats.size() + 2;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout, startPointLayout, endPointLayout,
                startPointMessageLayout, endPointMessageLayout;
        TextView tvStartPointMessage, tvEndPointMessage,
                tvStartPointTimestamp, tvEndPointTimestamp;
        ImageView startPointProfileImage, endPointProfileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
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
