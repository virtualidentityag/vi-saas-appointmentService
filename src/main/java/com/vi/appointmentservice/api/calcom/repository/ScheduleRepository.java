package com.vi.appointmentservice.api.calcom.repository;

import static org.openapitools.codegen.meta.features.DataTypeFeature.Maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
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

  public Set<Integer> deleteUserSchedules(Long calcomUserId) {
    var originalScheduleIds  = getScheduleIdsByUserId(db, calcomUserId);
    String DELETE_SCHEDULE = "DELETE FROM \"Schedule\" where \"userId\" = :userId";
    SqlParameterSource parameters = new MapSqlParameterSource("userId", calcomUserId);
    db.update(DELETE_SCHEDULE, parameters);
    var leftScheduleIds  = getScheduleIdsByUserId(db, calcomUserId);
    return Sets.difference(originalScheduleIds, leftScheduleIds);
  }

  public List<String> getTableNames(JdbcTemplate jdbcTemplate) throws SQLException {
    List<String> tableNames = new ArrayList<>();
    Connection connection = jdbcTemplate.getDataSource().getConnection();
    DatabaseMetaData metaData = connection.getMetaData();
    ResultSet rs = metaData.getTables(null, null, "%", null);
    while (rs.next()) {
      tableNames.add(rs.getString("TABLE_NAME"));
    }
    return tableNames;
  }
  public Set<Integer> getScheduleIdsByUserId(NamedParameterJdbcTemplate jdbcTemplate, Long userId) {
    Set<Integer> scheduleIds = Sets.newHashSet();
    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);
    String sql = "SELECT \"id\" FROM \"Schedule\" WHERE \"userId\" = :userId";
    try {
      List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);
      for (Map<String, Object> row : rows) {
        scheduleIds.add((Integer) row.get("id"));
      }
    } catch (DataAccessException e) {
      log.error("Error while fetching schedule ids for user: {}", userId, e);
    }

    return scheduleIds;
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
