package com.healthinsurance.product.mapper;

import com.healthinsurance.product.dto.response.RuleResponse;
import com.healthinsurance.product.entity.Copayment;
import com.healthinsurance.product.entity.Coverage;
import com.healthinsurance.product.entity.Deductible;
import com.healthinsurance.product.entity.Exclusion;
import com.healthinsurance.product.entity.PlanCopayment;
import com.healthinsurance.product.entity.PlanCoverage;
import com.healthinsurance.product.entity.PlanDeductible;
import com.healthinsurance.product.entity.PlanExclusion;
import com.healthinsurance.product.entity.PlanRider;
import com.healthinsurance.product.entity.Rider;
import java.util.UUID;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-01T11:12:48+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class RuleMapperImpl implements RuleMapper {

    @Override
    public RuleResponse toCoverageResponse(PlanCoverage entity) {
        if ( entity == null ) {
            return null;
        }

        RuleResponse ruleResponse = new RuleResponse();

        ruleResponse.setId( entityCoverageCoverageId( entity ) );
        ruleResponse.setName( entityCoverageName( entity ) );
        ruleResponse.setDescription( entityCoverageDescription( entity ) );

        return ruleResponse;
    }

    @Override
    public RuleResponse toExclusionResponse(PlanExclusion entity) {
        if ( entity == null ) {
            return null;
        }

        RuleResponse ruleResponse = new RuleResponse();

        ruleResponse.setId( entityExclusionExclusionId( entity ) );
        ruleResponse.setName( entityExclusionName( entity ) );
        ruleResponse.setDescription( entityExclusionDescription( entity ) );

        return ruleResponse;
    }

    @Override
    public RuleResponse toDeductibleResponse(PlanDeductible entity) {
        if ( entity == null ) {
            return null;
        }

        RuleResponse ruleResponse = new RuleResponse();

        ruleResponse.setId( entityDeductibleDeductibleId( entity ) );
        ruleResponse.setName( entityDeductibleName( entity ) );
        ruleResponse.setDescription( entityDeductibleDescription( entity ) );

        return ruleResponse;
    }

    @Override
    public RuleResponse toCopaymentResponse(PlanCopayment entity) {
        if ( entity == null ) {
            return null;
        }

        RuleResponse ruleResponse = new RuleResponse();

        ruleResponse.setId( entityCopaymentCopaymentId( entity ) );
        ruleResponse.setName( entityCopaymentName( entity ) );
        ruleResponse.setDescription( entityCopaymentDescription( entity ) );

        return ruleResponse;
    }

    @Override
    public RuleResponse toRiderResponse(PlanRider entity) {
        if ( entity == null ) {
            return null;
        }

        RuleResponse ruleResponse = new RuleResponse();

        ruleResponse.setId( entityRiderRiderId( entity ) );
        ruleResponse.setName( entityRiderName( entity ) );
        ruleResponse.setDescription( entityRiderDescription( entity ) );

        return ruleResponse;
    }

    private UUID entityCoverageCoverageId(PlanCoverage planCoverage) {
        if ( planCoverage == null ) {
            return null;
        }
        Coverage coverage = planCoverage.getCoverage();
        if ( coverage == null ) {
            return null;
        }
        UUID coverageId = coverage.getCoverageId();
        if ( coverageId == null ) {
            return null;
        }
        return coverageId;
    }

    private String entityCoverageName(PlanCoverage planCoverage) {
        if ( planCoverage == null ) {
            return null;
        }
        Coverage coverage = planCoverage.getCoverage();
        if ( coverage == null ) {
            return null;
        }
        String name = coverage.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String entityCoverageDescription(PlanCoverage planCoverage) {
        if ( planCoverage == null ) {
            return null;
        }
        Coverage coverage = planCoverage.getCoverage();
        if ( coverage == null ) {
            return null;
        }
        String description = coverage.getDescription();
        if ( description == null ) {
            return null;
        }
        return description;
    }

    private UUID entityExclusionExclusionId(PlanExclusion planExclusion) {
        if ( planExclusion == null ) {
            return null;
        }
        Exclusion exclusion = planExclusion.getExclusion();
        if ( exclusion == null ) {
            return null;
        }
        UUID exclusionId = exclusion.getExclusionId();
        if ( exclusionId == null ) {
            return null;
        }
        return exclusionId;
    }

    private String entityExclusionName(PlanExclusion planExclusion) {
        if ( planExclusion == null ) {
            return null;
        }
        Exclusion exclusion = planExclusion.getExclusion();
        if ( exclusion == null ) {
            return null;
        }
        String name = exclusion.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String entityExclusionDescription(PlanExclusion planExclusion) {
        if ( planExclusion == null ) {
            return null;
        }
        Exclusion exclusion = planExclusion.getExclusion();
        if ( exclusion == null ) {
            return null;
        }
        String description = exclusion.getDescription();
        if ( description == null ) {
            return null;
        }
        return description;
    }

    private UUID entityDeductibleDeductibleId(PlanDeductible planDeductible) {
        if ( planDeductible == null ) {
            return null;
        }
        Deductible deductible = planDeductible.getDeductible();
        if ( deductible == null ) {
            return null;
        }
        UUID deductibleId = deductible.getDeductibleId();
        if ( deductibleId == null ) {
            return null;
        }
        return deductibleId;
    }

    private String entityDeductibleName(PlanDeductible planDeductible) {
        if ( planDeductible == null ) {
            return null;
        }
        Deductible deductible = planDeductible.getDeductible();
        if ( deductible == null ) {
            return null;
        }
        String name = deductible.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String entityDeductibleDescription(PlanDeductible planDeductible) {
        if ( planDeductible == null ) {
            return null;
        }
        Deductible deductible = planDeductible.getDeductible();
        if ( deductible == null ) {
            return null;
        }
        String description = deductible.getDescription();
        if ( description == null ) {
            return null;
        }
        return description;
    }

    private UUID entityCopaymentCopaymentId(PlanCopayment planCopayment) {
        if ( planCopayment == null ) {
            return null;
        }
        Copayment copayment = planCopayment.getCopayment();
        if ( copayment == null ) {
            return null;
        }
        UUID copaymentId = copayment.getCopaymentId();
        if ( copaymentId == null ) {
            return null;
        }
        return copaymentId;
    }

    private String entityCopaymentName(PlanCopayment planCopayment) {
        if ( planCopayment == null ) {
            return null;
        }
        Copayment copayment = planCopayment.getCopayment();
        if ( copayment == null ) {
            return null;
        }
        String name = copayment.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String entityCopaymentDescription(PlanCopayment planCopayment) {
        if ( planCopayment == null ) {
            return null;
        }
        Copayment copayment = planCopayment.getCopayment();
        if ( copayment == null ) {
            return null;
        }
        String description = copayment.getDescription();
        if ( description == null ) {
            return null;
        }
        return description;
    }

    private UUID entityRiderRiderId(PlanRider planRider) {
        if ( planRider == null ) {
            return null;
        }
        Rider rider = planRider.getRider();
        if ( rider == null ) {
            return null;
        }
        UUID riderId = rider.getRiderId();
        if ( riderId == null ) {
            return null;
        }
        return riderId;
    }

    private String entityRiderName(PlanRider planRider) {
        if ( planRider == null ) {
            return null;
        }
        Rider rider = planRider.getRider();
        if ( rider == null ) {
            return null;
        }
        String name = rider.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String entityRiderDescription(PlanRider planRider) {
        if ( planRider == null ) {
            return null;
        }
        Rider rider = planRider.getRider();
        if ( rider == null ) {
            return null;
        }
        String description = rider.getDescription();
        if ( description == null ) {
            return null;
        }
        return description;
    }
}
