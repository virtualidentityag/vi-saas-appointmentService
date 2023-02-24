package com.vi.appointmentservice.api.calcom.model;

import com.vi.appointmentservice.api.model.Location;
import com.vi.appointmentservice.api.model.TeamEventTypeConsultant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class EventType {

  private Integer id;

  private String title;

  private String slug;

  private Integer length;

  private Boolean hidden;

  private Integer userId;

  private Number teamId;

  private String eventName;

  private String timeZone;

  private String periodType;

  private String periodStartDate;

  private String periodEndDate;

  private Integer periodDays;

  private Boolean periodCountCalendarDays;

  private Boolean requiresConfirmation;

  private String recurringEvent;

  private Boolean disableGuests;

  private Boolean hideCalendarNotes;

  private Integer minimumBookingNotice;

  private Integer beforeEventBuffer;

  private Integer afterEventBuffer;

  private String schedulingType;

  private Integer price;

  private String currency;

  private Integer slotInterval;

  private String successRedirectUrl;

  private String description;

  private List<Location> locations = null;

  private String metadata;

  private String type;

  private List<TeamEventTypeConsultant> consultants = null;

  private List<Long> memberIds = new ArrayList<>();

  public static EventType asInstance(Map<String, Object> params) {
    EventType eventType = new EventType();
    eventType.setId((Integer) params.get("id"));
    eventType.setMetadata(params.get("metadata").toString());
    eventType.setTeamId((Integer) params.get("teamId"));
    eventType.setTitle(params.get("title").toString());
    eventType.setDescription(params.get("description").toString());
    eventType.setLength((Integer) params.get("length"));
    return eventType;
  }

}
