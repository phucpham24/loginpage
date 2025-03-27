package vn.login.loginpage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.request.ReqLoginDTO;
import vn.login.loginpage.domain.response.ResLoginDTO;
import vn.login.loginpage.util.SecurityUtil;
import vn.login.loginpage.domain.User;

@Service
public class AuthService {

    private final ReactiveAuthenticationManager authenticationManager;
    private final SecurityUtil securityUtil;
    private final UserService userService;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthService(ReactiveAuthenticationManager authenticationManager,
            SecurityUtil securityUtil,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    public Mono<ResLoginDTO> authenticateAndLogin(ReqLoginDTO loginDTO, ServerHttpResponse response) {
        Authentication auth = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

        return authenticationManager.authenticate(auth)
                .flatMap(authentication -> userService.findUserByEmail(loginDTO.getUsername())
                        .flatMap(user -> {
                            ResLoginDTO res = buildLoginDTO(user);
                            String accessToken = securityUtil.createAccessToken(res);
                            res.setAccessToken(accessToken);
                            String refreshToken = securityUtil.createRefreshToken(res);

                            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                                    .httpOnly(true)
                                    .secure(true)
                                    .path("/")
                                    .maxAge(refreshTokenExpiration)
                                    .sameSite("Strict")
                                    .build();
                            response.addCookie(refreshCookie);

                            return userService.updateUserToken(refreshToken, user.getEmail())
                                    .thenReturn(res);
                        })
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));
    }

    public Mono<ResLoginDTO> refreshToken(ReqLoginDTO loginDTO, ServerHttpResponse response) {
        Authentication auth = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

        return authenticationManager.authenticate(auth)
                .flatMap(authentication -> userService.findUserByEmail(loginDTO.getUsername())
                        .flatMap(user -> {
                            ResLoginDTO res = buildLoginDTO(user);
                            String accessToken = securityUtil.createAccessToken(res);
                            res.setAccessToken(accessToken);
                            String refreshToken = securityUtil.createRefreshToken(res);

                            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                                    .httpOnly(true)
                                    .secure(true)
                                    .path("/")
                                    .maxAge(refreshTokenExpiration)
                                    .sameSite("Strict")
                                    .build();
                            response.addCookie(refreshCookie);

                            return userService.updateUserToken(refreshToken, user.getEmail())
                                    .thenReturn(res);
                        }));
    }

    private ResLoginDTO buildLoginDTO(User user) {
        ResLoginDTO dto = new ResLoginDTO();
        dto.setUserLogin(new ResLoginDTO.UserLogin(user.getId(), user.getEmail(), user.getName()));
        return dto;
    }

    public Mono<ResLoginDTO.UserGetAccount> getAccountInfo(String email) {
        return userService.findUserByEmail(email)
                .map(user -> {
                    ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(user.getId(), user.getEmail(),
                            user.getName());
                    ResLoginDTO.UserGetAccount account = new ResLoginDTO.UserGetAccount();
                    account.setUser(userLogin);
                    return account;
                });
    }
}
