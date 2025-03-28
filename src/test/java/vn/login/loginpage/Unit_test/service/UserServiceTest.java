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
import vn.login.loginpage.domain.Role;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.response.ResCreateUserDTO;
import vn.login.loginpage.repository.UserRepository;
import vn.login.loginpage.service.RoleService;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.constant.GenderEnum;
import vn.login.loginpage.util.error.InvalidException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;

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
        testUser.setRoleId(1L); // important

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("USER");
    }

    @Test
    void testCreateUser_Success() {
        when(userRepository.findUserByEmail("phucsaiyan@example.com")).thenReturn(Mono.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleService.findRoleById(1L)).thenReturn(Mono.just(testRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(userService.createUser(testUser))
                .assertNext(dto -> {
                    assertEquals("phucsaiyan", dto.getName());
                    assertEquals("phucsaiyan@example.com", dto.getEmail());
                    assertNotNull(dto.getCreatedAt());
                    assertEquals("USER", dto.getRole().getName());
                })
                .verifyComplete();
    }

    @Test
    void testCreateUser_EmailAlreadyExists() {
        when(userRepository.findUserByEmail("phucsaiyan@example.com")).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.createUser(testUser))
                .expectErrorMatches(error -> error instanceof InvalidException &&
                        error.getMessage().contains("already exists"))
                .verify();
    }

    @Test
    void testGetUserById_UserFound() {
        when(userRepository.findUserById(1L)).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.getUserById(1L))
                .expectNext(testUser)
                .verifyComplete();
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findUserById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.getUserById(1L))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void testDeleteUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUserById(1L))
                .verifyComplete();
    }

    @Test
    void testDeleteUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUserById(1L))
                .expectErrorMatches(error -> error instanceof InvalidException &&
                        error.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void testUpdateUser_Success() {
        when(userRepository.findUserById(1L)).thenReturn(Mono.just(testUser));
        when(roleService.findRoleById(1L)).thenReturn(Mono.just(testRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        User updated = new User();
        updated.setId(1L);
        updated.setName("phucsaiyan Updated");
        updated.setEmail("phucsaiyan@example.com"); // same email
        updated.setAge(30);
        updated.setGender(GenderEnum.FEMALE);
        updated.setAddress("Lyon");
        updated.setRoleId(1L);

        StepVerifier.create(userService.updateUser(updated))
                .assertNext(dto -> {
                    assertEquals("phucsaiyan Updated", dto.getName());
                    assertEquals("Lyon", dto.getAddress());
                    assertEquals(30, dto.getAge());
                    assertEquals("USER", dto.getRole().getName());
                })
                .verifyComplete();
    }

    @Test
    void testUpdateUser_ChangingEmailNotAllowed() {
        testUser.setRoleId(1L);
        when(userRepository.findUserById(1L)).thenReturn(Mono.just(testUser));
        when(roleService.findRoleById(1L)).thenReturn(Mono.just(testRole));

        User updated = new User();
        updated.setId(1L);
        updated.setName("phucsaiyan");
        updated.setEmail("new@example.com"); // different email
        updated.setRoleId(1L);

        StepVerifier.create(userService.updateUser(updated))
                .expectErrorMatches(error -> error instanceof InvalidException &&
                        error.getMessage().equals("Email update is not allowed"))
                .verify();
    }
}
