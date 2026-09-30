import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { QuotationService } from '../../../core/services/quotation.service';
import { NotificationService } from '../../../core/services/notification.service';
import { QuoteResponse } from '../../../core/models/quotation.model';

@Component({
  selector: 'app-calculate-quote',
  templateUrl: './calculate-quote.component.html',
  styleUrls: ['./calculate-quote.component.scss']
})
export class CalculateQuoteComponent implements OnInit {
  quoteIdInput: string = '';
  selectedQuote: QuoteResponse | null = null;
  allQuotes: QuoteResponse[] = [];
  draftQuotes: QuoteResponse[] = [];
  isLoading = false;
  isCalculating = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quotationService: QuotationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      const qId = params['quoteId'];
      if (qId) {
        this.quoteIdInput = qId;
        this.loadQuotesAndSelect(qId);
      } else {
        // Navigating via left sidebar: start fresh and clean, do NOT auto-select
        this.quoteIdInput = '';
        this.selectedQuote = null;
        this.loadDraftQuotes();
      }
    });
  }

  loadDraftQuotes(): void {
    this.isLoading = true;
    this.quotationService.getAllQuotes().subscribe({
      next: (quotes) => {
        this.isLoading = false;
        this.allQuotes = quotes || [];
        this.draftQuotes = this.allQuotes.filter((q) => q.status === 'DRAFT');
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  loadQuotesAndSelect(targetQuoteId: string): void {
    this.isLoading = true;
    this.quotationService.getAllQuotes().subscribe({
      next: (quotes) => {
        this.allQuotes = quotes || [];
        // Ensure dropdown includes drafts and the specified target quote
        this.draftQuotes = this.allQuotes.filter((q) => q.status === 'DRAFT' || q.quoteId === targetQuoteId);
        this.fetchQuote(targetQuoteId);
      },
      error: () => {
        this.fetchQuote(targetQuoteId);
      }
    });
  }

  fetchQuote(quoteId: string): void {
    if (!quoteId) return;
    this.isLoading = true;
    this.quotationService.getQuote(quoteId).subscribe({
      next: (quote) => {
        this.isLoading = false;
        this.selectedQuote = quote;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  onSelectQuote(quoteId: string): void {
    if (!quoteId) return;
    this.quoteIdInput = quoteId;
    this.fetchQuote(quoteId);
  }

  calculatePremium(): void {
    if (!this.selectedQuote || this.isCalculating) return;
    this.isCalculating = true;

    this.quotationService.calculatePremium(this.selectedQuote.quoteId).subscribe({
      next: (updatedQuote) => {
        this.isCalculating = false;
        this.selectedQuote = updatedQuote;
        this.notificationService.success(`Premium calculated successfully! Total: $${updatedQuote.totalPremium}`);
      },
      error: () => {
        this.isCalculating = false;
      }
    });
  }

  goToDecision(): void {
    if (this.selectedQuote) {
      this.router.navigate(['/quotes/decision'], { queryParams: { quoteId: this.selectedQuote.quoteId } });
    }
  }

  goToAllQuotes(): void {
    this.router.navigate(['/quotes']);
  }
}
