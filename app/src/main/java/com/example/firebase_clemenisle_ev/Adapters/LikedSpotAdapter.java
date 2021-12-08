package com.example.firebase_clemenisle_ev.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.SimpleTouristSpot;
import com.example.firebase_clemenisle_ev.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class LikedSpotAdapter extends RecyclerView.Adapter<LikedSpotAdapter.ViewHolder> {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);

    List<SimpleTouristSpot> likedSpots;
    String userId;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    long unlikePressedTime;
    Toast unlikeToast;

    Dialog confirmationDialog;
    ImageView confirmationDialogCloseImage;
    TextView tvDialogTitleConfirmation, tvDialogCaptionConfirmation;
    Button confirmationDialogConfirmButton, confirmationDialogCancelButton;
    ProgressBar confirmationDialogProgressBar;

    boolean isConfirmationDialogEnabled;

    int colorRed, colorInitial;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        isConfirmationDialogEnabled = sharedPreferences.getBoolean("isConfirmationDialogEnabled", true);
    }

    public LikedSpotAdapter(Context context, List<SimpleTouristSpot> likedSpots, String userId) {
        this.likedSpots = likedSpots;
        this.userId = userId;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_liked_spot_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView thumbnail = holder.thumbnail;
        TextView tvName = holder.tvName, tvUnliked = holder.tvUnliked;
        Button unlikeButton = holder.unlikeButton;
        ConstraintLayout backgroundLayout = holder.backgroundLayout;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        colorRed = myResources.getColor(R.color.red);
        colorInitial = myResources.getColor(R.color.initial);

        initSharedPreferences();

        SimpleTouristSpot likedSpot = likedSpots.get(position);

        String id = likedSpot.getId();
        String name = likedSpot.getName();
        String img = likedSpot.getImg();

        try {
            Glide.with(myContext).load(img).
                    placeholder(R.drawable.image_loading_placeholder).
                    override(Target.SIZE_ORIGINAL).into(thumbnail);
        }
        catch (Exception ignored) {}
        tvName.setText(name);

        int start = dpToPx(4), end = dpToPx(4);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == getItemCount();

        if(isFirstItem) {
            start = dpToPx(8);
        }
        if(isLastItem) {
            end = dpToPx(8);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                layoutParams.rightMargin, layoutParams.bottomMargin);
        layoutParams.setMarginStart(start);
        layoutParams.setMarginEnd(end);
        backgroundLayout.setLayoutParams(layoutParams);

        unlikeButton.setOnClickListener(view -> {
            if(isConfirmationDialogEnabled) {
                initConfirmationDialog();
                openConfirmationDialog("Unlike Tourist Spot", "Do you want to unlike the tourist spot?");

                confirmationDialogConfirmButton.setOnClickListener(view1 -> {
                    confirmationDialogProgressBar.setVisibility(View.VISIBLE);
                    setConfirmationDialogScreenEnabled(false);

                    firebaseDatabase.getReference("users").child(userId).
                            child("likedSpots").child(id).removeValue().addOnCompleteListener(task -> {
                        if(task.isSuccessful()) confirmationDialog.dismiss();
                        else {
                            Toast.makeText(
                                    myContext,
                                    "Failed to unlike the tourist spot",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        confirmationDialogProgressBar.setVisibility(View.GONE);
                        setConfirmationDialogScreenEnabled(true);
                    });

                });
            }
            else {
                if(unlikeToast != null) unlikeToast.cancel();

                if(unlikePressedTime + 2500 > System.currentTimeMillis() &&
                        tvUnliked.getText().toString().equals("true")) {

                    firebaseDatabase.getReference("users").child(userId).
                            child("likedSpots").child(id).removeValue().addOnCompleteListener(task -> {
                                if(!task.isSuccessful()) {
                                    unlikeToast = Toast.makeText(
                                            myContext,
                                            "Failed to unlike the tourist spot",
                                            Toast.LENGTH_LONG
                                    );
                                    unlikeToast.show();
                                }
                                tvUnliked.setText("false");
                            });
                }
                else {
                    unlikeToast = Toast.makeText(myContext,
                            "Press again to unlike", Toast.LENGTH_SHORT);
                    unlikeToast.show();

                    unlikePressedTime = System.currentTimeMillis();

                    tvUnliked.setText("true");
                }
            }
        });
    }

    private int dpToPx(int dp) {
        float px = dp * myResources.getDisplayMetrics().density;
        return (int) px;
    }

    private void openConfirmationDialog(String title, String caption) {
        tvDialogTitleConfirmation.setText(title);
        tvDialogCaptionConfirmation.setText(caption);
        confirmationDialog.show();
    }

    private void initConfirmationDialog() {
        confirmationDialog = new Dialog(myContext);
        confirmationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        confirmationDialog.setContentView(R.layout.dialog_confirmation_layout);

        confirmationDialogCloseImage = confirmationDialog.findViewById(R.id.dialogCloseImage);
        tvDialogTitleConfirmation = confirmationDialog.findViewById(R.id.tvDialogTitle);
        tvDialogCaptionConfirmation = confirmationDialog.findViewById(R.id.tvDialogCaption);
        confirmationDialogConfirmButton = confirmationDialog.findViewById(R.id.confirmButton);
        confirmationDialogCancelButton = confirmationDialog.findViewById(R.id.cancelButton);
        confirmationDialogProgressBar = confirmationDialog.findViewById(R.id.dialogProgressBar);

        confirmationDialogCloseImage.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialogCancelButton.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        confirmationDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        confirmationDialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        confirmationDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void setConfirmationDialogScreenEnabled(boolean value) {
        confirmationDialog.setCanceledOnTouchOutside(value);
        confirmationDialog.setCancelable(value);
        confirmationDialogConfirmButton.setEnabled(value);
        confirmationDialogCancelButton.setEnabled(value);

        if(value) confirmationDialogCloseImage.getDrawable().setTint(colorRed);
        else confirmationDialogCloseImage.getDrawable().setTint(colorInitial);
    }

    @Override
    public int getItemCount() {
        return likedSpots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView tvName, tvUnliked;
        Button unlikeButton;
        ConstraintLayout backgroundLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvName = itemView.findViewById(R.id.tvName);
            tvUnliked = itemView.findViewById(R.id.tvUnliked);
            unlikeButton = itemView.findViewById(R.id.unlikeButton);

            setIsRecyclable(false);
        }
    }
}
