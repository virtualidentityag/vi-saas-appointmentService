package com.vi.appointmentservice.api.service.caldav;

import com.vi.appointmentservice.api.model.CalDavCredentials;
import com.vi.appointmentservice.api.service.calcom.caldav.CalDavService;
import com.vi.appointmentservice.helper.AuthenticatedUser;
import com.vi.appointmentservice.repository.CalDavRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CalDavServiceTest {

  public final String MD5_TOKEN = "f4cc5ecca85d5145bbcfee34c71b714d";

  @Mock
  private AuthenticatedUser authenticatedUser;

  @InjectMocks
  private CalDavService calDavService;

  @Mock
  private CalDavRepository calDavRepository;

  @Captor
  ArgumentCaptor<String> token;

  @Test
  public void resetPassword_Should_Reset_To_New_Password() {
    CalDavCredentials credentials = new CalDavCredentials();
    credentials.setEmail("demo@demo.de");
    credentials.setPassword("demo");
    calDavService.resetPassword(credentials);
    Mockito.verify(calDavRepository).resetCredentials(Mockito.anyString(), token.capture());
    String tokenCaptorValue = token.getValue();
    assertThat(tokenCaptorValue).isEqualTo(MD5_TOKEN);
  }

  @Test
  public void hasCalDavAccount_Should_Return_True_DTO_If_Account_Exists() {
    when(calDavRepository.getAccountExists(any())).thenReturn(true);
    assertThat(calDavService.hasCalDavAccount("testEmail").getHasCalDavAccount()).isTrue();
  }

  @Test
  public void hasCalDavAccount_Should_Return_False_DTO_If_Account_Not_Exists() {
    when(calDavRepository.getAccountExists(any())).thenReturn(false);
    assertThat(calDavService.hasCalDavAccount("testEmail").getHasCalDavAccount()).isFalse();
  }

}
