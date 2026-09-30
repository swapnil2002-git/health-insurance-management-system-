export interface CustomerRequest {
  firstName: string;
  lastName: string;
  dateOfBirth: string; // YYYY-MM-DD
  gender: string;
  identificationNumber: string;
}

export interface CustomerResponse {
  customerId: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: string;
  identificationNumber: string;
  status: string;
}

export interface MemberRequest {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  relationshipToCustomer: string;
  gender: string;
}

export interface MemberResponse {
  memberId: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  relationshipToCustomer: string;
  gender: string;
}

export interface AddressRequest {
  street1: string;
  street2?: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  addressType: string;
}

export interface AddressResponse {
  customerAddressId: string;
  addressType: string;
  street1: string;
  street2?: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

export interface ContactRequest {
  contactType: string;
  contactValue: string;
  isPrimary: boolean;
}

export interface ContactResponse {
  contactId: string;
  contactType: string;
  contactValue: string;
  isPrimary: boolean;
}

export interface BeneficiaryRequest {
  name: string;
  allocationPercentage: number;
  relationship: string;
}

export interface BeneficiaryResponse {
  beneficiaryId: string;
  name: string;
  allocationPercentage: number;
  relationship: string;
}

export interface NomineeRequest {
  name: string;
  relationship: string;
  dateOfBirth: string;
}

export interface NomineeResponse {
  nomineeId: string;
  name: string;
  relationship: string;
  dateOfBirth: string;
}

export const GENDERS = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'OTHER', label: 'Other' }
];

export const RELATIONSHIPS = [
  'SPOUSE',
  'CHILD',
  'PARENT',
  'SIBLING',
  'DEPENDENT',
  'OTHER'
];

export const ADDRESS_TYPES = [
  'RESIDENTIAL',
  'PERMANENT',
  'MAILING',
  'BILLING'
];

export const CONTACT_TYPES = [
  'MOBILE',
  'PHONE',
  'EMAIL',
  'EMERGENCY'
];
