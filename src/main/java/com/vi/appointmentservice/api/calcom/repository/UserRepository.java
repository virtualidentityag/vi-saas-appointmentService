package com.vi.appointmentservice.api.calcom.repository;

import com.vi.appointmentservice.api.calcom.model.CalcomTeam;
import com.vi.appointmentservice.api.model.CalcomUser;
import java.util.Map;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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

  public com.vi.appointmentservice.api.calcom.model.CalcomUser getUserById(Long userId){
    String SELECT_USER = "SELECT * FROM users WHERE id = :userId";
    SqlParameterSource parameters = new MapSqlParameterSource("userId", userId);
    Map<String, Object> result = db.queryForMap(SELECT_USER, parameters);
    return com.vi.appointmentservice.api.calcom.model.CalcomUser.asInstance(result);
  }

  public com.vi.appointmentservice.api.calcom.model.CalcomUser creatUser(
      com.vi.appointmentservice.api.calcom.model.CalcomUser user) {
    GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
    String INSERT_USER =
        "INSERT INTO users(username,name,email,password,\"timeZone\",\"weekStart\",locale,\"timeFormat\") "
            + "VALUES (:username,:name,:email,:password,:timeZone,:weekStart,:locale,:timeFormat)";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("username", user.getUsername())
        .addValue("name", user.getName())
        .addValue("email", user.getEmail())
        .addValue("password", user.getPassword())
        .addValue("timeZone", user.getTimeZone())
        .addValue("weekStart", user.getWeekStart())
        .addValue("locale", user.getLocale())
        .addValue("timeFormat", user.getTimeFormat());
    db.update(INSERT_USER, parameters, generatedKeyHolder);
    user.setId(Long.valueOf((Integer) generatedKeyHolder.getKeys().get("id")));
    return user;
  }

  public com.vi.appointmentservice.api.calcom.model.CalcomUser updateUser(com.vi.appointmentservice.api.calcom.model.CalcomUser user) {
    Long userId = user.getId();
    String name = user.getName();
    Boolean isAway = false;
    String email = user.getEmail();
    String UPDATE_USER_QUERY =
        "update \"users\" set \"name\" = $nameParam, \"away\" = $awayParam, \"email\" = $emailParam where \"id\" = "
            + userId;
    UPDATE_USER_QUERY = UPDATE_USER_QUERY.replace("$nameParam", "'" + name + "'")
        .replace("$awayParam", isAway.toString())
        .replace("$emailParam", "'" + email + "'");
    jdbcTemplate.update(UPDATE_USER_QUERY);
    return getUserById(user.getId());
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
