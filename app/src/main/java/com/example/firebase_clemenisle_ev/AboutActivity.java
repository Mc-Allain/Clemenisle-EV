package com.example.firebase_clemenisle_ev;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Classes.AppMetaData;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

public class AboutActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    DatabaseReference usersRef;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ProgressBar progressBar;

    ImageView androidStudioLogoImage, fireBaseLogoImage, googleMapLogoImage, googleStreetViewImage;
    ExpandableTextView extvAbout, extvNewlyAddedFeatures;
    TextView tvAppVersion2;
    Button updateAppButton;

    TextView tvAppRating2;
    ImageView star1Image, star2Image, star3Image, star4Image, star5Image;
    ConstraintLayout loginAppRateLayout;
    Button loginButton, rateAppButton;

    Context myContext;
    Resources myResources;

    int colorInitial, colorRed, colorOrange;

    AppMetaData appMetaData;

    String userId;

    boolean isLoggedIn = false;

    double appRating = 0;

    Dialog dialog;
    ImageView dialogCloseImage, dialogStar1Image, dialogStar2Image, dialogStar3Image, dialogStar4Image, dialogStar5Image;
    Button dialogSubmitButton;
    ProgressBar dialogProgressBar;

    int starValue = 0;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("isRemembered", false);
        editor.apply();

        NotificationManager notificationManager = (NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        progressBar = findViewById(R.id.progressBar);

        androidStudioLogoImage = findViewById(R.id.androidStudioLogoImage);
        fireBaseLogoImage = findViewById(R.id.fireBaseLogoImage);
        googleMapLogoImage = findViewById(R.id.googleMapLogoImage);
        googleStreetViewImage = findViewById(R.id.googleStreetViewImage);

        extvAbout = findViewById(R.id.extvAbout);

        tvAppVersion2 = findViewById(R.id.tvAppVersion2);
        updateAppButton = findViewById(R.id.updateAppButton);

        extvNewlyAddedFeatures = findViewById(R.id.extvNewlyAddedFeatures);

        tvAppRating2 = findViewById(R.id.tvAppRating2);

        star1Image = findViewById(R.id.star1Image);
        star2Image = findViewById(R.id.star2Image);
        star3Image = findViewById(R.id.star3Image);
        star4Image = findViewById(R.id.star4Image);
        star5Image = findViewById(R.id.star5Image);

        loginAppRateLayout = findViewById(R.id.loginAppRateLayout);
        loginButton = findViewById(R.id.loginButton);
        rateAppButton = findViewById(R.id.rateAppButton);

        myContext = AboutActivity.this;
        myResources = myContext.getResources();

        colorInitial = myResources.getColor(R.color.initial);
        colorRed = myResources.getColor(R.color.red);
        colorOrange = myResources.getColor(R.color.orange);

        initSharedPreferences();
        initRateTheAppDialog();

        firebaseAuth = FirebaseAuth.getInstance();
        if(isLoggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) firebaseUser.reload();
            if(firebaseUser == null) {
                firebaseAuth.signOut();
                sendLoginPreferences();

                Toast.makeText(
                        myContext,
                        "Failed to get the current user. Account logged out.",
                        Toast.LENGTH_LONG
                ).show();
            }
            else userId = firebaseUser.getUid();
        }

        if(userId != null) {
            loginAppRateLayout.setVisibility(View.GONE);
            rateAppButton.setVisibility(View.VISIBLE);
        }
        else {
            loginAppRateLayout.setVisibility(View.VISIBLE);
            rateAppButton.setVisibility(View.GONE);
        }

        usersRef = firebaseDatabase.getReference("users");

        try {
            Glide.with(myContext).load(R.drawable.android_studio_logo)
                    .placeholder(R.drawable.image_loading_placeholder).into(androidStudioLogoImage);

            Glide.with(myContext).load(R.drawable.firebase_logo)
                    .placeholder(R.drawable.image_loading_placeholder).into(fireBaseLogoImage);

            Glide.with(myContext).load(R.drawable.google_map_logo)
                    .placeholder(R.drawable.image_loading_placeholder).into(googleMapLogoImage);

            Glide.with(myContext).load(R.drawable.google_street_view)
                    .placeholder(R.drawable.image_loading_placeholder).into(googleStreetViewImage);
        }
        catch (Exception ignored) {}

        appMetaData = new AppMetaData();
        getAppMetaData();

        updateAppButton.setOnClickListener(view -> {
            Intent newIntent = new Intent(myContext, WebViewActivity.class);
            newIntent.putExtra("toUpdate", true);
            startActivity(newIntent);
        });

        loginButton.setOnClickListener(view -> {
            Intent newIntent = new Intent(myContext, LoginActivity.class);
            startActivity(newIntent);
        });

        rateAppButton.setOnClickListener(view -> openRateTheAppDialog());
    }

    private void getAppMetaData() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference metaDataRef = firebaseDatabase.getReference("appMetaData");
        metaDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String aboutApp = "Failed to get data";
                double latestVersion = 0;

                if(snapshot.exists()) {
                    if(snapshot.child("about").exists())
                        aboutApp = snapshot.child("about").getValue(String.class);
                    if(snapshot.child("version").exists())
                        latestVersion = snapshot.child("version").getValue(Double.class);
                }

                extvAbout.setText(aboutApp);

                tvAppVersion2.setText(String.valueOf(appMetaData.getCurrentVersion()));

                if(appMetaData.getCurrentVersion() < latestVersion)
                    updateAppButton.setVisibility(View.VISIBLE);
                else updateAppButton.setVisibility(View.GONE);

                String newlyAddedFeatures = appMetaData.getNewlyAddedFeatures();
                extvNewlyAddedFeatures.setText(newlyAddedFeatures);

                getAppRating();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void getAppRating() {
        usersRef.orderByChild("appRating").startAt(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appRating = 0; double totalRates = 0; long userCount = snapshot.getChildrenCount();
                String ratedUsersText = "Rated by " + userCount + " " + (userCount == 1 ? "user" : "users");

                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = new User(dataSnapshot);
                        if(user.getId().equals(userId)) starValue = user.getAppRating();
                        totalRates += user.getAppRating();
                    }
                    appRating = totalRates / userCount;
                }

                tvAppRating2.setText(ratedUsersText);
                renderStars();

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void renderStars() {
        star1Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        star2Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        star3Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        star4Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        star5Image.setImageResource(R.drawable.ic_baseline_star_outline_24);

        if(appRating >= 0.5) star1Image.setImageResource(R.drawable.ic_baseline_star_half_24);
        if(appRating >= 1) star1Image.setImageResource(R.drawable.ic_baseline_star_24);

        if(appRating >= 1.5) star2Image.setImageResource(R.drawable.ic_baseline_star_half_24);
        if(appRating >= 2) star2Image.setImageResource(R.drawable.ic_baseline_star_24);

        if(appRating >= 2.5) star3Image.setImageResource(R.drawable.ic_baseline_star_half_24);
        if(appRating >= 3) star3Image.setImageResource(R.drawable.ic_baseline_star_24);

        if(appRating >= 3.5) star4Image.setImageResource(R.drawable.ic_baseline_star_half_24);
        if(appRating >= 4) star4Image.setImageResource(R.drawable.ic_baseline_star_24);

        if(appRating >= 4.5) star5Image.setImageResource(R.drawable.ic_baseline_star_half_24);
        if(appRating >= 5) star5Image.setImageResource(R.drawable.ic_baseline_star_24);
    }

    private void openRateTheAppDialog() {
        clickStar(starValue);

        dialogSubmitButton.setOnClickListener(view -> rate());

        dialog.show();
    }

    private void initRateTheAppDialog() {
        dialog = new Dialog(myContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_input_app_rate_layout);

        dialogSubmitButton = dialog.findViewById(R.id.submitButton);
        dialogCloseImage = dialog.findViewById(R.id.dialogCloseImage);
        dialogProgressBar = dialog.findViewById(R.id.dialogProgressBar);

        dialogStar1Image = dialog.findViewById(R.id.star1Image);
        dialogStar2Image = dialog.findViewById(R.id.star2Image);
        dialogStar3Image = dialog.findViewById(R.id.star3Image);
        dialogStar4Image = dialog.findViewById(R.id.star4Image);
        dialogStar5Image = dialog.findViewById(R.id.star5Image);

        dialogStar1Image.setOnClickListener(view -> clickStar(1));
        dialogStar2Image.setOnClickListener(view -> clickStar(2));
        dialogStar3Image.setOnClickListener(view -> clickStar(3));
        dialogStar4Image.setOnClickListener(view -> clickStar(4));
        dialogStar5Image.setOnClickListener(view -> clickStar(5));

        dialogCloseImage.setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(myContext, R.drawable.corner_top_white_layout));
        dialog.getWindow().getAttributes().windowAnimations = R.style.animBottomSlide;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void clickStar(int count) {
        starValue = count;

        dialogStar1Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        dialogStar2Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        dialogStar3Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        dialogStar4Image.setImageResource(R.drawable.ic_baseline_star_outline_24);
        dialogStar5Image.setImageResource(R.drawable.ic_baseline_star_outline_24);

        if(count >= 1) dialogStar1Image.setImageResource(R.drawable.ic_baseline_star_24);
        if(count >= 2) dialogStar2Image.setImageResource(R.drawable.ic_baseline_star_24);
        if(count >= 3) dialogStar3Image.setImageResource(R.drawable.ic_baseline_star_24);
        if(count >= 4) dialogStar4Image.setImageResource(R.drawable.ic_baseline_star_24);
        if(count >= 5) dialogStar5Image.setImageResource(R.drawable.ic_baseline_star_24);

        dialogSubmitButton.setEnabled(count != 0);
    }

    private void rate() {
        dialogProgressBar.setVisibility(View.VISIBLE);
        setDialogScreenEnabled(false);

        usersRef.child(userId).child("appRating").setValue(starValue)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(
                                myContext,
                                "Successfully rated the app",
                                Toast.LENGTH_LONG
                        ).show();
                        dialog.dismiss();
                    }
                    else {
                        Toast.makeText(
                                myContext,
                                "Failed to rate the app",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    dialogProgressBar.setVisibility(View.GONE);
                    setDialogScreenEnabled(true);
                });
    }

    private void setDialogScreenEnabled(boolean value) {
        dialog.setCanceledOnTouchOutside(value);
        dialog.setCancelable(value);
        dialogStar1Image.setClickable(value);
        dialogStar2Image.setClickable(value);
        dialogStar3Image.setClickable(value);
        dialogStar4Image.setClickable(value);
        dialogStar5Image.setClickable(value);
        dialogSubmitButton.setEnabled(value);

        if(value) {
            dialogCloseImage.getDrawable().setTint(colorRed);
            dialogStar1Image.getDrawable().setTint(colorOrange);
            dialogStar2Image.getDrawable().setTint(colorOrange);
            dialogStar3Image.getDrawable().setTint(colorOrange);
            dialogStar4Image.getDrawable().setTint(colorOrange);
            dialogStar5Image.getDrawable().setTint(colorOrange);
        }
        else {
            dialogCloseImage.getDrawable().setTint(colorInitial);
            dialogStar1Image.getDrawable().setTint(colorInitial);
            dialogStar2Image.getDrawable().setTint(colorInitial);
            dialogStar3Image.getDrawable().setTint(colorInitial);
            dialogStar4Image.getDrawable().setTint(colorInitial);
            dialogStar5Image.getDrawable().setTint(colorInitial);
        }
    }
}