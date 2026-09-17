package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.entity.Cart;
import com.aurainfo.foodapp.entity.CartItem;
import com.aurainfo.foodapp.entity.Product;
import com.aurainfo.foodapp.entity.ProductStatus;
import com.aurainfo.foodapp.entity.User;
import com.aurainfo.foodapp.repository.CartItemRepository;
import com.aurainfo.foodapp.repository.CartRepository;
import com.aurainfo.foodapp.repository.ProductRepository;
import com.aurainfo.foodapp.repository.UserRepository;
import com.aurainfo.foodapp.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public Cart getOrCreateCart(Long customerId) {

        validateCustomerId(customerId);

        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {

                    User customer = getCustomer(customerId);

                    Cart cart = Cart.builder()
                            .customer(customer)
                            .build();

                    return cartRepository.save(cart);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getCartByCustomerId(Long customerId) {

        validateCustomerId(customerId);

        return cartRepository.findByCustomerId(customerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart not found for customer id: "
                                        + customerId
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long customerId) {
        Cart cart = getCartByCustomerId(customerId);

        return cartItemRepository.findByCartIdWithProduct(cart.getId());
    }

    @Override
    public CartItem addProductToCart(
            Long customerId,
            Long productId,
            int quantity
    ) {

        validateCustomerId(customerId);
        validateProductId(productId);
        validatePositiveQuantity(quantity);

        Cart cart =
                getOrCreateCart(customerId);

        Product product =
                getProduct(productId);

        validateProductCanBeAdded(product);

        CartItem existingItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                productId
                        )
                        .orElse(null);

        if (existingItem != null) {

            int finalQuantity;

            try {

                finalQuantity = Math.addExact(
                        existingItem.getQuantity(),
                        quantity
                );

            } catch (ArithmeticException ex) {

                throw new IllegalArgumentException(
                        "Cart quantity is too large"
                );
            }

            validateStock(
                    product,
                    finalQuantity
            );

            existingItem.setQuantity(
                    finalQuantity
            );

            return cartItemRepository.save(
                    existingItem
            );
        }

        validateStock(
                product,
                quantity
        );

        CartItem cartItem =
                CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(quantity)
                        .build();

        return cartItemRepository.save(
                cartItem
        );
    }

    @Override
    public CartItem updateCartItemQuantity(
            Long customerId,
            Long productId,
            int quantity
    ) {

        validateCustomerId(customerId);
        validateProductId(productId);
        validatePositiveQuantity(quantity);

        Cart cart = getCartByCustomerId(customerId);

        Product product = getProduct(productId);

        validateProductCanBeAdded(product);

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                productId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product is not present in cart"
                                )
                        );

        validateStock(product, quantity);

        cartItem.setQuantity(quantity);

        return cartItemRepository.save(cartItem);
    }

    @Override
    public void removeProductFromCart(
            Long customerId,
            Long productId
    ) {

        validateCustomerId(customerId);
        validateProductId(productId);

        Cart cart = getCartByCustomerId(customerId);

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                productId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product is not present in cart"
                                )
                        );

        cartItemRepository.delete(cartItem);
    }

    @Override
    public void clearCart(Long customerId) {

        validateCustomerId(customerId);

        Cart cart = getCartByCustomerId(customerId);

        cartItemRepository.deleteByCartId(cart.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public int getCartItemCount(Long customerId) {

        return getCartItems(customerId)
                .stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    private Product getProduct(Long productId) {

        validateProductId(productId);
        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found with id: "
                                        + productId
                        )
                );
    }

    private User getCustomer(Long customerId) {

        return userRepository.findById(customerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer not found with id: "
                                        + customerId
                        )
                );
    }

    private void validateProductCanBeAdded(Product product) {

        if (product.getStatus() != ProductStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Inactive product cannot be added to cart"
            );
        }
    }

    private void validateStock(
            Product product,
            int requestedQuantity
    ) {

        if (requestedQuantity > product.getStockQuantity()) {

            throw new IllegalStateException(
                    "Insufficient stock for product: "
                            + product.getName()
                            + ". Available: "
                            + product.getStockQuantity()
                            + ", requested: "
                            + requestedQuantity
            );
        }
    }

    private void validatePositiveQuantity(int quantity) {

        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "Cart quantity must be greater than zero"
            );
        }
    }

    private void validateCustomerId(Long customerId) {

        if (customerId == null || customerId <= 0) {

            throw new IllegalArgumentException(
                    "Customer ID must be greater than zero"
            );
        }
    }

    private void validateProductId(Long productId) {

        if (productId == null || productId <= 0) {

            throw new IllegalArgumentException(
                    "Product ID must be greater than zero"
            );
        }
    }
}