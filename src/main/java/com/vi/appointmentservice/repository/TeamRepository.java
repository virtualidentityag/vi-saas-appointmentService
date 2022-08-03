package com.vi.appointmentservice.repository;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TeamRepository {

  private final @NotNull JdbcTemplate jdbcTemplate;

  public void deleteTeam(Long teamId) {
    String DELETE_TEAM_QUERY = "delete from \"Team\" where \"id\"=" + teamId;
    jdbcTemplate.update(DELETE_TEAM_QUERY);
  }

}
