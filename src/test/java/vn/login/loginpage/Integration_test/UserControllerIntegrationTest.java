package vn.login.loginpage.Integration_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.request.ReqLoginDTO;
import vn.login.loginpage.repository.UserRepository;
import vn.login.loginpage.util.constant.GenderEnum;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class UserControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private Long userId;

    @BeforeEach
    void setUp() {
        // ✅ Clean DB and create + persist a test user
        // ✅ Log in to get a valid JWT token for authenticated requests

        userRepository.deleteAll().block();

        User user = new User();
        user.setName("phucsaiyan");
        user.setEmail("phucsaiyan@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setAge(25);
        user.setGender(GenderEnum.FEMALE);
        user.setAddress("Paris");

        userId = userRepository.save(user).map(User -> User.getId()).block();

        ReqLoginDTO loginDTO = new ReqLoginDTO();
        loginDTO.setUsername("phucsaiyan@example.com");
        loginDTO.setPassword("password123");

        webTestClient.post()
                .uri("/login")
                .bodyValue(loginDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.access_token").value(token -> jwtToken = (String) token)
                .jsonPath("$.data.user.name").isNotEmpty()
                .jsonPath("$.data.user.name").isEqualTo("phucsaiyan");
    }

    // ✅ Test retrieving the list of users with a valid Bearer token
    // Ensures authentication is enforced and data is returned successfully
    @Test
    void testListUsers_withAuth() {
        webTestClient.get()
                .uri("/users")
                .header("Authorization", "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].email").isEqualTo("phucsaiyan@example.com")
                .jsonPath("$.message").isEqualTo("Users fetched successfully");
    }

    // ✅ Test user creation without authentication (if endpoint allows it)
    // Verifies the creation logic and that it returns the expected response
    @Test
    void testCreateUser_withNoAuth() {
        User newUser = new User();
        newUser.setName("nak");
        newUser.setEmail("nak@example.com");
        newUser.setPassword("pass");
        newUser.setAge(30);
        newUser.setGender(GenderEnum.MALE);
        newUser.setAddress("Lyon");

        webTestClient.post()
                .uri("/users")
                .bodyValue(newUser)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.name").isEqualTo("nak")
                .jsonPath("$.message").isEqualTo("User created successfully");
    }

    // ✅ Test retrieving a specific user by ID with a valid token
    // Verifies the endpoint returns the expected user info
    @Test
    void testGetUserById_withAuth() {
        webTestClient.get()
                .uri("/users/{id}", userId)
                .header("Authorization", "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.email").isEqualTo("phucsaiyan@example.com")
                .jsonPath("$.message").isEqualTo("User fetched successfully");
    }

    // ✅ Test updating a user with a valid Bearer token
    // Ensures the user data is modified and returned correctly
    @Test
    void testUpdateUser_withAuth() {
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("phucsaiyan Updated");
        updatedUser.setEmail("phucsaiyan@example.com"); // must remain the same
        updatedUser.setPassword("newpass123"); // ignored in update
        updatedUser.setAge(26);
        updatedUser.setGender(GenderEnum.FEMALE);
        updatedUser.setAddress("Marseille");

        webTestClient.put()
                .uri("/users")
                .header("Authorization", "Bearer " + jwtToken)
                .bodyValue(updatedUser)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.name").isEqualTo("phucsaiyan Updated")
                .jsonPath("$.data.address").isEqualTo("Marseille")
                .jsonPath("$.message").isEqualTo("User updated successfully");
    }

    // ✅ Test deleting a user and ensuring it no longer exists afterward
    // Confirms deletion logic and verifies user is truly removed
    @Test
    void testDeleteUser_withAuth() {
        webTestClient.delete()
                .uri("/users/{id}", userId)
                .header("Authorization", "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("User deleted successfully");

        // ❗ Double-check that the deleted user cannot be fetched anymore
        webTestClient.get()
                .uri("/users/{id}", userId)
                .header("Authorization", "Bearer " + jwtToken)
                .exchange()
                .expectStatus().is4xxClientError(); // Expected: 404 Not Found or 400 depending on logic
    }

    // ❌ Test request with an invalid Bearer token
    // Verifies that Spring Security returns 401 Unauthorized and proper error
    // message
    @Test
    void testAccessWithInvalidBearerToken() {
        webTestClient.get()
                .uri("/users") // 🔒 A secured endpoint that requires a valid JWT token
                .header("Authorization", "Bearer invalid_token") // 🛑 Simulate an invalid or tampered token
                .exchange()
                .expectStatus().isUnauthorized() // ✅ Expect HTTP 401 Unauthorized from Spring Security
                .expectBody()
                .jsonPath("$.data").isEmpty()
                .jsonPath("$.message").isEqualTo("Token invalid, expired, malformed or missing from header");
    }
}
