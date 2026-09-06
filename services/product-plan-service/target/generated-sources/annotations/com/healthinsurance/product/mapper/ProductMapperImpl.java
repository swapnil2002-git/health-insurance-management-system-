package com.healthinsurance.product.mapper;

import com.healthinsurance.product.dto.request.ProductRequest;
import com.healthinsurance.product.dto.response.ProductResponse;
import com.healthinsurance.product.entity.InsuranceProduct;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-01T11:12:48+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public InsuranceProduct toEntity(ProductRequest request) {
        if ( request == null ) {
            return null;
        }

        InsuranceProduct insuranceProduct = new InsuranceProduct();

        insuranceProduct.setName( request.getName() );
        insuranceProduct.setDescription( request.getDescription() );

        return insuranceProduct;
    }

    @Override
    public ProductResponse toResponse(InsuranceProduct entity) {
        if ( entity == null ) {
            return null;
        }

        ProductResponse productResponse = new ProductResponse();

        productResponse.setProductId( entity.getProductId() );
        productResponse.setName( entity.getName() );
        productResponse.setDescription( entity.getDescription() );
        productResponse.setStatus( entity.getStatus() );

        return productResponse;
    }

    @Override
    public void updateEntityFromRequest(ProductRequest request, InsuranceProduct entity) {
        if ( request == null ) {
            return;
        }

        entity.setName( request.getName() );
        entity.setDescription( request.getDescription() );
    }
}
