package com.vi.appointmentservice.api.service;

import com.vi.appointmentservice.api.facade.AppointmentType;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {

  private static final String DEFAULT_EVENT_DESCRIPTION =
      "Bitte wählen Sie Ihre gewünschte Terminart. Wir bemühen uns, Ihren Wunsch zu erfüllen. "
          + "Die Berater:innen werden Sie ggf per Chat auf unserer Plattform informieren. "
          + "Loggen Sie sich also vor einem Termin auf jeden Fall ein!";

  public AppointmentType createDefaultAppointmentType() {
    AppointmentType appointmentType = new AppointmentType();
    appointmentType.setAfterEventBuffer(10);
    appointmentType.setBeforeEventBuffer(0);
    appointmentType.setDescription(DEFAULT_EVENT_DESCRIPTION);
    appointmentType.setLength(50);
    appointmentType.setMinimumBookingNotice(240);
    appointmentType.setSlotInterval(15);
    return appointmentType;
  }

}
