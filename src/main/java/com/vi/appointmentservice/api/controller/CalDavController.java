package com.vi.appointmentservice.api.controller;

import com.vi.appointmentservice.api.model.CalDavCredentials;
import com.vi.appointmentservice.api.model.HasCalDavAccountDTO;
import com.vi.appointmentservice.api.service.calcom.caldav.CalDavService;
import com.vi.appointmentservice.generated.api.controller.CaldavApi;
import com.vi.appointmentservice.helper.AuthenticatedUser;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "calDav")
@RequiredArgsConstructor
public class CalDavController implements CaldavApi {

  private final @NonNull AuthenticatedUser authenticatedUser;

  private final @NotNull CalDavService calDavService;

  @Override
  public ResponseEntity<Void> resetCalDavPassword(
      @Valid CalDavCredentials calDavCredentials) {
    calDavService.resetPassword(calDavCredentials);
    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<HasCalDavAccountDTO> hasCalDavAccount() {
    return new ResponseEntity<>(calDavService.hasCalDavAccount(authenticatedUser.getEmail()), HttpStatus.OK);
  }
}
