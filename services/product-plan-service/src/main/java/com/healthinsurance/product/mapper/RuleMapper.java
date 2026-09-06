package com.healthinsurance.product.mapper;
import com.healthinsurance.product.dto.response.RuleResponse;
import com.healthinsurance.product.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RuleMapper {
    
    @Mapping(source = "coverage.coverageId", target = "id")
    @Mapping(source = "coverage.name", target = "name")
    @Mapping(source = "coverage.description", target = "description")
    RuleResponse toCoverageResponse(PlanCoverage entity);

    @Mapping(source = "exclusion.exclusionId", target = "id")
    @Mapping(source = "exclusion.name", target = "name")
    @Mapping(source = "exclusion.description", target = "description")
    RuleResponse toExclusionResponse(PlanExclusion entity);

    @Mapping(source = "deductible.deductibleId", target = "id")
    @Mapping(source = "deductible.name", target = "name")
    @Mapping(source = "deductible.description", target = "description")
    RuleResponse toDeductibleResponse(PlanDeductible entity);

    @Mapping(source = "copayment.copaymentId", target = "id")
    @Mapping(source = "copayment.name", target = "name")
    @Mapping(source = "copayment.description", target = "description")
    RuleResponse toCopaymentResponse(PlanCopayment entity);

    @Mapping(source = "rider.riderId", target = "id")
    @Mapping(source = "rider.name", target = "name")
    @Mapping(source = "rider.description", target = "description")
    RuleResponse toRiderResponse(PlanRider entity);
}