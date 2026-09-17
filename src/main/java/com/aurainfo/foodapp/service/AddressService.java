package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.entity.Address;

import java.util.List;

public interface AddressService {

    Address createAddress(Long userId, Address address);

    Address getAddressById(Long addressId);

    List<Address> getAddressesByUser(Long userId);

    Address updateAddress(Long addressId, Address updatedAddress);

    void setDefaultAddress(Long addressId);

    void deleteAddress(Long addressId);

    boolean existsById(Long addressId);
}