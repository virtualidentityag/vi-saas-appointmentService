package com.vi.appointmentservice.api.calcom.repository;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class ScheduleRepository {

  private final @NotNull JdbcTemplate jdbcTemplate;

  private final @NonNull AvailabilityRepository availabilityRepository;
  private final @NonNull UserRepository userRepository;

  private final NamedParameterJdbcTemplate db;

  public ScheduleRepository(@Qualifier("dbTemplate") NamedParameterJdbcTemplate db,
      JdbcTemplate jdbcTemplate,
      AvailabilityRepository availabilityRepository, UserRepository userRepository) {
    this.availabilityRepository = availabilityRepository;
    this.userRepository = userRepository;
    this.db = db;
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Integer> deleteUserSchedules(Long calcomUserId) {
    String DELETE_SCHEDULE = "DELETE FROM \"Schedule\" where \"userId\" = :userId";
    SqlParameterSource parameters = new MapSqlParameterSource("userId", calcomUserId);
    db.update(DELETE_SCHEDULE, parameters);
    //TODO: return ids of removed schedules
    return null;
  }

  public Long createDefaultSchedule(Long calcomUserId) {
    // Check if a schedule with name DEFAULT_SCHEDULE already exists
    //TODO: this check is probably not needed, since this will be called only once.
    String QUERY =
        "SELECT COUNT(\"id\") FROM \"Schedule\" WHERE \"name\" = 'DEFAULT_SCHEDULE' AND \"userId\" = "
            + calcomUserId;
    Integer defaultScheduleFound = jdbcTemplate.queryForObject(QUERY, Integer.class);
    if (defaultScheduleFound == null || defaultScheduleFound < 1) {
      // Create Schedule
      String INSERT_QUERY = "insert into \"Schedule\" (\"userId\", \"name\", \"timeZone\") values ($userIdParam, 'DEFAULT_SCHEDULE', 'Europe/Berlin')";
      INSERT_QUERY = INSERT_QUERY.replace("$userIdParam", calcomUserId.toString());
      jdbcTemplate.update(INSERT_QUERY);
    }

    // Get default scheduleId
    String SELECT_QUERY = "SELECT \"id\" FROM \"Schedule\" WHERE \"name\" = 'DEFAULT_SCHEDULE' AND \"userId\" = $userIdParam LIMIT 1";
    SELECT_QUERY = SELECT_QUERY.replace("$userIdParam", calcomUserId.toString());
    Long defaultScheduleId = jdbcTemplate.queryForObject(SELECT_QUERY, Long.class);

    if ((defaultScheduleFound == null || defaultScheduleFound < 1) && defaultScheduleId != null) {
      userRepository.setDefaultScheduleId(calcomUserId, defaultScheduleId);
      availabilityRepository.createDefaultAvailability(defaultScheduleId);
    }

    return defaultScheduleId;
  }
}
