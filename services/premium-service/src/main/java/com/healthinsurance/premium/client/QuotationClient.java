package com.healthinsurance.premium.client;

import com.healthinsurance.premium.client.dto.QuoteResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "quotation-service", path = "/api/quotes")
public interface QuotationClient {

    @GetMapping("/{id}")
    QuoteResponseDto getQuote(@PathVariable("id") UUID id);
}