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
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class WidgetProvider extends AppWidgetProvider {

    static final String NEW_BARCODE_URL = "https://bonuskaart.com/GetCard";

    private static final String GENERAL_PREF = "GENERAL_PREF";
    private static final String BARCODE = "BARCODE";
    private static final String BARCODE_TIME = "BARCODE_TIME";
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
                getNewBarcode(context,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String barcode) {
                                // Get barcode creation time
                                long barcodeTime = System.currentTimeMillis() / 1000;

                                // Get shared preferences editor
                                final SharedPreferences prefs = context.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();

                                // Save new cookie and generation time
                                editor.putString(BARCODE, barcode);
                                editor.putLong(BARCODE_TIME, barcodeTime);
                                editor.apply();
                                Log.d("Widget", "onReceive new barcode: " + barcode + " created at: " + barcodeTime);

                                // Call onUpdate with necessary parameters
                                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                                int[] lastWidgetIds = {widgetId};
                                onUpdate(context, appWidgetManager, lastWidgetIds);
                            }
                        }
                );
            }else{
                // Call onUpdate with necessary parameters
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                int[] lastWidgetIds = {widgetId};
                onUpdate(context, appWidgetManager, lastWidgetIds);
            }
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

            getNewBarcode(context,
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
                            Log.d("Widget", "update new barcode: " + barcode + " created at: " + barcodeTime);

                            // Update widget view
                            updateView(appWidgetManager, widgetId, options, widgetView, barcode);
                        }
                    }
            );
        }else{
            Log.d("Widget", "Barcode: " + barcode + " created at: " + barcodeTime + " still valid");

            // Update widget view
            updateView(appWidgetManager, widgetId, options, widgetView, barcode);
        }
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

    private void getNewBarcode(Context context, Response.Listener<String> callback){
        Log.d("Widget", "GetNewBarcode");

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, NEW_BARCODE_URL, callback, null);

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
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
