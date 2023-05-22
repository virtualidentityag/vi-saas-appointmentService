package com.vi.appointmentservice.api.calcom.service;

import com.vi.appointmentservice.api.calcom.model.LocationType;
import com.vi.appointmentservice.api.model.CalcomBooking;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class CalcomLocationsService {

  private static final String IN_PERSON_MEETING_MESSAGE = "Die Adresse der Beratungsstelle teilt Ihnen Ihr:e Berater:in im Chat mit";
  public static final String IN_PERSON_JSON =
      "{\"type\": \"inPerson\",\"address\": \"" + IN_PERSON_MEETING_MESSAGE + "\"}";

  private static final String PHONE_CALL_MEETING_MESSAGE = "Die Telefonnummer teilt Ihnen Ihr:e Berater:in im Chat mit";
  public static final String USER_PHONE_JSON =
      "{\"type\": \"userPhone\",\"hostPhoneNumber\": \"" + PHONE_CALL_MEETING_MESSAGE + "\"}";

  private static final String VIDEO_CALL = "integrations:daily";

  private static final String LINK = "suchtberatung.digital";
  public static final String LINK_JSON = "{\"link\": \"" + LINK + "\",\"type\": \"link\"}";
  public static final String INTEGRATIONS_DAILY_JSON = "{\"type\": \"integrations:daily\"}";

  public String resolveLocationType(CalcomBooking booking) {
    if (IN_PERSON_MEETING_MESSAGE.equals(booking.getLocation())) {
      return LocationType.IN_PERSON.name();
    } else if (PHONE_CALL_MEETING_MESSAGE.equals(booking.getLocation())) {
      return LocationType.USER_PHONE.name();
    } else if (VIDEO_CALL.equals(booking.getLocation())) {
      return LocationType.LINK.name();
    } else if (booking.getLocation().contains(LINK)) {
      return LocationType.CHAT.name();
    }
    throw new IllegalStateException("Unknown location type");
  }

  public String resolveToJsonByLocationType(String locationType) {

    Optional<LocationType> enumLocationType = LocationType.findByLocationType(locationType);
    if (enumLocationType.isPresent()) {
      switch (enumLocationType.get()) {
        case INTEGRATIONS_DAILY:
          return INTEGRATIONS_DAILY_JSON;
        case IN_PERSON:
          return IN_PERSON_JSON;
        case LINK:
          return LINK_JSON;
        case USER_PHONE:
          return USER_PHONE_JSON;
        default:
          break;
      }
    }
    return null;
  }

  public String buildDefaultCalcomLocations() {
    return "[" + INTEGRATIONS_DAILY_JSON + ","
        + IN_PERSON_JSON + ","
        + LINK_JSON + ","
        + USER_PHONE_JSON + "]";
  }
}
