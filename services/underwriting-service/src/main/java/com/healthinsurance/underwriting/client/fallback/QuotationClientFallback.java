package com.healthinsurance.underwriting.client.fallback;

import com.healthinsurance.underwriting.client.QuotationClient;
import com.healthinsurance.underwriting.client.dto.DependencyDto;
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
            public DependencyDto getQuote(UUID quoteId) {
                log.error("Fallback triggered for Underwriting QuotationClient.getQuote({}): {}", quoteId, cause.getMessage());
                throw new IllegalStateException("Quotation Service is temporarily unavailable for underwriting: " + quoteId, cause);
            }
        };
    }
}
