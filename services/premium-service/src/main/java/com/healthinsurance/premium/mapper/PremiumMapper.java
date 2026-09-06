package com.healthinsurance.premium.mapper;

import com.healthinsurance.premium.dto.response.PremiumInstallmentResponse;
import com.healthinsurance.premium.dto.response.PremiumOutstandingResponse;
import com.healthinsurance.premium.dto.response.PremiumScheduleResponse;
import com.healthinsurance.premium.entity.PremiumInstallment;
import com.healthinsurance.premium.entity.PremiumSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PremiumMapper {

    @Mapping(target = "installments", source = "installments")
    PremiumScheduleResponse toResponse(PremiumSchedule schedule);

    PremiumInstallmentResponse toInstallmentResponse(PremiumInstallment installment);

    List<PremiumInstallmentResponse> toInstallmentResponses(List<PremiumInstallment> installments);

    @Mapping(target = "status", source = "premiumStatus")
    PremiumOutstandingResponse toOutstandingResponse(PremiumSchedule schedule);
}