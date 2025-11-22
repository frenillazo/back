package acainfo.back.payment.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.payment.domain.model.PaymentStatus;
import acainfo.back.payment.infrastructure.adapters.out.persistence.entities.PaymentJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, Long> {

    List<PaymentJpaEntity> findByStudent(UserJpaEntity student);

    List<PaymentJpaEntity> findByStudentAndStatus(UserJpaEntity student, PaymentStatus status);

    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.status = 'PENDIENTE' AND p.dueDate < :date")
    List<PaymentJpaEntity> findOverduePayments(@Param("date") LocalDate date);

    List<PaymentJpaEntity> findByDueDateBefore(LocalDate date);

    List<PaymentJpaEntity> findByStatus(PaymentStatus status);

    boolean existsByStripePaymentId(String stripePaymentId);

    Optional<PaymentJpaEntity> findByStripePaymentId(String stripePaymentId);
}
