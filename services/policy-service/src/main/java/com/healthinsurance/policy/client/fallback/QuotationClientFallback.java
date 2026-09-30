package com.healthinsurance.policy.client.fallback;

import com.healthinsurance.policy.client.QuotationClient;
import com.healthinsurance.policy.client.dto.QuoteDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class QuotationClientFallback implements FallbackFactory<QuotationClient> {

    @Override
    public QuotationClient create(Throwable cause) {
        return new QuotationClient() {
            @Override
            public QuoteDto getQuoteById(UUID id) {
                log.error("Fallback triggered for Policy QuotationClient.getQuoteById({}): {}", id, cause.getMessage());
                throw new IllegalStateException("Quotation Service is temporarily unavailable for policy issuance: " + id, cause);
            }
        };
    }
}
