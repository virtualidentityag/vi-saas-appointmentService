package com.vi.appointmentservice.api.calcom.service;

import static org.assertj.core.api.Assertions.assertThat;
import com.vi.appointmentservice.api.calcom.model.CalcomEventType;
import com.vi.appointmentservice.api.model.EventTypeDTO;
import org.junit.jupiter.api.Test;

class EventTypeMapperTest {

  @Test
  void should_ConvertLocations_If_TheyHaveValidJsonFormat() {
    // given
    CalcomEventType eventType = new CalcomEventType();
    eventType.setLocations("[\n"
        + "  {\n"
        + "    \"type\": \"integrations:daily\"\n"
        + "  },\n"
        + "  {\n"
        + "    \"type\": \"inPerson\",\n"
        + "    \"address\": \"Die Adresse der Beratungsstelle teilt Ihnen Ihr:e Berater:in im Chat mit\"\n"
        + "  },\n"
        + "  {\n"
        + "    \"link\": \"suchtberatung.digital\",\n"
        + "    \"type\": \"link\"\n"
        + "  },\n"
        + "  {\n"
        + "    \"type\": \"userPhone\",\n"
        + "    \"hostPhoneNumber\": \"Die Telefonnummer teilt Ihnen Ihr:e Berater:in im Chat mit\"\n"
        + "  }\n"
        + "]");
    // when

    EventTypeDTO eventTypeDTO = new EventTypeMapper().asEventTypeDTO(eventType);
    // then
    assertThat(eventTypeDTO.getLocations()).isNotNull();
    assertThat(eventTypeDTO.getLocations()).hasSize(4);
    assertThat(eventTypeDTO.getLocations().get(0).getType()).isEqualTo("integrations:daily");
    assertThat(eventTypeDTO.getLocations().get(1).getType()).isEqualTo("inPerson");
    assertThat(eventTypeDTO.getLocations().get(1).getAddress()).isEqualTo("Die Adresse der Beratungsstelle teilt Ihnen Ihr:e Berater:in im Chat mit");
    assertThat(eventTypeDTO.getLocations().get(2).getType()).isEqualTo("link");
    assertThat(eventTypeDTO.getLocations().get(2).getLink()).isEqualTo("suchtberatung.digital");
    assertThat(eventTypeDTO.getLocations().get(3).getType()).isEqualTo("userPhone");
    assertThat(eventTypeDTO.getLocations().get(3).getHostPhoneNumber()).isEqualTo("Die Telefonnummer teilt Ihnen Ihr:e Berater:in im Chat mit");
  }

  @Test
  void should_NotConvertLocations_If_TheyAreInvalidOrNonParsableJson() {
    // given
    CalcomEventType eventType = new CalcomEventType();
    eventType.setLocations("invalid json");
    // when

    EventTypeDTO eventTypeDTO = new EventTypeMapper().asEventTypeDTO(eventType);
    // then
    assertThat(eventTypeDTO.getLocations()).isNull();
  }

  @Test
  void should_ConvertMetadata_If_MetadataSet() {
    // given
    CalcomEventType eventType = new CalcomEventType();
    eventType.setLocations("{}");
    eventType.setMetadata("{\"defaultEventType\": \"true\"}");
    // when

    EventTypeDTO eventTypeDTO = new EventTypeMapper().asEventTypeDTO(eventType);
    // then
    assertThat(eventTypeDTO.getMetadata()).isEqualTo("{\"defaultEventType\": \"true\"}");
  }

}