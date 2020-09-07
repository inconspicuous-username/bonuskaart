package com.bonuskaart;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    static String MAIN_URL = "https://bonuskaart.com/";
    static String NEW_BARCODE_URL = "https://bonuskaart.com/GetCard";
    static String GITHUB_URL = "https://github.com/inconspicuous-username/bonuskaart";
    static String DONATE_URL = "https://bonuskaart.com/donate_bonuskaart.html";

    private final String GENERAL_PREF = "GENERAL_PREF";
    private final String BARCODE = "BARCODE";
    private final String BARCODE_TIME = "BARCODE_TIME";

    private final String NEW_BARCODE_WEB_CALLBACK = "NEW_BARCODE_WEB_CALLBACK";

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Save context
        final Context context = this;

        // Load webView
        webView = findViewById(R.id.webview);

        // Load custom client to enable using the links
        webView.setWebViewClient(new myWebClient());

        // Enable javascript and scale site to screen width
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        // Load javascript interface
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        // Get shared preferences
        final SharedPreferences prefs = this.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);

        //Check if already has barcode
        final String barcode = prefs.getString(BARCODE, null);
        long barcodeTime = prefs.getLong(BARCODE_TIME, 0);

        // Get current number of seconds
        long now = System.currentTimeMillis() / 1000;

        // Get new barcode if no barcode or expired
        if(barcode == null || now - (24 * 60 * 60) > barcodeTime){
            Log.d("MainActivity", "barcode: " + barcode + " created at: " + barcodeTime + " not valid");

            getNewBarcode(
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String barcode) {
                        // Get barcode creation time
                        long barcodeTime = System.currentTimeMillis() / 1000;

                        // Get shared preferences editor
                        SharedPreferences.Editor editor = prefs.edit();

                        // Save new cookie and generation time
                        editor.putString(BARCODE, barcode);
                        editor.putLong(BARCODE_TIME, barcodeTime);
                        editor.apply();
                        Log.d("MainActivity", "New barcode: " + barcode + " created at: " + barcodeTime);

                        // Load website
                        loadWebsite(barcode);

                        // Update widgets
                        updateWidgets(context);
                    }
                }
            );
        }else{
            Log.d("MainActivity", "Barcode: " + barcode + " created at: " + barcodeTime + " still valid");
            loadWebsite(barcode);
        }
    }

    @Override
    protected void onRestart() {
        Log.d("MainActivity", "onRestart");
        super.onRestart();

        // Get shared preferences
        SharedPreferences prefs = this.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);

        //Get barcode
        String barcode = prefs.getString(BARCODE, null);

        // Store barcode in cookie to show in website
        CookieManager.getInstance().setCookie(MAIN_URL, "bonuskaart=" + barcode);

        // Reload when on mainpage
        if(webView.getUrl().equals(MAIN_URL)) {
            webView.reload();
        }
    }

    private void loadWebsite(String barcode){
        Log.d("MainActivity", "LoadWebsite with: " + barcode);

        // Store barcode in cookie to show in website
        CookieManager.getInstance().setCookie(MAIN_URL, "bonuskaart=" + barcode);

        // Load bonuskaart.com
        webView.loadUrl(MAIN_URL);
    }

    private void getNewBarcode(Response.Listener<String> callback){
        Log.d("MainActivity", "GetNewBarcode");

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, NEW_BARCODE_URL, callback, null);

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    // Custom WebViewClient to load clicked url's and extract the barcode
    public class myWebClient extends WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.equals(GITHUB_URL)){
                Log.d("MainActivity", "GITHUB_URL");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                return true;
            }

            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            Log.d("MainActivity", "onPageFinished URL: " + webView.getUrl());

            if(webView.getUrl().equals(MAIN_URL)){
                // Change the javascript to call android functions
                webView.loadUrl("javascript:function androidNewBarcode() {Android.androidNewBarcode();}");
                webView.loadUrl("javascript:document.getElementsByTagName('a')[0].setAttribute('onclick', \"androidNewBarcode()\");");
            }else if(webView.getUrl().equals(DONATE_URL)){
                webView.loadUrl("javascript:var a = document.createElement(\"a\");a.setAttribute('style', \"font-size: 40pt\");a.setAttribute('onclick', \"androidScanBarcode()\");a.innerHTML = \"Scan card\";var node = document.getElementsByTagName('form')[0];var b = document.querySelectorAll('input[type=Submit]');node.insertBefore(a, b);");
            }


        }
    }

    public class WebAppInterface {
        Context context;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            context = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void androidNewBarcode() {
            Log.d("MainActivity", "WebAppInterface androidNewBarcode");
            getNewBarcode(
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String barcode) {
                        // Get barcode creation time
                        long barcodeTime = System.currentTimeMillis() / 1000;

                        // Get shared preferences editor
                        SharedPreferences prefs = context.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        // Save new cookie and generation time
                        editor.putString(BARCODE, barcode);
                        editor.putLong(BARCODE_TIME, barcodeTime);
                        editor.apply();
                        Log.d("MainActivity", "WebAppInterface new barcode: " + barcode + " created at: " + barcodeTime);

                        // Load website
                        loadWebsite(barcode);

                        // Update widgets
                        updateWidgets(context);
                    }
                }
            );
        }
    }

    public void updateWidgets(Context context){
        // Build widget update intent
        Intent intent = new Intent(this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetI‌​ds(new ComponentName(getApplication(), WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    // Handle back key press event for WebView to go back to previous screen.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}