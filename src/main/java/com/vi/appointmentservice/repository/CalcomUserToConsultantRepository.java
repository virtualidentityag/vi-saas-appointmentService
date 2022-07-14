package com.vi.appointmentservice.repository;

import com.vi.appointmentservice.model.CalcomUserToConsultant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalcomUserToConsultantRepository extends JpaRepository<CalcomUserToConsultant, String> {
    CalcomUserToConsultant findByConsultantId(String userId);
    CalcomUserToConsultant findByCalComUserId(Long calComUserId);

    boolean existsByConsultantId(String consultantId);

    boolean existsByCalComUserId(Long calComUserId);

    long deleteByConsultantId(String consultantId);
}