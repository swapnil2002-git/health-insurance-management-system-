package com.healthinsurance.underwriting.mapper;

import com.healthinsurance.underwriting.dto.request.CreateUnderwritingCaseRequest;
import com.healthinsurance.underwriting.dto.response.UnderwritingCaseResponse;
import com.healthinsurance.underwriting.dto.response.UnderwritingDecisionResponse;
import com.healthinsurance.underwriting.entity.UnderwritingCase;
import com.healthinsurance.underwriting.entity.UnderwritingDecision;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-02T16:04:26+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class UnderwritingMapperImpl implements UnderwritingMapper {

    @Override
    public UnderwritingCase toEntity(CreateUnderwritingCaseRequest request) {
        if ( request == null ) {
            return null;
        }

        UnderwritingCase underwritingCase = new UnderwritingCase();

        underwritingCase.setQuoteId( request.getQuoteId() );
        underwritingCase.setCustomerId( request.getCustomerId() );
        underwritingCase.setAssessmentId( request.getAssessmentId() );

        return underwritingCase;
    }

    @Override
    public UnderwritingCaseResponse toResponse(UnderwritingCase entity) {
        if ( entity == null ) {
            return null;
        }

        UnderwritingCaseResponse underwritingCaseResponse = new UnderwritingCaseResponse();

        underwritingCaseResponse.setCaseId( entity.getCaseId() );
        underwritingCaseResponse.setQuoteId( entity.getQuoteId() );
        underwritingCaseResponse.setCustomerId( entity.getCustomerId() );
        underwritingCaseResponse.setAssessmentId( entity.getAssessmentId() );
        if ( entity.getStatus() != null ) {
            underwritingCaseResponse.setStatus( entity.getStatus().name() );
        }
        underwritingCaseResponse.setCreatedAt( entity.getCreatedAt() );
        underwritingCaseResponse.setUpdatedAt( entity.getUpdatedAt() );
        underwritingCaseResponse.setCompletedAt( entity.getCompletedAt() );
        underwritingCaseResponse.setDecisions( underwritingDecisionSetToUnderwritingDecisionResponseList( entity.getDecisions() ) );

        return underwritingCaseResponse;
    }

    @Override
    public UnderwritingDecisionResponse toDecisionResponse(UnderwritingDecision entity) {
        if ( entity == null ) {
            return null;
        }

        UnderwritingDecisionResponse underwritingDecisionResponse = new UnderwritingDecisionResponse();

        underwritingDecisionResponse.setDecisionId( entity.getDecisionId() );
        if ( entity.getDecisionType() != null ) {
            underwritingDecisionResponse.setDecisionType( entity.getDecisionType().name() );
        }
        underwritingDecisionResponse.setReason( entity.getReason() );
        underwritingDecisionResponse.setNotes( entity.getNotes() );
        underwritingDecisionResponse.setDecidedAt( entity.getDecidedAt() );

        return underwritingDecisionResponse;
    }

    protected List<UnderwritingDecisionResponse> underwritingDecisionSetToUnderwritingDecisionResponseList(Set<UnderwritingDecision> set) {
        if ( set == null ) {
            return null;
        }

        List<UnderwritingDecisionResponse> list = new ArrayList<UnderwritingDecisionResponse>( set.size() );
        for ( UnderwritingDecision underwritingDecision : set ) {
            list.add( toDecisionResponse( underwritingDecision ) );
        }

        return list;
    }
}
