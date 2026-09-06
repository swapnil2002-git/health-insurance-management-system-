package com.healthinsurance.quotation.service.impl;

import com.healthinsurance.quotation.client.CustomerClient;
import com.healthinsurance.quotation.client.ProductClient;
import com.healthinsurance.quotation.dto.request.CreateQuoteRequest;
import com.healthinsurance.quotation.dto.response.QuoteResponse;
import com.healthinsurance.quotation.entity.Quotation;
import com.healthinsurance.quotation.entity.QuotePremium;
import com.healthinsurance.quotation.entity.QuoteVersion;
import com.healthinsurance.quotation.enums.QuoteStatus;
import com.healthinsurance.quotation.event.QuoteEventProducer;
import com.healthinsurance.quotation.event.QuoteGeneratedEvent;
import com.healthinsurance.quotation.exception.DependencyNotFoundException;
import com.healthinsurance.quotation.exception.InvalidQuoteStatusException;
import com.healthinsurance.quotation.exception.QuoteNotFoundException;
import com.healthinsurance.quotation.mapper.QuotationMapper;
import com.healthinsurance.quotation.repository.QuotationRepository;
import com.healthinsurance.quotation.service.QuoteService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuotationRepository quotationRepository;
    private final QuotationMapper quotationMapper;
    
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    
    private final QuoteEventProducer eventProducer;

    @Override
    @Transactional
    public QuoteResponse createQuote(CreateQuoteRequest request) {
        log.info("Creating quote for customer {} and plan {}", request.getCustomerId(), request.getPlanId());

        try {
            customerClient.getCustomer(request.getCustomerId());
        } catch (FeignException.NotFound e) {
            throw new DependencyNotFoundException("Customer not found in Customer Service.");
        }

        try {
            productClient.getPlan(request.getPlanId());
        } catch (FeignException.NotFound e) {
            throw new DependencyNotFoundException("Plan not found in Product Service.");
        }

        Quotation quotation = quotationMapper.toEntity(request);
        quotation.setQuoteNumber("Q-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        quotation.setStatus(QuoteStatus.DRAFT);
        quotation.setCreatedAt(Instant.now());
        quotation.setExpiresAt(Instant.now().plusSeconds(30L * 24 * 60 * 60));

        if (quotation.getMembers() != null) {
            quotation.getMembers().forEach(member -> member.setQuote(quotation));
        }

        Quotation savedQuote = quotationRepository.save(quotation);
        log.info("Successfully created quote ID: {}", savedQuote.getQuoteId());
        return quotationMapper.toResponse(savedQuote);
    }

    @Override
    public QuoteResponse getQuote(UUID quoteId) {
        Quotation quote = quotationRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException("Quotation not found with ID: " + quoteId));
        return quotationMapper.toResponse(quote);
    }

    @Override
    public List<QuoteResponse> getAllQuotes() {
        return quotationRepository.findAll().stream()
                .map(quotationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuoteResponse calculatePremium(UUID quoteId) {
        Quotation quote = quotationRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException("Quotation not found with ID: " + quoteId));

        if (quote.getStatus() == QuoteStatus.ACCEPTED || quote.getStatus() == QuoteStatus.REJECTED || quote.getStatus() == QuoteStatus.EXPIRED) {
            throw new InvalidQuoteStatusException("Cannot calculate premium for quote in status: " + quote.getStatus());
        }
        
        if (quote.getExpiresAt() != null && quote.getExpiresAt().isBefore(Instant.now())) {
            quote.setStatus(QuoteStatus.EXPIRED);
            quotationRepository.save(quote);
            throw new InvalidQuoteStatusException("Quote has expired and can no longer be calculated.");
        }

        log.info("Calculating premium for quote: {}", quoteId);

        BigDecimal basePremium = new BigDecimal("500.00");
        int memberCount = quote.getMembers() != null ? quote.getMembers().size() : 1;
        BigDecimal perMemberCost = new BigDecimal("200.00");
        BigDecimal calculatedPremium = basePremium.add(perMemberCost.multiply(new BigDecimal(memberCount)));

        QuotePremium premiumRecord = new QuotePremium();
        premiumRecord.setQuote(quote);
        premiumRecord.setCalculatedPremium(calculatedPremium);
        premiumRecord.setCalculationDetails("Base Rate: $" + basePremium + " + (" + memberCount + " members x $" + perMemberCost + ")");
        premiumRecord.setCalculatedAt(Instant.now());

        if (quote.getPremiums() == null) quote.setPremiums(new HashSet<>());
        quote.getPremiums().add(premiumRecord);

        QuoteVersion versionRecord = new QuoteVersion();
        versionRecord.setQuote(quote);
        int currentVersion = quote.getVersions() == null ? 0 : quote.getVersions().size();
        versionRecord.setVersionNumber(currentVersion + 1);
        versionRecord.setPremiumAtVersion(calculatedPremium);
        versionRecord.setReason("Premium calculation triggered");
        versionRecord.setCreatedAt(Instant.now());

        if (quote.getVersions() == null) quote.setVersions(new HashSet<>());
        quote.getVersions().add(versionRecord);

        quote.setTotalPremium(calculatedPremium);
        quote.setStatus(QuoteStatus.CALCULATED);

        Quotation savedQuote = quotationRepository.save(quote);
        return quotationMapper.toResponse(savedQuote);
    }

    @Override
    @Transactional
    public QuoteResponse acceptQuote(UUID quoteId) {
        Quotation quote = quotationRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException("Quotation not found with ID: " + quoteId));

        if (quote.getStatus() != QuoteStatus.CALCULATED) {
            throw new InvalidQuoteStatusException("Only CALCULATED quotes can be accepted. Current status: " + quote.getStatus());
        }
        if (quote.getExpiresAt() != null && quote.getExpiresAt().isBefore(Instant.now())) {
            quote.setStatus(QuoteStatus.EXPIRED);
            quotationRepository.save(quote);
            throw new InvalidQuoteStatusException("Quote has expired and can no longer be accepted.");
        }

        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setAcceptedAt(Instant.now());
        Quotation savedQuote = quotationRepository.save(quote);
        
        log.info("Quote {} accepted.", quoteId);

        // Fire Kafka Event for Underwriting Service
        QuoteGeneratedEvent event = new QuoteGeneratedEvent(
                savedQuote.getQuoteId(),
                savedQuote.getCustomerId(),
                savedQuote.getPlanId(),
                savedQuote.getTotalPremium(),
                savedQuote.getStatus().name()
        );
        eventProducer.publishQuoteAcceptedEvent(event);

        return quotationMapper.toResponse(savedQuote);
    }

    @Override
    @Transactional
    public QuoteResponse rejectQuote(UUID quoteId) {
        Quotation quote = quotationRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException("Quotation not found with ID: " + quoteId));

        if (quote.getStatus() == QuoteStatus.ACCEPTED || quote.getStatus() == QuoteStatus.EXPIRED || quote.getStatus() == QuoteStatus.REJECTED) {
            throw new InvalidQuoteStatusException("Cannot reject quote in status: " + quote.getStatus());
        }

        quote.setStatus(QuoteStatus.REJECTED);
        quote.setRejectedAt(Instant.now());
        Quotation savedQuote = quotationRepository.save(quote);
        
        log.info("Quote {} rejected.", quoteId);
        return quotationMapper.toResponse(savedQuote);
    }
}