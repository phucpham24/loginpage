package vn.login.loginpage.Unit_test.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import vn.login.loginpage.controller.UserController;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.response.ResCreateUserDTO;
import vn.login.loginpage.domain.response.ResUpdateUserDTO;
import vn.login.loginpage.domain.response.ResUserDTO;
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
                    assertEquals(HttpStatus.CREATED.value(), response.getBody().getStatusCode());
                    assertEquals("User created successfully", response.getBody().getMessage());
                    assertEquals("phucsaiyan", response.getBody().getData().getName());
                })
                .verifyComplete();
    }

    @Test
    void testFindUserById() {
        when(userService.getUserById(1L)).thenReturn(Mono.just(testUser));

        StepVerifier.create(userController.findUser(1L))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK.value(), response.getBody().getStatusCode());
                    assertEquals("User fetched successfully", response.getBody().getMessage());
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
        ResUserDTO Use = new ResUserDTO();
        Use.setId(testUser.getId());
        Use.setName(testUser.getName());
        Use.setGender(testUser.getGender());
        Use.setAddress(testUser.getAddress());
        Use.setAge(testUser.getAge());
        Use.setUpdatedAt(testUser.getUpdatedAt());
        Use.setCreatedAt(testUser.getCreatedAt());

        when(userService.findAllUser()).thenReturn(Flux.just(Use));

        StepVerifier.create(userController.listUser())
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK.value(), response.getBody().getStatusCode());
                    assertEquals(1, response.getBody().getData().size());
                    assertEquals("phucsaiyan", response.getBody().getData().get(0).getName());
                })
                .verifyComplete();
    }

    @Test
    void testUpdateUser() {
        ResUpdateUserDTO updateDto = new ResUpdateUserDTO();
        updateDto.setId(testUser.getId());
        updateDto.setName(testUser.getName());
        updateDto.setGender(testUser.getGender());
        updateDto.setAddress(testUser.getAddress());
        updateDto.setAge(testUser.getAge());
        updateDto.setUpdateAt(Instant.now());

        when(userService.updateUser(any(User.class))).thenReturn(Mono.just(updateDto));

        StepVerifier.create(userController.updateUser(testUser))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK.value(), response.getBody().getStatusCode());
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
                    assertEquals(HttpStatus.OK.value(), response.getBody().getStatusCode());
                    assertEquals("User deleted successfully", response.getBody().getMessage());
                    assertNull(response.getBody().getData());
                })
                .verifyComplete();
    }
}
