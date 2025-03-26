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
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.name").isEqualTo("nak")
                .jsonPath("$.message").isEqualTo("User created successfully");
    }

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

    @Test
    void testDeleteUser_withAuth() {
        webTestClient.delete()
                .uri("/users/{id}", userId)
                .header("Authorization", "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("User deleted successfully");

        // Verify user no longer exists
        webTestClient.get()
                .uri("/users/{id}", userId)
                .header("Authorization", "Bearer " + jwtToken)
                .exchange()
                .expectStatus().is4xxClientError(); // or 404 if mapped accordingly
    }
}
