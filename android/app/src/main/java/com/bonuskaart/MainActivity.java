package com.bonuskaart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final String BARCODE_CASHE = "BARCODE_CASHE";
    private final String URL_TAG = "URL_TAG";

    private static final int REQUEST_CAMERA_PERMISSION = 201;

    private ImageView barcodeIV;
    private TextView barcodeTV;
    private TextView totalBRTV;
    private TextView refreshTV;
    private TextView uploadTV;
    private TextView whyTV;
    private TextView howTV;
    private TextView settingsTV;

    private BarcodeManager barcodeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = this;

        barcodeManager = new BarcodeManager(this, new BarcodeManager.OnBarcodereadyListener() {
            @Override
            public void onBarcodeReady(String barcode) {
                // Show barcode
                displayBarcode(barcode);

                // Update widgets
                updateWidgets(context);
            }
        });

        // Get all activity_main view handles
        getViews();

        // Get barcode
        barcodeManager.getBarcode();

        // Set all onClickListeners
        setOnClickListeners();
    }

    @Override
    protected void onRestart() {
        Log.d("MainActivity", "onRestart");
        super.onRestart();

        barcodeManager.getBarcode();
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
        // Load current or gets new barcode and displays in main view
        Log.d("MainActivity", "displayBarcode");

        if (barcode == null){
            barcode = "0000000000000";
        }

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

    private void setOnClickListeners() {
        Log.d("MainActivity", "setOnClickListeners");

        // Get context
        final Context context = this;

        // Set whyTV listener
        refreshTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get new barcode
                barcodeManager.getNewBarcode();
            }
        });

        // Set whyTV listener
        uploadTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    // Start ScannerActivity
                    Intent intent = new Intent(context, ScannerActivity.class);
                    startActivity(intent);

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }
            }
        });

        // Set whyTV listener
        whyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start WebActivity with WHY web page
                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra(URL_TAG, WHY_URL);
                startActivity(intent);
            }
        });

        // Set howTV listener
        howTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start WebActivity with how web page
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