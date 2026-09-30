package com.healthinsurance.product.service;

import com.healthinsurance.product.dto.request.ProductRequest;
import com.healthinsurance.product.dto.response.ProductResponse;
import com.healthinsurance.product.entity.InsuranceProduct;
import com.healthinsurance.product.exception.ProductNotFoundException;
import com.healthinsurance.product.mapper.ProductMapper;
import com.healthinsurance.product.repository.InsuranceProductRepository;
import com.healthinsurance.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private InsuranceProductRepository repository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_ShouldSaveAndReturnResponse() {
        ProductRequest request = new ProductRequest();
        request.setName("Family Health Shield");

        InsuranceProduct product = new InsuranceProduct();
        product.setProductId(UUID.randomUUID());
        product.setName(request.getName());

        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setName(product.getName());

        when(mapper.toEntity(request)).thenReturn(product);
        when(repository.save(any(InsuranceProduct.class))).thenReturn(product);
        when(mapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals("Family Health Shield", result.getName());
        verify(repository, times(1)).save(product);
    }

    @Test
    void getAllProducts_ShouldReturnList() {
        InsuranceProduct product = new InsuranceProduct();
        ProductResponse response = new ProductResponse();

        when(repository.findAll()).thenReturn(Collections.singletonList(product));
        when(mapper.toResponse(product)).thenReturn(response);

        List<ProductResponse> list = productService.getAllProducts();

        assertNotNull(list);
        assertEquals(1, list.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void getProduct_WhenNotFound_ShouldThrowException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProduct(id));
    }
}