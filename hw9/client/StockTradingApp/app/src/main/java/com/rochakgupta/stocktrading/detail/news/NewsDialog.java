package com.rochakgupta.stocktrading.detail.news;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.rochakgupta.stocktrading.R;
import com.rochakgupta.stocktrading.detail.common.NewsItem;

public class NewsDialog {
    private final Context context;

    private final Dialog dialog;

    private final TextView titleView;

    private final ImageView imageView;

    private final OnActionHandler actionHandler;

    private NewsItem item;

    interface OnActionHandler {
        void onNewsShare(NewsItem item);
        void onNewsView(NewsItem item);
    }

    public NewsDialog(Context context, OnActionHandler actionHandler) {
        this.context = context;

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.news_dialog);

        titleView = dialog.findViewById(R.id.news_dialog_tv_title);
        imageView = dialog.findViewById(R.id.news_dialog_iv);

        this.actionHandler = actionHandler;

        initializeShareButton();
        initializeViewButton();
    }

    private void reset(NewsItem item) {
        this.item = item;
        initializeTitleText();
        initializeImageView();
    }

    public void show(NewsItem item) {
        reset(item);
        dialog.show();
    }

    private void initializeTitleText() {
        titleView.setText(item.getTitle());
    }

    private void initializeImageView() {
        ImageLoader.load(context, imageView, item.getUrlToImage());
    }

    private void initializeShareButton() {
        ImageButton shareButton = dialog.findViewById(R.id.news_dialog_b_share);
        shareButton.setOnClickListener(v -> this.actionHandler.onNewsShare(item));
    }

    public void initializeViewButton() {
        ImageButton viewButton = dialog.findViewById(R.id.news_dialog_b_view);
        viewButton.setOnClickListener(v -> this.actionHandler.onNewsView(item));
    }
}