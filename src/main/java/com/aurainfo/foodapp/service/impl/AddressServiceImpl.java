package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.entity.Address;
import com.aurainfo.foodapp.entity.User;
import com.aurainfo.foodapp.repository.AddressRepository;
import com.aurainfo.foodapp.repository.UserRepository;
import com.aurainfo.foodapp.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public Address createAddress(Long userId, Address address) {

        validateUserId(userId);
        validateAddress(address);

        User user = getUser(userId);

        address.setUser(user);

        /*
         * First address of a user should become default automatically.
         */
        List<Address> existingAddresses =
                addressRepository.findByUserIdOrderByIdAsc(userId);

        if (existingAddresses.isEmpty()) {
            address.setDefaultAddress(true);
        }

        /*
         * If caller explicitly wants this address as default,
         * remove default from the previous default address.
         */
        if (Boolean.TRUE.equals(address.getDefaultAddress())) {
            clearExistingDefaultAddress(userId);
        }
        /*
         * Never allow NULL for a non-null database column.
         */
        if (address.getDefaultAddress() == null) {

            address.setDefaultAddress(false);
        }
        return addressRepository.save(address);
    }

    @Override
    @Transactional(readOnly = true)
    public Address getAddressById(Long addressId) {

        validateAddressId(addressId);

        return addressRepository.findById(addressId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found with id: " + addressId
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> getAddressesByUser(Long userId) {

        validateUserId(userId);

        /*
         * Validate that user exists.
         */
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException(
                    "User not found with id: " + userId
            );
        }

        return addressRepository.findByUserIdOrderByIdAsc(userId);
    }

    @Override
    public Address updateAddress(
            Long addressId,
            Address updatedAddress
    ) {

        validateAddressId(addressId);

        if (updatedAddress == null) {
            throw new IllegalArgumentException(
                    "Address data cannot be null"
            );
        }

        Address existingAddress = getAddressById(addressId);

        if (updatedAddress.getAddressLine() != null &&
                !updatedAddress.getAddressLine().isBlank()) {

            existingAddress.setAddressLine(
                    updatedAddress.getAddressLine().trim()
            );
        }

        if (updatedAddress.getCity() != null &&
                !updatedAddress.getCity().isBlank()) {

            existingAddress.setCity(
                    updatedAddress.getCity().trim()
            );
        }

        if (updatedAddress.getState() != null &&
                !updatedAddress.getState().isBlank()) {

            existingAddress.setState(
                    updatedAddress.getState().trim()
            );
        }

        if (updatedAddress.getPincode() != null &&
                !updatedAddress.getPincode().isBlank()) {

            existingAddress.setPincode(
                    updatedAddress.getPincode().trim()
            );
        }

        /*
         * If update request changes default status,
         * maintain the one-default rule.
         */
        if (updatedAddress.getDefaultAddress() != null) {

            if (Boolean.TRUE.equals(
                    updatedAddress.getDefaultAddress()
            )) {

                clearExistingDefaultAddress(
                        existingAddress.getUser().getId()
                );

                existingAddress.setDefaultAddress(true);

            } else {

                existingAddress.setDefaultAddress(false);
            }
        }

        validateAddress(existingAddress);

        return addressRepository.save(existingAddress);
    }

    @Override
    public void setDefaultAddress(Long addressId) {

        Address address = getAddressById(addressId);

        Long userId = address.getUser().getId();

        clearExistingDefaultAddress(userId);

        address.setDefaultAddress(true);

        addressRepository.save(address);
    }

    @Override
    public void deleteAddress(Long addressId) {

        Address address = getAddressById(addressId);

        Long userId = address.getUser().getId();

        boolean wasDefault =
                Boolean.TRUE.equals(address.getDefaultAddress());

        addressRepository.delete(address);

        /*
         * If the deleted address was the default address,
         * promote another address to default.
         */
        if (wasDefault) {

            List<Address> remainingAddresses =
                    addressRepository.findByUserIdOrderByIdAsc(userId);

            if (!remainingAddresses.isEmpty()) {

                Address newDefault =
                        remainingAddresses.get(0);

                newDefault.setDefaultAddress(true);

                addressRepository.save(newDefault);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long addressId) {

        if (addressId == null || addressId <= 0) {
            return false;
        }

        return addressRepository.existsById(addressId);
    }

    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with id: " + userId
                        )
                );
    }

    private void clearExistingDefaultAddress(Long userId) {

        List<Address> addresses =
                addressRepository.findByUserIdOrderByIdAsc(userId);

        for (Address address : addresses) {

            if (Boolean.TRUE.equals(
                    address.getDefaultAddress()
            )) {

                address.setDefaultAddress(false);
                addressRepository.save(address);
            }
        }
    }

    private void validateAddress(Address address) {

        if (address == null) {
            throw new IllegalArgumentException(
                    "Address cannot be null"
            );
        }

        if (address.getAddressLine() == null ||
                address.getAddressLine().isBlank()) {

            throw new IllegalArgumentException(
                    "Address line is required"
            );
        }

        if (address.getCity() == null ||
                address.getCity().isBlank()) {

            throw new IllegalArgumentException(
                    "City is required"
            );
        }

        if (address.getState() == null ||
                address.getState().isBlank()) {

            throw new IllegalArgumentException(
                    "State is required"
            );
        }

        if (address.getPincode() == null ||
                address.getPincode().isBlank()) {

            throw new IllegalArgumentException(
                    "Pincode is required"
            );
        }
    }

    private void validateUserId(Long userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be greater than zero"
            );
        }
    }

    private void validateAddressId(Long addressId) {

        if (addressId == null || addressId <= 0) {
            throw new IllegalArgumentException(
                    "Address ID must be greater than zero"
            );
        }
    }
}