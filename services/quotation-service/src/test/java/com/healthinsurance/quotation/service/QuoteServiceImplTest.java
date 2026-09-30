package com.healthinsurance.quotation.service;

import com.healthinsurance.quotation.client.CustomerClient;
import com.healthinsurance.quotation.client.ProductClient;
import com.healthinsurance.quotation.dto.response.QuoteResponse;
import com.healthinsurance.quotation.entity.Quotation;
import com.healthinsurance.quotation.enums.QuoteStatus;
import com.healthinsurance.quotation.event.QuoteEventProducer;
import com.healthinsurance.quotation.event.QuoteGeneratedEvent;
import com.healthinsurance.quotation.mapper.QuotationMapper;
import com.healthinsurance.quotation.repository.QuotationRepository;
import com.healthinsurance.quotation.service.impl.QuoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceImplTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private QuotationMapper quotationMapper;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private ProductClient productClient;

    @Mock
    private QuoteEventProducer eventProducer;

    @InjectMocks
    private QuoteServiceImpl quoteService;

    private Quotation testQuote;
    private UUID quoteId;

    @BeforeEach
    void setUp() {
        quoteId = UUID.randomUUID();
        testQuote = new Quotation();
        testQuote.setQuoteId(quoteId);
        testQuote.setCustomerId(UUID.randomUUID());
        testQuote.setPlanId(UUID.randomUUID());
        testQuote.setQuoteNumber("Q-12345678");
        testQuote.setStatus(QuoteStatus.CALCULATED);
        testQuote.setTotalPremium(new BigDecimal("900.00"));
        testQuote.setExpiresAt(Instant.now().plusSeconds(86400));
    }

    @Test
    void acceptQuote_PublishesStandardizedQuoteGeneratedEvent() {
        when(quotationRepository.findById(quoteId)).thenReturn(Optional.of(testQuote));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> i.getArgument(0));

        QuoteResponse response = new QuoteResponse();
        response.setQuoteId(quoteId);
        response.setStatus(QuoteStatus.ACCEPTED.name());
        when(quotationMapper.toResponse(any(Quotation.class))).thenReturn(response);

        QuoteResponse result = quoteService.acceptQuote(quoteId);

        assertNotNull(result);
        assertEquals("ACCEPTED", result.getStatus());

        ArgumentCaptor<QuoteGeneratedEvent> captor = ArgumentCaptor.forClass(QuoteGeneratedEvent.class);
        verify(eventProducer, times(1)).publishQuoteAcceptedEvent(captor.capture());

        QuoteGeneratedEvent published = captor.getValue();
        assertEquals(quoteId, published.getQuoteId());
        assertEquals(testQuote.getCustomerId(), published.getCustomerId());
        assertEquals(testQuote.getPlanId(), published.getPlanId());
        assertEquals(new BigDecimal("900.00"), published.getTotalPremium());
        assertEquals(QuoteStatus.ACCEPTED.name(), published.getStatus());
    }
}
