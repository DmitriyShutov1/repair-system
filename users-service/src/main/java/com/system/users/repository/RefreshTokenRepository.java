package com.system.users.repository;

import com.system.users.entity.RefreshToken;
import com.system.users.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUser(UserAccount user);

    void deleteAllByUser(UserAccount user);

    void deleteAllByExpiresAtBefore(LocalDateTime now);
    
    @Modifying
    @Query("""
        UPDATE RefreshToken rt
        SET rt.revoked = true
        WHERE rt.user = :user
          AND rt.deviceId = :deviceId
          AND rt.revoked = false
    """)
    int revokeAllActiveTokensForDevice(@Param("user") UserAccount user,
                                      @Param("deviceId") String deviceId);
}
