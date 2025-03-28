package vn.login.loginpage.Integration_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import vn.login.loginpage.domain.Role;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.request.ReqLoginDTO;

import vn.login.loginpage.repository.RoleRepository;
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
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private Long userId;
    private Long roleId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll().block();
        // ✅ Clean roles too

        // 👤 Create and persist a role
        Role role = new Role();
        role.setId(2L);
        role.setName("ADMIN");
        role.setDescription("Standard user role");

        // 👤 Create and persist a test user
        User user = new User();
        user.setName("phucsaiyan");
        user.setEmail("phucsaiyan@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setAge(25);
        user.setGender(GenderEnum.FEMALE);
        user.setAddress("Paris");
        user.setRoleId(role.getId());

        userId = userRepository.save(user).map(User::getId).block();

        // 🔐 Login to get JWT
        ReqLoginDTO loginDTO = new ReqLoginDTO();
        loginDTO.setUsername("phucsaiyan@example.com");
        loginDTO.setPassword("password123");

        webTestClient.post()
                .uri("/auth/login")
                .bodyValue(loginDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.access_token").value(token -> jwtToken = (String) token)
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
        newUser.setRoleId(1L); // ✅ Required

        webTestClient.post()
                .uri("/users")
                .bodyValue(newUser)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.name").isEqualTo("nak")
                .jsonPath("$.message").isEqualTo("User created successfully");
    }

    @Test
    void testAccessWithInvalidBearerToken() {
        webTestClient.get()
                .uri("/users")
                .header("Authorization", "Bearer invalid_token")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.data").isEmpty()
                .jsonPath("$.message").isEqualTo("Token invalid, expired, malformed or missing from header");
    }

    @Test
    void testGetUserById_notFound() {
        webTestClient.get()
                .uri("/users/99999")
                .header("Authorization", "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("User with ID 99999 not found");
    }
}
