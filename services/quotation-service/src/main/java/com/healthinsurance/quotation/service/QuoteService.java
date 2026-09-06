package com.healthinsurance.quotation.service;
import com.healthinsurance.quotation.dto.request.CreateQuoteRequest;
import com.healthinsurance.quotation.dto.response.QuoteResponse;
import java.util.List;
import java.util.UUID;

public interface QuoteService {
    QuoteResponse createQuote(CreateQuoteRequest request);
    QuoteResponse getQuote(UUID quoteId);
    List<QuoteResponse> getAllQuotes();
    QuoteResponse calculatePremium(UUID quoteId);
    QuoteResponse acceptQuote(UUID quoteId);
    QuoteResponse rejectQuote(UUID quoteId);
}