package com.vi.appointmentservice.api.calcom.service;

import com.vi.appointmentservice.api.calcom.model.CalcomUser;
import com.vi.appointmentservice.api.calcom.repository.UserRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CalComUserService {

  @Autowired
  private UserRepository userRepository;

  public CalcomUser createUser(String name, String email) {
    com.vi.appointmentservice.api.calcom.model.CalcomUser calcomUser = new CalcomUser();
    calcomUser.setName(name);
    calcomUser.setUsername(UUID.randomUUID().toString());
    calcomUser.setEmail(StringUtils.lowerCase(email));
    calcomUser.setTimeZone("Europe/Berlin");
    calcomUser.setWeekStart("Monday");
    calcomUser.setLocale("de");
    calcomUser.setTimeFormat(24);
    calcomUser.setAllowDynamicBooking(true);
    String userPassword = UUID.randomUUID().toString();
    calcomUser.setPassword(new BCryptPasswordEncoder().encode(userPassword));
    calcomUser.setPlainPassword(userPassword);
    return userRepository.creatUser(calcomUser);
  }

  public CalcomUser updateUser(Long userId, String name) {
    CalcomUser userDB = getUserById(userId);
    userDB.setName(name);
    return userRepository.updateUser(userDB);
  }

  public HttpStatus deleteUser(Long userId) {
    userRepository.deleteUser(userId);
    return null;
  }


  public CalcomUser getUserById(Long userId) {
    return userRepository.getUserById(userId);
  }
}
