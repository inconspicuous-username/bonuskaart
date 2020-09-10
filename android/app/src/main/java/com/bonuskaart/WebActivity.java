package com.bonuskaart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class WebActivity extends AppCompatActivity {

    private WebView webView;
    private final String URL_TAG = "URL_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("WebActivity", "onCreate");

        // Load web activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        // Get webView handle
        webView = findViewById(R.id.webviewWV);

        // Scale site to screen width
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        // Get en load url
        Intent intent = getIntent();
        String url = intent.getStringExtra(URL_TAG);
        webView.loadUrl(url);
    }
}
