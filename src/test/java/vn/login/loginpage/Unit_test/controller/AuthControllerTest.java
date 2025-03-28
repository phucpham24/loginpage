package vn.login.loginpage.Unit_test.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;

import reactor.core.publisher.Mono;
import vn.login.loginpage.controller.AuthController;
import vn.login.loginpage.domain.Role;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.request.ReqLoginDTO;
import vn.login.loginpage.domain.response.ResLoginDTO;
import vn.login.loginpage.domain.response.ResResponse;
import vn.login.loginpage.service.AuthService;
import vn.login.loginpage.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

        @Mock
        private AuthService authService;

        @Mock
        private SecurityUtil securityUtil;

        @Mock
        private ServerHttpResponse response;

        @InjectMocks
        private AuthController authController;

        private ReqLoginDTO loginDTO;
        private ResLoginDTO resLoginDTO;
        private ResLoginDTO.UserLogin userLogin;
        private Role role;
        private User user;

        @BeforeEach
        void setup() {
                loginDTO = new ReqLoginDTO();
                loginDTO.setUsername("test@example.com");
                loginDTO.setPassword("password");

                role = new Role();
                role.setName("ROLE_USER");

                user = new User();
                user.setId(1L);
                user.setEmail("test@example.com");
                user.setName("John");

                userLogin = new ResLoginDTO.UserLogin(user.getId(), user.getEmail(), user.getName(), role);
                resLoginDTO = new ResLoginDTO();
                resLoginDTO.setUserLogin(userLogin);
                resLoginDTO.setAccessToken("access_token");
        }

        @Test
        void login_shouldReturnSuccessResponse() {
                when(authService.authenticateAndLogin(eq(loginDTO), any())).thenReturn(Mono.just(resLoginDTO));

                var result = authController.login(loginDTO, response).block();

                assertNotNull(result);
                assertEquals(HttpStatus.OK, result.getStatusCode());
                assertEquals("Login successful", result.getBody().getMessage());
                assertEquals("access_token", result.getBody().getData().getAccessToken());
        }

        @Test
        void refresh_shouldReturnRefreshedToken() {
                when(authService.refresh(eq("refresh_token_value"), any()))
                                .thenReturn(Mono.just(resLoginDTO));

                var result = authController.refresh(response, "refresh_token_value").block();

                assertNotNull(result);
                assertEquals(HttpStatus.OK, result.getStatusCode());
                assertEquals("Token refreshed successfully", result.getBody().getMessage());
                assertEquals("access_token", result.getBody().getData().getAccessToken());
        }

        @Test
        void refresh_shouldReturnUnauthorizedOnError() {
                when(authService.refresh(eq("bad_token"), any()))
                                .thenReturn(Mono.error(new RuntimeException("Invalid refresh token")));

                var result = authController.refresh(response, "bad_token").block();

                assertNotNull(result);
                assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
                assertEquals("Invalid refresh token", result.getBody().getMessage());
        }

        @Test
        void getAccount_shouldReturnAccountInfo() {
                ResLoginDTO.UserGetAccount account = new ResLoginDTO.UserGetAccount();
                account.setUser(userLogin);

                when(authService.getAccountInfo("test@example.com")).thenReturn(Mono.just(account));

                try (MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class)) {
                        mockedStatic.when(SecurityUtil::getCurrentUserLoginReactive)
                                        .thenReturn(Mono.just("test@example.com"));

                        var result = authController.getAccount().block();

                        assertNotNull(result);
                        assertEquals(HttpStatus.OK, result.getStatusCode());
                        assertEquals("Account info retrieved successfully", result.getBody().getMessage());
                        assertEquals("test@example.com", result.getBody().getData().getUser().getEmail());
                }
        }

        @Test
        void logout_shouldReturnOkResponse() {
                try (MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class)) {
                        mockedStatic.when(SecurityUtil::getCurrentUserLoginReactive)
                                        .thenReturn(Mono.just("test@example.com"));

                        when(authService.logout(any())).thenReturn(Mono.empty());

                        var result = authController.logoutUser(response).block();

                        assertNotNull(result);
                        assertEquals(HttpStatus.OK, result.getStatusCode());
                        assertEquals("Logout successful", result.getBody().getMessage());
                }
        }

}
