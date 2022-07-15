package com.vi.appointmentservice.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.service.CalComEventTypeService;
import com.vi.appointmentservice.service.CalComUserService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RescheduleHelper {

  private final @NonNull CalComEventTypeService eventTypeService;
  private final @NonNull CalComUserService calComUserService;

  public CalcomBooking attachRescheduleLink(CalcomBooking calcomBooking)
      throws JsonProcessingException {
    String userSlug = this.calComUserService.getUserById(Long.valueOf(calcomBooking.getUserId())).getUsername();
    String eventTypeSlug = this.eventTypeService.getEventTypeById(
        Long.valueOf(calcomBooking.getEventTypeId())).getSlug();
    calcomBooking.setRescheduleLink("/" + userSlug +  "/" + eventTypeSlug + "?rescheduleUid=" + calcomBooking.getUid());
    return calcomBooking;
  }

}
