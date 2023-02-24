package com.vi.appointmentservice.api.calcom.model;

import com.vi.appointmentservice.api.model.Location;
import com.vi.appointmentservice.api.model.TeamEventTypeConsultant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CalcomEventType {

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

  public static CalcomEventType asInstance(Map<String, Object> params) {
    CalcomEventType eventType = new CalcomEventType();
    eventType.setId((Integer) params.get("id"));
    eventType.setMetadata((String)params.get("metadata"));
    eventType.setTeamId((Integer) params.get("teamId"));
    eventType.setTitle((String)params.get("title"));
    eventType.setDescription((String) params.get("description"));
    eventType.setLength((Integer) params.get("length"));
    return eventType;
  }

}
