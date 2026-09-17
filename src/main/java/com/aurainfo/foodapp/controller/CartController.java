package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.request.AddCartItemRequest;
import com.aurainfo.foodapp.dto.request.UpdateCartItemRequest;
import com.aurainfo.foodapp.dto.response.CartItemResponse;
import com.aurainfo.foodapp.dto.response.CartResponse;
import com.aurainfo.foodapp.entity.Cart;
import com.aurainfo.foodapp.entity.CartItem;
import com.aurainfo.foodapp.entity.Product;
import com.aurainfo.foodapp.entity.User;
import com.aurainfo.foodapp.service.CartService;
import com.aurainfo.foodapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    // =====================================================
    // GET CART
    // =====================================================

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication
    ) {

        Long customerId = getAuthenticatedCustomerId(authentication);

        Cart cart = cartService.getOrCreateCart(customerId);

        List<CartItemResponse> items =
                cartService.getCartItems(customerId)
                        .stream()
                        .map(this::mapToCartItemResponse)
                        .toList();

        int totalItemCount = items.stream().mapToInt(
                                CartItemResponse::getQuantity).sum();

        CartResponse response =
                CartResponse.builder()
                        .cartId(cart.getId())
                        .customerId(customerId)
                        .items(items)
                        .totalItemCount(totalItemCount)
                        .build();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // ADD PRODUCT TO CART
    // =====================================================

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addProductToCart(
            @Valid @RequestBody AddCartItemRequest request,
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        CartItem cartItem =
                cartService.addProductToCart(
                        customerId,
                        request.getProductId(),
                        request.getQuantity()
                );

        return ResponseEntity.ok(
                mapToCartItemResponse(cartItem)
        );
    }

    // =====================================================
    // UPDATE CART ITEM QUANTITY
    // =====================================================

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartItemResponse>
    updateCartItemQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        CartItem updatedCartItem =
                cartService.updateCartItemQuantity(
                        customerId,
                        productId,
                        request.getQuantity()
                );

        return ResponseEntity.ok(
                mapToCartItemResponse(updatedCartItem)
        );
    }

    // =====================================================
    // REMOVE PRODUCT FROM CART
    // =====================================================

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<String> removeProductFromCart(
            @PathVariable Long productId,
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        cartService.removeProductFromCart(
                customerId,
                productId
        );

        return ResponseEntity.ok(
                "Product removed from cart successfully"
        );
    }

    // =====================================================
    // CLEAR CART
    // =====================================================

    @DeleteMapping
    public ResponseEntity<String> clearCart(
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        cartService.clearCart(customerId);

        return ResponseEntity.ok(
                "Cart cleared successfully"
        );
    }

    // =====================================================
    // AUTHENTICATED CUSTOMER
    // =====================================================

    private Long getAuthenticatedCustomerId(
            Authentication authentication
    ) {

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new IllegalStateException(
                    "Authenticated customer could not be identified"
            );
        }

        User user =
                userService.getUserByEmail(
                        authentication.getName()
                );

        if (user == null) {
            throw new IllegalStateException(
                    "Authenticated customer could not be found"
            );
        }

        return user.getId();
    }

    // =====================================================
    // CART ITEM MAPPER
    // =====================================================

    private CartItemResponse mapToCartItemResponse(
            CartItem cartItem
    ) {

        Product product =
                cartItem.getProduct();
        return CartItemResponse.builder().productId(product.getId())
                .productName(product.getName())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .quantity(cartItem.getQuantity())
                .addedAt(cartItem.getAddedAt())
                .build();
    }
}