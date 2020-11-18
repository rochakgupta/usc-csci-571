package com.rochakgupta.stocktrading;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rochakgupta.stocktrading.api.Api;
import com.rochakgupta.stocktrading.api.ApiStatus;
import com.rochakgupta.stocktrading.detail.ChartItem;
import com.rochakgupta.stocktrading.detail.Detail;
import com.rochakgupta.stocktrading.detail.Everything;
import com.rochakgupta.stocktrading.detail.NewsItem;
import com.rochakgupta.stocktrading.detail.Summary;
import com.rochakgupta.stocktrading.gson.GsonUtils;
import com.rochakgupta.stocktrading.log.LoggingUtils;
import com.rochakgupta.stocktrading.main.favorites.FavoritesItem;
import com.rochakgupta.stocktrading.storage.Storage;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = DetailActivity.class.getSimpleName();

    private String ticker;

    private ConstraintLayout loadingLayout;
    private TextView errorView;

    private ApiStatus everythingFetchStatus;

    private Detail detail;

    private Summary summary;

    private NewsItem[] news;

    private ChartItem[] chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_StockTradingApp);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initializeActionBar();

        Intent intent = getIntent();
        if (!intent.hasExtra("ticker")) {
            throw new RuntimeException("DetailActivity needs ticker data in intent to run");
        }

        ticker = intent.getStringExtra("ticker");

        loadingLayout = findViewById(R.id.detail_cl_loading);
        errorView = findViewById(R.id.detail_tv_error);

        Storage.initialize(this);

        Api.initialize(this);

        everythingFetchStatus = new ApiStatus();
        everythingFetchStatus.loading();

        showLoadingLayout();

        startEverythingFetch();
    }

    private void initializeActionBar() {
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showLoadingLayout() {
        loadingLayout.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.INVISIBLE);
    }

    private void showErrorView() {
        loadingLayout.setVisibility(View.INVISIBLE);
        errorView.setVisibility(View.VISIBLE);
    }

    private void startEverythingFetch() {
        Api.makeEverythingFetchRequest(ticker, response -> {
            try {
                JSONObject jsonData = response.getJSONObject("data");
                LoggingUtils.logJSONObject(jsonData);
                Everything everything = GsonUtils.jsonToEverything(jsonData.toString());
                onEverythingFetchSuccess(everything);
            } catch (JSONException e) {
                e.printStackTrace();
                onEverythingFetchError();
            }
        }, error -> {
            LoggingUtils.logError(error);
            onEverythingFetchError();
        });
    }

    private void onEverythingFetchSuccess(Everything everything) {
        detail = everything.getDetail();
        summary = everything.getSummary();
        news = everything.getNews();
        chart = everything.getChart();
        showSuccessLayout();
        everythingFetchStatus.success();
    }

    private void onEverythingFetchError() {
        showErrorView();
        everythingFetchStatus.error();
    }

    private void showSuccessLayout() {
        loadingLayout.setVisibility(View.INVISIBLE);
        errorView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        Api.cancelRequests(Api.EVERYTHING_FETCH_REQUEST_TAG);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem item = menu.findItem(R.id.detail_action_favorite);
        int icon = getFavoriteIcon(Storage.isFavorite(ticker));
        item.setIcon(icon);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.detail_action_favorite) {
            onFavoriteClicked(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onFavoriteClicked(MenuItem item) {
        if (everythingFetchStatus.isLoading()) {
            Toast.makeText(this, "Still fetching data", Toast.LENGTH_SHORT).show();
        } else if (everythingFetchStatus.isError()) {
            Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
        } else {
            boolean isFavorite = Storage.isFavorite(ticker);
            int icon = getFavoriteIcon(!isFavorite);
            if (isFavorite) {
                Storage.removeFromFavorites(ticker);
                item.setIcon(icon);
            } else {
                Storage.addToFavorites(FavoritesItem.with(ticker, detail.getName(), detail.getLastPrice()));
                item.setIcon(icon);
            }
        }
    }

    private int getFavoriteIcon(boolean isFavorite) {
        return isFavorite ? R.drawable.ic_baseline_star_24 : R.drawable.ic_baseline_star_border_24;
    }
}