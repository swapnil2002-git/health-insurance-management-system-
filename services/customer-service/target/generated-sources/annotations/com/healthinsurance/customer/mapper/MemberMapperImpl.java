package com.healthinsurance.customer.mapper;

import com.healthinsurance.customer.dto.request.MemberRequest;
import com.healthinsurance.customer.dto.response.MemberResponse;
import com.healthinsurance.customer.entity.InsuredMember;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-31T13:00:37+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class MemberMapperImpl implements MemberMapper {

    @Override
    public InsuredMember toEntity(MemberRequest request) {
        if ( request == null ) {
            return null;
        }

        InsuredMember insuredMember = new InsuredMember();

        insuredMember.setFirstName( request.getFirstName() );
        insuredMember.setLastName( request.getLastName() );
        insuredMember.setDateOfBirth( request.getDateOfBirth() );
        insuredMember.setRelationshipToCustomer( request.getRelationshipToCustomer() );
        insuredMember.setGender( request.getGender() );

        return insuredMember;
    }

    @Override
    public MemberResponse toResponse(InsuredMember entity) {
        if ( entity == null ) {
            return null;
        }

        MemberResponse memberResponse = new MemberResponse();

        memberResponse.setMemberId( entity.getMemberId() );
        memberResponse.setFirstName( entity.getFirstName() );
        memberResponse.setLastName( entity.getLastName() );
        memberResponse.setDateOfBirth( entity.getDateOfBirth() );
        memberResponse.setRelationshipToCustomer( entity.getRelationshipToCustomer() );
        memberResponse.setGender( entity.getGender() );

        return memberResponse;
    }

    @Override
    public void updateEntityFromRequest(MemberRequest request, InsuredMember entity) {
        if ( request == null ) {
            return;
        }

        entity.setFirstName( request.getFirstName() );
        entity.setLastName( request.getLastName() );
        entity.setDateOfBirth( request.getDateOfBirth() );
        entity.setRelationshipToCustomer( request.getRelationshipToCustomer() );
        entity.setGender( request.getGender() );
    }
}
