package com.vi.appointmentservice.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.vi.appointmentservice.api.calcom.model.CalcomEventType;
import com.vi.appointmentservice.api.calcom.model.CalcomUser;
import com.vi.appointmentservice.api.calcom.repository.EventTypeRepository;
import com.vi.appointmentservice.api.calcom.service.CalComTeamService;
import com.vi.appointmentservice.api.calcom.service.CalComUserService;
import com.vi.appointmentservice.api.calcom.service.CalcomEventTypeService;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.service.onlineberatung.AdminUserService;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.UserToConsultantRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RescheduleHelperTest {

  @InjectMocks
  RescheduleHelper rescheduleHelper;

  @Mock
  CalComUserService calComUserService;

  @Mock
  EventTypeRepository eventTypeRepository;

  @Mock
  UserToConsultantRepository userToConsultantRepository;

  @Mock
  AdminUserService adminUserService;

  @Mock
  CalcomBookingToAskerRepository calcomBookingToAskerRepository;

  @Mock
  CalComTeamService calComTeamService;

  @Mock
  CalcomEventTypeService calcomEventTypeService;

  @Test
  void shouldNotAttachRescheduleLink_When_EventTypeIsNotFound() {
    // given
    when(calComUserService.getUserById(Mockito.any())).thenReturn(new CalcomUser());

    when(calcomEventTypeService.findEventTypeById(Mockito.any())).thenReturn(Optional.empty());
    // when
    var result = rescheduleHelper.attachRescheduleLink(new CalcomBooking().userId(1).eventTypeId(2));
    // then
    assertNull(result.getRescheduleLink());
  }

  @Test
  void should_AttachRescheduleLink_When_EventTypeIsFound() {
    // given
    when(calComUserService.getUserById(Mockito.any())).thenReturn(new CalcomUser());
    CalcomEventType calcomEventType = new CalcomEventType();
    calcomEventType.setSlug("slug");
    when(calcomEventTypeService.findEventTypeById(Mockito.any())).thenReturn(Optional.of(calcomEventType));
    // when
    var result = rescheduleHelper.attachRescheduleLink(new CalcomBooking().userId(1).eventTypeId(2));
    // then
    assertNotNull(result.getRescheduleLink());
  }
}