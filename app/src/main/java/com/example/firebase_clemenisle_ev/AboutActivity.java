package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    Context myContext;

    ImageView androidStudioLogoImage, fireBaseLogoImage, googleMapLogoImage, googleStreetViewImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        androidStudioLogoImage = findViewById(R.id.androidStudioLogoImage);
        fireBaseLogoImage = findViewById(R.id.fireBaseLogoImage);
        googleMapLogoImage = findViewById(R.id.googleMapLogoImage);
        googleStreetViewImage = findViewById(R.id.googleStreetViewImage);

        myContext = AboutActivity.this;

        Glide.with(myContext).load(R.drawable.android_studio_logo).
                placeholder(R.drawable.image_loading_placeholder).into(androidStudioLogoImage);
        Glide.with(myContext).load(R.drawable.firebase_logo).
                placeholder(R.drawable.image_loading_placeholder).into(fireBaseLogoImage);
        Glide.with(myContext).load(R.drawable.google_map_logo).
                placeholder(R.drawable.image_loading_placeholder).into(googleMapLogoImage);
        Glide.with(myContext).load(R.drawable.google_street_view).
                placeholder(R.drawable.image_loading_placeholder).into(googleStreetViewImage);
    }
}