package com.healthinsurance.payment.mapper;

import com.healthinsurance.payment.dto.response.PaymentAllocationResponse;
import com.healthinsurance.payment.dto.response.PaymentResponse;
import com.healthinsurance.payment.dto.response.RefundResponse;
import com.healthinsurance.payment.entity.PaymentAllocation;
import com.healthinsurance.payment.entity.PaymentTransaction;
import com.healthinsurance.payment.entity.RefundTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toPaymentResponse(PaymentTransaction payment);

    List<PaymentResponse> toPaymentResponses(List<PaymentTransaction> payments);

    @Mapping(target = "paymentId", source = "payment.paymentId")
    PaymentAllocationResponse toAllocationResponse(PaymentAllocation allocation);

    @Mapping(target = "paymentId", source = "payment.paymentId")
    RefundResponse toRefundResponse(RefundTransaction refund);
}