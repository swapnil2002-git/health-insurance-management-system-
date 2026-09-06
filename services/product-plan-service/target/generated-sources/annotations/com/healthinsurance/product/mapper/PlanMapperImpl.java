package com.healthinsurance.product.mapper;

import com.healthinsurance.product.dto.request.PlanRequest;
import com.healthinsurance.product.dto.response.PlanResponse;
import com.healthinsurance.product.entity.InsurancePlan;
import com.healthinsurance.product.entity.InsuranceProduct;
import java.util.UUID;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-01T11:12:48+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class PlanMapperImpl implements PlanMapper {

    @Override
    public InsurancePlan toEntity(PlanRequest request) {
        if ( request == null ) {
            return null;
        }

        InsurancePlan insurancePlan = new InsurancePlan();

        insurancePlan.setName( request.getName() );
        insurancePlan.setDescription( request.getDescription() );

        return insurancePlan;
    }

    @Override
    public PlanResponse toResponse(InsurancePlan entity) {
        if ( entity == null ) {
            return null;
        }

        PlanResponse planResponse = new PlanResponse();

        planResponse.setProductId( entityProductProductId( entity ) );
        planResponse.setPlanId( entity.getPlanId() );
        planResponse.setName( entity.getName() );
        planResponse.setDescription( entity.getDescription() );

        return planResponse;
    }

    @Override
    public void updateEntityFromRequest(PlanRequest request, InsurancePlan entity) {
        if ( request == null ) {
            return;
        }

        entity.setName( request.getName() );
        entity.setDescription( request.getDescription() );
    }

    private UUID entityProductProductId(InsurancePlan insurancePlan) {
        if ( insurancePlan == null ) {
            return null;
        }
        InsuranceProduct product = insurancePlan.getProduct();
        if ( product == null ) {
            return null;
        }
        UUID productId = product.getProductId();
        if ( productId == null ) {
            return null;
        }
        return productId;
    }
}
