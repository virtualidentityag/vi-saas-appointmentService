package com.vi.appointmentservice.config;

import com.vi.appointmentservice.port.out.IdentityClientConfig;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "identity")
public class IdentityConfig implements IdentityClientConfig {

  private static final char PATH_SEPARATOR = '/';

  @URL
  private String openidConnectUrl;


  public String getOpenIdConnectUrl(String path) {
    return getOpenIdConnectUrl(path, "");
  }

  public String getOpenIdConnectUrl(String path, String arg) {
    var builder = UriComponentsBuilder.fromUriString(openidConnectUrl);
    addPath(path, arg, builder);

    return builder.toUriString();
  }

  private void addPath(String path, String arg, UriComponentsBuilder builder) {
    Arrays.stream(StringUtils
        .trimLeadingCharacter(path, PATH_SEPARATOR)
        .replaceAll("(\\{).*(})", arg)
        .split(Character.toString(PATH_SEPARATOR))).forEach(builder::pathSegment);
  }


}
