package com.vi.appointmentservice.repository;

import com.vi.appointmentservice.api.model.CalcomUser;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventTypeRepository {

  private final @NotNull JdbcTemplate jdbcTemplate;

    // A = eventTypeId, B = userId
  public void removeTeamEventTypeMemberships(Long calcomUserId) {
    String DELETE_QUERY = "delete from \"_user_eventtype\" where \"B\"=" + calcomUserId;
    jdbcTemplate.update(DELETE_QUERY);
  }

  public void addTeamEventTypeMemberships(Long eventTypeId, Long calcomUserId) {
    String INSERT_QUERY = "insert into \"_user_eventtype\" (\"A\", \"B\") values ($eventTypeIdParam, $userIdParam)";
    INSERT_QUERY = INSERT_QUERY.replace("$eventTypeIdParam", eventTypeId.toString())
        .replace("$userIdParam", calcomUserId.toString());
    jdbcTemplate.update(INSERT_QUERY);
  }


}
