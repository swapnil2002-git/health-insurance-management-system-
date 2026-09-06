package com.healthinsurance.premium.mapper;

import com.healthinsurance.premium.dto.response.PremiumInstallmentResponse;
import com.healthinsurance.premium.dto.response.PremiumOutstandingResponse;
import com.healthinsurance.premium.dto.response.PremiumScheduleResponse;
import com.healthinsurance.premium.entity.PremiumInstallment;
import com.healthinsurance.premium.entity.PremiumSchedule;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/*
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-05T13:17:08+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
*/
@Component
public class PremiumMapperImpl implements PremiumMapper {

    @Override
    public PremiumScheduleResponse toResponse(PremiumSchedule schedule) {
        if ( schedule == null ) {
            return null;
        }

        PremiumScheduleResponse premiumScheduleResponse = new PremiumScheduleResponse();

        premiumScheduleResponse.setInstallments( toInstallmentResponses( schedule.getInstallments() ) );
        premiumScheduleResponse.setScheduleId( schedule.getScheduleId() );
        premiumScheduleResponse.setPolicyId( schedule.getPolicyId() );
        premiumScheduleResponse.setTotalPremium( schedule.getTotalPremium() );
        premiumScheduleResponse.setPaymentFrequency( schedule.getPaymentFrequency() );
        premiumScheduleResponse.setNumberOfInstallments( schedule.getNumberOfInstallments() );
        premiumScheduleResponse.setPaidAmount( schedule.getPaidAmount() );
        premiumScheduleResponse.setOutstandingAmount( schedule.getOutstandingAmount() );
        premiumScheduleResponse.setPremiumStatus( schedule.getPremiumStatus() );
        premiumScheduleResponse.setStartDate( schedule.getStartDate() );
        premiumScheduleResponse.setEndDate( schedule.getEndDate() );
        premiumScheduleResponse.setCreatedAt( schedule.getCreatedAt() );
        premiumScheduleResponse.setUpdatedAt( schedule.getUpdatedAt() );

        return premiumScheduleResponse;
    }

    @Override
    public PremiumInstallmentResponse toInstallmentResponse(PremiumInstallment installment) {
        if ( installment == null ) {
            return null;
        }

        PremiumInstallmentResponse premiumInstallmentResponse = new PremiumInstallmentResponse();

        premiumInstallmentResponse.setInstallmentId( installment.getInstallmentId() );
        premiumInstallmentResponse.setInstallmentNumber( installment.getInstallmentNumber() );
        premiumInstallmentResponse.setAmount( installment.getAmount() );
        premiumInstallmentResponse.setDueDate( installment.getDueDate() );
        premiumInstallmentResponse.setPaidAmount( installment.getPaidAmount() );
        premiumInstallmentResponse.setOutstandingAmount( installment.getOutstandingAmount() );
        premiumInstallmentResponse.setStatus( installment.getStatus() );

        return premiumInstallmentResponse;
    }

    @Override
    public List<PremiumInstallmentResponse> toInstallmentResponses(List<PremiumInstallment> installments) {
        if ( installments == null ) {
            return null;
        }

        List<PremiumInstallmentResponse> list = new ArrayList<PremiumInstallmentResponse>( installments.size() );
        for ( PremiumInstallment premiumInstallment : installments ) {
            list.add( toInstallmentResponse( premiumInstallment ) );
        }

        return list;
    }

    @Override
    public PremiumOutstandingResponse toOutstandingResponse(PremiumSchedule schedule) {
        if ( schedule == null ) {
            return null;
        }

        PremiumOutstandingResponse premiumOutstandingResponse = new PremiumOutstandingResponse();

        premiumOutstandingResponse.setStatus( schedule.getPremiumStatus() );
        premiumOutstandingResponse.setPolicyId( schedule.getPolicyId() );
        premiumOutstandingResponse.setScheduleId( schedule.getScheduleId() );
        premiumOutstandingResponse.setTotalPremium( schedule.getTotalPremium() );
        premiumOutstandingResponse.setPaidAmount( schedule.getPaidAmount() );
        premiumOutstandingResponse.setOutstandingAmount( schedule.getOutstandingAmount() );

        return premiumOutstandingResponse;
    }
}
