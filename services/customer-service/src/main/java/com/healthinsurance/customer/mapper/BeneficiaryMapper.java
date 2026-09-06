package com.healthinsurance.customer.mapper;
import com.healthinsurance.customer.dto.request.BeneficiaryRequest;
import com.healthinsurance.customer.dto.response.BeneficiaryResponse;
import com.healthinsurance.customer.entity.Beneficiary;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BeneficiaryMapper {
    Beneficiary toEntity(BeneficiaryRequest request);
    BeneficiaryResponse toResponse(Beneficiary entity);
    void updateEntityFromRequest(BeneficiaryRequest request, @MappingTarget Beneficiary entity);
}