package com.healthinsurance.product.service.impl;
import com.healthinsurance.product.dto.request.ProductRequest;
import com.healthinsurance.product.dto.response.ProductResponse;
import com.healthinsurance.product.entity.InsuranceProduct;
import com.healthinsurance.product.exception.ProductNotFoundException;
import com.healthinsurance.product.mapper.ProductMapper;
import com.healthinsurance.product.repository.InsuranceProductRepository;
import com.healthinsurance.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final InsuranceProductRepository repository;
    private final ProductMapper mapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        InsuranceProduct product = mapper.toEntity(request);
        product.setStatus("ACTIVE");
        return mapper.toResponse(repository.save(product));
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return repository.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProduct(UUID productId) {
        InsuranceProduct product = repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        return mapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID productId, ProductRequest request) {
        InsuranceProduct product = repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        mapper.updateEntityFromRequest(request, product);
        return mapper.toResponse(repository.save(product));
    }
}