package com.aurainfo.foodapp.repository;

import com.aurainfo.foodapp.entity.Product;
import com.aurainfo.foodapp.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByCategoryIdAndStatus(
            Long categoryId,
            ProductStatus status
    );

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByStockQuantityLessThanEqual(Integer quantity);

    // =====================================================
    // PRODUCT + CATEGORY FETCH
    // =====================================================

    @Query("""
            SELECT p
            FROM Product p
            JOIN FETCH p.category
            WHERE p.id = :productId
            """)
    Optional<Product> findByIdWithCategory(
            @Param("productId") Long productId
    );

    @Query("""
            SELECT p
            FROM Product p
            JOIN FETCH p.category
            """)
    List<Product> findAllWithCategory();


    @Query("""
            SELECT p
            FROM Product p
            JOIN FETCH p.category
            WHERE p.status = :status
            """)
    List<Product> findByStatusWithCategory(
            @Param("status") ProductStatus status
    );

    @Query("""
            SELECT p
            FROM Product p
            JOIN FETCH p.category
            WHERE p.category.id = :categoryId
              AND p.status = :status
            """)
    List<Product> findByCategoryIdAndStatusWithCategory(
            @Param("categoryId") Long categoryId,
            @Param("status") ProductStatus status
    );

    @Query("""
            SELECT p
            FROM Product p
            JOIN FETCH p.category
            WHERE LOWER(p.name)
                  LIKE LOWER(CONCAT('%', :name, '%'))
            """)
    List<Product> searchByNameWithCategory(
            @Param("name") String name
    );

    @Query("""
            SELECT p
            FROM Product p
            JOIN FETCH p.category
            WHERE p.stockQuantity <= p.minStockThreshold
            """)
    List<Product> findLowStockProducts();
}