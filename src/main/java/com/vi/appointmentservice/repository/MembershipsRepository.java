package com.vi.appointmentservice.repository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MembershipsRepository {

  private final @NotNull JdbcTemplate jdbcTemplate;

  public void updateMemberShipsOfUser(Long calcomUserIds, List<Long> teamIds) {
    String DELETE_QUERY = "delete from \"Membership\" where \"userId\"=" + calcomUserIds;
    jdbcTemplate.update(DELETE_QUERY);
    teamIds.forEach(teamId -> {
      String INSERT_QUERY = "insert into \"Membership\" (\"teamId\", \"userId\", \"accepted\", \"role\") values ($teamIdParam, $userIdParam, true, 'MEMBER')";
      INSERT_QUERY = INSERT_QUERY.replace("$teamIdParam", teamId.toString())
          .replace("$userIdParam", calcomUserIds.toString());
      jdbcTemplate.update(INSERT_QUERY);
    });
  }

  public void deleteTeamMemeberships(Long teamId) {
    String DELETE_MEMBERSHIP_QUERY = "delete from \"Membership\" where \"teamId\"=" + teamId;
    jdbcTemplate.update(DELETE_MEMBERSHIP_QUERY);
  }

  public List<Long> getUsersOfTeam(Long calcomTeamId) {
    String query = "select \"userId\" from \"Membership\" where \"teamId\"=" + calcomTeamId;
    return jdbcTemplate.queryForList(query, Integer.class).stream()
        .map(Integer::longValue)
        .collect(Collectors.toList());
  }
}
