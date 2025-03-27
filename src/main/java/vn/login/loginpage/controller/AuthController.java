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
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.request.ReqLoginDTO;
import vn.login.loginpage.domain.response.ResLoginDTO;
import vn.login.loginpage.domain.response.ResResponse;
import vn.login.loginpage.service.AuthService;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.SecurityUtil;
import vn.login.loginpage.util.response.ResponseWrapper;

@RestController
public class AuthController {

        private final AuthService authService;
        private final SecurityUtil securityUtil;

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
        public Mono<ResponseEntity<ResResponse<ResLoginDTO>>> refresh(@RequestBody ReqLoginDTO loginDTO,
                        ServerHttpResponse response) {
                return ResponseWrapper.wrapMono(
                                authService.refreshToken(loginDTO, response),
                                "Token refreshed successfully", HttpStatus.OK);
        }

        @GetMapping("/account")
        public Mono<ResponseEntity<ResResponse<ResLoginDTO.UserGetAccount>>> getAccount() {
                return SecurityUtil.getCurrentUserLoginReactive()
                                .flatMap(email -> authService.getAccountInfo(email))
                                .flatMap(account -> ResponseWrapper.wrapMono(
                                                Mono.just(account),
                                                "Account info retrieved successfully", HttpStatus.OK));
        }
}
