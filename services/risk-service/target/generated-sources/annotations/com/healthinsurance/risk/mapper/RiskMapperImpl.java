package com.healthinsurance.risk.mapper;

import com.healthinsurance.risk.dto.request.CreateRiskAssessmentRequest;
import com.healthinsurance.risk.dto.request.RiskFactorRequest;
import com.healthinsurance.risk.dto.response.RiskAssessmentResponse;
import com.healthinsurance.risk.dto.response.RiskFactorResponse;
import com.healthinsurance.risk.dto.response.RiskScoreResponse;
import com.healthinsurance.risk.entity.RiskAssessment;
import com.healthinsurance.risk.entity.RiskFactor;
import com.healthinsurance.risk.entity.RiskScore;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-02T11:39:28+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class RiskMapperImpl implements RiskMapper {

    @Override
    public RiskAssessment toEntity(CreateRiskAssessmentRequest request) {
        if ( request == null ) {
            return null;
        }

        RiskAssessment riskAssessment = new RiskAssessment();

        riskAssessment.setCustomerId( request.getCustomerId() );
        riskAssessment.setQuoteId( request.getQuoteId() );
        riskAssessment.setFactors( riskFactorRequestListToRiskFactorSet( request.getFactors() ) );

        return riskAssessment;
    }

    @Override
    public RiskFactor toFactorEntity(RiskFactorRequest request) {
        if ( request == null ) {
            return null;
        }

        RiskFactor riskFactor = new RiskFactor();

        riskFactor.setFactorName( request.getFactorName() );
        riskFactor.setFactorValue( request.getFactorValue() );
        riskFactor.setDescription( request.getDescription() );

        return riskFactor;
    }

    @Override
    public RiskAssessmentResponse toResponse(RiskAssessment entity) {
        if ( entity == null ) {
            return null;
        }

        RiskAssessmentResponse riskAssessmentResponse = new RiskAssessmentResponse();

        riskAssessmentResponse.setAssessmentId( entity.getAssessmentId() );
        riskAssessmentResponse.setCustomerId( entity.getCustomerId() );
        riskAssessmentResponse.setQuoteId( entity.getQuoteId() );
        if ( entity.getStatus() != null ) {
            riskAssessmentResponse.setStatus( entity.getStatus().name() );
        }
        if ( entity.getClassification() != null ) {
            riskAssessmentResponse.setClassification( entity.getClassification().name() );
        }
        riskAssessmentResponse.setCreatedAt( entity.getCreatedAt() );
        riskAssessmentResponse.setUpdatedAt( entity.getUpdatedAt() );
        riskAssessmentResponse.setCompletedAt( entity.getCompletedAt() );
        riskAssessmentResponse.setFactors( riskFactorSetToRiskFactorResponseList( entity.getFactors() ) );
        riskAssessmentResponse.setScores( riskScoreSetToRiskScoreResponseList( entity.getScores() ) );

        return riskAssessmentResponse;
    }

    @Override
    public RiskFactorResponse toFactorResponse(RiskFactor entity) {
        if ( entity == null ) {
            return null;
        }

        RiskFactorResponse riskFactorResponse = new RiskFactorResponse();

        riskFactorResponse.setRiskFactorId( entity.getRiskFactorId() );
        riskFactorResponse.setFactorName( entity.getFactorName() );
        riskFactorResponse.setFactorValue( entity.getFactorValue() );
        riskFactorResponse.setDescription( entity.getDescription() );

        return riskFactorResponse;
    }

    @Override
    public RiskScoreResponse toScoreResponse(RiskScore entity) {
        if ( entity == null ) {
            return null;
        }

        RiskScoreResponse riskScoreResponse = new RiskScoreResponse();

        riskScoreResponse.setRiskScoreId( entity.getRiskScoreId() );
        riskScoreResponse.setScore( entity.getScore() );
        if ( entity.getClassification() != null ) {
            riskScoreResponse.setClassification( entity.getClassification().name() );
        }
        riskScoreResponse.setCalculatedAt( entity.getCalculatedAt() );

        return riskScoreResponse;
    }

    protected Set<RiskFactor> riskFactorRequestListToRiskFactorSet(List<RiskFactorRequest> list) {
        if ( list == null ) {
            return null;
        }

        Set<RiskFactor> set = new LinkedHashSet<RiskFactor>( Math.max( (int) ( list.size() / .75f ) + 1, 16 ) );
        for ( RiskFactorRequest riskFactorRequest : list ) {
            set.add( toFactorEntity( riskFactorRequest ) );
        }

        return set;
    }

    protected List<RiskFactorResponse> riskFactorSetToRiskFactorResponseList(Set<RiskFactor> set) {
        if ( set == null ) {
            return null;
        }

        List<RiskFactorResponse> list = new ArrayList<RiskFactorResponse>( set.size() );
        for ( RiskFactor riskFactor : set ) {
            list.add( toFactorResponse( riskFactor ) );
        }

        return list;
    }

    protected List<RiskScoreResponse> riskScoreSetToRiskScoreResponseList(Set<RiskScore> set) {
        if ( set == null ) {
            return null;
        }

        List<RiskScoreResponse> list = new ArrayList<RiskScoreResponse>( set.size() );
        for ( RiskScore riskScore : set ) {
            list.add( toScoreResponse( riskScore ) );
        }

        return list;
    }
}
