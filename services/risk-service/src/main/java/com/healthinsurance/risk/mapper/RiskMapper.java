package com.healthinsurance.risk.mapper;

import com.healthinsurance.risk.dto.request.CreateRiskAssessmentRequest;
import com.healthinsurance.risk.dto.request.RiskFactorRequest;
import com.healthinsurance.risk.dto.response.*;
import com.healthinsurance.risk.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RiskMapper {

    // Request -> Entity
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "classification", ignore = true)
    RiskAssessment toEntity(CreateRiskAssessmentRequest request);
    
    RiskFactor toFactorEntity(RiskFactorRequest request);

    // Entity -> Response
    RiskAssessmentResponse toResponse(RiskAssessment entity);
    RiskFactorResponse toFactorResponse(RiskFactor entity);
    RiskScoreResponse toScoreResponse(RiskScore entity);
}