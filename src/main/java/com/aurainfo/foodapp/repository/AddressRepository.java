package com.aurainfo.foodapp.repository;

import com.aurainfo.foodapp.entity.Address;
import com.aurainfo.foodapp.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address,Long> {
    List<Address> findByUser(User user);
    List<Address> findByUserIdOrderByIdAsc(Long userId);
    /*
     * Address ownership checks need the User relationship.
     * Fetch User together with Address to avoid
     * LazyInitializationException outside the persistence session.
     */
    @EntityGraph(attributePaths = "user")
    Optional<Address> findById(Long id);

    List<Address> findByUserIdAndDefaultAddressTrue(Long userId);
}
