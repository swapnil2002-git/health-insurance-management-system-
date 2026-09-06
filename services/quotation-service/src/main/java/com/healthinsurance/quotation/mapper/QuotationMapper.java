package com.healthinsurance.quotation.mapper;

import com.healthinsurance.quotation.dto.request.CreateQuoteRequest;
import com.healthinsurance.quotation.dto.request.QuoteMemberRequest;
import com.healthinsurance.quotation.dto.response.*;
import com.healthinsurance.quotation.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuotationMapper {

    // Request -> Entity
    @Mapping(target = "quoteNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    Quotation toEntity(CreateQuoteRequest request);
    
    QuoteMember toMemberEntity(QuoteMemberRequest request);

    // Entity -> Response
    QuoteResponse toResponse(Quotation entity);
    QuoteMemberResponse toMemberResponse(QuoteMember entity);
    QuotePremiumResponse toPremiumResponse(QuotePremium entity);
    QuoteVersionResponse toVersionResponse(QuoteVersion entity);
}