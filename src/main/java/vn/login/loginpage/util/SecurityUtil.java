package vn.login.loginpage.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.util.Base64;

import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.response.ResLoginDTO;

@Component
public class SecurityUtil {

    private final JwtEncoder jwtEncoder;

    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    @Value("${jwt.base64-secret}")
    private String jwtKey;

    @Value("${jwt.access-token-validity-in-seconds}")
    private Long jwtExpire;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private Long jwtRefreshExpire;

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }

    public Mono<Jwt> checkValidRefreshToken(String token) {
        return Mono.fromCallable(() -> {
            NimbusJwtDecoder jwtDecode = NimbusJwtDecoder
                    .withSecretKey(getSecretKey())
                    .macAlgorithm(JWT_ALGORITHM)
                    .build();
            return jwtDecode.decode(token);
        }).onErrorMap(e -> new RuntimeException("Invalid refresh token", e));
    }

    public String createAccessToken(ResLoginDTO dto) {
        ResLoginDTO.UserInsideToken userToken = new ResLoginDTO.UserInsideToken();
        userToken.setId(dto.getUserLogin().getId());
        userToken.setEmail(dto.getUserLogin().getEmail());
        userToken.setName(dto.getUserLogin().getName());

        Instant now = Instant.now();
        Instant validity = now.plus(this.jwtExpire, ChronoUnit.SECONDS);

        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(dto.getUserLogin().getEmail())
            .claim("user", userToken )
            .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

    }

    public String createRefreshToken( ResLoginDTO dto) {
        ResLoginDTO.UserInsideToken userToken = new ResLoginDTO.UserInsideToken();
        userToken.setId(dto.getUserLogin().getId());
        userToken.setEmail(dto.getUserLogin().getEmail());
        userToken.setName(dto.getUserLogin().getName());

        Instant now = Instant.now();
        Instant validity = now.plus(this.jwtRefreshExpire, ChronoUnit.SECONDS);

        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(dto.getUserLogin().getEmail())
            .claim("user", userToken)
            .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

    }


            /**
     * Get the login of the current user.
     *
     * @return the login of the current user.
     */
public static Mono<String> getCurrentUserLoginReactive() {
    return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(SecurityUtil::extractPrincipal)
            .switchIfEmpty(Mono.error(new RuntimeException("User not authenticated")));
}

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }
}
