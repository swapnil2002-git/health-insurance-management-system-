import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CustomerRequest,
  CustomerResponse,
  MemberRequest,
  MemberResponse,
  AddressRequest,
  AddressResponse,
  ContactRequest,
  ContactResponse,
  BeneficiaryRequest,
  BeneficiaryResponse,
  NomineeRequest,
  NomineeResponse
} from '../models/customer.model';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private readonly baseUrl = '/api/customers';

  constructor(private http: HttpClient) {}

  // 1. Personal Information (Customer)
  public createCustomer(request: CustomerRequest): Observable<CustomerResponse> {
    return this.http.post<CustomerResponse>(this.baseUrl, request);
  }

  public getCustomer(customerId: string): Observable<CustomerResponse> {
    return this.http.get<CustomerResponse>(`${this.baseUrl}/${encodeURIComponent(customerId)}`);
  }

  public updateCustomer(customerId: string, request: CustomerRequest): Observable<CustomerResponse> {
    return this.http.put<CustomerResponse>(`${this.baseUrl}/${encodeURIComponent(customerId)}`, request);
  }

  // 2. Members
  public addMember(customerId: string, request: MemberRequest): Observable<MemberResponse> {
    return this.http.post<MemberResponse>(`${this.baseUrl}/${encodeURIComponent(customerId)}/members`, request);
  }

  public getMembers(customerId: string): Observable<MemberResponse[]> {
    return this.http.get<MemberResponse[]>(`${this.baseUrl}/${encodeURIComponent(customerId)}/members`);
  }

  public updateMember(customerId: string, memberId: string, request: MemberRequest): Observable<MemberResponse> {
    return this.http.put<MemberResponse>(
      `${this.baseUrl}/${encodeURIComponent(customerId)}/members/${encodeURIComponent(memberId)}`,
      request
    );
  }

  public removeMember(customerId: string, memberId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/${encodeURIComponent(customerId)}/members/${encodeURIComponent(memberId)}`
    );
  }

  // 3. Addresses
  public addAddress(customerId: string, request: AddressRequest): Observable<AddressResponse> {
    return this.http.post<AddressResponse>(`${this.baseUrl}/${encodeURIComponent(customerId)}/addresses`, request);
  }

  public getAddresses(customerId: string): Observable<AddressResponse[]> {
    return this.http.get<AddressResponse[]>(`${this.baseUrl}/${encodeURIComponent(customerId)}/addresses`);
  }

  public updateAddress(customerId: string, addressId: string, request: AddressRequest): Observable<AddressResponse> {
    return this.http.put<AddressResponse>(
      `${this.baseUrl}/${encodeURIComponent(customerId)}/addresses/${encodeURIComponent(addressId)}`,
      request
    );
  }

  public removeAddress(customerId: string, addressId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/${encodeURIComponent(customerId)}/addresses/${encodeURIComponent(addressId)}`
    );
  }

  // 4. Contacts
  public addContact(customerId: string, request: ContactRequest): Observable<ContactResponse> {
    return this.http.post<ContactResponse>(`${this.baseUrl}/${encodeURIComponent(customerId)}/contacts`, request);
  }

  public getContacts(customerId: string): Observable<ContactResponse[]> {
    return this.http.get<ContactResponse[]>(`${this.baseUrl}/${encodeURIComponent(customerId)}/contacts`);
  }

  public updateContact(customerId: string, contactId: string, request: ContactRequest): Observable<ContactResponse> {
    return this.http.put<ContactResponse>(
      `${this.baseUrl}/${encodeURIComponent(customerId)}/contacts/${encodeURIComponent(contactId)}`,
      request
    );
  }

  public removeContact(customerId: string, contactId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/${encodeURIComponent(customerId)}/contacts/${encodeURIComponent(contactId)}`
    );
  }

  // 5. Beneficiaries
  public addBeneficiary(customerId: string, request: BeneficiaryRequest): Observable<BeneficiaryResponse> {
    return this.http.post<BeneficiaryResponse>(`${this.baseUrl}/${encodeURIComponent(customerId)}/beneficiaries`, request);
  }

  public getBeneficiaries(customerId: string): Observable<BeneficiaryResponse[]> {
    return this.http.get<BeneficiaryResponse[]>(`${this.baseUrl}/${encodeURIComponent(customerId)}/beneficiaries`);
  }

  public updateBeneficiary(
    customerId: string,
    beneficiaryId: string,
    request: BeneficiaryRequest
  ): Observable<BeneficiaryResponse> {
    return this.http.put<BeneficiaryResponse>(
      `${this.baseUrl}/${encodeURIComponent(customerId)}/beneficiaries/${encodeURIComponent(beneficiaryId)}`,
      request
    );
  }

  public removeBeneficiary(customerId: string, beneficiaryId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/${encodeURIComponent(customerId)}/beneficiaries/${encodeURIComponent(beneficiaryId)}`
    );
  }

  // 6. Nominees
  public addNominee(customerId: string, request: NomineeRequest): Observable<NomineeResponse> {
    return this.http.post<NomineeResponse>(`${this.baseUrl}/${encodeURIComponent(customerId)}/nominees`, request);
  }

  public getNominees(customerId: string): Observable<NomineeResponse[]> {
    return this.http.get<NomineeResponse[]>(`${this.baseUrl}/${encodeURIComponent(customerId)}/nominees`);
  }

  public updateNominee(customerId: string, nomineeId: string, request: NomineeRequest): Observable<NomineeResponse> {
    return this.http.put<NomineeResponse>(
      `${this.baseUrl}/${encodeURIComponent(customerId)}/nominees/${encodeURIComponent(nomineeId)}`,
      request
    );
  }

  public removeNominee(customerId: string, nomineeId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/${encodeURIComponent(customerId)}/nominees/${encodeURIComponent(nomineeId)}`
    );
  }
}
