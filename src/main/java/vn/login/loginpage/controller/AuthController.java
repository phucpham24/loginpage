package vn.login.loginpage.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;

import reactor.core.publisher.Mono;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.request.ReqLoginDTO;
import vn.login.loginpage.domain.response.ResLoginDTO;
import vn.login.loginpage.domain.response.ResResponse;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.SecurityUtil;
import vn.login.loginpage.util.response.ResponseWrapper;

@RestController
public class AuthController {

    private final ReactiveAuthenticationManager authenticationManager;
    private final SecurityUtil securityUtil;
    private final UserService userService;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(ReactiveAuthenticationManager authenticationManager,
            SecurityUtil securityUtil,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ResResponse<ResLoginDTO>>> login(
            @RequestBody ReqLoginDTO loginDTO,
            ServerHttpResponse response) {

        Authentication auth = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        Mono<ResLoginDTO> loginMono = authenticationManager.authenticate(auth)
                .flatMap(authentication -> {

                    return userService.findUserByEmail(loginDTO.getUsername())
                            .flatMap(user -> {
                                ResLoginDTO res = new ResLoginDTO();
                                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                        user.getId(),
                                        user.getEmail(),
                                        user.getName());
                                res.setUserLogin(userLogin);

                                // Create access token
                                String token = securityUtil.createAccessToken(res);
                                res.setAccessToken(token);

                                // Create refresh token
                                String refreshToken = securityUtil.createRefreshToken(res);

                                // Update user's refresh token in DB
                                // this.userService.updateUserToken(refreshToken, user.getEmail());

                                // Set cookie in response
                                ResponseCookie responseCookie = ResponseCookie
                                        .from("refresh_token", refreshToken)
                                        .httpOnly(true)
                                        .secure(true)
                                        .path("/")
                                        .maxAge(refreshTokenExpiration)
                                        .sameSite("Strict")
                                        .build();

                                response.addCookie(responseCookie);

                                return this.userService.updateUserToken(refreshToken, user.getEmail())
                                        .then(Mono.just(res));
                            })
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                });

        return ResponseWrapper.wrapMono(loginMono, "Login successful", HttpStatus.OK);
    }

    @GetMapping("/refresh")
    public Mono<ResponseEntity<ResResponse<ResLoginDTO>>> refreshToken(
            @RequestBody ReqLoginDTO loginDTO,
            ServerHttpResponse response) {

        Authentication auth = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        Mono<ResLoginDTO> loginMono = authenticationManager.authenticate(auth)
                .flatMap(authentication -> {

                    return userService.findUserByEmail(loginDTO.getUsername())
                            .flatMap(user -> {
                                ResLoginDTO res = new ResLoginDTO();
                                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                        user.getId(),
                                        user.getEmail(),
                                        user.getName());
                                res.setUserLogin(userLogin);

                                // Create new access token
                                String accessToken = securityUtil.createAccessToken(res);
                                res.setAccessToken(accessToken);

                                // Create new refresh token
                                String refreshToken = securityUtil.createRefreshToken(res);

                                // Update stored refresh token
                                // userService.updateUserToken(refreshToken, user.getEmail());

                                // Set refresh token cookie
                                ResponseCookie responseCookie = ResponseCookie
                                        .from("refresh_token", refreshToken)
                                        .httpOnly(true)
                                        .secure(true)
                                        .path("/")
                                        .maxAge(refreshTokenExpiration)
                                        .sameSite("Strict")
                                        .build();

                                response.addCookie(responseCookie);

                                return this.userService.updateUserToken(refreshToken, user.getEmail())
                                        .then(Mono.just(res));
                            });
                });

        return ResponseWrapper.wrapMono(loginMono, "Token refreshed successfully", HttpStatus.OK);
    }

    @GetMapping("/account")
    public Mono<ResponseEntity<ResResponse<ResLoginDTO.UserGetAccount>>> getAccount() {
        return SecurityUtil.getCurrentUserLoginReactive()
                .flatMap(email -> userService.findUserByEmail(email)
                        .map(user -> {
                            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
                            userLogin.setId(user.getId());
                            userLogin.setEmail(user.getEmail());
                            userLogin.setName(user.getName());

                            ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
                            userGetAccount.setUser(userLogin);
                            return userGetAccount;
                        }))
                .flatMap(userGetAccount -> ResponseWrapper.wrapMono(Mono.just(userGetAccount),
                        "Account info retrieved successfully", HttpStatus.OK));
    }

}
