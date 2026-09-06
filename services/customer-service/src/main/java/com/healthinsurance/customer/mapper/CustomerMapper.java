package com.healthinsurance.customer.mapper;
import com.healthinsurance.customer.dto.request.CustomerRequest;
import com.healthinsurance.customer.dto.response.CustomerResponse;
import com.healthinsurance.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toEntity(CustomerRequest request);
    CustomerResponse toResponse(Customer entity);
    void updateEntityFromRequest(CustomerRequest request, @MappingTarget Customer entity);
}