import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ProviderRequest,
  ProviderResponse,
  ProviderStatus,
  ProviderAddressRequest,
  ProviderAddressResponse,
  ProviderNetworkRequest,
  ProviderNetworkResponse,
  ProviderNetworkMappingRequest,
  ProviderNetworkMappingResponse,
  ProviderNetworkEligibilityResponse
} from '../models/provider.model';

@Injectable({
  providedIn: 'root'
})
export class ProviderService {
  constructor(private http: HttpClient) {}

  // ================= Provider APIs =================
  createProvider(request: ProviderRequest): Observable<ProviderResponse> {
    return this.http.post<ProviderResponse>('/api/providers', request);
  }

  getProviderById(providerId: string): Observable<ProviderResponse> {
    return this.http.get<ProviderResponse>(`/api/providers/${providerId}`);
  }

  getAllProviders(status?: ProviderStatus): Observable<ProviderResponse[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<ProviderResponse[]>('/api/providers', { params });
  }

  updateProvider(providerId: string, request: ProviderRequest): Observable<ProviderResponse> {
    return this.http.put<ProviderResponse>(`/api/providers/${providerId}`, request);
  }

  updateProviderStatus(providerId: string, status: ProviderStatus): Observable<ProviderResponse> {
    let params = new HttpParams().set('status', status);
    return this.http.patch<ProviderResponse>(`/api/providers/${providerId}/status`, null, { params });
  }

  addAddress(providerId: string, request: ProviderAddressRequest): Observable<ProviderAddressResponse> {
    return this.http.post<ProviderAddressResponse>(`/api/providers/${providerId}/addresses`, request);
  }

  getAddresses(providerId: string): Observable<ProviderAddressResponse[]> {
    return this.http.get<ProviderAddressResponse[]>(`/api/providers/${providerId}/addresses`);
  }

  deleteAddress(addressId: string): Observable<void> {
    return this.http.delete<void>(`/api/providers/addresses/${addressId}`);
  }

  getNetworksForProvider(providerId: string): Observable<ProviderNetworkMappingResponse[]> {
    return this.http.get<ProviderNetworkMappingResponse[]>(`/api/providers/${providerId}/networks`);
  }

  // ================= Provider Network APIs =================
  createNetwork(request: ProviderNetworkRequest): Observable<ProviderNetworkResponse> {
    return this.http.post<ProviderNetworkResponse>('/api/provider-networks', request);
  }

  getNetworkById(networkId: string): Observable<ProviderNetworkResponse> {
    return this.http.get<ProviderNetworkResponse>(`/api/provider-networks/${networkId}`);
  }

  getAllNetworks(): Observable<ProviderNetworkResponse[]> {
    return this.http.get<ProviderNetworkResponse[]>('/api/provider-networks');
  }

  updateNetwork(networkId: string, request: ProviderNetworkRequest): Observable<ProviderNetworkResponse> {
    return this.http.put<ProviderNetworkResponse>(`/api/provider-networks/${networkId}`, request);
  }

  mapProviderToNetwork(request: ProviderNetworkMappingRequest): Observable<ProviderNetworkMappingResponse> {
    return this.http.post<ProviderNetworkMappingResponse>('/api/provider-networks/mappings', request);
  }

  getProvidersInNetwork(networkId: string): Observable<ProviderNetworkMappingResponse[]> {
    return this.http.get<ProviderNetworkMappingResponse[]>(`/api/provider-networks/${networkId}/providers`);
  }

  removeProviderFromNetwork(networkId: string, providerId: string): Observable<void> {
    return this.http.delete<void>(`/api/provider-networks/${networkId}/providers/${providerId}`);
  }

  verifyEligibility(networkId: string, providerId: string): Observable<ProviderNetworkEligibilityResponse> {
    return this.http.get<ProviderNetworkEligibilityResponse>(`/api/provider-networks/${networkId}/eligibility/${providerId}`);
  }
}
