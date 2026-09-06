package com.healthinsurance.quotation.controller;

import com.healthinsurance.quotation.dto.request.CreateQuoteRequest;
import com.healthinsurance.quotation.dto.response.QuoteResponse;
import com.healthinsurance.quotation.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    public ResponseEntity<QuoteResponse> createQuote(@Valid @RequestBody CreateQuoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quoteService.createQuote(request));
    }

    @GetMapping("/{quoteId}")
    public ResponseEntity<QuoteResponse> getQuote(@PathVariable("quoteId") UUID quoteId) {
        return ResponseEntity.ok(quoteService.getQuote(quoteId));
    }

    @GetMapping
    public ResponseEntity<List<QuoteResponse>> getAllQuotes() {
        return ResponseEntity.ok(quoteService.getAllQuotes());
    }

    @PostMapping("/{quoteId}/calculate")
    public ResponseEntity<QuoteResponse> calculatePremium(@PathVariable("quoteId") UUID quoteId) {
        return ResponseEntity.ok(quoteService.calculatePremium(quoteId));
    }

    @PostMapping("/{quoteId}/accept")
    public ResponseEntity<QuoteResponse> acceptQuote(@PathVariable("quoteId") UUID quoteId) {
        return ResponseEntity.ok(quoteService.acceptQuote(quoteId));
    }

    @PostMapping("/{quoteId}/reject")
    public ResponseEntity<QuoteResponse> rejectQuote(@PathVariable("quoteId") UUID quoteId) {
        return ResponseEntity.ok(quoteService.rejectQuote(quoteId));
    }
}