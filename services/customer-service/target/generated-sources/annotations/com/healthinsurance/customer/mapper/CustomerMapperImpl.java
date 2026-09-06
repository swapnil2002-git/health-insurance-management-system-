package com.healthinsurance.customer.mapper;

import com.healthinsurance.customer.dto.request.CustomerRequest;
import com.healthinsurance.customer.dto.response.CustomerResponse;
import com.healthinsurance.customer.entity.Customer;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-31T13:00:37+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class CustomerMapperImpl implements CustomerMapper {

    @Override
    public Customer toEntity(CustomerRequest request) {
        if ( request == null ) {
            return null;
        }

        Customer customer = new Customer();

        customer.setFirstName( request.getFirstName() );
        customer.setLastName( request.getLastName() );
        customer.setDateOfBirth( request.getDateOfBirth() );
        customer.setGender( request.getGender() );
        customer.setIdentificationNumber( request.getIdentificationNumber() );

        return customer;
    }

    @Override
    public CustomerResponse toResponse(Customer entity) {
        if ( entity == null ) {
            return null;
        }

        CustomerResponse customerResponse = new CustomerResponse();

        customerResponse.setCustomerId( entity.getCustomerId() );
        customerResponse.setFirstName( entity.getFirstName() );
        customerResponse.setLastName( entity.getLastName() );
        customerResponse.setDateOfBirth( entity.getDateOfBirth() );
        customerResponse.setGender( entity.getGender() );
        customerResponse.setIdentificationNumber( entity.getIdentificationNumber() );
        customerResponse.setStatus( entity.getStatus() );

        return customerResponse;
    }

    @Override
    public void updateEntityFromRequest(CustomerRequest request, Customer entity) {
        if ( request == null ) {
            return;
        }

        entity.setFirstName( request.getFirstName() );
        entity.setLastName( request.getLastName() );
        entity.setDateOfBirth( request.getDateOfBirth() );
        entity.setGender( request.getGender() );
        entity.setIdentificationNumber( request.getIdentificationNumber() );
    }
}
