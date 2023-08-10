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
    String selectUser = "SELECT * FROM users WHERE id = :userId";
    SqlParameterSource parameters = new MapSqlParameterSource("userId", userId);
    Map<String, Object> result = db.queryForMap(selectUser, parameters);
    return CalcomUser.asInstance(result);
  }

  public CalcomUser creatUser(
      CalcomUser user) {
    GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
    String insertUser =
        "INSERT INTO users(username,name,email,password,\"timeZone\",\"weekStart\",locale,\"timeFormat\",\"completedOnboarding\",\"hideBranding\") "
            + "VALUES (:username,:name,:email,:password,:timeZone,:weekStart,:locale,:timeFormat,:completedOnboarding,:hideBranding)";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("username", user.getUsername())
        .addValue("name", user.getName())
        .addValue("email", user.getEmail())
        .addValue("password", user.getPassword())
        .addValue("timeZone", user.getTimeZone())
        .addValue("weekStart", user.getWeekStart())
        .addValue("locale", user.getLocale())
        .addValue("timeFormat", user.getTimeFormat())
        .addValue("completedOnboarding", true)
        .addValue("hideBranding", true);
    db.update(insertUser, parameters, generatedKeyHolder);
    user.setId(Long.valueOf((Integer) generatedKeyHolder.getKeys().get("id")));
    return user;
  }

  public CalcomUser updateUser(Long userId, String name, String email) {
    String updateUserQuery = "UPDATE \"users\" SET \"name\" = :name, \"email\" = :email WHERE \"id\" = :id";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("name", name)
        .addValue("email", email)
        .addValue("id", userId);
    db.update(updateUserQuery, parameters);
    return getUserById(userId);
  }

  public void setDefaultScheduleId(Long calcomUserId, Long defaultScheduleId) {
    String updateUserQuery =
        "update \"users\" set \"defaultScheduleId\" = $scheduleIdParam where \"id\" = "
            + calcomUserId;
    updateUserQuery = updateUserQuery.replace("$scheduleIdParam", defaultScheduleId.toString());
    jdbcTemplate.update(updateUserQuery);
  }

  public void deleteUser(Long userId) {
    jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
  }
}
