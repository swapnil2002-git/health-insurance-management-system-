package com.healthinsurance.customer.service;

import com.healthinsurance.customer.dto.request.*;
import com.healthinsurance.customer.dto.response.*;
import java.util.List;
import java.util.UUID;

public interface CustomerService {
    // Customer
    CustomerResponse createCustomer(CustomerRequest request);
    CustomerResponse getCustomer(UUID customerId);
    CustomerResponse updateCustomer(UUID customerId, CustomerRequest request);

    // Members
    MemberResponse addMember(UUID customerId, MemberRequest request);
    List<MemberResponse> getMembers(UUID customerId);
    MemberResponse updateMember(UUID customerId, UUID memberId, MemberRequest request);
    void removeMember(UUID customerId, UUID memberId);

    // Addresses
    AddressResponse addAddress(UUID customerId, AddressRequest request);
    List<AddressResponse> getAddresses(UUID customerId);
    AddressResponse updateAddress(UUID customerId, UUID addressId, AddressRequest request);
    void removeAddress(UUID customerId, UUID addressId);

    // Contacts
    ContactResponse addContact(UUID customerId, ContactRequest request);
    List<ContactResponse> getContacts(UUID customerId);
    ContactResponse updateContact(UUID customerId, UUID contactId, ContactRequest request);
    void removeContact(UUID customerId, UUID contactId);

    // Beneficiaries
    BeneficiaryResponse addBeneficiary(UUID customerId, BeneficiaryRequest request);
    List<BeneficiaryResponse> getBeneficiaries(UUID customerId);
    BeneficiaryResponse updateBeneficiary(UUID customerId, UUID beneficiaryId, BeneficiaryRequest request);
    void removeBeneficiary(UUID customerId, UUID beneficiaryId);

    // Nominees
    NomineeResponse addNominee(UUID customerId, NomineeRequest request);
    List<NomineeResponse> getNominees(UUID customerId);
    NomineeResponse updateNominee(UUID customerId, UUID nomineeId, NomineeRequest request);
    void removeNominee(UUID customerId, UUID nomineeId);
}