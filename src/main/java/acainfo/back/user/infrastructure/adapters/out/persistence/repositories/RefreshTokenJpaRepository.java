package acainfo.back.user.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.user.infrastructure.adapters.out.persistence.entities.RefreshTokenJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for RefreshTokenJpaEntity.
 */
@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {

    Optional<RefreshTokenJpaEntity> findByToken(String token);

    List<RefreshTokenJpaEntity> findByUser(UserJpaEntity user);

    List<RefreshTokenJpaEntity> findByUserAndRevokedFalse(UserJpaEntity user);

    @Modifying
    @Query("UPDATE RefreshTokenJpaEntity r SET r.revoked = true WHERE r.user = :user")
    void revokeAllByUser(@Param("user") UserJpaEntity user);

    @Modifying
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.expiryDate < :date")
    void deleteByExpiryDateBefore(@Param("date") LocalDateTime date);
}
