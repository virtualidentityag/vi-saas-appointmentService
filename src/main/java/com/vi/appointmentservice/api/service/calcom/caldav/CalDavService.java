package com.vi.appointmentservice.api.service.calcom.caldav;

import com.vi.appointmentservice.api.model.CalDavCredentials;
import com.vi.appointmentservice.repository.CalDavRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.validation.constraints.NotNull;
import javax.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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