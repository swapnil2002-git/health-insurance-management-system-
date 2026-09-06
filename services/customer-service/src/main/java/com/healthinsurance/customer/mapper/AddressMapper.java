package com.healthinsurance.customer.mapper;
import com.healthinsurance.customer.dto.request.AddressRequest;
import com.healthinsurance.customer.dto.response.AddressResponse;
import com.healthinsurance.customer.entity.Address;
import com.healthinsurance.customer.entity.CustomerAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toAddressEntity(AddressRequest request);
    
    @Mapping(source = "address.street1", target = "street1")
    @Mapping(source = "address.street2", target = "street2")
    @Mapping(source = "address.city", target = "city")
    @Mapping(source = "address.state", target = "state")
    @Mapping(source = "address.zipCode", target = "zipCode")
    @Mapping(source = "address.country", target = "country")
    AddressResponse toResponse(CustomerAddress customerAddress);

    void updateAddressFromRequest(AddressRequest request, @MappingTarget Address entity);
}