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
import java.util.Map;

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

        // Get (new) barcode
        String barcode = getBarcode();

        displayBarcode(barcode);

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        final Context context = this;

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
        } catch (WriterException e) {
            Log.d("MainActivity", "Failed to displayBarcode: " + barcode);
            e.printStackTrace();
        }

        // Display barcode in textview
        barcodeTV.setText(barcode);
    }

    private String getBarcode() {
        // Get shared preferences
        final SharedPreferences prefs = this.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);

        //Check if already has barcode
        final String barcode = prefs.getString(BARCODE, null);
        long barcodeTime = prefs.getLong(BARCODE_TIME, 0);

        // Get current number of seconds
        long now = System.currentTimeMillis() / 1000;

        // Get new barcode if no barcode or expired
        if(barcode == null || now - (24 * 60 * 60) > barcodeTime) {
            Log.d("MainActivity", "barcode: " + barcode + " created at: " + barcodeTime + " not valid");
            //TODO get new barcode from cache
        }else{
            Log.d("MainActivity", "barcode: " + barcode + " created at: " + barcodeTime + " valid");
        }

        return barcode;
    }

    private void getViews(){
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

    public void updateWidgets(Context context){
        // Build widget update intent
        Intent intent = new Intent(this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetI‌​ds(new ComponentName(getApplication(), WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }
}