package com.vi.appointmentservice.api.calcom.repository;

import com.vi.appointmentservice.api.calcom.model.CalcomUser;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

  private final @NotNull JdbcTemplate jdbcTemplate;

  private final @NotNull NamedParameterJdbcTemplate db;

  public UserRepository(@Qualifier("dbTemplate") NamedParameterJdbcTemplate db,
      JdbcTemplate jdbcTemplate) {
    this.db = db;
    this.jdbcTemplate = jdbcTemplate;
  }

  public CalcomUser getUserById(Long userId) {
    String SELECT_USER = "SELECT * FROM users WHERE id = :userId";
    SqlParameterSource parameters = new MapSqlParameterSource("userId", userId);
    Map<String, Object> result = db.queryForMap(SELECT_USER, parameters);
    return CalcomUser.asInstance(result);
  }

  public CalcomUser creatUser(
      CalcomUser user) {
    GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
    String INSERT_USER =
        "INSERT INTO users(username,name,email,password,\"timeZone\",\"weekStart\",locale,\"timeFormat\",\"completedOnboarding\") "
            + "VALUES (:username,:name,:email,:password,:timeZone,:weekStart,:locale,:timeFormat,:completedOnboarding)";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("username", user.getUsername())
        .addValue("name", user.getName())
        .addValue("email", user.getEmail())
        .addValue("password", user.getPassword())
        .addValue("timeZone", user.getTimeZone())
        .addValue("weekStart", user.getWeekStart())
        .addValue("locale", user.getLocale())
        .addValue("timeFormat", user.getTimeFormat())
        .addValue("completedOnboarding", true);
    db.update(INSERT_USER, parameters, generatedKeyHolder);
    user.setId(Long.valueOf((Integer) generatedKeyHolder.getKeys().get("id")));
    return user;
  }

  public CalcomUser updateUser(Long userId, String name, String email) {
    String UPDATE_USER_QUERY = "UPDATE \"users\" SET \"name\" = :name, \"email\" = :email WHERE \"id\" = :id";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("name", name)
        .addValue("email", email)
        .addValue("id", userId);
    db.update(UPDATE_USER_QUERY, parameters);
    return getUserById(userId);
  }

  public void setDefaultScheduleId(Long calcomUserId, Long defaultScheduleId) {
    String UPDATE_USER_QUERY =
        "update \"users\" set \"defaultScheduleId\" = $scheduleIdParam where \"id\" = "
            + calcomUserId;
    UPDATE_USER_QUERY = UPDATE_USER_QUERY.replace("$scheduleIdParam", defaultScheduleId.toString());
    jdbcTemplate.update(UPDATE_USER_QUERY);
  }

  public void deleteUser(Long userId) {
  }
}
