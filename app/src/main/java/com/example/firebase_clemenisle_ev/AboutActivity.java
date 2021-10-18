package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebase_clemenisle_ev.Classes.AppMetaData;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);

    ProgressBar progressBar;

    ImageView androidStudioLogoImage, fireBaseLogoImage, googleMapLogoImage, googleStreetViewImage;
    ExpandableTextView extvAbout;
    TextView tvAppVersion2;
    Button updateAppButton;

    Context myContext;

    AppMetaData appMetaData;

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

        myContext = AboutActivity.this;

        Glide.with(myContext).load(R.drawable.android_studio_logo).
                placeholder(R.drawable.image_loading_placeholder).into(androidStudioLogoImage);
        Glide.with(myContext).load(R.drawable.firebase_logo).
                placeholder(R.drawable.image_loading_placeholder).into(fireBaseLogoImage);
        Glide.with(myContext).load(R.drawable.google_map_logo).
                placeholder(R.drawable.image_loading_placeholder).into(googleMapLogoImage);
        Glide.with(myContext).load(R.drawable.google_street_view).
                placeholder(R.drawable.image_loading_placeholder).into(googleStreetViewImage);

        appMetaData = new AppMetaData();
        getAppMetaData();

        updateAppButton.setOnClickListener(view -> {
            Intent newIntent = new Intent(myContext, WebViewActivity.class);
            startActivity(newIntent);
        });
    }

    private void getAppMetaData() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference metaDataRef = firebaseDatabase.getReference("appMetaData");
        metaDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String aboutApp = "Failed to get data";
                double latestVersion = 0;
                String status = "Failed to get data";

                if(snapshot.exists()) {
                    if(snapshot.child("about").exists())
                        aboutApp = snapshot.child("about").getValue(String.class);
                    if(snapshot.child("version").exists())
                        latestVersion = snapshot.child("version").getValue(Double.class);
                    if(snapshot.child("status").exists())
                        status = snapshot.child("status").getValue(String.class);
                }

                appMetaData.setAboutApp(aboutApp);
                appMetaData.setLatestVersion(latestVersion);
                appMetaData.setStatus(status);

                extvAbout.setText(aboutApp);

                tvAppVersion2.setText(String.valueOf(latestVersion));

                if(appMetaData.getCurrentVersion() < latestVersion)
                    updateAppButton.setVisibility(View.VISIBLE);
                else updateAppButton.setVisibility(View.GONE);

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
}