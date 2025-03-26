package vn.login.loginpage.Unit_test.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import reactor.test.StepVerifier;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.response.ResCreateUserDTO;
import vn.login.loginpage.repository.UserRepository;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.constant.GenderEnum;
import vn.login.loginpage.util.error.InvalidException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("phucsaiyan");
        testUser.setEmail("phucsaiyan@example.com");
        testUser.setPassword("password123");
        testUser.setAge(25);
        testUser.setGender(GenderEnum.FEMALE);
        testUser.setAddress("Paris");
        testUser.setCreatedAt(Instant.now());
    }

    // ✅ Test that a user is successfully created when the email doesn't already
    // exist.
    // Ensures password encoding and default fields are handled correctly.
    @Test
    void testCreateUser_Success() {
        when(userRepository.findUserByEmail("phucsaiyan@example.com")).thenReturn(Mono.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(userService.createUser(testUser))
                .assertNext(dto -> {
                    assertEquals("phucsaiyan", dto.getName());
                    assertEquals("phucsaiyan@example.com", dto.getEmail());
                    assertNotNull(dto.getCreatedAt());
                })
                .verifyComplete();
    }

    // ❌ Test that user creation fails if the email already exists.
    // Ensures duplicate user validation works.
    @Test
    void testCreateUser_EmailAlreadyExists() {
        when(userRepository.findUserByEmail("phucsaiyan@example.com")).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.createUser(testUser))
                .expectErrorMatches(error -> error instanceof InvalidException &&
                        error.getMessage().contains("already exists"))
                .verify();
    }

    // ✅ Test that retrieving a user by ID returns the correct user if found.
    @Test
    void testGetUserById_UserFound() {
        when(userRepository.findUserById(1L)).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.getUserById(1L))
                .expectNext(testUser)
                .verifyComplete();
    }

    // ❌ Test that retrieving a user by ID returns an empty Mono when not found.
    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findUserById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.getUserById(1L))
                .expectNextCount(0)
                .verifyComplete();
    }

    // ✅ Test that deleting a user by ID completes successfully when the user
    // exists.
    @Test
    void testDeleteUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUserById(1L))
                .verifyComplete();
    }

    // ❌ Test that deleting a user by ID throws an exception when the user doesn't
    // exist.
    @Test
    void testDeleteUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUserById(1L))
                .expectErrorMatches(error -> error instanceof InvalidException &&
                        error.getMessage().contains("not found"))
                .verify();
    }

    // ✅ Test that updating a user works correctly when the user exists and email
    // remains unchanged.
    @Test
    void testUpdateUser_Success() {
        when(userRepository.findUserById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        User updated = new User();
        updated.setId(1L);
        updated.setName("phucsaiyan Updated");
        updated.setEmail("phucsaiyan@example.com"); // same email
        updated.setAge(30);
        updated.setGender(GenderEnum.FEMALE);
        updated.setAddress("Lyon");

        StepVerifier.create(userService.updateUser(updated))
                .assertNext(dto -> {
                    assertEquals("phucsaiyan Updated", dto.getName());
                    assertEquals("Lyon", dto.getAddress());
                    assertEquals(30, dto.getAge());
                })
                .verifyComplete();
    }

    // ❌ Test that updating the user's email is not allowed and throws an exception.
    @Test
    void testUpdateUser_ChangingEmailNotAllowed() {
        when(userRepository.findUserById(1L)).thenReturn(Mono.just(testUser));

        User updated = new User();
        updated.setId(1L);
        updated.setName("phucsaiyan");
        updated.setEmail("new@example.com"); // different email

        StepVerifier.create(userService.updateUser(updated))
                .expectErrorMatches(error -> error instanceof InvalidException &&
                        error.getMessage().equals("Email update is not allowed"))
                .verify();
    }
}
