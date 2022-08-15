package com.vi.appointmentservice.repository;

import com.vi.appointmentservice.model.TeamToAgency;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamToAgencyRepository extends JpaRepository<TeamToAgency, Long> {
    Optional<TeamToAgency> findByAgencyId(Long agencyId);

    Optional<TeamToAgency> findByTeamid(Long teamid);

    boolean existsByAgencyId(Long agencyId);




}