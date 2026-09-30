package com.healthinsurance.policy.client;
import com.healthinsurance.policy.client.dto.QuoteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "quotation-service", path = "/api/quotes", url = "${quotation.service.url:http://localhost:8085}")
public interface QuotationClient {
    @GetMapping("/{id}")
    QuoteDto getQuoteById(@PathVariable("id") UUID id);
}