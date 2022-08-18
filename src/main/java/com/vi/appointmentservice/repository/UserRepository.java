package com.vi.appointmentservice.repository;

import com.vi.appointmentservice.api.model.CalcomUser;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepository {

  private final @NotNull JdbcTemplate jdbcTemplate;

  public void updateUser(CalcomUser user) {
    Long userId = user.getId();
    String name = user.getName();
    Boolean isAway = user.getAway();
    String email = user.getEmail();
    String UPDATE_USER_QUERY = "update \"users\" set \"name\" = $nameParam, \"away\" = $awayParam, \"email\" = $emailParam where \"id\" = " + userId;
    UPDATE_USER_QUERY = UPDATE_USER_QUERY.replace("$nameParam", "'" + name + "'")
        .replace("$awayParam", isAway.toString())
        .replace("$emailParam", "'" + email + "'");
    jdbcTemplate.update(UPDATE_USER_QUERY);
  }

  public void initUserAddExtraData(Long calcomUserId, String userPassword){
    String UPDATE_USER_QUERY = "update \"users\" set \"username\" = $usernameParam, \"password\" = $password, \"plan\" = 'PRO', \"completedOnboarding\" = true, \"verified\" = true where \"id\" = " + calcomUserId;
    UPDATE_USER_QUERY = UPDATE_USER_QUERY.replace("$usernameParam", "'" + UUID.randomUUID() + "'");
    UPDATE_USER_QUERY = UPDATE_USER_QUERY.replace("password", "'" + userPassword + "'");
    jdbcTemplate.update(UPDATE_USER_QUERY);
  }

  public void setDefaultScheduleId(Long calcomUserId, Long defaultScheduleId){
    String UPDATE_USER_QUERY = "update \"users\" set \"defaultScheduleId\" = $scheduleIdParam where \"id\" = " + calcomUserId;
    UPDATE_USER_QUERY = UPDATE_USER_QUERY.replace("$scheduleIdParam", defaultScheduleId.toString());
    jdbcTemplate.update(UPDATE_USER_QUERY);
  }

}
