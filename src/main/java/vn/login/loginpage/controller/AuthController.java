package vn.login.loginpage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.dto.LoginDTO;
import vn.login.loginpage.domain.dto.RestLoginDTO;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.SecurityUtil;
import vn.login.loginpage.util.error.InvalidException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private UserService userService;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
            UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<RestLoginDTO> login(@RequestBody LoginDTO loginDTO) {
        RestLoginDTO res = new RestLoginDTO();
        // input username/password into Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        // valid user by loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // authentication does not have user password
        String accessToken = this.securityUtil.createAccessToken(authentication);
        res.setAccessToken(accessToken);

        // impose (if success) in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // update user
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

}
