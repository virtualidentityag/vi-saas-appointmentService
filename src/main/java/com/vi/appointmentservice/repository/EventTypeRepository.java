package com.vi.appointmentservice.repository;

import com.vi.appointmentservice.api.model.CalcomUser;
import com.vi.appointmentservice.api.model.TeamEventTypeConsultant;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventTypeRepository {

  private final @NotNull JdbcTemplate jdbcTemplate;
  private final @NonNull CalcomUserToConsultantRepository calcomUserToConsultantRepository;
  /**
   * A = eventTypeId
   * B = userId
   */

  public void removeTeamEventTypeMembershipsForUser(Long calcomUserId) {
    String DELETE_QUERY = "delete from \"_user_eventtype\" where \"B\"=" + calcomUserId;
    jdbcTemplate.update(DELETE_QUERY);
  }

  public void removeTeamEventTypeMembershipsForEventType(Long eventTypeId) {
    String DELETE_QUERY = "delete from \"_user_eventtype\" where \"A\"=" + eventTypeId;
    jdbcTemplate.update(DELETE_QUERY);
  }

  public void addTeamEventTypeMemberships(Long eventTypeId, Long calcomUserId) {
    String INSERT_QUERY = "insert into \"_user_eventtype\" (\"A\", \"B\") values ($eventTypeIdParam, $userIdParam)";
    INSERT_QUERY = INSERT_QUERY.replace("$eventTypeIdParam", eventTypeId.toString())
        .replace("$userIdParam", calcomUserId.toString());
    jdbcTemplate.update(INSERT_QUERY);
  }

  public void updateUsersOfEventType(Long eventTypeId, List<TeamEventTypeConsultant> eventTypeConsultants) {
    String DELETE_QUERY = "delete from \"_user_eventtype\" where \"A\"=" + eventTypeId;
    jdbcTemplate.update(DELETE_QUERY);
    eventTypeConsultants.forEach(eventTypeConsultant -> {
      Long calcomUserId = calcomUserToConsultantRepository.findByConsultantId(eventTypeConsultant.getConsultantId()).getCalComUserId();
      String INSERT_QUERY = "insert into \"_user_eventtype\" (\"A\", \"B\") values ($eventTypeIdParam, $userIdParam)";
      INSERT_QUERY = INSERT_QUERY.replace("$eventTypeIdParam", eventTypeId.toString())
          .replace("$userIdParam", calcomUserId.toString());
      jdbcTemplate.update(INSERT_QUERY);
    });
  }

  public List<Long> getUserIdsOfEventTypeMembers(Long eventTypeId){
    String QUERY = "SELECT \"B\" FROM \"_user_eventtype\" WHERE \"A\" = " + eventTypeId;
    return jdbcTemplate.queryForList(QUERY, Long.class);
  }

  public void updateEventTypeDescription(Long eventTypeId, String description) {
    String UPDATE_QUERY = "update \"EventType\" set \"description\" = $descriptionParam where \"id\" = " + eventTypeId;
    UPDATE_QUERY = UPDATE_QUERY.replace("$descriptionParam", "'" + description + "'");
    jdbcTemplate.update(UPDATE_QUERY);
  }


}
