package com.vi.appointmentservice.api.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vi.appointmentservice.api.model.CalcomWebhookInputPayload;
import com.vi.appointmentservice.api.model.CalcomWebhookInputPayloadMetadata;
import com.vi.appointmentservice.api.model.CalcomWebhookInputPayloadOrganizer;
import com.vi.appointmentservice.api.service.calcom.CalComBookingService;
import com.vi.appointmentservice.api.service.calcom.CalComEventTypeService;
import com.vi.appointmentservice.api.service.onlineberatung.AdminUserService;
import com.vi.appointmentservice.api.service.onlineberatung.MessagesService;
import com.vi.appointmentservice.api.service.onlineberatung.UserService;
import com.vi.appointmentservice.api.service.onlineberatung.VideoAppointmentService;
import com.vi.appointmentservice.api.service.statistics.StatisticsService;
import com.vi.appointmentservice.appointmentservice.generated.web.model.Appointment;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.CalcomRepository;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import com.vi.appointmentservice.userservice.generated.web.model.EnquiryAppointmentDTO;
import com.vi.appointmentservice.userservice.generated.web.AppointmentControllerApi;

@ExtendWith(MockitoExtension.class)
class CalcomWebhookHandlerServiceTest {

  @Mock VideoAppointmentService videoAppointmentService;
  @Mock CalcomBookingToAskerRepository calcomBookingToAskerRepository;
  @Mock MessagesService messagesService;
  @Mock CalComBookingService calComBookingService;
  @Mock CalComEventTypeService calComEventTypeService;
  @Mock StatisticsService statisticsService;
  @Mock CalcomUserToConsultantRepository calcomUserToConsultantRepository;
  @Mock AdminUserService adminUserService;
  @Mock CalcomRepository calcomRepository;
  @Mock UserService userService;
  @Mock
  AppointmentControllerApi userAppointmentControllerApi;

  @InjectMocks
  CalcomWebhookHandlerService calcomWebhookHandlerService;

  @Test
  void handleCreateEvent_Should_CallUserServiceAndCreateRocketchatRoomsIfInitialAppointmentEvent() {
    // given
    CalcomWebhookInputPayload payload = new CalcomWebhookInputPayload().bookingId(1).organizer(
        new CalcomWebhookInputPayloadOrganizer().email("email")).metadata(new CalcomWebhookInputPayloadMetadata().rcUserId("rcUserId").isInitialAppointment(true).sessionId("1520"));
    when(userService.getUserAppointmentApi()).thenReturn(userAppointmentControllerApi);
    when(videoAppointmentService.createAppointment(payload.getOrganizer().getEmail(), payload.getStartTime())).thenReturn(new Appointment().id(
        UUID.randomUUID()));
    // when
    calcomWebhookHandlerService.handleCreateEvent(payload);

    // then
    verify(videoAppointmentService).createAppointment(payload.getOrganizer().getEmail(), payload.getStartTime());
    CalcomWebhookInputPayloadMetadata metadata = payload.getMetadata();
    verify(userAppointmentControllerApi).createEnquiryAppointment(Mockito.eq(Long.valueOf(metadata.getSessionId())), Mockito.eq(
            metadata.getRcToken()), Mockito.eq(metadata.getRcUserId()),
        Mockito.any(EnquiryAppointmentDTO.class));
  }

  @Test
  void handleCreateEvent_Should_CallUserServiceAndNotCreateRocketchatRoomsIfNotInitialAppointmentEvent() {
    // given
    CalcomWebhookInputPayload payload = new CalcomWebhookInputPayload().bookingId(1).organizer(
        new CalcomWebhookInputPayloadOrganizer().email("email")).metadata(new CalcomWebhookInputPayloadMetadata().rcUserId("rcUserId").isInitialAppointment(false).sessionId("1520"));
    when(videoAppointmentService.createAppointment(payload.getOrganizer().getEmail(), payload.getStartTime())).thenReturn(new Appointment().id(
        UUID.randomUUID()));
    // when
    calcomWebhookHandlerService.handleCreateEvent(payload);

    // then
    verify(videoAppointmentService).createAppointment(payload.getOrganizer().getEmail(), payload.getStartTime());
    verify(userAppointmentControllerApi, Mockito.never()).createEnquiryAppointment(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
        Mockito.any(EnquiryAppointmentDTO.class));
  }
}