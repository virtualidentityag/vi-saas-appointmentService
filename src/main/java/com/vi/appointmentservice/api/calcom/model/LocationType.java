package com.vi.appointmentservice.api.calcom.model;

import java.util.Arrays;
import java.util.Optional;


public enum LocationType {

  INTEGRATIONS_DAILY("integrations:daily"),
  USER_PHONE("userPhone"),
  LINK("link"),
  IN_PERSON("inPerson"),
  CHAT("");

  private final String value;

  LocationType(String locationType) {
    this.value = locationType;
  }

  public String getValue() {
    return value;
  }

  public static Optional<LocationType> findByLocationType(String locationType) {
    return Arrays.stream(LocationType.values())
        .filter(locationEnum -> locationEnum.value.equals(locationType))
        .findFirst();
  }

}