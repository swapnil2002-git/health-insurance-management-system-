package com.healthinsurance.customer.mapper;

import com.healthinsurance.customer.dto.request.AddressRequest;
import com.healthinsurance.customer.dto.response.AddressResponse;
import com.healthinsurance.customer.entity.Address;
import com.healthinsurance.customer.entity.CustomerAddress;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-31T13:00:37+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public Address toAddressEntity(AddressRequest request) {
        if ( request == null ) {
            return null;
        }

        Address address = new Address();

        address.setStreet1( request.getStreet1() );
        address.setStreet2( request.getStreet2() );
        address.setCity( request.getCity() );
        address.setState( request.getState() );
        address.setZipCode( request.getZipCode() );
        address.setCountry( request.getCountry() );

        return address;
    }

    @Override
    public AddressResponse toResponse(CustomerAddress customerAddress) {
        if ( customerAddress == null ) {
            return null;
        }

        AddressResponse addressResponse = new AddressResponse();

        addressResponse.setStreet1( customerAddressAddressStreet1( customerAddress ) );
        addressResponse.setStreet2( customerAddressAddressStreet2( customerAddress ) );
        addressResponse.setCity( customerAddressAddressCity( customerAddress ) );
        addressResponse.setState( customerAddressAddressState( customerAddress ) );
        addressResponse.setZipCode( customerAddressAddressZipCode( customerAddress ) );
        addressResponse.setCountry( customerAddressAddressCountry( customerAddress ) );
        addressResponse.setCustomerAddressId( customerAddress.getCustomerAddressId() );
        addressResponse.setAddressType( customerAddress.getAddressType() );

        return addressResponse;
    }

    @Override
    public void updateAddressFromRequest(AddressRequest request, Address entity) {
        if ( request == null ) {
            return;
        }

        entity.setStreet1( request.getStreet1() );
        entity.setStreet2( request.getStreet2() );
        entity.setCity( request.getCity() );
        entity.setState( request.getState() );
        entity.setZipCode( request.getZipCode() );
        entity.setCountry( request.getCountry() );
    }

    private String customerAddressAddressStreet1(CustomerAddress customerAddress) {
        if ( customerAddress == null ) {
            return null;
        }
        Address address = customerAddress.getAddress();
        if ( address == null ) {
            return null;
        }
        String street1 = address.getStreet1();
        if ( street1 == null ) {
            return null;
        }
        return street1;
    }

    private String customerAddressAddressStreet2(CustomerAddress customerAddress) {
        if ( customerAddress == null ) {
            return null;
        }
        Address address = customerAddress.getAddress();
        if ( address == null ) {
            return null;
        }
        String street2 = address.getStreet2();
        if ( street2 == null ) {
            return null;
        }
        return street2;
    }

    private String customerAddressAddressCity(CustomerAddress customerAddress) {
        if ( customerAddress == null ) {
            return null;
        }
        Address address = customerAddress.getAddress();
        if ( address == null ) {
            return null;
        }
        String city = address.getCity();
        if ( city == null ) {
            return null;
        }
        return city;
    }

    private String customerAddressAddressState(CustomerAddress customerAddress) {
        if ( customerAddress == null ) {
            return null;
        }
        Address address = customerAddress.getAddress();
        if ( address == null ) {
            return null;
        }
        String state = address.getState();
        if ( state == null ) {
            return null;
        }
        return state;
    }

    private String customerAddressAddressZipCode(CustomerAddress customerAddress) {
        if ( customerAddress == null ) {
            return null;
        }
        Address address = customerAddress.getAddress();
        if ( address == null ) {
            return null;
        }
        String zipCode = address.getZipCode();
        if ( zipCode == null ) {
            return null;
        }
        return zipCode;
    }

    private String customerAddressAddressCountry(CustomerAddress customerAddress) {
        if ( customerAddress == null ) {
            return null;
        }
        Address address = customerAddress.getAddress();
        if ( address == null ) {
            return null;
        }
        String country = address.getCountry();
        if ( country == null ) {
            return null;
        }
        return country;
    }
}
