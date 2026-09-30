package com.healthinsurance.risk.client.fallback;

import com.healthinsurance.risk.client.QuotationClient;
import com.healthinsurance.risk.client.dto.DependencyDto;
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
                log.error("Fallback triggered for Risk QuotationClient.getQuote({}): {}", quoteId, cause.getMessage());
                throw new IllegalStateException("Quotation Service is temporarily unavailable for risk assessment: " + quoteId, cause);
            }
        };
    }
}
