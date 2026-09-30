import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { QuotationService } from '../../../core/services/quotation.service';
import { QuoteResponse, QuoteStatus } from '../../../core/models/quotation.model';

@Component({
  selector: 'app-quote-list',
  templateUrl: './quote-list.component.html',
  styleUrls: ['./quote-list.component.scss']
})
export class QuoteListComponent implements OnInit {
  quotes: QuoteResponse[] = [];
  filteredQuotes: QuoteResponse[] = [];
  searchQuery: string = '';
  selectedStatus: string = 'ALL';
  isLoading = false;

  statusFilters = ['ALL', 'DRAFT', 'CALCULATED', 'ACCEPTED', 'REJECTED', 'EXPIRED'];

  constructor(
    private quotationService: QuotationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadQuotes();
  }

  loadQuotes(): void {
    this.isLoading = true;
    this.quotationService.getAllQuotes().subscribe({
      next: (quotes) => {
        this.isLoading = false;
        this.quotes = quotes || [];
        this.applyFilter();
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  onStatusFilter(status: string): void {
    this.selectedStatus = status;
    this.applyFilter();
  }

  applyFilter(): void {
    const q = this.searchQuery.trim().toLowerCase();
    this.filteredQuotes = this.quotes.filter((quote) => {
      const matchesStatus = this.selectedStatus === 'ALL' || quote.status === this.selectedStatus;
      const matchesQuery =
        !q ||
        quote.quoteNumber?.toLowerCase().includes(q) ||
        quote.quoteId?.toLowerCase().includes(q) ||
        quote.customerId?.toLowerCase().includes(q) ||
        quote.planId?.toLowerCase().includes(q);
      return matchesStatus && matchesQuery;
    });
  }

  goToCreate(): void {
    this.router.navigate(['/quotes/create']);
  }

  viewQuote(quoteId: string): void {
    this.router.navigate(['/quotes/view', quoteId]);
  }

  calculateQuote(quoteId: string): void {
    this.router.navigate(['/quotes/calculate'], { queryParams: { quoteId } });
  }

  decideQuote(quoteId: string): void {
    this.router.navigate(['/quotes/decision'], { queryParams: { quoteId } });
  }
}
