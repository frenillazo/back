package com.acainfo.payment.infrastructure.mapper;

import com.acainfo.payment.domain.model.Payment;
import com.acainfo.payment.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Payment persistence.
 * Converts between domain model (Payment) and JPA entity (PaymentJpaEntity).
 */
@Mapper(componentModel = "spring")
public interface PaymentPersistenceMapper {

    /**
     * Convert domain Payment to JPA entity.
     *
     * @param payment Domain model
     * @return JPA entity
     */
    PaymentJpaEntity toJpaEntity(Payment payment);

    /**
     * Convert JPA entity to domain Payment.
     *
     * @param entity JPA entity
     * @return Domain model
     */
    Payment toDomain(PaymentJpaEntity entity);

    /**
     * Convert list of JPA entities to domain models.
     *
     * @param entities List of JPA entities
     * @return List of domain models
     */
    List<Payment> toDomainList(List<PaymentJpaEntity> entities);

    /**
     * Convert list of domain models to JPA entities.
     *
     * @param payments List of domain models
     * @return List of JPA entities
     */
    List<PaymentJpaEntity> toJpaEntityList(List<Payment> payments);
}
