package com.vi.appointmentservice.repository;

import com.vi.appointmentservice.api.model.TeamEventTypeConsultant;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventTypeRepository {

  private final @NotNull JdbcTemplate jdbcTemplate;
  private final @NotNull NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private final @NonNull CalcomUserToConsultantRepository calcomUserToConsultantRepository;

  /**
   * A = eventTypeId
   * B = userId
   */

  public void removeTeamEventTypeMembershipsForUser(Long calcomUserId, List<Long> teamIds) {
    String QUERY = "DELETE FROM \"_user_eventtype\" WHERE \"B\"= :calcomUserId AND "
        + "\"A\" NOT IN (SELECT id from \"EventType\" WHERE \"teamId\" in (:teamIds)) AND "
        + "\"A\" IN (SELECT ID FROM \"EventType\" where \"schedulingType\" in ('roundRobin'))";
    SqlParameterSource parameters = new MapSqlParameterSource("teamIds", teamIds)
        .addValue("calcomUserId", calcomUserId);
    namedParameterJdbcTemplate.update(QUERY, parameters);
  }

  public void removeTeamEventTypeMembershipsForEventType(Long eventTypeId) {
    String DELETE_QUERY = "delete from \"_user_eventtype\" where \"A\"=" + eventTypeId;
    jdbcTemplate.update(DELETE_QUERY);
  }

  public void addUserEventTypeRelation(Long eventTypeId, Long calcomUserId) {
    try {
      String INSERT_QUERY = "insert into \"_user_eventtype\" (\"A\", \"B\") values ($eventTypeIdParam, $userIdParam)";
      INSERT_QUERY = INSERT_QUERY.replace("$eventTypeIdParam", eventTypeId.toString())
          .replace("$userIdParam", calcomUserId.toString());
      jdbcTemplate.update(INSERT_QUERY);
    } catch (Exception e) {
      //do nothing in case relation existss
    }

  }

  public void updateUsersOfEventType(Long eventTypeId, List<TeamEventTypeConsultant> eventTypeConsultants) {
    String DELETE_QUERY = "delete from \"_user_eventtype\" where \"A\"=" + eventTypeId;
    jdbcTemplate.update(DELETE_QUERY);
    eventTypeConsultants.forEach(eventTypeConsultant -> {
      Optional<CalcomUserToConsultant> calcomUserToConsultant = calcomUserToConsultantRepository.findByConsultantId(eventTypeConsultant.getConsultantId());
      if(calcomUserToConsultant.isPresent()){
        Long calcomUserId = calcomUserToConsultant.get().getCalComUserId();
        String INSERT_QUERY = "insert into \"_user_eventtype\" (\"A\", \"B\") values ($eventTypeIdParam, $userIdParam)";
        INSERT_QUERY = INSERT_QUERY.replace("$eventTypeIdParam", eventTypeId.toString())
            .replace("$userIdParam", calcomUserId.toString());
        jdbcTemplate.update(INSERT_QUERY);
      }
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

  public void updateEventTypeScheduleId(Long eventTypeId, Long scheduleId) {
    String UPDATE_QUERY = "update \"EventType\" set \"scheduleId\" = $scheduleIdParam where \"id\" = " + eventTypeId;
    UPDATE_QUERY = UPDATE_QUERY.replace("$scheduleIdParam", scheduleId.toString());
    jdbcTemplate.update(UPDATE_QUERY);
  }


}
