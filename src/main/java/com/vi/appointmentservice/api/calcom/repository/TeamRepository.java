package com.vi.appointmentservice.api.calcom.repository;

import com.vi.appointmentservice.api.calcom.model.CalcomTeam;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class TeamRepository {

  private final NamedParameterJdbcTemplate db;

  public TeamRepository(@Qualifier("dbTemplate") NamedParameterJdbcTemplate db) {
    this.db = db;
  }

  public CalcomTeam getTeamById(Long teamId) {
    String SELECT_TEAM = "select * from \"Team\" where id = :teamId";
    SqlParameterSource parameters = new MapSqlParameterSource("teamId", teamId);
    Map<String, Object> result = db.queryForMap(SELECT_TEAM, parameters);
    return CalcomTeam.asInstance(result);
  }

  public void deleteTeam(Long teamId) {
    String DELETE_TEAM = "delete from \"Team\" where id = :teamId";
    SqlParameterSource parameters = new MapSqlParameterSource("teamId", teamId);
    db.update(DELETE_TEAM, parameters);
  }

}
