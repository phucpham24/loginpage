package vn.login.loginpage.Unit_test.controller;

import static org.mockito.ArgumentMatchers.any;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import reactor.test.StepVerifier;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.login.loginpage.controller.UserController;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.response.ResCreateUserDTO;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.constant.GenderEnum;
import vn.login.loginpage.util.error.InvalidException;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private ResCreateUserDTO userDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("phucsaiyan");
        testUser.setEmail("phucsaiyan@example.com");
        testUser.setPassword("secret");
        testUser.setAge(25);
        testUser.setGender(GenderEnum.FEMALE);
        testUser.setAddress("Paris");
        testUser.setCreatedAt(Instant.now());

        userDto = new ResCreateUserDTO();
        userDto.setId(testUser.getId());
        userDto.setName(testUser.getName());
        userDto.setEmail(testUser.getEmail());
        userDto.setAddress(testUser.getAddress());
        userDto.setAge(testUser.getAge());
        userDto.setGender(testUser.getGender());
        userDto.setCreatedAt(testUser.getCreatedAt());
    }

    @Test
    void testCreateNewUser() {
        when(userService.createUser(any(User.class))).thenReturn(Mono.just(userDto));

        StepVerifier.create(userController.createNewUser(testUser))
                .assertNext(response -> {
                    assertEquals("User created successfully", response.getBody().getMessage());
                    assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(response.getBody().getStatusCode()));
                    assertEquals("phucsaiyan", response.getBody().getData().getName());
                })
                .verifyComplete();
    }

    @Test
    void testFindUserById() {
        when(userService.getUserById(1L)).thenReturn(Mono.just(testUser));

        StepVerifier.create(userController.findUser(1L))
                .assertNext(response -> {
                    assertEquals("User fetched successfully", response.getBody().getMessage());
                    assertEquals(HttpStatus.OK, HttpStatus.valueOf(response.getBody().getStatusCode()));
                    assertEquals("phucsaiyan", response.getBody().getData().getName());
                })
                .verifyComplete();
    }

    @Test
    void testFindUserById_NotFound() {
        when(userService.getUserById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userController.findUser(1L))
                .expectErrorMatches(error -> error instanceof InvalidException &&
                        error.getMessage().equals("User with ID 1 not found"))
                .verify();
    }

    @Test
    void testListUsers() {
        when(userService.findAllUser()).thenReturn(Flux.just(testUser));

        StepVerifier.create(userController.listUser())
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, HttpStatus.valueOf(response.getBody().getStatusCode()));
                    assertEquals(1, response.getBody().getData().size());
                    assertEquals("phucsaiyan", response.getBody().getData().get(0).getName());
                })
                .verifyComplete();
    }

    @Test
    void testUpdateUser() {
        when(userService.updateUser(any(User.class))).thenReturn(Mono.just(userDto));

        StepVerifier.create(userController.updateUser(testUser))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, HttpStatus.valueOf(response.getBody().getStatusCode()));
                    assertEquals("User updated successfully", response.getBody().getMessage());
                    assertEquals("phucsaiyan", response.getBody().getData().getName());
                })
                .verifyComplete();
    }

    @Test
    void testDeleteUser() {
        when(userService.deleteUserById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userController.deleteUser(1L))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, HttpStatus.valueOf(response.getBody().getStatusCode()));
                    assertEquals("User deleted successfully", response.getBody().getMessage());
                    assertNull(response.getBody().getData());
                })
                .verifyComplete();
    }
}
