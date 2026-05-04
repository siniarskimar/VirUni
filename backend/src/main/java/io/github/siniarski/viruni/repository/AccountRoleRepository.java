package io.github.siniarski.viruni.repository;

import io.github.siniarski.viruni.model.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRoleRepository extends JpaRepository<AccountRole, String> {
    Optional<AccountRole> findByName(String name);
}
