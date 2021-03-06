package com.example.firebase_clemenisle_ev;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    WebView webView;

    boolean toUpdate, toPrivacyNotice, toVideoTutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        Intent intent = getIntent();
        toUpdate = intent.getBooleanExtra("toUpdate", false);
        toPrivacyNotice = intent.getBooleanExtra("toPrivacyNotice", false);
        toVideoTutorial = intent.getBooleanExtra("toVideoTutorial", false);

        String link = "https://mc-allain.github.io/Clemenisle-EV-Web/";
        if(toUpdate) link = "https://mc-allain.github.io/Clemenisle-EV-Web/";
        if(toPrivacyNotice) link = "https://mc-allain.github.io/Clemenisle-EV-Web/privacy_policy.html";
        if(toVideoTutorial) link = "https://www.youtube.com/playlist?list=PLE_CHrmDUq9lnEaJmQ-jgecjCyhcg6quj";

        webView = findViewById(R.id.webView);
        webView.loadUrl(link);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }
}