package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.entity.Product;
import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    Product getProductById(Long productid);
    List<Product> getAllProduct();
    List<Product> getActiveProduct();
    List<Product> getProductByCategory(Long categoryId);
    List<Product> searchProduct(String name);
    Product updateProduct(Long productId, Product updateProduct);
    void deactiveProduct(Long productId);

    void activateProduct(Long productId);

}
