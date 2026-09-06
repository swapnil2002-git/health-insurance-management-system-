package com.healthinsurance.customer.mapper;
import com.healthinsurance.customer.dto.request.ContactRequest;
import com.healthinsurance.customer.dto.response.ContactResponse;
import com.healthinsurance.customer.entity.Contact;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ContactMapper {
    Contact toEntity(ContactRequest request);
    ContactResponse toResponse(Contact entity);
    void updateEntityFromRequest(ContactRequest request, @MappingTarget Contact entity);
}