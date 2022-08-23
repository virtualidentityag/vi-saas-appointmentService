package com.vi.appointmentservice.helper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class HmacHelper {

  public String generateHmac(String algorithm, String data, String key)
      throws NoSuchAlgorithmException, InvalidKeyException {
    SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
    Mac mac = Mac.getInstance(algorithm);
    mac.init(secretKeySpec);
    return Hex.encodeHexString(mac.doFinal(data.getBytes()));
  }

}
