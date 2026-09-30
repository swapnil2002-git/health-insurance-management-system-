export type ProviderType = 'HOSPITAL' | 'CLINIC' | 'DOCTOR' | 'DIAGNOSTIC_CENTER';

export type ProviderStatus = 'ACTIVE' | 'INACTIVE';

export type NetworkStatus = 'ACTIVE' | 'INACTIVE';

export interface ProviderAddressRequest {
  streetAddress: string;
  city: string;
  state: string;
  postalCode: string;
  country?: string;
  primary?: boolean;
}

export interface ProviderAddressResponse {
  providerAddressId: string;
  providerId: string;
  streetAddress: string;
  city: string;
  state: string;
  postalCode: string;
  country?: string;
  primary: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface ProviderRequest {
  providerName: string;
  providerType: ProviderType;
  contactEmail?: string;
  contactPhone?: string;
  status?: ProviderStatus;
  addresses?: ProviderAddressRequest[];
}

export interface ProviderResponse {
  providerId: string;
  providerName: string;
  providerType: ProviderType;
  contactEmail?: string;
  contactPhone?: string;
  status: ProviderStatus;
  addresses?: ProviderAddressResponse[];
  createdAt: string;
  updatedAt?: string;
}

export interface ProviderNetworkRequest {
  networkName: string;
  description?: string;
  status?: NetworkStatus;
}

export interface ProviderNetworkResponse {
  networkId: string;
  networkName: string;
  description?: string;
  status: NetworkStatus;
  createdAt: string;
  updatedAt?: string;
}

export interface ProviderNetworkMappingRequest {
  providerId: string;
  networkId: string;
  active?: boolean;
}

export interface ProviderNetworkMappingResponse {
  mappingId: string;
  providerId: string;
  providerName?: string;
  networkId: string;
  networkName?: string;
  active: boolean;
  joinedDate?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface ProviderNetworkEligibilityResponse {
  networkProvider: boolean;
  providerId: string;
  providerName: string;
  networkId: string;
  networkName: string;
  providerStatus: ProviderStatus;
  mappingActive: boolean;
  message: string;
}
