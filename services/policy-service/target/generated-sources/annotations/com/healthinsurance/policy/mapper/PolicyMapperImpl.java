package com.healthinsurance.policy.mapper;

import com.healthinsurance.policy.dto.request.PolicyBeneficiaryRequest;
import com.healthinsurance.policy.dto.request.PolicyCoverageRequest;
import com.healthinsurance.policy.dto.request.PolicyCreateRequest;
import com.healthinsurance.policy.dto.request.PolicyMemberRequest;
import com.healthinsurance.policy.dto.response.PolicyBeneficiaryResponse;
import com.healthinsurance.policy.dto.response.PolicyCancellationResponse;
import com.healthinsurance.policy.dto.response.PolicyCoverageResponse;
import com.healthinsurance.policy.dto.response.PolicyEndorsementResponse;
import com.healthinsurance.policy.dto.response.PolicyMemberResponse;
import com.healthinsurance.policy.dto.response.PolicyResponse;
import com.healthinsurance.policy.entity.Policy;
import com.healthinsurance.policy.entity.PolicyBeneficiary;
import com.healthinsurance.policy.entity.PolicyCancellation;
import com.healthinsurance.policy.entity.PolicyCoverage;
import com.healthinsurance.policy.entity.PolicyEndorsement;
import com.healthinsurance.policy.entity.PolicyMember;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/*
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-05T13:16:51+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
*/
@Component
public class PolicyMapperImpl implements PolicyMapper {

    @Override
    public Policy toEntity(PolicyCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Policy policy = new Policy();

        policy.setCustomerId( request.getCustomerId() );
        policy.setPlanId( request.getPlanId() );
        policy.setQuoteId( request.getQuoteId() );
        policy.setEffectiveDate( request.getEffectiveDate() );
        policy.setExpiryDate( request.getExpiryDate() );
        policy.setMembers( policyMemberRequestListToPolicyMemberSet( request.getMembers() ) );
        policy.setCoverages( policyCoverageRequestListToPolicyCoverageSet( request.getCoverages() ) );
        policy.setBeneficiaries( policyBeneficiaryRequestListToPolicyBeneficiarySet( request.getBeneficiaries() ) );

        return policy;
    }

    @Override
    public PolicyMember toEntity(PolicyMemberRequest request) {
        if ( request == null ) {
            return null;
        }

        PolicyMember policyMember = new PolicyMember();

        policyMember.setMemberId( request.getMemberId() );

        return policyMember;
    }

    @Override
    public PolicyCoverage toEntity(PolicyCoverageRequest request) {
        if ( request == null ) {
            return null;
        }

        PolicyCoverage policyCoverage = new PolicyCoverage();

        policyCoverage.setCoverageName( request.getCoverageName() );
        policyCoverage.setCoverageAmount( request.getCoverageAmount() );
        policyCoverage.setDeductible( request.getDeductible() );

        return policyCoverage;
    }

    @Override
    public PolicyBeneficiary toEntity(PolicyBeneficiaryRequest request) {
        if ( request == null ) {
            return null;
        }

        PolicyBeneficiary policyBeneficiary = new PolicyBeneficiary();

        policyBeneficiary.setBeneficiaryName( request.getBeneficiaryName() );
        policyBeneficiary.setRelationship( request.getRelationship() );
        policyBeneficiary.setPercentage( request.getPercentage() );

        return policyBeneficiary;
    }

    @Override
    public PolicyResponse toResponse(Policy entity) {
        if ( entity == null ) {
            return null;
        }

        PolicyResponse policyResponse = new PolicyResponse();

        policyResponse.setPolicyId( entity.getPolicyId() );
        policyResponse.setPolicyNumber( entity.getPolicyNumber() );
        policyResponse.setCustomerId( entity.getCustomerId() );
        policyResponse.setPlanId( entity.getPlanId() );
        policyResponse.setQuoteId( entity.getQuoteId() );
        if ( entity.getStatus() != null ) {
            policyResponse.setStatus( entity.getStatus().name() );
        }
        policyResponse.setEffectiveDate( entity.getEffectiveDate() );
        policyResponse.setExpiryDate( entity.getExpiryDate() );
        policyResponse.setCreatedAt( entity.getCreatedAt() );
        policyResponse.setMembers( policyMemberSetToPolicyMemberResponseList( entity.getMembers() ) );
        policyResponse.setCoverages( policyCoverageSetToPolicyCoverageResponseList( entity.getCoverages() ) );
        policyResponse.setBeneficiaries( policyBeneficiarySetToPolicyBeneficiaryResponseList( entity.getBeneficiaries() ) );

        return policyResponse;
    }

    @Override
    public PolicyMemberResponse toResponse(PolicyMember entity) {
        if ( entity == null ) {
            return null;
        }

        PolicyMemberResponse policyMemberResponse = new PolicyMemberResponse();

        policyMemberResponse.setPolicyMemberId( entity.getPolicyMemberId() );
        policyMemberResponse.setMemberId( entity.getMemberId() );

        return policyMemberResponse;
    }

