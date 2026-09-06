package com.healthinsurance.underwriting.mapper;

import com.healthinsurance.underwriting.dto.request.CreateUnderwritingCaseRequest;
import com.healthinsurance.underwriting.dto.response.*;
import com.healthinsurance.underwriting.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UnderwritingMapper {

    // Request -> Entity
    @Mapping(target = "status", ignore = true)
    UnderwritingCase toEntity(CreateUnderwritingCaseRequest request);

    // Entity -> Response
    UnderwritingCaseResponse toResponse(UnderwritingCase entity);
    UnderwritingDecisionResponse toDecisionResponse(UnderwritingDecision entity);
}