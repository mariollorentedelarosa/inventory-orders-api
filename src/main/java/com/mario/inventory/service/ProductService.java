package com.mario.inventory.service;

import com.mario.inventory.domain.Product;
import com.mario.inventory.exception.BusinessRuleException;
import com.mario.inventory.exception.NotFoundException;
import com.mario.inventory.repository.ProductRepository;
import com.mario.inventory.web.dto.ProductDtos.ProductRequest;
import com.mario.inventory.web.dto.ProductDtos.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public Page<ProductResponse> search(String query, Pageable pageable) {
        Page<Product> page = (query == null || query.isBlank())
                ? products.findAll(pageable)
                : products.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(query, query, pageable);
        return page.map(ProductResponse::from);
    }

    public ProductResponse get(Long id) {
        return ProductResponse.from(find(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (products.existsBySku(request.sku())) {
            throw new BusinessRuleException("Ya existe un producto con SKU " + request.sku());
        }
        Product saved = products.save(new Product(request.sku(), request.name(), request.price(), request.minStock()));
        return ProductResponse.from(saved);
    }

    /** El SKU no se modifica: es el identificador de negocio del producto. */
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = find(id);
        if (!product.getSku().equals(request.sku())) {
            throw new BusinessRuleException("El SKU de un producto no se puede cambiar");
        }
        product.update(request.name(), request.price(), request.minStock());
        return ProductResponse.from(product);
    }

    private Product find(Long id) {
        return products.findById(id).orElseThrow(() -> new NotFoundException("Producto", id));
    }
}
