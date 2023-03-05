package com.vi.appointmentservice.api.calcom.repository;

import com.vi.appointmentservice.api.calcom.model.CalcomEventType;
import com.vi.appointmentservice.repository.UserToConsultantRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class EventTypeRepository {

  private final NamedParameterJdbcTemplate db;
  private final DataSource datasource;

  public EventTypeRepository(@Qualifier("dbTemplate") NamedParameterJdbcTemplate db,
      @Qualifier("calcomDBDataSource") DataSource calcomDBDataSource, JdbcTemplate jdbcTemplate) {
    this.db = db;
    this.datasource = calcomDBDataSource;
    this.jdbcTemplate = jdbcTemplate;
  }

  //TODO: replace this with named
  private final @NotNull JdbcTemplate jdbcTemplate;

  public CalcomEventType getEventTypeById(Number eventTypeId) {
    String SELECT_EVENT = "SELECT * FROM \"EventType\" WHERE id = :eventTypeId";
    SqlParameterSource parameters = new MapSqlParameterSource("eventTypeId", eventTypeId);
    Map<String, Object> result = db.queryForMap(SELECT_EVENT, parameters);
    return CalcomEventType.asInstance(result);
  }

  public CalcomEventType getEventTypeByUserId(Number userId) {
    String SELECT_EVENT = "SELECT * FROM \"EventType\" WHERE \"userId\" = :userId";
    SqlParameterSource parameters = new MapSqlParameterSource("userId", userId);
    Map<String, Object> result = db.queryForMap(SELECT_EVENT, parameters);
    return CalcomEventType.asInstance(result);
  }

  public List<CalcomEventType> getEventTypes4Team(Number teamId) {
    String SELECT_EVENT = "SELECT * FROM \"EventType\" WHERE \"teamId\" = :teamId";
    SqlParameterSource parameters = new MapSqlParameterSource("teamId", teamId);
    List<Map<String, Object>> result = db.queryForList(SELECT_EVENT, parameters);
    return result.stream().map(el -> CalcomEventType.asInstance(el)).collect(Collectors.toList());
  }

  /**
   * A = eventTypeId B = userId
   */

  public void removeTeamEventTypeMembershipsForUser(Long calcomUserId, List<Long> teamIds) {

    //TODO: check this since a another instance of named query is injected
    String QUERY = "DELETE FROM \"_user_eventtype\" WHERE \"B\"= :calcomUserId AND "
        + "\"A\" NOT IN (SELECT id from \"EventType\" WHERE \"teamId\" in (:teamIds)) AND "
        + "\"A\" IN (SELECT ID FROM \"EventType\" where \"schedulingType\" in ('roundRobin'))";
    SqlParameterSource parameters = new MapSqlParameterSource("teamIds", teamIds)
        .addValue("calcomUserId", calcomUserId);
    db.update(QUERY, parameters);
  }

  public void removeTeamEventTypeMembershipsForEventType(Number eventTypeId) {
    String DELETE_QUERY = "delete from \"_user_eventtype\" where \"A\"=" + eventTypeId;
    jdbcTemplate.update(DELETE_QUERY);
  }

  public void addUserEventTypeRelation(Number eventTypeId, Number calcomUserId) {
    try {
      String INSERT_QUERY = "insert into \"_user_eventtype\" (\"A\", \"B\") values ($eventTypeIdParam, $userIdParam)";
      INSERT_QUERY = INSERT_QUERY.replace("$eventTypeIdParam", eventTypeId.toString())
          .replace("$userIdParam", calcomUserId.toString());
      jdbcTemplate.update(INSERT_QUERY);
    } catch (Exception e) {
      //do nothing in case relation existss
    }
  }


  public List<Long> getUserIdsOfEventTypeMembers(Number eventTypeId) {
    String QUERY = "SELECT \"B\" FROM \"_user_eventtype\" WHERE \"A\" = " + eventTypeId;
    return jdbcTemplate.queryForList(QUERY, Long.class);
  }

  public void updateEventTypeScheduleId(Number eventTypeId, Number scheduleId) {
    String UPDATE_QUERY =
        "update \"EventType\" set \"scheduleId\" = $scheduleIdParam where \"id\" = " + eventTypeId;
    UPDATE_QUERY = UPDATE_QUERY.replace("$scheduleIdParam", scheduleId.toString());
    jdbcTemplate.update(UPDATE_QUERY);
  }

  public void markAsRoundRobin(Number eventTypeId) {
    String UPDATE_QUERY = "update \"EventType\" set \"schedulingType\"='roundRobin' where \"id\" = eventTypeId";
    UPDATE_QUERY = UPDATE_QUERY.replace("eventTypeId", eventTypeId.toString());
    jdbcTemplate.update(UPDATE_QUERY);
  }

  public void updateLocations(Number eventTypeId, String locations) {
    String UPDATE_QUERY = "update \"EventType\" set \"locations\"='locationsParam' where \"id\" = eventTypeId";
    UPDATE_QUERY = UPDATE_QUERY.replace("eventTypeId", eventTypeId.toString())
        .replace("locationsParam", locations);
    jdbcTemplate.update(UPDATE_QUERY);
  }

  public void markAsDefaultEventType(Number eventTypeId) {
    String UPDATE_QUERY = "update \"EventType\" set \"metadata\"='{\"defaultEventType\": \"true\"}' where \"id\" = eventTypeId";
    UPDATE_QUERY = UPDATE_QUERY.replace("eventTypeId", eventTypeId.toString());
    jdbcTemplate.update(UPDATE_QUERY);
  }

  public CalcomEventType createEventType(CalcomEventType eventType) {
    GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
    String INSERT_EVENT_TYPE = "INSERT INTO \"EventType\""
        + "(title,slug,description,length,hidden,\"userId\",\"eventName\",\"periodCountCalendarDays\",\"periodDays\",\"periodEndDate\",\"periodStartDate\",\"requiresConfirmation\""
        + ",\"minimumBookingNotice\",\"teamId\",\"disableGuests\",\"periodType\",\"slotInterval\",metadata,\"afterEventBuffer\",\"beforeEventBuffer\",\"hideCalendarNotes\") "
        + "VALUES (:title,:slug,:description,:length,:hidden,:userId,:eventName,:periodCountCalendarDays,:periodDays,:periodEndDate,:periodStartDate"
        + ",:requiresConfirmation,:minimumBookingNotice,:teamId,:disableGuests,'rolling',:slotInterval,'{}',:afterEventBuffer,:beforeEventBuffer,:hideCalendarNotes)";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("title", eventType.getTitle())
        .addValue("slug", eventType.getSlug())
        .addValue("description", eventType.getDescription())
        .addValue("length", eventType.getLength())
        .addValue("hidden", false)
        .addValue("userId", eventType.getUserId())
        .addValue("eventName", eventType.getEventName())
        .addValue("periodCountCalendarDays", true)
        .addValue("periodDays", eventType.getPeriodDays())
        .addValue("periodEndDate", eventType.getPeriodEndDate())
        .addValue("periodStartDate", eventType.getPeriodStartDate())
        .addValue("requiresConfirmation", eventType.getRequiresConfirmation())
        .addValue("minimumBookingNotice", eventType.getMinimumBookingNotice())
        .addValue("schedulingType", eventType.getSchedulingType())
        .addValue("teamId", eventType.getTeamId())
        .addValue("disableGuests", eventType.getDisableGuests())
        .addValue("periodType", "rolling")
        .addValue("slotInterval", eventType.getSlotInterval())
        .addValue("metadata", eventType.getMetadata())
        .addValue("afterEventBuffer", eventType.getAfterEventBuffer())
        .addValue("beforeEventBuffer", eventType.getBeforeEventBuffer())
        .addValue("hideCalendarNotes", eventType.getHideCalendarNotes());

    db.update(INSERT_EVENT_TYPE, parameters, generatedKeyHolder);
    var eventTypeId = Integer.valueOf((Integer) generatedKeyHolder.getKeys().get("id"));
    updateLocations(eventTypeId, eventType.getLocations());
    return getEventTypeById(eventTypeId);
  }

  public void updateEventType(CalcomEventType eventType) {
    String UPDATE_QUERY =
        "update \"EventType\" set description = :description, title = :title, length = :length  where \"id\" = :id";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("title", eventType.getTitle())
        .addValue("description", eventType.getDescription())
        .addValue("length", eventType.getLength())
        .addValue("id", eventType.getId());
    db.update(UPDATE_QUERY, parameters);
  }


  public void addRoundRobinHosts(Number eventTypeId, Number calComUserId) {
    String INSERT_HOSTS = "INSERT INTO \"Host\" (\"userId\", \"eventTypeId\", \"isFixed\") VALUES (:userId, :eventTypeId, false)";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("eventTypeId", eventTypeId)
        .addValue("userId", calComUserId);
    db.update(INSERT_HOSTS, parameters);
  }

  public void removeTeamEventTypeHostsForUser(Number calComUserId) {
    String QUERY = "DELETE FROM \"Host\" WHERE \"userId\"= :userId";
    SqlParameterSource parameters = new MapSqlParameterSource("userId", calComUserId);
    db.update(QUERY, parameters);
  }

  public void deleteAllEventTypesOfUser(Long calcomUserId) {
    String QUERY = "DELETE FROM \"EventType\" WHERE \"userId\"= :userId";
    SqlParameterSource parameters = new MapSqlParameterSource("userId", calcomUserId);
    db.update(QUERY, parameters);
  }

  public void deleteEventType(Long eventTypeId) {
    String QUERY = "DELETE FROM \"EventType\" WHERE id = :eventTypeId";
    SqlParameterSource parameters = new MapSqlParameterSource("eventTypeId", eventTypeId);
    db.update(QUERY, parameters);
  }
}
