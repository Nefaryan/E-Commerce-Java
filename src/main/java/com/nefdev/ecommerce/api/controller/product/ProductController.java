package com.nefdev.ecommerce.api.controller.product;

import com.nefdev.ecommerce.model.Product;
import com.nefdev.ecommerce.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Gets the list of products available.
     * @return The list of products.
     */
    @GetMapping
    public List<Product> getProducts() {
        return productService.getProducts();
    }
}
