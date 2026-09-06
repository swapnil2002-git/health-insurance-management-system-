package com.healthinsurance.customer.mapper;
import com.healthinsurance.customer.dto.request.MemberRequest;
import com.healthinsurance.customer.dto.response.MemberResponse;
import com.healthinsurance.customer.entity.InsuredMember;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    InsuredMember toEntity(MemberRequest request);
    MemberResponse toResponse(InsuredMember entity);
    void updateEntityFromRequest(MemberRequest request, @MappingTarget InsuredMember entity);
}