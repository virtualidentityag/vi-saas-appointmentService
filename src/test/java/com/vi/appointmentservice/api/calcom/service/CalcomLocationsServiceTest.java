package com.vi.appointmentservice.api.calcom.service;

import static org.assertj.core.api.Assertions.assertThat;
import com.vi.appointmentservice.api.calcom.model.LocationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalcomLocationsServiceTest {

  @InjectMocks
  CalcomLocationsService calcomLocationsService;

  @Test
  void resolveToJsonByLocationType_Should_ConvertToProperJsonForIntegrationsDaily() {
    // When
    String jsonResult = calcomLocationsService.resolveToJsonByLocationType(LocationType.INTEGRATIONS_DAILY.getValue());
    // Then
    assertThat(jsonResult).isEqualTo("{\"type\": \"integrations:daily\"}");
  }

  @Test
  void resolveToJsonByLocationType_Should_ConvertToProperJsonForInPerson() {
    // When
    String jsonResult = calcomLocationsService.resolveToJsonByLocationType(LocationType.IN_PERSON.getValue());
    // Then
    assertThat(jsonResult).isEqualTo("{\"type\": \"inPerson\",\"address\": \"Die Adresse der Beratungsstelle teilt Ihnen Ihr:e Berater:in im Chat mit\"}");
  }

  @Test
  void resolveToJsonByLocationType_Should_ConvertToProperJsonForUserPhone() {
    // When
    String jsonResult = calcomLocationsService.resolveToJsonByLocationType(LocationType.USER_PHONE.getValue());
    // Then
    assertThat(jsonResult).isEqualTo("{\"type\": \"userPhone\",\"hostPhoneNumber\": \"Die Telefonnummer teilt Ihnen Ihr:e Berater:in im Chat mit\"}");
  }

  @Test
  void resolveToJsonByLocationType_Should_ConvertToProperJsonForLink() {
    // When
    String jsonResult = calcomLocationsService.resolveToJsonByLocationType(LocationType.LINK.getValue());
    // Then
    assertThat(jsonResult).isEqualTo("{\"link\": \"suchtberatung.digital\",\"type\": \"link\"}");
  }

  @Test
  void resolveToJsonByLocationType_Should_ReturnNullForChatAsThisIsDeprecated() {
    // When
    String jsonResult = calcomLocationsService.resolveToJsonByLocationType(LocationType.CHAT.getValue());
    // Then
    assertThat(jsonResult).isNull();
  }

}