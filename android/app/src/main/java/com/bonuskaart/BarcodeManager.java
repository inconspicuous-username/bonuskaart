package com.bonuskaart;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;



public class BarcodeManager {

    public interface OnBarcodereadyListener{
        void onBarcodeReady(String barcode);
    }

    static String MAIN_URL = "https://bonuskaart.com/";
    static String NEW_BARCODE_URL = "https://bonuskaart.com/GetCard";

    private final String GENERAL_PREF = "GENERAL_PREF";
    private final String BARCODE = "BARCODE";
    private final String BARCODE_TIME = "BARCODE_TIME";
    private final String BARCODE_CASHE = "BARCODE_CASHE";

    private Context context;
    private OnBarcodereadyListener onBarcodereadyListener;
    private static boolean isDownloading;

    // DEBUG
    List<String> barcode_web_temp = new ArrayList<String>();

    public BarcodeManager(Context c, OnBarcodereadyListener b){
        context = c;
        onBarcodereadyListener = b;

        isDownloading = false;

        // DEBUG
        barcode_web_temp.add("2620683455263");
        barcode_web_temp.add("2620683455287");
        barcode_web_temp.add("2620683481125");
        barcode_web_temp.add("2620683481149");
        barcode_web_temp.add("2620683481163");
        barcode_web_temp.add("2620683593859");
        barcode_web_temp.add("2620683593927");
        barcode_web_temp.add("2620683593958");
        barcode_web_temp.add("2620683594009");
        barcode_web_temp.add("2620683594023");
        barcode_web_temp.add("2620683621859");
        barcode_web_temp.add("2620683621897");
        barcode_web_temp.add("2620683621910");
        barcode_web_temp.add("2620683621941");
        barcode_web_temp.add("2620683622207");
        barcode_web_temp.add("2620683622238");
        barcode_web_temp.add("2620683648108");
        barcode_web_temp.add("2620683648115");
        barcode_web_temp.add("2620683648122");
        barcode_web_temp.add("2620683648177");
        barcode_web_temp.add("2620683730780");
        barcode_web_temp.add("2620683730810");
        barcode_web_temp.add("2620683730896");
        barcode_web_temp.add("2620683730940");
        barcode_web_temp.add("2620683731039");
        barcode_web_temp.add("2620683428243");
        barcode_web_temp.add("2620683428281");
        barcode_web_temp.add("2620683428335");
        barcode_web_temp.add("2620683428359");
        barcode_web_temp.add("2620683428366");
        barcode_web_temp.add("2620683428403");
        barcode_web_temp.add("2620683428434");
        barcode_web_temp.add("2620683428465");
        barcode_web_temp.add("2620683428489");
        barcode_web_temp.add("2620683429004");
        barcode_web_temp.add("2620683429035");
        barcode_web_temp.add("2620683429066");
        barcode_web_temp.add("2620683400843");
        barcode_web_temp.add("2620683400836");
        barcode_web_temp.add("2620683400812");
        barcode_web_temp.add("2620683400799");
        barcode_web_temp.add("2620683400775");
        barcode_web_temp.add("2620683400744");
        barcode_web_temp.add("2620683400713");
        barcode_web_temp.add("2620683400737");
        //END DEBUG

    }

    public void getBarcode(){
        // Loads current or gets new barcode from cache

        // Get shared preferences
        SharedPreferences prefs = context.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);

        //Check if already has barcode
        String barcode = prefs.getString(BARCODE, null);
        long barcodeTime = prefs.getLong(BARCODE_TIME, 0);

        // Get current number of seconds
        long now = System.currentTimeMillis() / 1000;

        // Get new barcode if no barcode or expired
        if(barcode == null || now - (24 * 60 * 60) > barcodeTime) {
            // Get new barcode from cache
            getNewBarcode();

        }else{
            Log.d("BarcodeManager", "barcode: " + barcode + " created at: " + barcodeTime + " is still valid");
            onBarcodereadyListener.onBarcodeReady(barcode);
        }
    }

    public void getNewBarcode(){
        // Gets new barcode from cache

        // Get shared preferences
        SharedPreferences prefs = context.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);

        // Retrieve barcode cache
        Set<String> barcode_cache = prefs.getStringSet(BARCODE_CASHE, null);

        if(barcode_cache != null && barcode_cache.size() > 0){
            Log.d("BarcodeManager", "getNewBarcode: barcode_cache found " + barcode_cache.size());
            if(barcode_cache.size() < 5){
                // Cache low, download new barcodes
                downloadBarcodes(false);
            }

            // Get and remove first barcode from cache
            Iterator<String> iterator = barcode_cache.iterator();
            String barcode = iterator.next();
            iterator.remove();

            // Save barcode
            saveBarcode(prefs, barcode);

            // Save updated cache
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet(BARCODE_CASHE, barcode_cache);
            editor.apply();

            onBarcodereadyListener.onBarcodeReady(barcode);
        }else{
            // No cache found download new barcodes
            downloadBarcodes(true);
        }
    }

    private void downloadBarcodes(final Boolean refresh){
        Log.d("BarcodeManager", "downloadBarcodes: refresh " + refresh);

        // Check if already downloading
        if(isDownloading){
            return;
        }
        isDownloading = true;

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        // Get shared preferences
        final SharedPreferences prefs = context.getSharedPreferences(GENERAL_PREF, MODE_PRIVATE);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, NEW_BARCODE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Reset downloading flag
                        isDownloading = false;

                        // Get cache
                        Set<String> barcode_cache = prefs.getStringSet(BARCODE_CASHE, null);

                        //TODO replace with parsing actual response
                        // Parse cache set from response and append to cache
                        Random r = new Random();
                        int number = 5;
                        for(int counter = 0; counter<number; counter++){
                            int index = r.nextInt(barcode_web_temp.size());
                            barcode_cache.add(barcode_web_temp.get(index));
                            //barcode_web_temp.remove(index);
                        }

                        if(refresh){
                            // Get and remove first barcode from cache
                            Iterator<String> iterator = barcode_cache.iterator();
                            String barcode = iterator.next();
                            iterator.remove();

                            // Save barcode
                            saveBarcode(prefs, barcode);

                            // call onbarcode ready
                            onBarcodereadyListener.onBarcodeReady(barcode);
                        }

                        // Save updated cache
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putStringSet(BARCODE_CASHE, barcode_cache);
                        editor.apply();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("BarcodeManager", "downloadBarcodes onErrorResponse");

                        // Reset downloading flag
                        isDownloading = false;
                    }
                }
        );

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void saveBarcode(SharedPreferences prefs, String barcode){
        Log.d("BarcodeManager", "saveBarcode: " + barcode);

        // Get barcode creation time
        long barcodeTime = System.currentTimeMillis() / 1000;

        // Get shared preferences editor
        SharedPreferences.Editor editor = prefs.edit();

        // Save new barcode and generation time
        editor.putString(BARCODE, barcode);
        editor.putLong(BARCODE_TIME, barcodeTime);
        editor.apply();
    }
}
