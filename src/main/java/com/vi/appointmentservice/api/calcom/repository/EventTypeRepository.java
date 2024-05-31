package com.vi.appointmentservice.api.calcom.repository;

import com.vi.appointmentservice.api.calcom.model.CalcomEventType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  private static final String EVENT_TYPE_ID = "eventTypeId";
  private static final String USER_ID = "userId";
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
    String selectEvent = "SELECT * FROM \"EventType\" WHERE id = :eventTypeId";
    SqlParameterSource parameters = new MapSqlParameterSource(EVENT_TYPE_ID, eventTypeId);
    Map<String, Object> result = db.queryForMap(selectEvent, parameters);
    return getCalcomEventType(result);
  }

  public Optional<CalcomEventType> findEventTypeById(Number eventTypeId) {
    String selectEvent = "SELECT * FROM \"EventType\" WHERE id = :eventTypeId";
    SqlParameterSource parameters = new MapSqlParameterSource(EVENT_TYPE_ID, eventTypeId);
    List<Map<String, Object>> result = db.queryForList(selectEvent, parameters);
    if (result.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(getCalcomEventType(result.get(0)));
    }
  }

  public CalcomEventType getEventTypeByUserId(Number userId) {
    String selectEvent = "SELECT * FROM \"EventType\" WHERE \"userId\" = :userId";
    SqlParameterSource parameters = new MapSqlParameterSource(USER_ID, userId);
    Map<String, Object> result = db.queryForMap(selectEvent, parameters);
    return getCalcomEventType(result);
  }

  public List<CalcomEventType> getEventTypes4Team(Number teamId) {
    String selectEvent = "SELECT * FROM \"EventType\" WHERE \"teamId\" = :teamId";
    SqlParameterSource parameters = new MapSqlParameterSource("teamId", teamId);
    List<Map<String, Object>> result = db.queryForList(selectEvent, parameters);
    return result.stream().map(el -> getCalcomEventType(el)).collect(Collectors.toList());
  }

  private static CalcomEventType getCalcomEventType(Map<String, Object> el) {
    CalcomEventType eventType = CalcomEventType.asInstance(el);
    if (el.containsKey("locations")) {
      eventType.setLocations(el.get("locations").toString());
    }
    return eventType;
  }

  /**
   * A = eventTypeId B = userId
   */

  public void removeTeamEventTypeMembershipsForUser(Long calcomUserId, List<Long> teamIds) {

    //TODO: check this since a another instance of named query is injected
    String query = "DELETE FROM \"_user_eventtype\" WHERE \"B\"= :calcomUserId AND "
        + "\"A\" NOT IN (SELECT id from \"EventType\" WHERE \"teamId\" in (:teamIds)) AND "
        + "\"A\" IN (SELECT ID FROM \"EventType\" where \"schedulingType\" in ('roundRobin'))";
    SqlParameterSource parameters = new MapSqlParameterSource("teamIds", teamIds)
        .addValue("calcomUserId", calcomUserId);
    db.update(query, parameters);
  }

  public void removeTeamEventTypeMembershipsForEventType(Number eventTypeId) {
    String deleteQuery = "delete from \"_user_eventtype\" where \"A\"=" + eventTypeId;
    jdbcTemplate.update(deleteQuery);
  }

  public void removeTeamEventHostsForEventType(Number eventTypeId) {
    String deleteQuery = "delete from \"Host\" where \"eventTypeId\"=" + eventTypeId;
    jdbcTemplate.update(deleteQuery);
  }

  public void addUserEventTypeRelation(Number eventTypeId, Number calcomUserId) {
    try {
      String insertQuery = "insert into \"_user_eventtype\" (\"A\", \"B\") values ($eventTypeIdParam, $userIdParam)";
      insertQuery = insertQuery.replace("$eventTypeIdParam", eventTypeId.toString())
          .replace("$userIdParam", calcomUserId.toString());
      jdbcTemplate.update(insertQuery);
    } catch (Exception e) {
      //do nothing in case relation existss
    }
  }


  public List<Long> getUserIdsOfEventTypeMembers(Number eventTypeId) {
    String query = "SELECT \"B\" FROM \"_user_eventtype\" WHERE \"A\" = " + eventTypeId;
    return jdbcTemplate.queryForList(query, Long.class);
  }

  public void updateEventTypeScheduleId(Number eventTypeId, Number scheduleId) {
    String updateQuery =
        "update \"EventType\" set \"scheduleId\" = $scheduleIdParam where \"id\" = " + eventTypeId;
    updateQuery = updateQuery.replace("$scheduleIdParam", scheduleId.toString());
    jdbcTemplate.update(updateQuery);
  }

  public void markAsRoundRobin(Number eventTypeId) {
    String updateQuery = "update \"EventType\" set \"schedulingType\"='roundRobin' where \"id\" = eventTypeId";
    updateQuery = updateQuery.replace(EVENT_TYPE_ID, eventTypeId.toString());
    jdbcTemplate.update(updateQuery);
  }

  public void updateLocations(Number eventTypeId, String locations) {
    String updateQuery = "update \"EventType\" set \"locations\"='locationsParam' where \"id\" = eventTypeId";
    updateQuery = updateQuery.replace(EVENT_TYPE_ID, eventTypeId.toString())
        .replace("locationsParam", locations);
    jdbcTemplate.update(updateQuery);
  }

  public void markAsDefaultEventType(Number eventTypeId) {
    String updateQuery = "update \"EventType\" set \"metadata\"='{\"defaultEventType\": \"true\"}' where \"id\" = eventTypeId";
    updateQuery = updateQuery.replace(EVENT_TYPE_ID, eventTypeId.toString());
    jdbcTemplate.update(updateQuery);
  }

  public CalcomEventType createEventType(CalcomEventType eventType) {
    GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
    String insertEventType = "INSERT INTO \"EventType\""
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
        .addValue(USER_ID, eventType.getUserId())
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

    db.update(insertEventType, parameters, generatedKeyHolder);
    Map<String, Object> keys = generatedKeyHolder.getKeys();
    if (keys == null) {
      throw new IllegalStateException("Unable to create event type");
    }
    var eventTypeId = (Integer) keys.get("id");
    updateLocations(eventTypeId, eventType.getLocations());
    return getEventTypeById(eventTypeId);
  }

  public void updateEventType(CalcomEventType eventType) {
    String updateQuery =
        "update \"EventType\" set description = :description, title = :title, length = :length  where \"id\" = :id";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("title", eventType.getTitle())
        .addValue("description", eventType.getDescription())
        .addValue("length", eventType.getLength())
        .addValue("id", eventType.getId());
    db.update(updateQuery, parameters);
  }


  public void addRoundRobinHosts(Number eventTypeId, Number calComUserId) {
    String insertHosts = "INSERT INTO \"Host\" (\"userId\", \"eventTypeId\", \"isFixed\") VALUES (:userId, :eventTypeId, false)";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue(EVENT_TYPE_ID, eventTypeId)
        .addValue(USER_ID, calComUserId);
    db.update(insertHosts, parameters);
  }

  public void removeTeamEventTypeHostsForUser(Number calComUserId) {
    String query = "DELETE FROM \"Host\" WHERE \"userId\"= :userId";
    SqlParameterSource parameters = new MapSqlParameterSource(USER_ID, calComUserId);
    db.update(query, parameters);
  }

  public void deleteAllEventTypesOfUser(Long calcomUserId) {
    String query = "DELETE FROM \"EventType\" WHERE \"userId\"= :userId";
    SqlParameterSource parameters = new MapSqlParameterSource(USER_ID, calcomUserId);
    db.update(query, parameters);
  }

  public void deleteEventType(Long eventTypeId) {
    String query = "DELETE FROM \"EventType\" WHERE id = :eventTypeId";
    SqlParameterSource parameters = new MapSqlParameterSource(EVENT_TYPE_ID, eventTypeId);
    db.update(query, parameters);
  }
}
