package com.vi.appointmentservice.api.service.calcom;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class CalComCalcomEventTypeServiceTest {

  @Mock
  private RestTemplate restTemplate;
  @Mock
  private ObjectMapper objectMapper;

  @Test
  public void getDefaultEventTypeOfTeam() {
//    Integer teamId = 1;
//    Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(String.class)))
//        .thenReturn(buildResponse(teamId));
////    CalcomEventTypeDTO defaultEventTypeOfTeam = calComEventTypeService
////        .getDefaultEventTypeOfTeam(Long.valueOf(teamId));
////
////    MatcherAssert.assertThat(defaultEventTypeOfTeam.getId(), Matchers.is(2));
  }



}