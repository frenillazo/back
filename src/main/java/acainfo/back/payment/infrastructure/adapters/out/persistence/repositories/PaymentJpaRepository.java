package acainfo.back.payment.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.payment.domain.model.PaymentStatus;
import acainfo.back.payment.infrastructure.adapters.out.persistence.entities.PaymentJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    List<PaymentJpaEntity> findByAcademicPeriod(String academicPeriod);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentJpaEntity p WHERE p.status = 'PAGADO' AND p.paidDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentJpaEntity p WHERE p.student.id = :studentId AND p.status IN ('PENDIENTE', 'ATRASADO')")
    BigDecimal calculateTotalPendingByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT DISTINCT p.student.id FROM PaymentJpaEntity p WHERE p.status = 'ATRASADO' OR (p.status = 'PENDIENTE' AND p.dueDate < :date)")
    List<Long> findStudentIdsWithOverduePayments(@Param("date") LocalDate date);

    default List<Long> findStudentIdsWithOverduePayments() {
        return findStudentIdsWithOverduePayments(LocalDate.now().minusDays(5));
    }
}
