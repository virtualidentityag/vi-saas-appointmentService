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

  public Long createDefaultScheduleIfNoneExists(Long calcomUserId) {
    // Check if a schedule with name MAIN_SCHEDULE already exists
    String QUERY = "SELECT COUNT(\"id\") FROM \"Schedule\" WHERE \"name\" = 'DEFAULT_SCHEDULE' AND \"userId\" = " + calcomUserId;
    Integer defaultScheduleFound = jdbcTemplate.queryForObject(QUERY, Integer.class);
    if(defaultScheduleFound == null ||  defaultScheduleFound < 1){
      // Create Schedule
      String INSERT_QUERY = "insert into \"Schedule\" (\"userId\", \"name\", \"timeZone\") values ($userIdParam, 'DEFAULT_SCHEDULE', 'Europe/Berlin')";
      INSERT_QUERY = INSERT_QUERY.replace("$userIdParam", calcomUserId.toString());
      jdbcTemplate.update(INSERT_QUERY);
    }

    // Get default scheduleId
    String SELECT_QUERY = "SELECT \"id\" FROM \"Schedule\" WHERE \"name\" = 'DEFAULT_SCHEDULE' AND \"userId\" = $userIdParam LIMIT 1";
    SELECT_QUERY = SELECT_QUERY.replace("$userIdParam", calcomUserId.toString());
    Long defaultScheduleId = jdbcTemplate.queryForObject(SELECT_QUERY, Long.class);

    if((defaultScheduleFound == null || defaultScheduleFound < 1) && defaultScheduleId != null){
      String UPDATE_USER_QUERY = "update \"users\" set \"defaultScheduleId\" = $scheduleIdParam where \"id\" = " + calcomUserId;
      UPDATE_USER_QUERY = UPDATE_USER_QUERY.replace("$scheduleIdParam", defaultScheduleId.toString());
      jdbcTemplate.update(UPDATE_USER_QUERY);

      // Create default availability
      String INSERT_QUERY = "insert into \"Availability\" (\"scheduleId\", \"days\", \"startTime\", \"endTime\") values ($scheduleIdParam, '{0,1,2,3,4,5,6}', '00:00:00', '00:00:00')";
      INSERT_QUERY = INSERT_QUERY
          .replace("$scheduleIdParam", defaultScheduleId.toString());
      jdbcTemplate.update(INSERT_QUERY);
    }

    return defaultScheduleId;
  }
}