package com.system.users.repository;

import com.system.users.entity.UserAccount;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<UserAccount> findAllByBranch_Id(Long branchId);
    
    List<UserAccount> findAllByBranch_IdAndStatus(Long branchId, UserAccount.Status status);
    
    Page<UserAccount> findAllByRoleAndStatus(
            UserAccount.Role role,
            UserAccount.Status status,
            Pageable pageable
    );

    Optional<UserAccount> findByIdAndStatus(Long id, UserAccount.Status status);
    
    // Fetch user with branch (join fetch) — useful if вы хотите иметь branch в detached entity
    @Query("select u from UserAccount u left join fetch u.branch where u.id = :id")
    Optional<UserAccount> findByIdWithBranch(@Param("id") Long id);

    // Быстрый метод, возвращающий только branch id (рекомендую)
    @Query("select b.id from UserAccount u left join u.branch b where u.id = :id")
    Optional<Long> findBranchIdByUserId(@Param("id") Long id);
}
