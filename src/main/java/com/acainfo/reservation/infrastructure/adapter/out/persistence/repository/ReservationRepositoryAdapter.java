package com.acainfo.reservation.infrastructure.adapter.out.persistence.repository;

import com.acainfo.reservation.application.dto.ReservationFilters;
import com.acainfo.reservation.application.port.out.ReservationRepositoryPort;
import com.acainfo.reservation.domain.model.OnlineRequestStatus;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.reservation.infrastructure.adapter.out.persistence.entity.SessionReservationJpaEntity;
import com.acainfo.reservation.infrastructure.adapter.out.persistence.specification.ReservationSpecifications;
import com.acainfo.reservation.infrastructure.mapper.ReservationPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing ReservationRepositoryPort.
 * Translates domain operations to JPA operations.
 */
@Component
@RequiredArgsConstructor
public class ReservationRepositoryAdapter implements ReservationRepositoryPort {

    private final JpaReservationRepository jpaReservationRepository;
    private final ReservationPersistenceMapper reservationPersistenceMapper;

    @Override
    public SessionReservation save(SessionReservation reservation) {
        SessionReservationJpaEntity jpaEntity = reservationPersistenceMapper.toJpaEntity(reservation);
        SessionReservationJpaEntity savedEntity = jpaReservationRepository.save(jpaEntity);
        return reservationPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public List<SessionReservation> saveAll(List<SessionReservation> reservations) {
        List<SessionReservationJpaEntity> jpaEntities = reservationPersistenceMapper.toJpaEntityList(reservations);
        List<SessionReservationJpaEntity> savedEntities = jpaReservationRepository.saveAll(jpaEntities);
        return reservationPersistenceMapper.toDomainList(savedEntities);
    }

    @Override
    public Optional<SessionReservation> findById(Long id) {
        return jpaReservationRepository.findById(id)
                .map(reservationPersistenceMapper::toDomain);
    }

    @Override
    public Page<SessionReservation> findWithFilters(ReservationFilters filters) {
        Specification<SessionReservationJpaEntity> spec = ReservationSpecifications.withFilters(filters);

        Sort sort = filters.sortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filters.sortBy()).ascending()
                : Sort.by(filters.sortBy()).descending();

        PageRequest pageRequest = PageRequest.of(filters.page(), filters.size(), sort);

        return jpaReservationRepository.findAll(spec, pageRequest)
                .map(reservationPersistenceMapper::toDomain);
    }

    @Override
    public List<SessionReservation> findBySessionId(Long sessionId) {
        return reservationPersistenceMapper.toDomainList(
                jpaReservationRepository.findBySessionId(sessionId)
        );
    }

    @Override
    public List<SessionReservation> findByStudentId(Long studentId) {
        return reservationPersistenceMapper.toDomainList(
                jpaReservationRepository.findByStudentId(studentId)
        );
    }

    @Override
    public Optional<SessionReservation> findByStudentIdAndSessionId(Long studentId, Long sessionId) {
        return jpaReservationRepository.findByStudentIdAndSessionId(studentId, sessionId)
                .map(reservationPersistenceMapper::toDomain);
    }

    @Override
    public List<SessionReservation> findBySessionIdAndStatus(Long sessionId, ReservationStatus status) {
        return reservationPersistenceMapper.toDomainList(
                jpaReservationRepository.findBySessionIdAndStatus(sessionId, status)
        );
    }

    @Override
    public List<SessionReservation> findPendingOnlineRequestsByTeacherId(Long teacherId) {
        return reservationPersistenceMapper.toDomainList(
                jpaReservationRepository.findPendingOnlineRequestsByTeacherId(teacherId)
        );
    }

    @Override
    public List<SessionReservation> findByOnlineRequestStatus(OnlineRequestStatus status) {
        return reservationPersistenceMapper.toDomainList(
                jpaReservationRepository.findByOnlineRequestStatus(status)
        );
    }

    @Override
    public boolean existsByStudentIdAndSessionId(Long studentId, Long sessionId) {
        return jpaReservationRepository.existsByStudentIdAndSessionId(studentId, sessionId);
    }

    @Override
    public long countBySessionIdAndStatusAndMode(Long sessionId, ReservationStatus status, ReservationMode mode) {
        return jpaReservationRepository.countBySessionIdAndStatusAndMode(sessionId, status, mode);
    }

    @Override
    public List<SessionReservation> findBySessionIdAndAttendanceStatusIsNull(Long sessionId) {
        return reservationPersistenceMapper.toDomainList(
                jpaReservationRepository.findBySessionIdAndAttendanceStatusIsNull(sessionId)
        );
    }

    @Override
    public void delete(Long id) {
        jpaReservationRepository.deleteById(id);
    }
}
