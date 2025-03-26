package vn.login.loginpage.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.response.ResCreateUserDTO;
import vn.login.loginpage.domain.response.ResResponse;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.error.InvalidException;
import vn.login.loginpage.util.response.ResponseWrapper;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;

    }

    @PostMapping
    public Mono<ResponseEntity<ResResponse<ResCreateUserDTO>>> createNewUser(@RequestBody User user) {
        return ResponseWrapper.wrapMono(this.userService.createUser(user), "User created successfully",
                HttpStatus.CREATED);
    }

    @GetMapping
    public Mono<ResponseEntity<ResResponse<List<User>>>> listUser() {
        return ResponseWrapper.wrapFlux(this.userService.findAllUser(), "Users fetched successfully", HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ResResponse<User>>> findUser(@PathVariable Long id) {
        return ResponseWrapper.wrapMono(
                this.userService.getUserById(id)
                        .switchIfEmpty(Mono.error(new InvalidException("User with ID " + id + " not found"))),
                "User fetched successfully", HttpStatus.OK);
    }

    @PutMapping
    public Mono<ResponseEntity<ResResponse<ResCreateUserDTO>>> updateUser(@RequestBody User user) {
        return ResponseWrapper.wrapMono(this.userService.updateUser(user), "User updated successfully", HttpStatus.OK);
    }

    // Delete user
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ResResponse<Object>>> deleteUser(@PathVariable Long id) {
        return ResponseWrapper.wrapMono(
                this.userService.deleteUserById(id).then(Mono.justOrEmpty(
                        null)),
                "User deleted successfully",
                HttpStatus.OK);
    }

}
