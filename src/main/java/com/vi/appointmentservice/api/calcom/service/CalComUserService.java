package com.vi.appointmentservice.api.calcom.service;

import com.vi.appointmentservice.api.calcom.model.CalcomUser;
import com.vi.appointmentservice.api.calcom.repository.UserRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CalComUserService {

  @Autowired
  private UserRepository userRepository;

  public CalcomUser createUser(String name, String email) {
    CalcomUser calcomUser = new CalcomUser();
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

  public CalcomUser updateUser(Long userId, String name, String email) {
    return userRepository.updateUser(userId, name, email);
  }

  public CalcomUser updateUsername(Long userId, String name) {
    return userRepository.updateUsername(userId, name);
  }

  public void deleteUser(Long userId) {
    userRepository.deleteUser(userId);
  }


  public CalcomUser getUserById(Long userId) {
    return userRepository.getUserById(userId);
  }
}
