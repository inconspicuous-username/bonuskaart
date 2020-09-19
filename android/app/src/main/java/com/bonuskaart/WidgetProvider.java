package com.bonuskaart;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;

public class WidgetProvider extends AppWidgetProvider {

    private static final String WIDGET_UPDATE_GET_NEW_BARCODE = "WIDGET_UPDATE_GET_NEW_BARCODE";

    private BarcodeManager barcodeManager;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("Widget", "onUpdate");

        barcodeManager.getBarcode();
    }

    @Override
    public void onAppWidgetOptionsChanged (Context context, AppWidgetManager appWidgetManager, int widgetId, Bundle newOptions) {
        Log.d("Widget", "onAppWidgetOptionsChanged: " + widgetId);

        barcodeManager.getBarcode();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if(AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {

            // Init barcode manager once
            if(barcodeManager == null){
                barcodeManager = new BarcodeManager(context, new BarcodeManager.OnBarcodereadyListener() {
                    @Override
                    public void onBarcodeReady(String barcode) {
                        Log.d("Widget", "onBarcodeReady " + barcode);

                        // Loop over all widget ids and update
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetI‌​ds(new ComponentName(context, WidgetProvider.class));
                        for (int appWidgetId : appWidgetIds) {
                            update(context, appWidgetManager, appWidgetId, appWidgetManager.getAppWidgetOptions(appWidgetId), barcode);
                        }
                    }
                });
            }

            // Get extras
            Bundle extras = intent.getExtras();

            // Check if requesting new barcode
            if(extras != null && extras.getBoolean(WIDGET_UPDATE_GET_NEW_BARCODE, false)){
                Log.d("Widget", "onReceive: get_new_barcode");

                // Get new barcode
                barcodeManager.getNewBarcode();
            }else{
                Log.d("Widget", "onReceive: normal");
                // Get barcode
                barcodeManager.getBarcode();
            }

        }else{
            super.onReceive(context, intent);
        }
    };

    private void update(Context context, final AppWidgetManager appWidgetManager, final int widgetId, final Bundle options, String barcode){
        Log.d("Widget", "update: " + widgetId);

        // Get widget view
        final RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);

        // Set onclick intent for reload button
        widgetView.setOnClickPendingIntent(R.id.ReloadIV, getWidgetUpdateIntent(context, true, widgetId));

        // Update the widget view
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
