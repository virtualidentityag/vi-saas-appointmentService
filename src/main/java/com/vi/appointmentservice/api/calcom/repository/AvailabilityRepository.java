package com.vi.appointmentservice.api.calcom.repository;

import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AvailabilityRepository {

    private final @NotNull JdbcTemplate jdbcTemplate;

    public void deleteAvailabilityByScheduleId(Long scheduleId) {
      String DELETE_QUERY = "delete from \"Availability\" where \"scheduleId\"=" + scheduleId;
      jdbcTemplate.update(DELETE_QUERY);
    }

    public void createDefaultAvailability(Long scheduleId){
      // Create default availability
        String INSERT_QUERY = "insert into \"Availability\" (\"scheduleId\", \"days\", \"startTime\", \"endTime\") values ($scheduleIdParam, '{}', '00:00:00', '00:00:00')";
      INSERT_QUERY = INSERT_QUERY
          .replace("$scheduleIdParam", scheduleId.toString());
      jdbcTemplate.update(INSERT_QUERY);
    }

}
