package group5.ebay2.user.repositories;

import group5.ebay2.user.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressTypeRepository extends JpaRepository<AddressType, String> {

    Optional<AddressType> findByCodeAndActiveTrue(String code);
}
