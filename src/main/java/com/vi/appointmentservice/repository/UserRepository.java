package com.vi.appointmentservice.repository;

import com.vi.appointmentservice.api.model.CalcomUser;
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
    String UPDATE_USER_QUERY = "update \"users\" set \"name\" = $nameParam, \"away\" = $awayParam where \"id\" = " + userId;
    UPDATE_USER_QUERY = UPDATE_USER_QUERY.replace("$nameParam", "'" + name + "'")
        .replace("$awayParam", isAway.toString());
    jdbcTemplate.update(UPDATE_USER_QUERY);
  }

}
