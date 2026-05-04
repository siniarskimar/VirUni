package io.github.siniarski.viruni.repository;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Page<Account> findAllByRole(Pageable pageable, AccountRole role);
    int countByRole(AccountRole role);

    Optional<Account> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndSubjectsId(String username, long subjectId);

    List<Account> findByRole(AccountRole role);
    Optional<Account> findByRoleAndId(AccountRole role, long id);

    boolean existsByIdAndSubjectsId(long id, long subjectId);
    Page<Account> findBySubjectsId(long subjectId, Pageable pageable);

    @Query("SELECT u FROM Account u " +
            "WHERE " +
            "u.firstname LIKE CONCAT('%', :query, '%') " +
            "OR u.lastname LIKE CONCAT('%', :query, '%')")
    Page<Account> findByFirstnameOrLastnameContaining(Pageable pageable, String query);
}
