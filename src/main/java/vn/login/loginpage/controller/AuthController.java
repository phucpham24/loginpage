package vn.login.loginpage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;

import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.*;

import vn.login.loginpage.domain.request.ReqLoginDTO;
import vn.login.loginpage.domain.response.ResLoginDTO;
import vn.login.loginpage.domain.response.ResResponse;
import vn.login.loginpage.service.AuthService;
import vn.login.loginpage.util.SecurityUtil;
import vn.login.loginpage.util.response.ResponseWrapper;

@RestController
@RequestMapping("/auth")
public class AuthController {

        private final SecurityUtil securityUtil;

        private final AuthService authService;

        public AuthController(AuthService authService, SecurityUtil securityUtil) {
                this.authService = authService;
                this.securityUtil = securityUtil;

        }

        @PostMapping("/login")
        public Mono<ResponseEntity<ResResponse<ResLoginDTO>>> login(@RequestBody ReqLoginDTO loginDTO,
                        ServerHttpResponse response) {
                return ResponseWrapper.wrapMono(
                                authService.authenticateAndLogin(loginDTO, response),
                                "Login successful", HttpStatus.OK);
        }

        @GetMapping("/refresh")
        public Mono<ResponseEntity<ResResponse<ResLoginDTO>>> refresh(
                        ServerHttpResponse response,
                        @CookieValue(name = "refresh_token", defaultValue = "abc") String refreshToken) {

                return authService.refresh(refreshToken, response)
                                .flatMap(dto -> ResponseWrapper.wrapMono(
                                                Mono.just(dto),
                                                "Token refreshed successfully",
                                                HttpStatus.OK))
                                .onErrorResume(ex -> {
                                        return ResponseWrapper.wrapMono(
                                                        Mono.empty(),
                                                        ex.getMessage() != null ? ex.getMessage()
                                                                        : "Invalid refresh token",
                                                        HttpStatus.UNAUTHORIZED);
                                });
        }

        @GetMapping("/account")
        public Mono<ResponseEntity<ResResponse<ResLoginDTO.UserGetAccount>>> getAccount() {
                return this.securityUtil.getCurrentUserLoginReactive()
                                .flatMap(email -> authService.getAccountInfo(email))
                                .flatMap(account -> ResponseWrapper.wrapMono(
                                                Mono.just(account),
                                                "Account info retrieved successfully", HttpStatus.OK));
        }

        @PostMapping("/logout")
        public Mono<ResponseEntity<ResResponse<Void>>> logoutUser(ServerHttpResponse response) {
                return this.authService.logout(response)
                                .then(ResponseWrapper.wrapMono(
                                                Mono.justOrEmpty((Void) null), // ✅ wrap null safely
                                                "Logout successful",
                                                HttpStatus.OK));
        }
}
