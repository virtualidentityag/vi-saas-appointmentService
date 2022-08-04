package com.vi.appointmentservice.repository;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduleRepository {

  private final @NotNull JdbcTemplate jdbcTemplate;

  // Delete

  public void updateSchedulesOfUser(Long calcomUserId) {
    // Get schedules

    // Delete availabilities

    // Delete schedule

    // Create Schedule

    // Create availabilities

  }
}
