package group5.ebay2.user.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import group5.ebay2.user.UserProfile;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByEmail(String email);

    Optional<UserProfile> findByUsername(String username);

    Optional<UserProfile> findByEmailOrUsername(String email, String username);

    Optional<UserProfile> findByAuthUserId(UUID authUserId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByAuthUserId(UUID authUserId);
}