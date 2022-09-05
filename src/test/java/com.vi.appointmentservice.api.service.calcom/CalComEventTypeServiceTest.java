package com.vi.appointmentservice.api.service.calcom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.CalcomEventTypeDTO;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class CalComEventTypeServiceTest {

  @InjectMocks
  private CalComEventTypeService calComEventTypeService;

  @Mock
  private RestTemplate restTemplate;
  @Mock
  private ObjectMapper objectMapper;

  @Test
  public void getDefaultEventTypeOfTeam() {
    Integer teamId = 1;
    Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(String.class)))
        .thenReturn(buildResponse(teamId));
    CalcomEventTypeDTO defaultEventTypeOfTeam = calComEventTypeService
        .getDefaultEventTypeOfTeam(Long.valueOf(teamId));

    MatcherAssert.assertThat(defaultEventTypeOfTeam.getId(), Matchers.is(2));
  }

  private String buildResponse(Integer teamId) {
    CalcomEventTypeDTO dto1 = new CalcomEventTypeDTO();
    dto1.setId(1);
    dto1.setTeamId(teamId);
    dto1.setMetadata("{}");

    CalcomEventTypeDTO dto2 = new CalcomEventTypeDTO();
    dto2.setId(2);
    dto2.setTeamId(teamId);
    dto2.setMetadata("{defaultEventType: 'true'}");

    CalcomEventTypeDTO dto3 = new CalcomEventTypeDTO();
    dto3.setId(3);
    dto3.setTeamId(teamId);
    dto3.setMetadata(null);

    try {
      return "{event_types: " + new ObjectMapper()
          .writeValueAsString(Arrays.asList(new CalcomEventTypeDTO[]{dto1, dto2, dto3})) + "}";
    } catch (Exception e) {
      return "";
    }
  }


}