package com.vi.appointmentservice.api.service.calcom.caldav;

import com.vi.appointmentservice.api.model.CalDavCredentials;
import com.vi.appointmentservice.api.model.HasCalDavAccountDTO;
import com.vi.appointmentservice.repository.CalDavRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.validation.constraints.NotNull;
import javax.xml.bind.DatatypeConverter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalDavService {

  private final @NotNull CalDavRepository calDavRepository;

  public void resetPassword(CalDavCredentials credentials) {
    String token = String.format(
        "%s:BaikalDAV:%s",
        calDavRepository.getUserName(credentials.getEmail()), credentials.getPassword());
    calDavRepository.resetCredentials(credentials.getEmail(), toMD5(token));
  }

  public HasCalDavAccountDTO hasCalDavAccount(String email){
    if(email == null){
      throw new AccessDeniedException("Authenticated User has no email");
    }
    HasCalDavAccountDTO result = new HasCalDavAccountDTO();
    result.setHasCalDavAccount(calDavRepository.getAccountExists(email));
    return result;
  }

  private String toMD5(String token) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(token.getBytes());
      byte[] digest = md.digest();
      return DatatypeConverter
          .printHexBinary(digest).toLowerCase();
    } catch (NoSuchAlgorithmException e) {
      log.error("Issue during encrypting", e);
    }
    return null;
  }


}
