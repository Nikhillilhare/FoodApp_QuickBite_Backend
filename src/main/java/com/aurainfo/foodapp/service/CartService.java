package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.entity.Cart;
import com.aurainfo.foodapp.entity.CartItem;

import java.util.List;

public interface CartService {

    Cart getOrCreateCart(Long customerId);

    Cart getCartByCustomerId(Long customerId);

    List<CartItem> getCartItems(Long customerId);

    CartItem addProductToCart(
            Long customerId,
            Long productId,
            int quantity
    );

    CartItem updateCartItemQuantity(
            Long customerId,
            Long productId,
            int quantity
    );

    void removeProductFromCart(
            Long customerId,
            Long productId
    );

    void clearCart(Long customerId);

    int getCartItemCount(Long customerId);
}