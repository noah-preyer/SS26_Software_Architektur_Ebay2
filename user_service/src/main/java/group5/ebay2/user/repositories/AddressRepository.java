package group5.ebay2.user.repositories;

import group5.ebay2.user.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByUserProfileId(UUID userProfileId);
}
