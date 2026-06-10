package group5.ebay2.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import group5.ebay2.auth.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}