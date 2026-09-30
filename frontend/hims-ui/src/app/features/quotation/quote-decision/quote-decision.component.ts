import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { QuotationService } from '../../../core/services/quotation.service';
import { NotificationService } from '../../../core/services/notification.service';
import { QuoteResponse } from '../../../core/models/quotation.model';

export interface DecisionFeedbackModal {
  type: 'success' | 'warn' | 'error';
  decision: 'ACCEPTED' | 'REJECTED';
  title: string;
  message: string;
  quoteId: string;
  quoteNumber: string;
  customerId: string;
  totalPremium?: number | null;
}

@Component({
  selector: 'app-quote-decision',
  templateUrl: './quote-decision.component.html',
  styleUrls: ['./quote-decision.component.scss']
})
export class QuoteDecisionComponent implements OnInit {
  quoteIdInput: string = '';
  selectedQuote: QuoteResponse | null = null;
  allQuotes: QuoteResponse[] = [];
  calculatedQuotes: QuoteResponse[] = [];
  isLoading = false;
  isProcessing = false;
  modalData: DecisionFeedbackModal | null = null;
  copiedField: string | null = null;

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
        this.loadCalculatedQuotes();
      }
    });
  }

  loadCalculatedQuotes(): void {
    this.isLoading = true;
    this.quotationService.getAllQuotes().subscribe({
      next: (quotes) => {
        this.isLoading = false;
        this.allQuotes = quotes || [];
        this.calculatedQuotes = this.allQuotes.filter((q) => q.status === 'CALCULATED');
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
        // Ensure dropdown includes calculated quotes and the target quote
        this.calculatedQuotes = this.allQuotes.filter((q) => q.status === 'CALCULATED' || q.quoteId === targetQuoteId);
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

  acceptQuote(): void {
    if (!this.selectedQuote || this.isProcessing) return;
    this.isProcessing = true;

    this.quotationService.acceptQuote(this.selectedQuote.quoteId).subscribe({
      next: (updatedQuote) => {
        this.isProcessing = false;
        this.selectedQuote = updatedQuote;
        this.modalData = {
          type: 'success',
          decision: 'ACCEPTED',
          title: 'Quotation Accepted Successfully!',
          message: `Quotation ${updatedQuote.quoteNumber} has been officially accepted. Underwriting events have been dispatched and the case is ready for risk assessment.`,
          quoteId: updatedQuote.quoteId,
          quoteNumber: updatedQuote.quoteNumber,
          customerId: updatedQuote.customerId,
          totalPremium: updatedQuote.totalPremium
        };
        // Refresh calculated quotes list so accepted one is removed from pending dropdown
        this.loadCalculatedQuotes();
      },
      error: (err) => {
        this.isProcessing = false;
        this.modalData = {
          type: 'error',
          decision: 'ACCEPTED',
          title: 'Error Accepting Quotation',
          message: err?.error?.message || 'Failed to accept quotation. Please try again.',
          quoteId: this.selectedQuote!.quoteId,
          quoteNumber: this.selectedQuote!.quoteNumber,
          customerId: this.selectedQuote!.customerId
        };
      }
    });
  }

  rejectQuote(): void {
    if (!this.selectedQuote || this.isProcessing) return;
    this.isProcessing = true;

    this.quotationService.rejectQuote(this.selectedQuote.quoteId).subscribe({
      next: (updatedQuote) => {
        this.isProcessing = false;
        this.selectedQuote = updatedQuote;
        this.modalData = {
          type: 'warn',
          decision: 'REJECTED',
          title: 'Quotation Rejected',
          message: `Quotation ${updatedQuote.quoteNumber} has been rejected and closed.`,
          quoteId: updatedQuote.quoteId,
          quoteNumber: updatedQuote.quoteNumber,
          customerId: updatedQuote.customerId,
          totalPremium: updatedQuote.totalPremium
        };
        // Refresh calculated quotes list so rejected one is removed from pending dropdown
        this.loadCalculatedQuotes();
      },
      error: (err) => {
        this.isProcessing = false;
        this.modalData = {
          type: 'error',
          decision: 'REJECTED',
          title: 'Error Rejecting Quotation',
          message: err?.error?.message || 'Failed to reject quotation. Please try again.',
          quoteId: this.selectedQuote!.quoteId,
          quoteNumber: this.selectedQuote!.quoteNumber,
          customerId: this.selectedQuote!.customerId
        };
      }
    });
  }

  closeModal(): void {
    this.modalData = null;
    this.copiedField = null;
  }

  copyText(text?: string, fieldName: string = 'id'): void {
    if (!text) return;
    navigator.clipboard.writeText(text).then(() => {
      this.copiedField = fieldName;
      setTimeout(() => {
        if (this.copiedField === fieldName) {
          this.copiedField = null;
        }
      }, 2000);
    });
  }

  goToCalculate(): void {
    if (this.selectedQuote) {
      this.router.navigate(['/quotes/calculate'], { queryParams: { quoteId: this.selectedQuote.quoteId } });
    }
  }

  goToAllQuotes(): void {
    this.router.navigate(['/quotes']);
  }
}
