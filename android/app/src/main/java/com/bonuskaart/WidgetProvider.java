package com.bonuskaart;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class WidgetProvider extends AppWidgetProvider {

    static final String NEW_BARCODE_URL = "https://bonuskaart.com/GetCard";

    private static final String GENERAL_PREF = "GENERAL_PREF";
    private static final String BARCODE = "BARCODE";
    private static final String BARCODE_TIME = "BARCODE_TIME";
    private static final String BARCODE_CASHE = "BARCODE_CASHE";
    private static final String WIDGET_UPDATE_GET_NEW_BARCODE = "WIDGET_UPDATE_GET_NEW_BARCODE";
    private static final String WIDGET_ID = "WIDGET_ID";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("Widget", "onUpdate");

        // Loop over all widget ids
        for (int appWidgetId : appWidgetIds) {
            update(context, appWidgetManager, appWidgetId, appWidgetManager.getAppWidgetOptions(appWidgetId));
        }
    }

    @Override
    public void onAppWidgetOptionsChanged (Context context, AppWidgetManager appWidgetManager, int widgetId, Bundle newOptions) {
        Log.d("Widget", "onAppWidgetOptionsChanged: " + widgetId);
        update(context, appWidgetManager, widgetId, newOptions);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if(AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            Log.d("Widget", "onReceive");

            // Get widgetID
            int id = AppWidgetManager.INVALID_APPWIDGET_ID;
            Bundle extras = intent.getExtras();
            if(extras != null){
                id = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,  AppWidgetManager.INVALID_APPWIDGET_ID);
            }
            final int widgetId = id;

            // If no id call supper
            if(widgetId == AppWidgetManager.INVALID_APPWIDGET_ID){
                super.onReceive(context, intent);
                return;
            }

            // Check if requesting new barcode
            if(extras != null && extras.getBoolean(WIDGET_UPDATE_GET_NEW_BARCODE, false)){
                getNewBarcode(context);
            }

            // Call onUpdate with necessary parameters
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] lastWidgetIds = {widgetId};
            onUpdate(context, appWidgetManager, lastWidgetIds);

        }else{
            super.onReceive(context, intent);
        }
    };

    private void update(Context context, final AppWidgetManager appWidgetManager, final int widgetId, final Bundle options){
        Log.d("Widget", "update: " + widgetId);

        // Get widget view
        final RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);

        // Set onclick intent for reload button
        widgetView.setOnClickPendingIntent(R.id.ReloadIV, getWidgetUpdateIntent(context, true, widgetId));
        Log.d("Widget", "update: Set pending intent with: " + widgetId);

        // Get shared preferences
        final SharedPreferences prefs = context.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);

        //Check if already has barcode
        String barcode = prefs.getString(BARCODE, null);
        long barcodeTime = prefs.getLong(BARCODE_TIME, 0);

        // Get current number of seconds
        long now = System.currentTimeMillis() / 1000;

        // Get new barcode if no barcode or expired
        if(barcode == null || now - (24 * 60 * 60) > barcodeTime){
            Log.d("Widget", "barcode: " + barcode + " created at: " + barcodeTime + " not valid");

            // Get new barcode
            barcode = getNewBarcode(context);
        }

        updateView(appWidgetManager, widgetId, options, widgetView, barcode);
    }

    private void updateView(AppWidgetManager appWidgetManager, int widgetId, Bundle options, RemoteViews widgetView, String barcode){
        Log.d("Widget", "updateView: " + widgetId);

        // Get widget width
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

        // Setup barcode bitmap generator
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            // Generate barcode bitmap
            Map<EncodeHintType, Object> hintMap = new HashMap<EncodeHintType, Object>();
            hintMap.put(EncodeHintType.MARGIN, new Integer(1));
            BitMatrix bitMatrix = multiFormatWriter.encode(barcode, BarcodeFormat.EAN_13, minWidth * 2,100, hintMap);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            // Load bitmap into imageview
            widgetView.setImageViewBitmap(R.id.barcodeIV, bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        // Set barcode text
        widgetView.setTextViewText(R.id.BarcodeTV, barcode);

        // Update widget view
        appWidgetManager.updateAppWidget(widgetId, widgetView);
    }

    private String getNewBarcode(Context context){
        Log.d("Widget", "getNewBarcode");

        // Get shared preferences
        SharedPreferences prefs = context.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);

        // Retrieve barcode cache
        Set<String> barcode_cache = prefs.getStringSet(BARCODE_CASHE, null);

        String new_barcode;

        if(barcode_cache != null && barcode_cache.size() > 0){
            Log.d("Widget", "getNewBarcode: barcode_cache found " + barcode_cache.size());
            if(barcode_cache.size() < 5){
                // Cache low, download new barcodes
                // TODO get new barcodes in new thread
            }

            // Get and remove first barcode from cache
            Iterator<String> iterator = barcode_cache.iterator();
            new_barcode = iterator.next();
            iterator.remove();

            Log.d("Widget", "getNewBarcode: barcode_cache found " + barcode_cache.size());
            // Save updated cache
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet(BARCODE_CASHE, barcode_cache);
            editor.apply();

        }else{
            Log.d("Widget", "getNewBarcode: No barcode_cache found");
            // No cache found download new barcodes
            //TODO get new barcodes in this thread
            new_barcode = "000000000000";
        }

        // Save and update widget
        saveBarcode(prefs, new_barcode);

        return new_barcode;
    }

    private void saveBarcode(SharedPreferences prefs, String barcode){
        Log.d("Widget", "saveBarcode: " + barcode);

        // Get barcode creation time
        long barcodeTime = System.currentTimeMillis() / 1000;

        // Get shared preferences editor
        SharedPreferences.Editor editor = prefs.edit();

        // Save new barcode and generation time
        editor.putString(BARCODE, barcode);
        editor.putLong(BARCODE_TIME, barcodeTime);
        editor.apply();
        Log.d("Widget", "saved new barcode: " + barcode + " created at: " + barcodeTime);
    }


    protected PendingIntent getWidgetUpdateIntent(Context context, Boolean getNewBarcode, int widgetID) {
        // Build widget update pendingIntent
        Intent intent = new Intent(context, getClass());
        intent.putExtra(WIDGET_UPDATE_GET_NEW_BARCODE, getNewBarcode);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}
