import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { QuotationService } from '../../../core/services/quotation.service';
import { NotificationService } from '../../../core/services/notification.service';
import { QuoteResponse } from '../../../core/models/quotation.model';

@Component({
  selector: 'app-quote-view',
  templateUrl: './quote-view.component.html',
  styleUrls: ['./quote-view.component.scss']
})
export class QuoteViewComponent implements OnInit {
  searchQuoteId: string = '';
  quote: QuoteResponse | null = null;
  isLoading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quotationService: QuotationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      if (params['quoteId']) {
        this.searchQuoteId = params['quoteId'];
        this.loadQuote(this.searchQuoteId);
      }
    });

    this.route.queryParams.subscribe((params) => {
      if (params['quoteId'] && !this.quote) {
        this.searchQuoteId = params['quoteId'];
        this.loadQuote(this.searchQuoteId);
      }
    });
  }

  loadQuote(quoteId: string): void {
    if (!quoteId.trim()) return;
    this.isLoading = true;
    this.quotationService.getQuote(quoteId.trim()).subscribe({
      next: (q) => {
        this.isLoading = false;
        this.quote = q;
      },
      error: () => {
        this.isLoading = false;
        this.quote = null;
      }
    });
  }

  onSearch(): void {
    if (this.searchQuoteId) {
      this.loadQuote(this.searchQuoteId);
    }
  }

  copyQuoteId(): void {
    if (this.quote) {
      navigator.clipboard.writeText(this.quote.quoteId);
      this.notificationService.success('Quote ID copied to clipboard!');
    }
  }

  goToCalculate(): void {
    if (this.quote) {
      this.router.navigate(['/quotes/calculate'], { queryParams: { quoteId: this.quote.quoteId } });
    }
  }

  goToDecision(): void {
    if (this.quote) {
      this.router.navigate(['/quotes/decision'], { queryParams: { quoteId: this.quote.quoteId } });
    }
  }

  goToAllQuotes(): void {
    this.router.navigate(['/quotes']);
  }
}
