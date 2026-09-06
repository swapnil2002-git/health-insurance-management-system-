package com.healthinsurance.customer.mapper;
import com.healthinsurance.customer.dto.request.NomineeRequest;
import com.healthinsurance.customer.dto.response.NomineeResponse;
import com.healthinsurance.customer.entity.Nominee;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NomineeMapper {
    Nominee toEntity(NomineeRequest request);
    NomineeResponse toResponse(Nominee entity);
    void updateEntityFromRequest(NomineeRequest request, @MappingTarget Nominee entity);
}