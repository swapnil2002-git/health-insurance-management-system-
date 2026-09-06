package com.healthinsurance.product.mapper;
import com.healthinsurance.product.dto.request.PlanRequest;
import com.healthinsurance.product.dto.response.PlanResponse;
import com.healthinsurance.product.entity.InsurancePlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlanMapper {
    InsurancePlan toEntity(PlanRequest request);
    
    @Mapping(source = "product.productId", target = "productId")
    PlanResponse toResponse(InsurancePlan entity);
    
    void updateEntityFromRequest(PlanRequest request, @MappingTarget InsurancePlan entity);
}