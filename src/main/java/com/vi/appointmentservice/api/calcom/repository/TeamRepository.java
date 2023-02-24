package com.vi.appointmentservice.api.calcom.repository;

import com.vi.appointmentservice.api.calcom.model.CalcomTeam;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TeamRepository {

  private final NamedParameterJdbcTemplate db;

  public TeamRepository(@Qualifier("dbTemplate") NamedParameterJdbcTemplate db,
      @Qualifier("calcomDBDataSource") DataSource calcomDBDataSource) {
    this.db = db;
  }

  public CalcomTeam getTeamById(Number teamId) {
    String SELECT_TEAM = "SELECT * FROM \"Team\" WHERE id = :teamId";
    SqlParameterSource parameters = new MapSqlParameterSource("teamId", teamId);
    Map<String, Object> result = db.queryForMap(SELECT_TEAM, parameters);
    return CalcomTeam.asInstance(result);
  }

  public void deleteTeam(Long teamId) {
    String DELETE_TEAM = "DELETE FROM \"Team\" where id = :teamId";
    SqlParameterSource parameters = new MapSqlParameterSource("teamId", teamId);
    db.update(DELETE_TEAM, parameters);
  }

  public CalcomTeam createTeam(CalcomTeam calcomTeam) {
    GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
    String INSERT_TEAM = "INSERT INTO \"Team\"(name,slug,\"hideBranding\") VALUES (:name,:slug,:hideBranding)";
    SqlParameterSource parameters = new MapSqlParameterSource("name", calcomTeam.getName())
        .addValue("slug", calcomTeam.getSlug())
        .addValue("hideBranding", true);
    db.update(INSERT_TEAM, parameters, generatedKeyHolder);
    return getTeamById(Long.valueOf((Integer) generatedKeyHolder.getKeys().get("id")));
  }

  public CalcomTeam updateTeam(CalcomTeam calcomTeam) {
    String UPDATE_TEAM = "UPDATE \"Team\" SET name=:name WHERE id=:id";
    SqlParameterSource parameters = new MapSqlParameterSource("name", calcomTeam.getName())
        .addValue("id", calcomTeam.getId());
    db.update(UPDATE_TEAM, parameters);
    return getTeamById(calcomTeam.getId());

  }

}
