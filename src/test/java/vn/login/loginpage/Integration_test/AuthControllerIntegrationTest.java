package vn.login.loginpage.Integration_test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.Role;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.request.ReqLoginDTO;
import vn.login.loginpage.domain.response.ResLoginDTO;
import vn.login.loginpage.domain.response.ResResponse;
import vn.login.loginpage.service.AuthService;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.SecurityUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidity;

    private final String username = "test@gmail.com";
    private final String password = "123456";

    private String accessToken;
    private String refreshToken;

    @BeforeAll
    void setupUser() {
        userService.deleteUserByEmail(username).block();

        User user = new User();
        user.setEmail(username);
        user.setName("Test User");
        user.setPassword(password);
        user.setRoleId(1L);
        userService.createUser(user).block();
    }

    @Test
    void login_ShouldSucceedAndReturnTokens() {
        ReqLoginDTO loginDTO = new ReqLoginDTO(username, password);

        // Étape 1 : appeler et récupérer la réponse
        EntityExchangeResult<ResResponse<ResLoginDTO>> result = webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginDTO)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("refresh_token")
                .expectBody(new ParameterizedTypeReference<ResResponse<ResLoginDTO>>() {
                })
                .returnResult(); // ✅ récupère le body et les cookies

        ResResponse<ResLoginDTO> res = result.getResponseBody();

        // Étape 2 : assertions sur le body
        assertNotNull(res);
        assertEquals("Login successful", res.getMessage());
        assertNotNull(res.getData());
        accessToken = res.getData().getAccessToken();
        assertNotNull(accessToken);

        // Étape 3 : récupération du cookie
        refreshToken = result.getResponseCookies()
                .getFirst("refresh_token")
                .getValue();

        assertNotNull(refreshToken);
    }

    @Test
    void refreshToken_ShouldFail_WhenInvalid() {
        webTestClient.get()
                .uri("/auth/refresh")
                .cookie("refresh_token", "invalid_token")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ResResponse.class)
                .value(res -> assertEquals("Invalid refresh token", res.getMessage()));
    }

    @Test
    void getAccount_ShouldReturnUserInfo_WhenAuthenticated() {
        login_ShouldSucceedAndReturnTokens();

        webTestClient.get()
                .uri("/auth/account")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResResponse<ResLoginDTO.UserGetAccount>>() {
                })
                .value(res -> {
                    assertEquals("Account info retrieved successfully", res.getMessage());
                    assertEquals(username, res.getData().getUser().getEmail());
                });
    }

    @Test
    void logout_ShouldInvalidateTokenAndClearCookie() {
        login_ShouldSucceedAndReturnTokens();

        webTestClient.post()
                .uri("/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().maxAge("refresh_token", Duration.ZERO)
                .expectBody(new ParameterizedTypeReference<ResResponse<Void>>() {
                })
                .value(res -> assertEquals("Logout successful", res.getMessage()));

        // 🧪 Validate that the refresh token has been cleared in the DB
        User user = userService.findUserByEmail(username).block();
        assertNotNull(user);
        assertNull(user.getRefreshToken());
    }
}
