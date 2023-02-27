package com.vi.appointmentservice.api.calcom.model;

import java.util.Map;
import lombok.Data;

@Data
public class CalcomTeam {

  private Long id;

  private String name;

  private String slug;

  public static CalcomTeam asInstance(Map<String, Object> params) {
    CalcomTeam team = new CalcomTeam();
    team.setId(Long.valueOf((Integer) params.get("id")));
    team.setSlug((String) params.get("slug"));
    team.setName((String) params.get("name"));
    return team;
  }

}