    @Override
    public PolicyCoverageResponse toResponse(PolicyCoverage entity) {
        if ( entity == null ) {
            return null;
        }

        PolicyCoverageResponse policyCoverageResponse = new PolicyCoverageResponse();

        policyCoverageResponse.setPolicyCoverageId( entity.getPolicyCoverageId() );
        policyCoverageResponse.setCoverageName( entity.getCoverageName() );
        policyCoverageResponse.setCoverageAmount( entity.getCoverageAmount() );
        policyCoverageResponse.setDeductible( entity.getDeductible() );

        return policyCoverageResponse;
    }

    @Override
    public PolicyBeneficiaryResponse toResponse(PolicyBeneficiary entity) {
        if ( entity == null ) {
            return null;
        }

        PolicyBeneficiaryResponse policyBeneficiaryResponse = new PolicyBeneficiaryResponse();

        policyBeneficiaryResponse.setPolicyBeneficiaryId( entity.getPolicyBeneficiaryId() );
        policyBeneficiaryResponse.setBeneficiaryName( entity.getBeneficiaryName() );
        policyBeneficiaryResponse.setRelationship( entity.getRelationship() );
        policyBeneficiaryResponse.setPercentage( entity.getPercentage() );

        return policyBeneficiaryResponse;
    }

    @Override
    public PolicyEndorsementResponse toResponse(PolicyEndorsement entity) {
        if ( entity == null ) {
            return null;
        }

        PolicyEndorsementResponse policyEndorsementResponse = new PolicyEndorsementResponse();

        policyEndorsementResponse.setEndorsementId( entity.getEndorsementId() );
        policyEndorsementResponse.setDescription( entity.getDescription() );
        policyEndorsementResponse.setAppliedAt( entity.getAppliedAt() );

        return policyEndorsementResponse;
    }

    @Override
    public PolicyCancellationResponse toResponse(PolicyCancellation entity) {
        if ( entity == null ) {
            return null;
        }

        PolicyCancellationResponse policyCancellationResponse = new PolicyCancellationResponse();

        policyCancellationResponse.setCancellationId( entity.getCancellationId() );
        policyCancellationResponse.setReason( entity.getReason() );
        policyCancellationResponse.setCancelledAt( entity.getCancelledAt() );

        return policyCancellationResponse;
    }

    protected Set<PolicyMember> policyMemberRequestListToPolicyMemberSet(List<PolicyMemberRequest> list) {
        if ( list == null ) {
            return null;
        }

        Set<PolicyMember> set = new LinkedHashSet<PolicyMember>( Math.max( (int) ( list.size() / .75f ) + 1, 16 ) );
        for ( PolicyMemberRequest policyMemberRequest : list ) {
            set.add( toEntity( policyMemberRequest ) );
        }

        return set;
    }

    protected Set<PolicyCoverage> policyCoverageRequestListToPolicyCoverageSet(List<PolicyCoverageRequest> list) {
        if ( list == null ) {
            return null;
        }

        Set<PolicyCoverage> set = new LinkedHashSet<PolicyCoverage>( Math.max( (int) ( list.size() / .75f ) + 1, 16 ) );
        for ( PolicyCoverageRequest policyCoverageRequest : list ) {
            set.add( toEntity( policyCoverageRequest ) );
        }

        return set;
    }

    protected Set<PolicyBeneficiary> policyBeneficiaryRequestListToPolicyBeneficiarySet(List<PolicyBeneficiaryRequest> list) {
        if ( list == null ) {
            return null;
        }

        Set<PolicyBeneficiary> set = new LinkedHashSet<PolicyBeneficiary>( Math.max( (int) ( list.size() / .75f ) + 1, 16 ) );
        for ( PolicyBeneficiaryRequest policyBeneficiaryRequest : list ) {
            set.add( toEntity( policyBeneficiaryRequest ) );
        }

        return set;
    }

    protected List<PolicyMemberResponse> policyMemberSetToPolicyMemberResponseList(Set<PolicyMember> set) {
        if ( set == null ) {
            return null;
        }

        List<PolicyMemberResponse> list = new ArrayList<PolicyMemberResponse>( set.size() );
        for ( PolicyMember policyMember : set ) {
            list.add( toResponse( policyMember ) );
        }

        return list;
    }

    protected List<PolicyCoverageResponse> policyCoverageSetToPolicyCoverageResponseList(Set<PolicyCoverage> set) {
        if ( set == null ) {
            return null;
        }

        List<PolicyCoverageResponse> list = new ArrayList<PolicyCoverageResponse>( set.size() );
        for ( PolicyCoverage policyCoverage : set ) {
            list.add( toResponse( policyCoverage ) );
        }

        return list;
    }

    protected List<PolicyBeneficiaryResponse> policyBeneficiarySetToPolicyBeneficiaryResponseList(Set<PolicyBeneficiary> set) {
        if ( set == null ) {
            return null;
        }

        List<PolicyBeneficiaryResponse> list = new ArrayList<PolicyBeneficiaryResponse>( set.size() );
        for ( PolicyBeneficiary policyBeneficiary : set ) {
            list.add( toResponse( policyBeneficiary ) );
        }

        return list;
    }
}
