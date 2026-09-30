package com.healthinsurance.reporting.mapper;

import com.healthinsurance.reporting.dto.ClaimSummaryDto;
import com.healthinsurance.reporting.dto.PolicySummaryDto;
import com.healthinsurance.reporting.dto.PremiumSummaryDto;
import com.healthinsurance.reporting.entity.ClaimDailySummary;
import com.healthinsurance.reporting.entity.PolicyDailySummary;
import com.healthinsurance.reporting.entity.PremiumDailySummary;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReportingMapper {

    PolicySummaryDto toPolicyDto(PolicyDailySummary entity);

    List<PolicySummaryDto> toPolicyDtoList(List<PolicyDailySummary> entities);

    ClaimSummaryDto toClaimDto(ClaimDailySummary entity);

    List<ClaimSummaryDto> toClaimDtoList(List<ClaimDailySummary> entities);

    PremiumSummaryDto toPremiumDto(PremiumDailySummary entity);

    List<PremiumSummaryDto> toPremiumDtoList(List<PremiumDailySummary> entities);
}
