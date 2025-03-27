package vn.login.loginpage.Unit_test.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import vn.login.loginpage.controller.AuthController;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.request.ReqLoginDTO;
import vn.login.loginpage.domain.response.ResLoginDTO;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

        @Mock
        private ReactiveAuthenticationManager authenticationManager;

        @Mock
        private SecurityUtil securityUtil;

        @Mock
        private UserService userService;

        @InjectMocks
        private AuthController authController;

        private ReqLoginDTO loginDTO;
        private User mockUser;
        private Authentication mockAuthentication;

        @BeforeEach
        void setUp() {
                loginDTO = new ReqLoginDTO();
                loginDTO.setUsername("phucsaiyan@example.com");
                loginDTO.setPassword("password");

                mockUser = new User();
                mockUser.setId(1L);
                mockUser.setName("phucsaiyan");
                mockUser.setEmail("phucsaiyan@example.com");

                mockAuthentication = mock(Authentication.class);
                when(mockAuthentication.getName()).thenReturn("phucsaiyan@example.com");
        }

        @Test
        void testLoginSuccess() {
                ServerHttpResponse mockResponse = mock(ServerHttpResponse.class);

                when(authenticationManager.authenticate(any(Authentication.class)))
                                .thenReturn(Mono.just(mockAuthentication));

                when(userService.findUserByEmail("phucsaiyan@example.com"))
                                .thenReturn(Mono.just(mockUser));

                when(securityUtil.createAccessToken(any(ResLoginDTO.class)))
                                .thenReturn("mock-jwt-token");

                when(securityUtil.createRefreshToken(any(ResLoginDTO.class)))
                                .thenReturn("mock-refresh-token");

                doNothing().when(userService).updateUserToken("mock-refresh-token", "phucsaiyan@example.com");

                doNothing().when(mockResponse).addCookie(any());

                StepVerifier.create(authController.login(loginDTO, mockResponse))
                                .assertNext(response -> {
                                        assertEquals(HttpStatus.OK, response.getStatusCode());
                                        assertEquals("Login successful", response.getBody().getMessage());

                                        ResLoginDTO dto = response.getBody().getData();
                                        assertNotNull(dto);
                                        assertEquals("mock-jwt-token", dto.getAccessToken());

                                        ResLoginDTO.UserLogin userLogin = dto.getUserLogin();
                                        assertEquals("phucsaiyan", userLogin.getName());
                                        assertEquals("phucsaiyan@example.com", userLogin.getEmail());
                                        assertEquals(1L, userLogin.getId());
                                })
                                .verifyComplete();
        }

        @Test
        void testLogin_Failure_AuthenticationError() {
                ServerHttpResponse mockResponse = mock(ServerHttpResponse.class);

                when(authenticationManager.authenticate(any(Authentication.class)))
                                .thenReturn(Mono.error(new BadCredentialsException("Invalid credentials")));

                StepVerifier.create(authController.login(loginDTO, mockResponse))
                                .expectErrorMatches(error -> error instanceof BadCredentialsException &&
                                                error.getMessage().equals("Invalid credentials"))
                                .verify();
        }
}
