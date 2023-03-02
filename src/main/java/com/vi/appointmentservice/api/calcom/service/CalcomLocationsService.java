package com.vi.appointmentservice.api.calcom.service;

import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.calcom.model.LocationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CalcomLocationsService {

  private static final String IN_PERSON_MEETING_MESSAGE = "Die Adresse der Beratungsstelle teilt Ihnen ihr:e Berater:in im Chat mit";

  private static final String PHONE_CALL_MEETING_MESSAGE = "Die Telefonnummer teilt Ihnen ihr:e Berater:in im Chat mit";

  private static final String VIDEO_CALL = "integrations:jitsi";

  private static final String LINK = "suchtberatung.digital";

  public String resolveLocationType(CalcomBooking booking) {
    if (IN_PERSON_MEETING_MESSAGE.equals(booking.getLocation())) {
      return LocationType.IN_PERSON.name();
    } else if (PHONE_CALL_MEETING_MESSAGE.equals(booking.getLocation())) {
      return LocationType.PHONE_CALL.name();
    } else if (VIDEO_CALL.equals(booking.getLocation())) {
      return LocationType.VIDEO_CALL.name();
    } else if (booking.getLocation().contains(LINK)) {
      return LocationType.CHAT.name();
    }
    throw new IllegalStateException("Unknown location type");
  }

  public String buildCalcomLocations() {
    return "[{\"type\": \"integrations:jitsi\"},"
        + "{\"type\": \"inPerson\",\"address\": \"" + IN_PERSON_MEETING_MESSAGE + "\"},"
        + "{\"link\": \"" + LINK + "\",\"type\": \"link\"},"
        + "{\"type\": \"userPhone\",\"hostPhoneNumber\": \"" + PHONE_CALL_MEETING_MESSAGE + "\"}]";
  }
}
