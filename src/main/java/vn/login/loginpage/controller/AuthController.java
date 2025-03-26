package vn.login.loginpage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public AuthController(ReactiveAuthenticationManager authenticationManager,
            SecurityUtil securityUtil,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ResResponse<ResLoginDTO>>> login(@RequestBody ReqLoginDTO loginDTO) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        Mono<ResLoginDTO> loginMono = authenticationManager.authenticate(auth)
                .flatMap(authentication -> {
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    return userService.findUserByEmail(loginDTO.getUsername())
                            .map(user -> {
                                ResLoginDTO res = new ResLoginDTO();
                                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                        user.getId(),
                                        user.getEmail(),
                                        user.getName());
                                res.setUserLogin(userLogin);

                                String token = securityUtil.createAccessToken(authentication.getName(), res);
                                res.setAccessToken(token);

                                return res;
                            });
                });

        return ResponseWrapper.wrapMono(loginMono, "Login successful", HttpStatus.OK);
    }

}
