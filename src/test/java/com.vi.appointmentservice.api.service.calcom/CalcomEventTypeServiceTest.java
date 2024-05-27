package com.vi.appointmentservice.api.service.calcom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.calcom.model.CalcomEventType;
import com.vi.appointmentservice.api.calcom.repository.EventTypeRepository;
import com.vi.appointmentservice.api.calcom.repository.WebhookRepository;
import com.vi.appointmentservice.api.calcom.service.CalcomEventTypeService;
import com.vi.appointmentservice.api.calcom.service.CalcomLocationsService;
import com.vi.appointmentservice.api.facade.AppointmentType;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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

  @Test
  public void shouldCreateEventTypeAndNotUpdateLocationsIfAppointmentTypeDoesNotContainLocations() {
    // given
    when(eventTypeRepository.createEventType(Mockito.any(CalcomEventType.class))).thenReturn(new CalcomEventType());
    // when
    calcomEventTypeService.createEventType(1, new AppointmentType());
    // then

    Mockito.verify(eventTypeRepository, Mockito.never()).updateLocations(Mockito.anyInt(), Mockito.anyString());
  }

  @Test
  public void shouldUpdateEventTypeTitle() {
    // given
    var eventType = new CalcomEventType();
    eventType.setId(1);
    eventType.setTitle("Beratung mit dem / der Berater:in ConsultantFirstname");

    Long calcomUserId = 2L;
    when(eventTypeRepository.getEventTypeByUserId(calcomUserId)).thenReturn(eventType);

    // when
    calcomEventTypeService.updateEventTypeTitle(calcomUserId, "ConsultantDisplayName");
    // then

    ArgumentCaptor<CalcomEventType> captor = ArgumentCaptor.forClass(CalcomEventType.class);
    Mockito.verify(eventTypeRepository).updateEventType(captor.capture());
    assertThat(captor.getValue().getTitle()).isEqualTo("Beratung mit dem / der Berater:in ConsultantDisplayName");
  }

}