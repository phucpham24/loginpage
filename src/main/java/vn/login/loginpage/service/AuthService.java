package vn.login.loginpage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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
import vn.login.loginpage.util.error.InvalidException;
import vn.login.loginpage.domain.Role;
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
                Authentication auth = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(),
                                loginDTO.getPassword());

                return authenticationManager.authenticate(auth)
                                .flatMap(authentication -> userService.findUserAndRoleByEmail(loginDTO.getUsername())
                                                .flatMap(tuple -> {
                                                        User user = tuple.getT1();
                                                        Role role = tuple.getT2();

                                                        ResLoginDTO res = buildLoginDTO(user, role);
                                                        String accessToken = securityUtil.createAccessToken(res);
                                                        res.setAccessToken(accessToken);
                                                        String refreshToken = securityUtil.createRefreshToken(res);

                                                        ResponseCookie refreshCookie = ResponseCookie
                                                                        .from("refresh_token", refreshToken)
                                                                        .httpOnly(true)
                                                                        .secure(true)
                                                                        .path("/")
                                                                        .maxAge(refreshTokenExpiration)
                                                                        .sameSite("Strict")
                                                                        .build();
                                                        response.addCookie(refreshCookie);

                                                        return userService
                                                                        .updateUserToken(refreshToken, user.getEmail())
                                                                        .thenReturn(res);
                                                })
                                                .contextWrite(ReactiveSecurityContextHolder
                                                                .withAuthentication(authentication)));
        }

        private ResLoginDTO buildLoginDTO(User user, Role role) {
                ResLoginDTO dto = new ResLoginDTO();
                dto.setUserLogin(new ResLoginDTO.UserLogin(
                                user.getId(),
                                user.getEmail(),
                                user.getName(),
                                role));
                return dto;
        }

        public Mono<ResLoginDTO.UserGetAccount> getAccountInfo(String email) {
                return userService.findUserAndRoleByEmail(email)
                                .map(tuple -> {
                                        User user = tuple.getT1();
                                        Role role = tuple.getT2();

                                        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                                        user.getId(),
                                                        user.getEmail(),
                                                        user.getName(),
                                                        role);
                                        ResLoginDTO.UserGetAccount account = new ResLoginDTO.UserGetAccount();
                                        account.setUser(userLogin);
                                        return account;
                                });
        }

        public Mono<ResLoginDTO> refresh(String refreshToken, ServerHttpResponse response) {
                if ("abc".equals(refreshToken)) {
                        return Mono.error(new InvalidException("No refresh token in cookies"));
                }

                return securityUtil.checkValidRefreshToken(refreshToken)
                                .map(jwt -> jwt.getSubject())
                                .flatMap(email -> userService.getUserByRefreshTokenAndEmail(email, refreshToken)
                                                .flatMap(user -> userService.findUserAndRoleByEmail(email)
                                                                .flatMap(tuple -> {
                                                                        Role role = tuple.getT2();

                                                                        ResLoginDTO dto = buildLoginDTO(user, role);
                                                                        String newAccessToken = securityUtil
                                                                                        .createAccessToken(dto);
                                                                        dto.setAccessToken(newAccessToken);
                                                                        String newRefreshToken = securityUtil
                                                                                        .createRefreshToken(dto);

                                                                        ResponseCookie refreshCookie = ResponseCookie
                                                                                        .from("refresh_token",
                                                                                                        newRefreshToken)
                                                                                        .httpOnly(true)
                                                                                        .secure(true)
                                                                                        .path("/")
                                                                                        .maxAge(refreshTokenExpiration)
                                                                                        .sameSite("Strict")
                                                                                        .build();
                                                                        response.addCookie(refreshCookie);

                                                                        return userService
                                                                                        .updateUserToken(
                                                                                                        newRefreshToken,
                                                                                                        user.getEmail())
                                                                                        .thenReturn(dto);
                                                                })));
        }

        public Mono<Void> logout(ServerHttpResponse response) {
                return SecurityUtil.getCurrentUserLoginReactive()
                                .switchIfEmpty(Mono.error(new InvalidException("Access token is invalid or missing")))
                                .flatMap(email -> this.userService.updateUserToken(null, email)
                                                .then(Mono.fromRunnable(() -> {
                                                        ResponseCookie deletedCookie = ResponseCookie
                                                                        .from("refresh_token",
                                                                                        null)
                                                                        .httpOnly(true)
                                                                        .secure(true)
                                                                        .path("/")
                                                                        .maxAge(0)
                                                                        .sameSite("Strict")
                                                                        .build();
                                                        response.addCookie(deletedCookie);
                                                })));
        }

}
