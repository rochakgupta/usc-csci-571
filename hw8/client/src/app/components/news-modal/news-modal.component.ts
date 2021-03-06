import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { NewsItem } from 'src/app/models/news-item';

@Component({
  selector: 'app-news-modal',
  templateUrl: './news-modal.component.html',
  styleUrls: ['./news-modal.component.scss']
})
export class NewsModalComponent implements OnInit {
  @Input() item: NewsItem;

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
  }

  close(): void {
    this.activeModal.close(null);
  }

  encode(value: string): string {
    return encodeURIComponent(value);
  }

  getTwitterUrl(): string {
    return `https://twitter.com/intent/tweet?text=${this.encode(this.item.title)}&url=${this.encode(this.item.url)}`;
  }

  getFacebookUrl(): string {
    return `https://www.facebook.com/sharer/sharer.php?u=${this.encode(this.item.url)}&src=sdkpreparse`;
  }
}
