package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
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

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AppStatusActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);

    TextView tvActivityName, tvCaption;
    ImageView activityIconImage;
    Button exitAppButton;

    Context myContext;
    Resources myResources;

    boolean isErrorStatus = false;

    String defaultCaption = "Sorry, the application is currently ",
            comebackCaption = ". Please come back again later.";

    AppMetaData appMetaData;

    DatabaseReference metaDataRef;

    List<String> statusPromptArray = Arrays.asList("Under Development", "Under Maintenance");

    boolean isMainActivityShown = false;
    boolean isOnScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_status);

        tvActivityName = findViewById(R.id.tvActivityName);
        tvCaption = findViewById(R.id.tvCaption);
        activityIconImage = findViewById(R.id.activityIconImage);
        exitAppButton = findViewById(R.id.exitAppButton);

        activityIconImage = findViewById(R.id.activityIconImage);

        myContext = AppStatusActivity.this;
        myResources = myContext.getResources();

        Intent intent = getIntent();
        isErrorStatus = intent.getBooleanExtra("isErrorStatus", false);

        isOnScreen = true;

        appMetaData = new AppMetaData();
        if(!isErrorStatus) getAppMetaData();
        else statusError();

        exitAppButton.setOnClickListener(view -> finishAffinity());
    }

    private void getAppMetaData() {
        metaDataRef = firebaseDatabase.getReference("appMetaData");
        metaDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = "Failed to get data";

                if(snapshot.exists()) {
                    if(snapshot.child("status").exists())
                        status = snapshot.child("status").getValue(String.class);
                }

                if(statusPromptArray.contains(status) && !appMetaData.isDeveloper() && status != null) {
                    if(status.equals(statusPromptArray.get(0)) && isOnScreen) {
                        try {
                            Glide.with(myContext).load(R.drawable.under_development)
                                    .placeholder(R.drawable.image_loading_placeholder)
                                    .into(activityIconImage);
                        }
                        catch (Exception ignored) {}
                    }
                    else if(status.equals(statusPromptArray.get(1)) && isOnScreen) {
                        try {
                            Glide.with(myContext).load(R.drawable.under_maintenance)
                                    .placeholder(R.drawable.image_loading_placeholder)
                                    .into(activityIconImage);
                        }
                        catch (Exception ignored) {}
                    }

                    tvActivityName.setText(status);
                    String caption = defaultCaption + status.toLowerCase() + comebackCaption;
                    tvCaption.setText(caption);
                }
                else if (!isMainActivityShown){
                    Intent newIntent = new Intent(myContext, MainActivity.class);
                    startActivity(newIntent);
                    finishAffinity();
                    isMainActivityShown = !isMainActivityShown;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                statusError();
            }
        });
    }

    private void statusError() {
        if(isOnScreen) {
            try {
                Glide.with(myContext).load(R.drawable.ic_baseline_error_outline_24)
                        .placeholder(R.drawable.image_loading_placeholder)
                        .into(activityIconImage);
            }
            catch (Exception ignored) {}
        }

        String activityName = "Application Error";
        tvActivityName.setText(activityName);
        String caption = "Something went wrong to the application. Please try again later.";
        tvCaption.setText(caption);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishAffinity();
        isMainActivityShown = true;
        isOnScreen = false;
    }
}