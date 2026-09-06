package com.healthinsurance.product.mapper;
import com.healthinsurance.product.dto.request.ProductRequest;
import com.healthinsurance.product.dto.response.ProductResponse;
import com.healthinsurance.product.entity.InsuranceProduct;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    InsuranceProduct toEntity(ProductRequest request);
    ProductResponse toResponse(InsuranceProduct entity);
    void updateEntityFromRequest(ProductRequest request, @MappingTarget InsuranceProduct entity);
}