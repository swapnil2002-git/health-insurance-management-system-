package com.healthinsurance.product.service;
import com.healthinsurance.product.dto.request.ProductRequest;
import com.healthinsurance.product.dto.response.ProductResponse;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    List<ProductResponse> getAllProducts();
    ProductResponse getProduct(UUID productId);
    ProductResponse updateProduct(UUID productId, ProductRequest request);
}