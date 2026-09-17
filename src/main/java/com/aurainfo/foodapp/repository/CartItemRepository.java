package com.aurainfo.foodapp.repository;

import com.aurainfo.foodapp.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    @Query("""
            SELECT ci
            FROM CartItem ci
            JOIN FETCH ci.product
            WHERE ci.cart.id = :cartId
            ORDER BY ci.addedAt ASC
            """)
    List<CartItem> findByCartIdWithProduct(
            @Param("cartId") Long cartId
    );

    Optional<CartItem> findByCartIdAndProductId(
            Long cartId,
            Long productId);

    boolean existsByCartIdAndProductId(
            Long cartId,
            Long productId
    );

    void deleteByCartId(Long cartId);
}