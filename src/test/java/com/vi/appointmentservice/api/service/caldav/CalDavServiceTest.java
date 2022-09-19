package com.vi.appointmentservice.api.service.caldav;

import com.vi.appointmentservice.api.model.CalDavCredentials;
import com.vi.appointmentservice.api.service.calcom.caldav.CalDavService;
import com.vi.appointmentservice.repository.CalDavRepository;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CalDavServiceTest {

  @InjectMocks
  private CalDavService calDavService;

  @Mock
  private CalDavRepository calDavRepository;

  @Captor
  ArgumentCaptor<String> token;

  @Test
  public void resetPassword() {
    CalDavCredentials credentials = new CalDavCredentials();
    credentials.setEmail("demo@demo.de");
    credentials.setPassword("demo");
    calDavService.resetPassword(credentials);
    Mockito.verify(calDavRepository).resetCredentials(Mockito.anyString(), token.capture());
    String tokenCaptorValue = token.getValue();
    MatcherAssert.assertThat(tokenCaptorValue, Matchers.is("f4cc5ecca85d5145bbcfee34c71b714d"));
  }

}
