package com.vi.appointmentservice.repository;

import com.vi.appointmentservice.model.TeamToAgency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamToAgencyRepository extends JpaRepository<TeamToAgency, Long> {
    List<TeamToAgency> findByAgencyId(Long agencyId);
}