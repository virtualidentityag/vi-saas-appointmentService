package com.vi.appointmentservice.repository;

import com.vi.appointmentservice.api.model.TeamEventTypeConsultant;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserEventTypeRepository {
  private final @NotNull JdbcTemplate jdbcTemplate;

  /**
   * A = eventTypeId
   * B = userId
   */

  public void updateUsersOfEventType(Long eventTypeId, List<TeamEventTypeConsultant> eventTypeConsultants) {
    String DELETE_QUERY = "delete from \"_user_eventtype\" where \"A\"=" + eventTypeId;
    jdbcTemplate.update(DELETE_QUERY);
    eventTypeConsultants.forEach(eventTypeConsultant -> {
      String INSERT_QUERY = "insert into \"_user_eventtype\" (\"A\", \"B\") values ($eventTypeIdParam, $userIdParam)";
      INSERT_QUERY = INSERT_QUERY.replace("$eventTypeIdParam", eventTypeId.toString())
          .replace("$userIdParam", eventTypeConsultant.getConsultantId());
      jdbcTemplate.update(INSERT_QUERY);
    });
  }

}
