package com.rochakgupta.stocktrading.detail.portfolio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.Button;
import android.widget.TextView;

import com.rochakgupta.stocktrading.R;
import com.rochakgupta.stocktrading.common.Formatter;
import com.rochakgupta.stocktrading.common.Storage;
import com.rochakgupta.stocktrading.common.ToastManager;
import com.rochakgupta.stocktrading.detail.common.Info;
import com.rochakgupta.stocktrading.main.section.portfolio.PortfolioItem;

import java.util.List;

public class PortfolioManager implements TradeDialog.OnActionHandler {
    private final TextView stocksView;

    private final TextView marketPriceView;

    private final TradeDialog tradeDialog;

    private final TradeSuccessDialog tradeSuccessDialog;

    private final ToastManager toastManager;

    private final Info info;

    public PortfolioManager(Activity activity, ToastManager toastManager, Info info) {
        stocksView = activity.findViewById(R.id.detail_tv_portfolio_stocks);
        marketPriceView = activity.findViewById(R.id.detail_tv_portfolio_market_value);
        tradeDialog = new TradeDialog(activity, info, this);
        Button tradeButton = activity.findViewById(R.id.detail_b_portfolio_trade);
        tradeButton.setOnClickListener(v -> tradeDialog.show());
        tradeSuccessDialog = new TradeSuccessDialog(activity, info.getTicker());
        this.toastManager = toastManager;
        this.info = info;
    }

    @SuppressLint("SetTextI18n")
    public void display() {
        String stocksText = null;
        String marketPriceText = null;
        String ticker = info.getTicker();
        if (Storage.isPresentInPortfolio(ticker)) {
            PortfolioItem item = Storage.getPortfolioItem(ticker);
            stocksText = String.format("Shares owned: %s", Formatter.getQuantityString(item.getStocks(), 4));
            item.setLastPrice(info.getLastPrice());
            marketPriceText = String
                    .format("Market Value: %s", Formatter.getPriceString(item.getWorth()));
        } else {
            stocksText = String.format("You have 0 shares of %s.", ticker);
            marketPriceText = "Start trading!";
        }
        stocksView.setText(stocksText);
        marketPriceView.setText(marketPriceText);
    }

    @Override
    public void onStockBuy(Integer stocks) {
        if (stocks == null) {
            toastManager.show("Please enter valid amount");
        } else if (stocks == 0) {
            toastManager.show("Cannot buy less than 0 shares");
        } else {
            double balance = Storage.getBalance();
            double stocksPrice = getStocksPrice(stocks, info.getLastPrice());
            if (stocksPrice > balance) {
                toastManager.show("Not enough money to buy");
            } else {
                buyStocks(stocks);
                onTradeSuccess(TradeType.BUY, stocks);
            }
        }
    }

    private void buyStocks(Integer stocks) {
        String ticker = info.getTicker();
        double lastPrice = info.getLastPrice();

        PortfolioItem item;
        if (Storage.isPresentInPortfolio(ticker)) {
            List<PortfolioItem> items = Storage.getPortfolio();
            item = findItem(items, ticker);
            item.buy(stocks, lastPrice);
            Storage.updatePortfolio(items);
        } else {
            item = PortfolioItem.with(ticker, stocks, lastPrice);
            Storage.addToPortfolio(item);
        }

        Storage.updateBalance(Storage.getBalance() - getStocksPrice(stocks, lastPrice));
    }

    @Override
    public void onStockSell(Integer stocks) {
        if (stocks == null) {
            toastManager.show("Please enter valid amount");
        } else if (stocks == 0) {
            toastManager.show("Cannot sell less than 0 shares");
        } else {
            String ticker = info.getTicker();
            int portfolioStocks;
            if (Storage.isPresentInPortfolio(ticker)) {
                PortfolioItem item = Storage.getPortfolioItem(ticker);
                portfolioStocks = item.getStocks();
            } else {
                portfolioStocks = 0;
            }
            if (stocks > portfolioStocks) {
                toastManager.show("Not enough shares to sell");
            } else {
                sellStocks(stocks);
                onTradeSuccess(TradeType.SELL, stocks);
            }
        }
    }

    private void sellStocks(Integer stocks) {
        String ticker = info.getTicker();
        double lastPrice = info.getLastPrice();

        List<PortfolioItem> items = Storage.getPortfolio();
        PortfolioItem item = findItem(items, ticker);
        if (item.sell(stocks)) {
            items.remove(item);
        }
        Storage.updatePortfolio(items);

        Storage.updateBalance(Storage.getBalance() + getStocksPrice(stocks, lastPrice));
    }

    private PortfolioItem findItem(List<PortfolioItem> items, String ticker) {
        return items.stream().filter(item -> item.getTicker().equals(ticker)).findAny().orElse(null);
    }

    private void onTradeSuccess(TradeType tradeType, int stocks) {
        tradeDialog.dismiss();
        display();
        tradeSuccessDialog.show(tradeType, stocks);
    }

    private double getStocksPrice(int stocks, double pricePerStock) {
        return stocks * pricePerStock;
    }
}
