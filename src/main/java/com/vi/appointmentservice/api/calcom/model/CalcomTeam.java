package com.vi.appointmentservice.api.calcom.model;

import java.util.Map;
import lombok.Data;

@Data
public class CalcomTeam {

  private Integer id;

  private String name;

  private String slug;

  private Boolean hideBranding;

  public static CalcomTeam asInstance(Map<String, Object> params) {
    CalcomTeam team = new CalcomTeam();
    team.setId((Integer) params.get("id"));
    team.setSlug((String) params.get("slug"));
    return team;
  }

}
