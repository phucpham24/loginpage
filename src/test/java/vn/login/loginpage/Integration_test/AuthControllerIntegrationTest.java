package vn.login.loginpage.Integration_test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.request.ReqLoginDTO;
import vn.login.loginpage.domain.response.ResLoginDTO;
import vn.login.loginpage.domain.response.ResResponse;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.SecurityUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityUtil securityUtil;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidity;

    private final String username = "test@example.com";
    private final String password = "password";

    private String accessToken;
    private String refreshToken;

    @BeforeAll
    void setupUser() {

        User user = new User();
        // user.setId(1L);
        user.setEmail(username);
        user.setName("Test User");
        user.setPassword(password);
        userService.deleteUserByEmail(user.getEmail()).block();
        userService.createUser(user).block();
    }

    @Test
    void login_ShouldSucceedAndReturnTokens() {
        ReqLoginDTO loginDTO = new ReqLoginDTO(username, password);

        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginDTO)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("refresh_token")
                .expectBody(new ParameterizedTypeReference<ResResponse<ResLoginDTO>>() {
                })
                .value(res -> {
                    assertEquals("Login successful", res.getMessage());
                    assertNotNull(res.getData());
                    accessToken = res.getData().getAccessToken();
                    assertNotNull(accessToken);
                });

        // Extract refresh token from cookie
        WebTestClient.ResponseSpec response = webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginDTO)
                .exchange();

        refreshToken = response.returnResult(String.class)
                .getResponseCookies()
                .getFirst("refresh_token")
                .getValue();

        assertNotNull(refreshToken);
    }

    @Test
    void refreshToken_ShouldSucceed_WhenValid() {
        // Must first login to get a valid refresh token
        login_ShouldSucceedAndReturnTokens();

        webTestClient.get()
                .uri("/auth/refresh")
                .cookie("refresh_token", refreshToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResResponse<ResLoginDTO>>() {
                })
                .value(res -> {
                    assertEquals("Token refreshed successfully", res.getMessage());
                    assertNotNull(res.getData().getAccessToken());
                });
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
        if (accessToken != null) {
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
            assertEquals(null, user.getRefreshToken(), "Refresh token should be null after logout");
        }
    }
}
