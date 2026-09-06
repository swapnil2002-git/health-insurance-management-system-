package com.healthinsurance.payment.mapper;

import com.healthinsurance.payment.dto.response.PaymentAllocationResponse;
import com.healthinsurance.payment.dto.response.PaymentResponse;
import com.healthinsurance.payment.dto.response.RefundResponse;
import com.healthinsurance.payment.entity.PaymentAllocation;
import com.healthinsurance.payment.entity.PaymentTransaction;
import com.healthinsurance.payment.entity.RefundTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/*
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-05T12:03:01+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
*/
@Component
public class PaymentMapperImpl implements PaymentMapper {

    @Override
    public PaymentResponse toPaymentResponse(PaymentTransaction payment) {
        if ( payment == null ) {
            return null;
        }

        PaymentResponse paymentResponse = new PaymentResponse();

        paymentResponse.setPaymentId( payment.getPaymentId() );
        paymentResponse.setPolicyId( payment.getPolicyId() );
        paymentResponse.setAmount( payment.getAmount() );
        paymentResponse.setCurrency( payment.getCurrency() );
        paymentResponse.setPaymentMethod( payment.getPaymentMethod() );
        paymentResponse.setStatus( payment.getStatus() );
        paymentResponse.setGatewayReference( payment.getGatewayReference() );
        paymentResponse.setFailureReason( payment.getFailureReason() );
        paymentResponse.setCreatedAt( payment.getCreatedAt() );
        paymentResponse.setUpdatedAt( payment.getUpdatedAt() );

        return paymentResponse;
    }

    @Override
    public List<PaymentResponse> toPaymentResponses(List<PaymentTransaction> payments) {
        if ( payments == null ) {
            return null;
        }

        List<PaymentResponse> list = new ArrayList<PaymentResponse>( payments.size() );
        for ( PaymentTransaction paymentTransaction : payments ) {
            list.add( toPaymentResponse( paymentTransaction ) );
        }

        return list;
    }

    @Override
    public PaymentAllocationResponse toAllocationResponse(PaymentAllocation allocation) {
        if ( allocation == null ) {
            return null;
        }

        PaymentAllocationResponse paymentAllocationResponse = new PaymentAllocationResponse();

        paymentAllocationResponse.setPaymentId( allocationPaymentPaymentId( allocation ) );
        paymentAllocationResponse.setAllocationId( allocation.getAllocationId() );
        paymentAllocationResponse.setInstallmentId( allocation.getInstallmentId() );
        paymentAllocationResponse.setAllocatedAmount( allocation.getAllocatedAmount() );
        paymentAllocationResponse.setAllocatedAt( allocation.getAllocatedAt() );

        return paymentAllocationResponse;
    }

    @Override
    public RefundResponse toRefundResponse(RefundTransaction refund) {
        if ( refund == null ) {
            return null;
        }

        RefundResponse refundResponse = new RefundResponse();

        refundResponse.setPaymentId( refundPaymentPaymentId( refund ) );
        refundResponse.setRefundId( refund.getRefundId() );
        refundResponse.setAmount( refund.getAmount() );
        refundResponse.setReason( refund.getReason() );
        refundResponse.setRefundReference( refund.getRefundReference() );
        refundResponse.setCreatedAt( refund.getCreatedAt() );

        return refundResponse;
    }

    private UUID allocationPaymentPaymentId(PaymentAllocation paymentAllocation) {
        if ( paymentAllocation == null ) {
            return null;
        }
        PaymentTransaction payment = paymentAllocation.getPayment();
        if ( payment == null ) {
            return null;
        }
        UUID paymentId = payment.getPaymentId();
        if ( paymentId == null ) {
            return null;
        }
        return paymentId;
    }

    private UUID refundPaymentPaymentId(RefundTransaction refundTransaction) {
        if ( refundTransaction == null ) {
            return null;
        }
        PaymentTransaction payment = refundTransaction.getPayment();
        if ( payment == null ) {
            return null;
        }
        UUID paymentId = payment.getPaymentId();
        if ( paymentId == null ) {
            return null;
        }
        return paymentId;
    }
}
