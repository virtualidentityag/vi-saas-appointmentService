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
      String deleteQuery = "delete from \"Availability\" where \"scheduleId\"=" + scheduleId;
      jdbcTemplate.update(deleteQuery);
    }

    public void createDefaultAvailability(Long scheduleId){
      // Create default availability
      String insertQuery = "insert into \"Availability\" (\"scheduleId\", \"days\", \"startTime\", \"endTime\") values ($scheduleIdParam, '{1}', '09:00:00', '9:15:00')";
      insertQuery = insertQuery
          .replace("$scheduleIdParam", scheduleId.toString());
      jdbcTemplate.update(insertQuery);
    }

}
