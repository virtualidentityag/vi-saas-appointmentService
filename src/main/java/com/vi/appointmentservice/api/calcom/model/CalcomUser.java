package com.vi.appointmentservice.api.calcom.model;

import java.util.Map;
import lombok.Data;

@Data
public class CalcomUser {

  private Long id;

  private String username;

  private String name;

  private String email;

  private String timeZone;

  private String weekStart;

  private Integer defaultScheduleId;

  private String locale;

  private Integer timeFormat;

  private Boolean allowDynamicBooking;

  private String password;

  private String plainPassword;

  public static CalcomUser asInstance(Map<String, Object> result) {
    CalcomUser user = new CalcomUser();
    user.setName(result.get("name").toString());
    return user;
  }
}

