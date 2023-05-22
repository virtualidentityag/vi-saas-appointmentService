package com.vi.appointmentservice.api.service.calcom;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.calcom.model.CalcomEventType;
import com.vi.appointmentservice.api.calcom.repository.EventTypeRepository;
import com.vi.appointmentservice.api.calcom.repository.WebhookRepository;
import com.vi.appointmentservice.api.calcom.service.CalcomEventTypeService;
import com.vi.appointmentservice.api.calcom.service.CalcomLocationsService;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class CalcomEventTypeServiceTest {

  @InjectMocks
  CalcomEventTypeService calcomEventTypeService;

  @Mock
  EventTypeRepository eventTypeRepository;

  @Mock
  CalcomLocationsService calcomLocationsService;

  @Mock
  WebhookRepository webhookRepository;


  @Test
  public void shouldUpdateEventTypeAndUpdateLocations() {
    // given
    var eventType = new CalcomEventType();
    eventType.setId(1);
    when(calcomLocationsService.resolveToJsonByLocationType(Mockito.anyString()))
        .thenReturn("{locationJson}");
    when( eventTypeRepository.getEventTypeById(eventType.getId())).thenReturn(eventType);
    when(eventTypeRepository
        .getUserIdsOfEventTypeMembers(eventType.getId())).thenReturn(Lists.newArrayList());
    // when
    calcomEventTypeService.updateEventType(eventType, Lists.newArrayList("location"));
    // then

    Mockito.verify(eventTypeRepository).updateLocations(1, "[{locationJson}]");
    Mockito.verify(eventTypeRepository).updateEventType(eventType);
  }

}