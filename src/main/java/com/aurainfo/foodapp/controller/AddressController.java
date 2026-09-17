package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.request.CreateAddressRequest;
import com.aurainfo.foodapp.dto.request.UpdateAddressRequest;
import com.aurainfo.foodapp.dto.response.AddressResponse;
import com.aurainfo.foodapp.entity.Address;
import com.aurainfo.foodapp.entity.User;
import com.aurainfo.foodapp.service.AddressService;
import com.aurainfo.foodapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;

    // =====================================================
    // CREATE ADDRESS
    // =====================================================

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @Valid @RequestBody CreateAddressRequest request,
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        Address address = Address.builder()
                .addressLine(request.getAddressLine().trim())
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .pincode(request.getPincode().trim())
                .defaultAddress(  Boolean.TRUE.equals(
                                request.getDefaultAddress()))
                .build();

        Address savedAddress =
                addressService.createAddress(
                        customerId,
                        address
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToResponse(savedAddress));
    }

    // =====================================================
    // GET ALL CUSTOMER ADDRESSES
    // =====================================================

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        List<AddressResponse> response =
                addressService.getAddressesByUser(customerId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // UPDATE ADDRESS
    // =====================================================

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequest request,
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        Address existingAddress =
                getOwnedAddress(addressId, customerId);

        Address updatedAddress = Address.builder()
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .defaultAddress(request.getDefaultAddress())
                .build();

        Address savedAddress =
                addressService.updateAddress(
                        existingAddress.getId(),
                        updatedAddress
                );

        return ResponseEntity.ok(
                mapToResponse(savedAddress)
        );
    }

    // =====================================================
    // DELETE ADDRESS
    // =====================================================

    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @PathVariable Long addressId,
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        getOwnedAddress(addressId, customerId);

        addressService.deleteAddress(addressId);

        return ResponseEntity.ok(
                "Address deleted successfully"
        );
    }

    // =====================================================
    // SET DEFAULT ADDRESS
    // =====================================================

    @PatchMapping("/{addressId}/set-default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @PathVariable Long addressId,
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        getOwnedAddress(addressId, customerId);

        addressService.setDefaultAddress(addressId);

        Address updatedAddress =
                addressService.getAddressById(addressId);

        return ResponseEntity.ok(
                mapToResponse(updatedAddress)
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
    // OWNERSHIP CHECK
    // =====================================================

    private Address getOwnedAddress(
            Long addressId,
            Long customerId
    ) {

        Address address =
                addressService.getAddressById(addressId);

        if (address.getUser() == null ||
                address.getUser().getId() == null ||
                !address.getUser()
                        .getId()
                        .equals(customerId)) {

            throw new IllegalArgumentException(
                    "Address does not belong to the current customer"
            );
        }

        return address;
    }

    // =====================================================
    // RESPONSE MAPPER
    // =====================================================

    private AddressResponse mapToResponse(
            Address address
    ) {

        return AddressResponse.builder()
                .id(address.getId())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .defaultAddress(address.getDefaultAddress())
                .createdAt(address.getCreatedAt())
                .build();
    }
}