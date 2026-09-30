package com.healthinsurance.underwriting.client;
import com.healthinsurance.underwriting.client.dto.DependencyDto;
import com.healthinsurance.underwriting.client.fallback.RiskAssessmentClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "risk-service", url = "${risk.service.url:http://localhost:8086}", fallbackFactory = RiskAssessmentClientFallback.class)
public interface RiskAssessmentClient {
    @GetMapping("/api/risk-assessments/{assessmentId}")
    DependencyDto getAssessment(@PathVariable("assessmentId") UUID assessmentId);
}