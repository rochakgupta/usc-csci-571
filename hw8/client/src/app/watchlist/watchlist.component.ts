import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { WatchlistService } from '../watchlist.service';
import { WatchlistItem } from '../watchlist-item';
import { StockService } from '../stock.service';
import { ApiStatus } from '../api-status';
import { AlertManager } from '../alert-manager';
import { Alert } from '../alert';

@Component({
  selector: 'app-watchlist',
  templateUrl: './watchlist.component.html',
  styleUrls: ['./watchlist.component.scss']
})
export class WatchlistComponent implements OnInit {
  successWatchlist: WatchlistItem[] = [];
  lastPrices = {};
  alertManager: AlertManager = new AlertManager();
  apiStatus = new ApiStatus();

  constructor(private stockService: StockService, public watchlistService: WatchlistService, private router: Router) { }

  ngOnInit(): void {
    if (this.watchlistService.isWatchlistEmpty()) {
      this.showEmptyWatchlistAlert();
      this.apiStatus.success();
    } else {
      this.getWatchlistData();
    }
  }

  private getWatchlistData(): void {
    const watchlist = this.watchlistService.getWatchlist();
    forkJoin(watchlist.map(item =>
      this.stockService.getLastPrice(item.ticker)
        .pipe(
          tap(() => {
            if (this.apiStatus.isInitial()) {
              this.apiStatus.loading();
            }
          }),
          catchError(_ => of(null))
        )
    )).subscribe(lastPrices => {
      const successWatchlist: WatchlistItem[] = [];
      const errorTickers: string[] = [];
      for (let i = 0; i < watchlist.length; i++) {
        const item = watchlist[i];
        const ticker = item.ticker;
        const lastPrice = lastPrices[i];
        if (lastPrice === null) {
          errorTickers.push(ticker);
        } else {
          this.lastPrices[ticker] = lastPrice;
          successWatchlist.push(item);
        }
      }
      let message = null;
      if (errorTickers.length > 0) {
        message = `Error occurred while fetching last prices of stock(s): ${errorTickers.join(', ')}.`;
        this.alertManager.addDangerAlert(message, false);
      }
      if (successWatchlist.length === 0) {
        this.apiStatus.error(message);
      } else {
        this.successWatchlist = successWatchlist;
        this.apiStatus.success();
      }
    });
  }

  removeFromWatchlist(ticker: string): void {
    this.watchlistService.remove(ticker);
    this.successWatchlist = this.watchlistService.getFilteredWatchlist(this.successWatchlist.map(item => item.ticker));
    if (this.watchlistService.isWatchlistEmpty()) {
      this.showEmptyWatchlistAlert();
    }
  }

  navigateToDetails(ticker): void {
    this.router.navigate(['/details', ticker]);
  }

  removeAlert(alert: Alert): void {
    this.alertManager.removeAlert(alert);
  }

  showEmptyWatchlistAlert(): void {
    this.alertManager.addWarningAlert('Currently you don\'t have any stock in your watchlist.', false);
  }
}
