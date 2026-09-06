package com.healthinsurance.customer.service.impl;

import com.healthinsurance.customer.dto.request.*;
import com.healthinsurance.customer.dto.response.*;
import com.healthinsurance.customer.entity.*;
import com.healthinsurance.customer.exception.*;
import com.healthinsurance.customer.mapper.*;
import com.healthinsurance.customer.repository.*;
import com.healthinsurance.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.healthinsurance.customer.event.CustomerCreatedEvent;
import com.healthinsurance.customer.event.CustomerEventPublisher;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final InsuredMemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final ContactRepository contactRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final NomineeRepository nomineeRepository;

    private final CustomerMapper customerMapper;
    private final MemberMapper memberMapper;
    private final AddressMapper addressMapper;
    private final ContactMapper contactMapper;
    private final BeneficiaryMapper beneficiaryMapper;
    private final NomineeMapper nomineeMapper;
    private final CustomerEventPublisher eventPublisher;

    private Customer findCustomer(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
    }

    // --- CUSTOMER ---
    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = customerMapper.toEntity(request);
        customer.setStatus("ACTIVE");
        Customer savedCustomer = customerRepository.save(customer);
        
        CustomerCreatedEvent event = CustomerCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("CustomerCreated")
                .customerId(savedCustomer.getCustomerId())
                .timestamp(Instant.now())
                .version(savedCustomer.getVersion())
                .build();
                
        eventPublisher.publishCustomerCreatedEvent(event);
        
        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    public CustomerResponse getCustomer(UUID customerId) {
        return customerMapper.toResponse(findCustomer(customerId));
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(UUID customerId, CustomerRequest request) {
        Customer customer = findCustomer(customerId);
        customerMapper.updateEntityFromRequest(request, customer);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    // --- MEMBERS ---
    @Override
    @Transactional
    public MemberResponse addMember(UUID customerId, MemberRequest request) {
        Customer customer = findCustomer(customerId);
        InsuredMember member = memberMapper.toEntity(request);
        member.setCustomer(customer);
        return memberMapper.toResponse(memberRepository.save(member));
    }

    @Override
    public List<MemberResponse> getMembers(UUID customerId) {
        if (!customerRepository.existsById(customerId)) throw new CustomerNotFoundException("Customer not found.");
        return memberRepository.findByCustomer_CustomerId(customerId).stream()
                .map(memberMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MemberResponse updateMember(UUID customerId, UUID memberId, MemberRequest request) {
        InsuredMember member = memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException("Member not found"));
        if (!member.getCustomer().getCustomerId().equals(customerId)) throw new BusinessValidationException("Member belongs to another customer");
        memberMapper.updateEntityFromRequest(request, member);
        return memberMapper.toResponse(memberRepository.save(member));
    }

    @Override
    @Transactional
    public void removeMember(UUID customerId, UUID memberId) {
        InsuredMember member = memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException("Member not found"));
        if (!member.getCustomer().getCustomerId().equals(customerId)) throw new BusinessValidationException("Mismatch");
        memberRepository.delete(member);
    }

    // --- ADDRESSES ---
    @Override
    @Transactional
    public AddressResponse addAddress(UUID customerId, AddressRequest request) {
        Customer customer = findCustomer(customerId);
        Address address = addressMapper.toAddressEntity(request);
        address = addressRepository.save(address);

        CustomerAddress ca = new CustomerAddress();
        ca.setCustomer(customer);
        ca.setAddress(address);
        ca.setAddressType(request.getAddressType());
        
        return addressMapper.toResponse(customerAddressRepository.save(ca));
    }

    @Override
    public List<AddressResponse> getAddresses(UUID customerId) {
        if (!customerRepository.existsById(customerId)) throw new CustomerNotFoundException("Customer not found.");
        return customerAddressRepository.findByCustomer_CustomerId(customerId).stream()
                .map(addressMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(UUID customerId, UUID addressId, AddressRequest request) {
        CustomerAddress ca = customerAddressRepository.findById(addressId).orElseThrow(() -> new AddressNotFoundException("Address relation not found"));
        if (!ca.getCustomer().getCustomerId().equals(customerId)) throw new BusinessValidationException("Mismatch");
        
        ca.setAddressType(request.getAddressType());
        addressMapper.updateAddressFromRequest(request, ca.getAddress());
        addressRepository.save(ca.getAddress());
        
        return addressMapper.toResponse(customerAddressRepository.save(ca));
    }

    @Override
    @Transactional
    public void removeAddress(UUID customerId, UUID addressId) {
        CustomerAddress ca = customerAddressRepository.findById(addressId).orElseThrow(() -> new AddressNotFoundException("Not found"));
        if (!ca.getCustomer().getCustomerId().equals(customerId)) throw new BusinessValidationException("Mismatch");
        customerAddressRepository.delete(ca);
    }

    // --- CONTACTS ---
    @Override
    @Transactional
    public ContactResponse addContact(UUID customerId, ContactRequest request) {
        Customer customer = findCustomer(customerId);
        Contact contact = contactMapper.toEntity(request);
        contact.setCustomer(customer);
        return contactMapper.toResponse(contactRepository.save(contact));
    }

    @Override
    public List<ContactResponse> getContacts(UUID customerId) {
        if (!customerRepository.existsById(customerId)) throw new CustomerNotFoundException("Customer not found.");
        return contactRepository.findByCustomer_CustomerId(customerId).stream()
                .map(contactMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContactResponse updateContact(UUID customerId, UUID contactId, ContactRequest request) {
        Contact contact = contactRepository.findById(contactId).orElseThrow(() -> new ContactNotFoundException("Not found"));
        if (!contact.getCustomer().getCustomerId().equals(customerId)) throw new BusinessValidationException("Mismatch");
        contactMapper.updateEntityFromRequest(request, contact);
        return contactMapper.toResponse(contactRepository.save(contact));
    }

    @Override
    @Transactional
    public void removeContact(UUID customerId, UUID contactId) {
        Contact contact = contactRepository.findById(contactId).orElseThrow(() -> new ContactNotFoundException("Not found"));
        if (!contact.getCustomer().getCustomerId().equals(customerId)) throw new BusinessValidationException("Mismatch");
        contactRepository.delete(contact);
    }

    // --- BENEFICIARIES ---
    @Override
    @Transactional
    public BeneficiaryResponse addBeneficiary(UUID customerId, BeneficiaryRequest request) {
        Customer customer = findCustomer(customerId);
        Beneficiary ben = beneficiaryMapper.toEntity(request);
        ben.setCustomer(customer);
        return beneficiaryMapper.toResponse(beneficiaryRepository.save(ben));
    }

    @Override
    public List<BeneficiaryResponse> getBeneficiaries(UUID customerId) {
        if (!customerRepository.existsById(customerId)) throw new CustomerNotFoundException("Customer not found.");
        return beneficiaryRepository.findByCustomer_CustomerId(customerId).stream()
                .map(beneficiaryMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BeneficiaryResponse updateBeneficiary(UUID customerId, UUID beneficiaryId, BeneficiaryRequest request) {
        Beneficiary ben = beneficiaryRepository.findById(beneficiaryId).orElseThrow(() -> new BeneficiaryNotFoundException("Not found"));
        if (!ben.getCustomer().getCustomerId().equals(customerId)) throw new BusinessValidationException("Mismatch");
        beneficiaryMapper.updateEntityFromRequest(request, ben);
        return beneficiaryMapper.toResponse(beneficiaryRepository.save(ben));
    }

    @Override
    @Transactional
    public void removeBeneficiary(UUID customerId, UUID beneficiaryId) {
        Beneficiary ben = beneficiaryRepository.findById(beneficiaryId).orElseThrow(() -> new BeneficiaryNotFoundException("Not found"));
        if (!ben.getCustomer().getCustomerId().equals(customerId)) throw new BusinessValidationException("Mismatch");
        beneficiaryRepository.delete(ben);
    }

    // --- NOMINEES ---
    @Override
    @Transactional
    public NomineeResponse addNominee(UUID customerId, NomineeRequest request) {
        Customer customer = findCustomer(customerId);
        Nominee nom = nomineeMapper.toEntity(request);
        nom.setCustomer(customer);
        return nomineeMapper.toResponse(nomineeRepository.save(nom));
    }

    @Override
    public List<NomineeResponse> getNominees(UUID customerId) {
        if (!customerRepository.existsById(customerId)) throw new CustomerNotFoundException("Customer not found.");
        return nomineeRepository.findByCustomer_CustomerId(customerId).stream()
                .map(nomineeMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NomineeResponse updateNominee(UUID customerId, UUID nomineeId, NomineeRequest request) {
        Nominee nom = nomineeRepository.findById(nomineeId).orElseThrow(() -> new NomineeNotFoundException("Not found"));
        if (!nom.getCustomer().getCustomerId().equals(customerId)) throw new BusinessValidationException("Mismatch");
        nomineeMapper.updateEntityFromRequest(request, nom);
        return nomineeMapper.toResponse(nomineeRepository.save(nom));
    }

    @Override
    @Transactional
    public void removeNominee(UUID customerId, UUID nomineeId) {
        Nominee nom = nomineeRepository.findById(nomineeId).orElseThrow(() -> new NomineeNotFoundException("Not found"));
        if (!nom.getCustomer().getCustomerId().equals(customerId)) throw new BusinessValidationException("Mismatch");
        nomineeRepository.delete(nom);
    }
}