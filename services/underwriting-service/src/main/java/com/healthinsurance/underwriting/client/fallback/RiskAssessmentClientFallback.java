package com.healthinsurance.underwriting.client.fallback;

import com.healthinsurance.underwriting.client.RiskAssessmentClient;
import com.healthinsurance.underwriting.client.dto.DependencyDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class RiskAssessmentClientFallback implements FallbackFactory<RiskAssessmentClient> {

    @Override
    public RiskAssessmentClient create(Throwable cause) {
        return new RiskAssessmentClient() {
            @Override
            public DependencyDto getAssessment(UUID assessmentId) {
                log.error("Fallback triggered for Underwriting RiskAssessmentClient.getAssessment({}): {}", assessmentId, cause.getMessage());
                throw new IllegalStateException("Risk Assessment Service is temporarily unavailable for underwriting: " + assessmentId, cause);
            }
        };
    }
}
