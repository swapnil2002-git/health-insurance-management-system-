import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { CustomerResponse } from '../models/customer.model';

const STORAGE_KEY = 'hims_active_customer';

@Injectable({
  providedIn: 'root'
})
export class CustomerContextService {
  private activeCustomerSubject: BehaviorSubject<CustomerResponse | null>;
  public activeCustomer$: Observable<CustomerResponse | null>;

  constructor() {
    let initial: CustomerResponse | null = null;
    try {
      const saved = sessionStorage.getItem(STORAGE_KEY);
      if (saved) {
        initial = JSON.parse(saved);
      }
    } catch {
      initial = null;
    }
    this.activeCustomerSubject = new BehaviorSubject<CustomerResponse | null>(initial);
    this.activeCustomer$ = this.activeCustomerSubject.asObservable();
  }

  public setActiveCustomer(customer: CustomerResponse | null): void {
    if (customer) {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(customer));
    } else {
      sessionStorage.removeItem(STORAGE_KEY);
    }
    this.activeCustomerSubject.next(customer);
  }

  public getActiveCustomer(): CustomerResponse | null {
    return this.activeCustomerSubject.value;
  }

  public getActiveCustomerId(): string | null {
    return this.activeCustomerSubject.value?.customerId || null;
  }
}
