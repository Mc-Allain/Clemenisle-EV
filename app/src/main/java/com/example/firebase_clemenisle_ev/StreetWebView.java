package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class StreetWebView extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);

    WebView webView;
    ImageView prevImage, nextImage;
    TextView tvCount;
    ProgressBar progressBar;

    Context myContext;

    String id;

    List<String> vriList = new ArrayList<>();
    String vri;
    int selectedIndex = 0;
    int maxIndex = 0;
    String countText = (selectedIndex + 1) + "/" + (maxIndex + 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_web_view);

        webView = findViewById(R.id.webView);
        prevImage = findViewById(R.id.prevImage);
        nextImage = findViewById(R.id.nextImage);
        tvCount = findViewById(R.id.tvCount);
        progressBar = findViewById(R.id.progressBar);

        myContext = StreetWebView.this;

        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                new Handler().postDelayed(() -> {
                    webView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                    if(vriList.size() > 1) {
                        prevImage.setVisibility(View.VISIBLE);
                        nextImage.setVisibility(View.VISIBLE);
                        tvCount.setVisibility(View.VISIBLE);
                    }
                }, 2000);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                webView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                prevImage.setVisibility(View.GONE);
                nextImage.setVisibility(View.GONE);
                tvCount.setVisibility(View.GONE);
            }
        });

        getVRI();

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        prevImage.setOnClickListener(view -> {
            if(selectedIndex == 0) {
                selectedIndex = maxIndex;
            }
            else {
                selectedIndex--;
            }
            vri = vriList.get(selectedIndex);
            selectVRI();
        });

        nextImage.setOnClickListener(view -> {
            if(selectedIndex == maxIndex) {
                selectedIndex = 0;
            }
            else {
                selectedIndex++;
                selectVRI();
            }
        });
    }

    private void selectVRI() {
        vri = vriList.get(selectedIndex);
        webView.loadUrl(vri);
        countText = (selectedIndex + 1) + "/" + (maxIndex + 1);
        tvCount.setText(countText);
    }

    private void getVRI() {
        DatabaseReference touristSpotsRef = firebaseDatabase.getReference("touristSpots")
                .child(id).child("vri");
        touristSpotsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vriList.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        vriList.add(dataSnapshot.getValue().toString());
                    }

                    selectedIndex = 0;
                    vri = vriList.get(selectedIndex);
                    maxIndex = vriList.size() - 1;

                    webView.loadUrl(vri);
                    countText = (selectedIndex + 1) + "/" + (maxIndex + 1);
                    tvCount.setText(countText);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}