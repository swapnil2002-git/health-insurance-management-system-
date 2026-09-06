package com.healthinsurance.customer.mapper;

import com.healthinsurance.customer.dto.request.NomineeRequest;
import com.healthinsurance.customer.dto.response.NomineeResponse;
import com.healthinsurance.customer.entity.Nominee;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-31T13:00:37+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class NomineeMapperImpl implements NomineeMapper {

    @Override
    public Nominee toEntity(NomineeRequest request) {
        if ( request == null ) {
            return null;
        }

        Nominee nominee = new Nominee();

        nominee.setName( request.getName() );
        nominee.setRelationship( request.getRelationship() );
        nominee.setDateOfBirth( request.getDateOfBirth() );

        return nominee;
    }

    @Override
    public NomineeResponse toResponse(Nominee entity) {
        if ( entity == null ) {
            return null;
        }

        NomineeResponse nomineeResponse = new NomineeResponse();

        nomineeResponse.setNomineeId( entity.getNomineeId() );
        nomineeResponse.setName( entity.getName() );
        nomineeResponse.setRelationship( entity.getRelationship() );
        nomineeResponse.setDateOfBirth( entity.getDateOfBirth() );

        return nomineeResponse;
    }

    @Override
    public void updateEntityFromRequest(NomineeRequest request, Nominee entity) {
        if ( request == null ) {
            return;
        }

        entity.setName( request.getName() );
        entity.setRelationship( request.getRelationship() );
        entity.setDateOfBirth( request.getDateOfBirth() );
    }
}
