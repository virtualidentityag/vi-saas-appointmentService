package com.vi.appointmentservice.api.facade;

import java.util.List;
import lombok.Data;

@Data
public class AppointmentType {

  private String title;
  private String description;
  private Integer length;
  private Integer minimumBookingNotice;
  private Integer beforeEventBuffer;
  private Integer afterEventBuffer;
  private Integer slotInterval;
  private List<String> locations;
}
