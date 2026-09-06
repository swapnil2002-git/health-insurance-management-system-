package com.healthinsurance.customer.service;

import com.healthinsurance.customer.dto.request.CustomerRequest;
import com.healthinsurance.customer.dto.response.CustomerResponse;
import com.healthinsurance.customer.entity.Customer;
import com.healthinsurance.customer.event.CustomerCreatedEvent;
import com.healthinsurance.customer.event.CustomerEventPublisher;
import com.healthinsurance.customer.exception.CustomerNotFoundException;
import com.healthinsurance.customer.mapper.CustomerMapper;
import com.healthinsurance.customer.repository.CustomerRepository;
import com.healthinsurance.customer.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private CustomerEventPublisher eventPublisher;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void createCustomer_ShouldSaveAndPublishEvent() {
        // Arrange
        CustomerRequest request = new CustomerRequest();
        request.setFirstName("John");

        Customer entity = new Customer();
        entity.setCustomerId(UUID.randomUUID());
        entity.setVersion(1L);

        CustomerResponse response = new CustomerResponse();
        response.setCustomerId(entity.getCustomerId());

        when(customerMapper.toEntity(request)).thenReturn(entity);
        when(customerRepository.save(any(Customer.class))).thenReturn(entity);
        when(customerMapper.toResponse(entity)).thenReturn(response);

        // Act
        CustomerResponse result = customerService.createCustomer(request);

        // Assert
        assertNotNull(result);
        assertEquals(entity.getCustomerId(), result.getCustomerId());
        verify(customerRepository, times(1)).save(entity);
        verify(eventPublisher, times(1)).publishCustomerCreatedEvent(any(CustomerCreatedEvent.class));
    }

    @Test
    void getCustomer_WhenExists_ShouldReturnResponse() {
        // Arrange
        UUID id = UUID.randomUUID();
        Customer entity = new Customer();
        CustomerResponse response = new CustomerResponse();
        
        when(customerRepository.findById(id)).thenReturn(Optional.of(entity));
        when(customerMapper.toResponse(entity)).thenReturn(response);

        // Act
        CustomerResponse result = customerService.getCustomer(id);

        // Assert
        assertNotNull(result);
        verify(customerRepository, times(1)).findById(id);
    }

    @Test
    void getCustomer_WhenNotExists_ShouldThrowException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomer(id));
    }
}