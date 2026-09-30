import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-document-list',
  templateUrl: './document-list.component.html',
  styleUrls: ['./document-list.component.scss']
})
export class DocumentListComponent implements OnInit {
  quickSearchId = '';
  searchType: 'DOCUMENT' | 'REFERENCE' = 'DOCUMENT';

  constructor(private router: Router) {}

  ngOnInit(): void {}

  onQuickSearch(): void {
    if (!this.quickSearchId.trim()) return;

    const id = this.quickSearchId.trim();
    if (this.searchType === 'DOCUMENT') {
      this.router.navigate(['/documents/view', id]);
    } else {
      this.router.navigate(['/documents/reference', id]);
    }
  }

  goToUpload(type?: string): void {
    const queryParams = type ? { type } : undefined;
    this.router.navigate(['/documents/upload'], { queryParams });
  }

  goToView(): void {
    this.router.navigate(['/documents/view']);
  }

  goToReference(): void {
    this.router.navigate(['/documents/reference']);
  }
}
