package com.vi.appointmentservice.repository;

import com.vi.appointmentservice.model.CalcomUserToConsultant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserToConsultantRepository extends JpaRepository<CalcomUserToConsultant, String> {
    Optional<CalcomUserToConsultant> findByConsultantId(String consultantId);

    Optional<CalcomUserToConsultant> findByCalComUserId(Long calComUserId);

    boolean existsByConsultantId(String consultantId);
    boolean existsByCalComUserId(Long calComUserId);

    long deleteByConsultantId(String consultantId);
}