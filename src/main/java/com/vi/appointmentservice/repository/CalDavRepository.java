package com.vi.appointmentservice.repository;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CalDavRepository {

  private @Autowired
  NamedParameterJdbcTemplate caldavDBNamedParameterTemplate;

  public void resetCredentials(String email, String digesta1) {
    String QUERY = "update users set digesta1 = :digesta1 where "
        + "username = (select SUBSTRING_INDEX(uri,'/',-1) from principals where email = :email)";
    SqlParameterSource parameters = new MapSqlParameterSource("email", email)
        .addValue("digesta1", digesta1);
    caldavDBNamedParameterTemplate.update(QUERY, parameters);
  }

  public String getUserName(String email) {
    String QUERY = "select SUBSTRING_INDEX(uri,'/',-1) from principals where email = :email";
    SqlParameterSource parameters = new MapSqlParameterSource("email", email);
    return new String(caldavDBNamedParameterTemplate
        .queryForObject(QUERY, parameters, new SingleColumnRowMapper<>()), StandardCharsets.UTF_8);
  }

  public boolean getAccountExists(String email) {
    String QUERY = "SELECT COUNT(id) FROM users WHERE username = :email";
    SqlParameterSource parameters = new MapSqlParameterSource("email", email);
    Integer count = caldavDBNamedParameterTemplate.queryForObject(QUERY, parameters, Integer.class);
    return count != null && count > 0;
  }


}
