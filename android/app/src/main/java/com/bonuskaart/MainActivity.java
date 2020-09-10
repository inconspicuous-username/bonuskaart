package com.bonuskaart;

import androidx.appcompat.app.AppCompatActivity;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    static String MAIN_URL = "https://bonuskaart.com/";
    static String NEW_BARCODE_URL = "https://bonuskaart.com/GetCard";
    static String GITHUB_URL = "https://github.com/inconspicuous-username/bonuskaart";
    static String DONATE_URL = "https://bonuskaart.com/donate_bonuskaart.html";
    static String WHY_URL = "https://bonuskaart.com/why.html";
    static String HOW_URL = "https://bonuskaart.com/how.html";

    private final String GENERAL_PREF = "GENERAL_PREF";
    private final String BARCODE = "BARCODE";
    private final String BARCODE_TIME = "BARCODE_TIME";
    private final String BARCODE_CASHE = "BARCODE_CASHE";
    private final String URL_TAG = "URL_TAG";

    private ImageView barcodeIV;
    private TextView barcodeTV;
    private TextView totalBRTV;
    private TextView refreshTV;
    private TextView uploadTV;
    private TextView whyTV;
    private TextView howTV;
    private TextView settingsTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get all activity_main view handles
        getViews();

        // Get current barcode
        String barcode = getBarcode();

        // Show barcode
        displayBarcode(barcode);

        // Set all onClickListeners
        setOnClickListeners();

        //TODO remove debug code
        //START DEBUG build debug barcode cashe
        Set<String> barcode_cashe_temp = new HashSet<String>();
        barcode_cashe_temp.add("000000000001");
        barcode_cashe_temp.add("000000000002");
        barcode_cashe_temp.add("000000000003");
        barcode_cashe_temp.add("000000000004");
        barcode_cashe_temp.add("000000000005");
        barcode_cashe_temp.add("000000000006");

        SharedPreferences prefs = this.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(BARCODE_CASHE, barcode_cashe_temp);
        editor.apply();
        //END DEBUG
    }

    @Override
    protected void onRestart() {
        Log.d("MainActivity", "onRestart");
        super.onRestart();

        // Get shared preferences
        SharedPreferences prefs = this.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);

        //Get barcode
        String barcode = prefs.getString(BARCODE, null);

        // Show barcode
        displayBarcode(barcode);
    }

    private void getViews(){
        Log.d("MainActivity", "getViews");

        //Get the views by ID
        barcodeIV = findViewById(R.id.barcodeIV);
        barcodeTV = findViewById(R.id.barcodeTV);
        totalBRTV = findViewById(R.id.totalBRTV);
        refreshTV = findViewById(R.id.refreshTV);
        uploadTV = findViewById(R.id.uploadTV);
        whyTV = findViewById(R.id.whyTV);
        howTV = findViewById(R.id.howTV);
        settingsTV = findViewById(R.id.settingsTV);
    }

    private void displayBarcode(String barcode) {
        Log.d("MainActivity", "displayBarcode: " + barcode);

        // Setup barcode bitmap generator
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            // Generate barcode bitmap
            Map<EncodeHintType, Object> hintMap = new HashMap<EncodeHintType, Object>();
            hintMap.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = multiFormatWriter.encode(barcode, BarcodeFormat.EAN_13, 500,100, hintMap);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            // Load bitmap into imageview
            barcodeIV.setImageBitmap(bitmap);
        } catch (WriterException | IllegalArgumentException e) {
            Log.d("MainActivity", "Failed to displayBarcode: " + barcode);
            //e.printStackTrace();
        }

        // Display barcode in textview
        barcodeTV.setText(barcode);
    }

    private String getBarcode() {
        Log.d("MainActivity", "getBarcode");

        // Get shared preferences
        SharedPreferences prefs = this.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);

        //Check if already has barcode
        String barcode = prefs.getString(BARCODE, null);
        long barcodeTime = prefs.getLong(BARCODE_TIME, 0);

        // Get current number of seconds
        long now = System.currentTimeMillis() / 1000;

        // Get new barcode if no barcode or expired
        if(barcode == null || now - (24 * 60 * 60) > barcodeTime) {
            Log.d("MainActivity", "barcode: " + barcode + " created at: " + barcodeTime + " not valid");
            barcode = getNewBarcode();
        }else{
            Log.d("MainActivity", "barcode: " + barcode + " created at: " + barcodeTime + " valid");
        }

        return barcode;
    }

    private String getNewBarcode(){
        Log.d("MainActivity", "getNewBarcode");

        // Get shared preferences
        SharedPreferences prefs = this.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);

        // Retrieve barcode cache
        Set<String> barcode_cache = prefs.getStringSet(BARCODE_CASHE, null);

        String new_barcode;

        if(barcode_cache != null && barcode_cache.size() > 0){
            Log.d("MainActivity", "getNewBarcode: barcode_cache found " + barcode_cache.size());
            if(barcode_cache.size() < 5){
                // Cache low, download new barcodes
                // TODO get new barcodes in new thread
            }

            // Get and remove first barcode from cache
            Iterator<String> iterator = barcode_cache.iterator();
            new_barcode = iterator.next();
            iterator.remove();

            Log.d("MainActivity", "getNewBarcode: barcode_cache found " + barcode_cache.size());
            // Save updated cache
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet(BARCODE_CASHE, barcode_cache);
            editor.apply();

        }else{
            Log.d("MainActivity", "getNewBarcode: No barcode_cache found");
            // No cache found download new barcodes
            //TODO get new barcodes in this thread
            new_barcode = "000000000000";
        }

        // Save and update widget
        saveBarcode(prefs, new_barcode);
        updateWidgets(this);

        return new_barcode;
    }

    private void saveBarcode(SharedPreferences prefs, String barcode){
        Log.d("MainActivity", "saveBarcode: " + barcode);

        // Get barcode creation time
        long barcodeTime = System.currentTimeMillis() / 1000;

        // Get shared preferences editor
        SharedPreferences.Editor editor = prefs.edit();

        // Save new barcode and generation time
        editor.putString(BARCODE, barcode);
        editor.putLong(BARCODE_TIME, barcodeTime);
        editor.apply();
        Log.d("MainActivity", "saved new barcode: " + barcode + " created at: " + barcodeTime);
    }

    private void setOnClickListeners() {
        Log.d("MainActivity", "setOnClickListeners");

        // Get context
        final Context context = this;

        // Set whyTV listener
        refreshTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get new barcode
                String barcode = getNewBarcode();

                // Show new barcode
                displayBarcode(barcode);

                // Update widget
            }
        });

        // Set whyTV listener
        whyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra(URL_TAG, WHY_URL);
                startActivity(intent);
            }
        });

        // Set howTV listener
        howTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra(URL_TAG, HOW_URL);
                startActivity(intent);
            }
        });
    }

    public void updateWidgets(Context context){
        Log.d("MainActivity", "updateWidgets");

        // Build widget update intent
        Intent intent = new Intent(this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetI‌​ds(new ComponentName(getApplication(), WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }
}