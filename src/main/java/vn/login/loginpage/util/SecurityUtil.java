package vn.login.loginpage.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtil {
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;
    // @Value("${jwt.access-token-validity-in-seconds}")
    // private long accessTokenExpiration;

    // private final JwtEncoder jwtEncoder;
    // @Value("${jwt.base64-secret}")
    // private String jwtKey;

    // public SecurityUtil(JwtEncoder jwtEncoder) {
    // this.jwtEncoder = jwtEncoder;
    // }
}
