package com.healthinsurance.customer.mapper;

import com.healthinsurance.customer.dto.request.BeneficiaryRequest;
import com.healthinsurance.customer.dto.response.BeneficiaryResponse;
import com.healthinsurance.customer.entity.Beneficiary;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-31T13:00:37+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class BeneficiaryMapperImpl implements BeneficiaryMapper {

    @Override
    public Beneficiary toEntity(BeneficiaryRequest request) {
        if ( request == null ) {
            return null;
        }

        Beneficiary beneficiary = new Beneficiary();

        beneficiary.setName( request.getName() );
        beneficiary.setAllocationPercentage( request.getAllocationPercentage() );
        beneficiary.setRelationship( request.getRelationship() );

        return beneficiary;
    }

    @Override
    public BeneficiaryResponse toResponse(Beneficiary entity) {
        if ( entity == null ) {
            return null;
        }

        BeneficiaryResponse beneficiaryResponse = new BeneficiaryResponse();

        beneficiaryResponse.setBeneficiaryId( entity.getBeneficiaryId() );
        beneficiaryResponse.setName( entity.getName() );
        beneficiaryResponse.setAllocationPercentage( entity.getAllocationPercentage() );
        beneficiaryResponse.setRelationship( entity.getRelationship() );

        return beneficiaryResponse;
    }

    @Override
    public void updateEntityFromRequest(BeneficiaryRequest request, Beneficiary entity) {
        if ( request == null ) {
            return;
        }

        entity.setName( request.getName() );
        entity.setAllocationPercentage( request.getAllocationPercentage() );
        entity.setRelationship( request.getRelationship() );
    }
}
