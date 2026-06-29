package group5.ebay2.user.repositories;

import group5.ebay2.user.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserProfileId(Long userProfileId);
}
