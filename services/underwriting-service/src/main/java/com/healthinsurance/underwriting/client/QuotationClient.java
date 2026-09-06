package com.healthinsurance.underwriting.client;
import com.healthinsurance.underwriting.client.dto.DependencyDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "quotation-service")
public interface QuotationClient {
    @GetMapping("/api/quotes/{quoteId}")
    DependencyDto getQuote(@PathVariable("quoteId") UUID quoteId);
}