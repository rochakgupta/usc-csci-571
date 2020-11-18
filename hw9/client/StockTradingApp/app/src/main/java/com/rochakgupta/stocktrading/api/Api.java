package com.rochakgupta.stocktrading.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.List;

public class Api {
    private static final String TAG = Api.class.getName();

    private static boolean initialized;
    private static RequestQueue requestQueue;

    public static final String LAST_PRICES_FETCH_REQUEST_TAG = "LAST_PRICES_FETCH_REQUEST_TAG";
    public static final String SEARCH_OPTIONS_FETCH_REQUEST_TAG = "SEARCH_OPTIONS_FETCH_REQUEST_TAG";
    public static final String EVERYTHING_FETCH_REQUEST_TAG = "DETAILS_FETCH_REQUEST_TAG";

    synchronized public static void initialize(Context context) {
        if (!initialized) {
            initialized = true;
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
    }

    public static void makeLastPricesFetchRequest(List<String> tickers, Response.Listener<JSONObject> listener,
                                                  Response.ErrorListener errorListener) {
        Log.d(TAG, "Fetching last prices");
        String url = (new UrlBuilder())
                .path("api/last-price")
                .addQueryParameter("tickers", String.join(",", tickers))
                .build();
        JsonObjectRequest request = buildRequest(url, listener, errorListener, LAST_PRICES_FETCH_REQUEST_TAG);
        addRequestToQueue(request);
    }

    public static void makeSearchOptionsFetchRequest(String query, Response.Listener<JSONObject> listener,
                                                     Response.ErrorListener errorListener) {
        Log.d(TAG, "Fetching search options");
        String url = (new UrlBuilder())
                .path("api/search")
                .addQueryParameter("query", query)
                .build();
        JsonObjectRequest request = buildRequest(url, listener, errorListener, SEARCH_OPTIONS_FETCH_REQUEST_TAG);
        addRequestToQueue(request);
    }

    public static void makeEverythingFetchRequest(String ticker, Response.Listener<JSONObject> listener,
                                                  Response.ErrorListener errorListener) {
        Log.d(TAG, "Fetching details");
        String url = (new UrlBuilder())
                .path("api/everything")
                .addQueryParameter("ticker", ticker)
                .build();
        JsonObjectRequest request = buildRequest(url, listener, errorListener, EVERYTHING_FETCH_REQUEST_TAG);
        addRequestToQueue(request);
    }

    private static JsonObjectRequest buildRequest(String url, Response.Listener<JSONObject> listener,
                                                  Response.ErrorListener errorListener, String tag) {
        Log.d(TAG, url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, listener, errorListener);
        request.setTag(tag);
        return request;
    }

    synchronized private static void addRequestToQueue(JsonObjectRequest request) {
        requestQueue.add(request);
    }

    synchronized public static void cancelRequests(String tag) {
        requestQueue.cancelAll(tag);
    }

    public static boolean isNotFoundError(VolleyError error) {
        return error.networkResponse != null && error.networkResponse.statusCode == 404;
    }
}