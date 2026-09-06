package com.healthinsurance.quotation.mapper;

import com.healthinsurance.quotation.dto.request.CreateQuoteRequest;
import com.healthinsurance.quotation.dto.request.QuoteMemberRequest;
import com.healthinsurance.quotation.dto.response.QuoteMemberResponse;
import com.healthinsurance.quotation.dto.response.QuotePremiumResponse;
import com.healthinsurance.quotation.dto.response.QuoteResponse;
import com.healthinsurance.quotation.dto.response.QuoteVersionResponse;
import com.healthinsurance.quotation.entity.Quotation;
import com.healthinsurance.quotation.entity.QuoteMember;
import com.healthinsurance.quotation.entity.QuotePremium;
import com.healthinsurance.quotation.entity.QuoteVersion;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-01T16:03:27+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class QuotationMapperImpl implements QuotationMapper {

    @Override
    public Quotation toEntity(CreateQuoteRequest request) {
        if ( request == null ) {
            return null;
        }

        Quotation quotation = new Quotation();

        quotation.setCustomerId( request.getCustomerId() );
        quotation.setPlanId( request.getPlanId() );
        quotation.setMembers( quoteMemberRequestListToQuoteMemberSet( request.getMembers() ) );

        return quotation;
    }

    @Override
    public QuoteMember toMemberEntity(QuoteMemberRequest request) {
        if ( request == null ) {
            return null;
        }

        QuoteMember quoteMember = new QuoteMember();

        quoteMember.setMemberName( request.getMemberName() );
        quoteMember.setDateOfBirth( request.getDateOfBirth() );
        quoteMember.setRelationship( request.getRelationship() );
        quoteMember.setGender( request.getGender() );

        return quoteMember;
    }

    @Override
    public QuoteResponse toResponse(Quotation entity) {
        if ( entity == null ) {
            return null;
        }

        QuoteResponse quoteResponse = new QuoteResponse();

        quoteResponse.setQuoteId( entity.getQuoteId() );
        quoteResponse.setCustomerId( entity.getCustomerId() );
        quoteResponse.setPlanId( entity.getPlanId() );
        quoteResponse.setQuoteNumber( entity.getQuoteNumber() );
        if ( entity.getStatus() != null ) {
            quoteResponse.setStatus( entity.getStatus().name() );
        }
        quoteResponse.setTotalPremium( entity.getTotalPremium() );
        quoteResponse.setCreatedAt( entity.getCreatedAt() );
        quoteResponse.setExpiresAt( entity.getExpiresAt() );
        quoteResponse.setAcceptedAt( entity.getAcceptedAt() );
        quoteResponse.setRejectedAt( entity.getRejectedAt() );
        quoteResponse.setMembers( quoteMemberSetToQuoteMemberResponseList( entity.getMembers() ) );
        quoteResponse.setPremiums( quotePremiumSetToQuotePremiumResponseList( entity.getPremiums() ) );
        quoteResponse.setVersions( quoteVersionSetToQuoteVersionResponseList( entity.getVersions() ) );

        return quoteResponse;
    }

    @Override
    public QuoteMemberResponse toMemberResponse(QuoteMember entity) {
        if ( entity == null ) {
            return null;
        }

        QuoteMemberResponse quoteMemberResponse = new QuoteMemberResponse();

        quoteMemberResponse.setQuoteMemberId( entity.getQuoteMemberId() );
        quoteMemberResponse.setMemberName( entity.getMemberName() );
        quoteMemberResponse.setDateOfBirth( entity.getDateOfBirth() );
        quoteMemberResponse.setRelationship( entity.getRelationship() );
        quoteMemberResponse.setGender( entity.getGender() );

        return quoteMemberResponse;
    }

    @Override
    public QuotePremiumResponse toPremiumResponse(QuotePremium entity) {
        if ( entity == null ) {
            return null;
        }

        QuotePremiumResponse quotePremiumResponse = new QuotePremiumResponse();

        quotePremiumResponse.setQuotePremiumId( entity.getQuotePremiumId() );
        quotePremiumResponse.setCalculatedPremium( entity.getCalculatedPremium() );
        quotePremiumResponse.setCalculationDetails( entity.getCalculationDetails() );
        quotePremiumResponse.setCalculatedAt( entity.getCalculatedAt() );

        return quotePremiumResponse;
    }

    @Override
    public QuoteVersionResponse toVersionResponse(QuoteVersion entity) {
        if ( entity == null ) {
            return null;
        }

        QuoteVersionResponse quoteVersionResponse = new QuoteVersionResponse();

        quoteVersionResponse.setVersionId( entity.getVersionId() );
        quoteVersionResponse.setVersionNumber( entity.getVersionNumber() );
        quoteVersionResponse.setPremiumAtVersion( entity.getPremiumAtVersion() );
        quoteVersionResponse.setReason( entity.getReason() );
        quoteVersionResponse.setCreatedAt( entity.getCreatedAt() );

        return quoteVersionResponse;
    }

    protected Set<QuoteMember> quoteMemberRequestListToQuoteMemberSet(List<QuoteMemberRequest> list) {
        if ( list == null ) {
            return null;
        }

        Set<QuoteMember> set = new LinkedHashSet<QuoteMember>( Math.max( (int) ( list.size() / .75f ) + 1, 16 ) );
        for ( QuoteMemberRequest quoteMemberRequest : list ) {
            set.add( toMemberEntity( quoteMemberRequest ) );
        }

        return set;
    }

    protected List<QuoteMemberResponse> quoteMemberSetToQuoteMemberResponseList(Set<QuoteMember> set) {
        if ( set == null ) {
            return null;
        }

        List<QuoteMemberResponse> list = new ArrayList<QuoteMemberResponse>( set.size() );
        for ( QuoteMember quoteMember : set ) {
            list.add( toMemberResponse( quoteMember ) );
        }

        return list;
    }

    protected List<QuotePremiumResponse> quotePremiumSetToQuotePremiumResponseList(Set<QuotePremium> set) {
        if ( set == null ) {
            return null;
        }

        List<QuotePremiumResponse> list = new ArrayList<QuotePremiumResponse>( set.size() );
        for ( QuotePremium quotePremium : set ) {
            list.add( toPremiumResponse( quotePremium ) );
        }

        return list;
    }

    protected List<QuoteVersionResponse> quoteVersionSetToQuoteVersionResponseList(Set<QuoteVersion> set) {
        if ( set == null ) {
            return null;
        }

        List<QuoteVersionResponse> list = new ArrayList<QuoteVersionResponse>( set.size() );
        for ( QuoteVersion quoteVersion : set ) {
            list.add( toVersionResponse( quoteVersion ) );
        }

        return list;
    }
}
