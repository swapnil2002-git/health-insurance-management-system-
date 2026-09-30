package com.healthinsurance.premium.client.fallback;

import com.healthinsurance.premium.client.QuotationClient;
import com.healthinsurance.premium.client.dto.QuoteResponseDto;
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
            public QuoteResponseDto getQuote(UUID id) {
                log.error("Fallback triggered for Premium QuotationClient.getQuote({}): {}", id, cause.getMessage());
                throw new IllegalStateException("Quotation Service is temporarily unavailable for premium calculation: " + id, cause);
            }
        };
    }
}
