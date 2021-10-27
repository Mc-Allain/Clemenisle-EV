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

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Classes.Chat;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.example.firebase_clemenisle_ev.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef = firebaseDatabase.getReference("users");

    List<Chat> chats;
    String startPointId, passengerUserId, driverUserId, initialMessage;
    boolean inDriverMode;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    public ChatAdapter(Context context, List<Chat> chats, String startPointId, String passengerUserId,
                       String driverUserId, String initialMessage, boolean inDriverMode) {
        this.chats = chats;
        this.startPointId = startPointId;
        this.passengerUserId = passengerUserId;
        this.driverUserId = driverUserId;
        this.initialMessage = initialMessage;
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
        ConstraintLayout startPointLayout = holder.startPointLayout,
                endPointLayout = holder.endPointLayout;
        TextView tvStartPointMessage = holder.tvStartPointMessage,
                tvEndPointMessage = holder.tvEndPointMessage;
        ImageView startPointProfileImage = holder.startPointProfileImage,
                endPointProfileImage = holder.endPointProfileImage;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        if(position == chats.size() + 1) {
            if(inDriverMode) {
                startPointLayout.setVisibility(View.GONE);
                endPointLayout.setVisibility(View.VISIBLE);

                getProfileImage(passengerUserId, endPointProfileImage, tvEndPointMessage, endPointLayout, position);
            }
            else {
                startPointLayout.setVisibility(View.VISIBLE);
                endPointLayout.setVisibility(View.GONE);

                getProfileImage(passengerUserId, startPointProfileImage, tvStartPointMessage, startPointLayout, position);
            }
        }
        else if(position == chats.size()) {
            if(inDriverMode) {
                startPointLayout.setVisibility(View.VISIBLE);
                endPointLayout.setVisibility(View.GONE);

                getProfileImage(driverUserId, startPointProfileImage, tvStartPointMessage, startPointLayout, position);
            }
            else {
                startPointLayout.setVisibility(View.GONE);
                endPointLayout.setVisibility(View.VISIBLE);

                getProfileImage(driverUserId, endPointProfileImage, tvEndPointMessage, endPointLayout, position);
            }
        }
        else {
            Chat chat = chats.get(position);
            String senderId = chat.getSenderId();
            String message = chat.getMessage();

            if(senderId.equals(startPointId)) {
                startPointLayout.setVisibility(View.VISIBLE);
                endPointLayout.setVisibility(View.GONE);

                tvStartPointMessage.setText(message);
                getProfileImage(senderId, startPointProfileImage, tvStartPointMessage, startPointLayout, position);
            }
            else {
                startPointLayout.setVisibility(View.GONE);
                endPointLayout.setVisibility(View.VISIBLE);

                tvEndPointMessage.setText(message);
                getProfileImage(senderId, endPointProfileImage, tvEndPointMessage, endPointLayout, position);
            }
        }
    }

    private void getProfileImage(String senderId, ImageView profileImage,
                                 TextView tvMessage, ConstraintLayout messageLayout, int position) {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User thisUser = new User(dataSnapshot);

                        if(thisUser.getId().equals(senderId)) {
                            try {
                                Glide.with(myContext).load(thisUser.getProfileImage())
                                        .placeholder(R.drawable.image_loading_placeholder)
                                        .into(profileImage);
                            }
                            catch (Exception ignored) {}

                            if(position == chats.size() + 1) {
                                if(initialMessage == null || initialMessage.length() == 0)
                                    messageLayout.setVisibility(View.GONE);
                                else {
                                    messageLayout.setVisibility(View.VISIBLE);
                                    tvMessage.setText(initialMessage);
                                }
                            }
                            else if(position == chats.size()) {
                                String fullName = "<b>" + thisUser.getLastName() + "</b>, " + thisUser.getFirstName();
                                if(thisUser.getMiddleName().length() > 0) fullName += " " + thisUser.getMiddleName();

                                String message = "こんにちは (Hello), I am " + fullName + ", your assigned driver.";
                                tvMessage.setText(fromHtml(message));
                            }
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
        if(html == null) {
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
        ConstraintLayout startPointLayout, endPointLayout;
        TextView tvStartPointMessage, tvEndPointMessage;
        ImageView startPointProfileImage, endPointProfileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            startPointLayout = itemView.findViewById(R.id.startPointLayout);
            endPointLayout = itemView.findViewById(R.id.endPointLayout);
            tvStartPointMessage = itemView.findViewById(R.id.tvStartPointMessage);
            tvEndPointMessage = itemView.findViewById(R.id.tvEndPointMessage);
            startPointProfileImage = itemView.findViewById(R.id.startPointProfileImage);
            endPointProfileImage = itemView.findViewById(R.id.endPointProfileImage);
        }
    }

    public void setPassengerUserId(String passengerUserId) {
        this.passengerUserId = passengerUserId;
        notifyDataSetChanged();
    }

    public void setDriverUserId(String driverUserId) {
        this.driverUserId = driverUserId;
        notifyDataSetChanged();
    }

    public void setInitialMessage(String initialMessage) {
        this.initialMessage = initialMessage;
        notifyDataSetChanged();
    }
}
