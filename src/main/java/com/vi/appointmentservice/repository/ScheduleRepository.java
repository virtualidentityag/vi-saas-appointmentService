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

  public void createDefaultScheduleIfNoneExists(Long calcomUserId) {
    // Check if a schedule with name MAIN_SCHEDULE already exists
    String QUERY = "SELECT COUNT(\"id\") FROM \"Schedule\" WHERE \"name\" = 'DEFAULT_SCHEDULE' AND \"userId\" = " + calcomUserId;
    Integer defaultScheduleFound = jdbcTemplate.queryForObject(QUERY, Integer.class);
    if(defaultScheduleFound != null && defaultScheduleFound >= 1){
      return;
    }
    // Create Schedule
    String INSERT_QUERY = "insert into \"Schedule\" (\"userId\", \"name\", \"timeZone\") values ($userIdParam, 'DEFAULT_SCHEDULE', 'Europe/Berlin')";
    INSERT_QUERY = INSERT_QUERY.replace("$userIdParam", calcomUserId.toString());
    jdbcTemplate.update(INSERT_QUERY);
  }
}
