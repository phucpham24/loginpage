package vn.login.loginpage.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.response.ResResponse;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.error.InvalidException;

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

public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;

    }

    @PostMapping("/users")
    public Mono<ResponseEntity<ResResponse<User>>> createNewUser(@RequestBody User user) {
        return userService.checkExistsByEmail(user.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        // Throwing reactive exception here
                        return Mono.error(new InvalidException(
                                "Email " + user.getEmail() + " already exists. Please use another email."));
                    }
                    String hashPassword = this.passwordEncoder.encode(user.getPassword());
                    user.setPassword(hashPassword);
                    return userService.handleSaveUser(user)
                            .map(savedUser -> {
                                ResResponse<User> res = new ResResponse<>();
                                res.setStatusCode(HttpStatus.CREATED.value());
                                res.setMessage("User created successfully");
                                res.setData(savedUser);
                                res.setError(null);
                                return ResponseEntity.status(HttpStatus.CREATED).body(res);
                            });
                });
    }

    // Get all users
    @GetMapping("/users")
    public Mono<ResponseEntity<ResResponse<List<User>>>> listUser() {
        return userService.findAllUser()
                .collectList()
                .map(users -> {
                    ResResponse<List<User>> res = new ResResponse<>();
                    res.setStatusCode(HttpStatus.OK.value());
                    res.setMessage("Users fetched successfully");
                    res.setData(users);
                    res.setError(null);
                    return ResponseEntity.ok(res);
                });
    }

    // Get user by ID
    @GetMapping("/users/{id}")
    public Mono<ResponseEntity<ResResponse<User>>> findUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .switchIfEmpty(Mono.error(new InvalidException("User with ID " + id + " not found")))
                .map(user -> {
                    ResResponse<User> res = new ResResponse<>();
                    res.setStatusCode(HttpStatus.OK.value());
                    res.setMessage("User fetched successfully");
                    res.setData(user);
                    res.setError(null);
                    return ResponseEntity.ok(res);
                });
    }

    // Update user
    @PutMapping("/users")
    public Mono<ResponseEntity<ResResponse<User>>> updateUser(@RequestBody User user) {
        return userService.getUserById(user.getId())
                .switchIfEmpty(Mono.error(new InvalidException("User with ID " + user.getId() + " not found")))
                .flatMap(existingUser -> {
                    existingUser.setName(user.getName());
                    existingUser.setAddress(user.getAddress());
                    existingUser.setAge(user.getAge());
                    existingUser.setPassword(user.getPassword());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setGender(user.getGender());
                    existingUser.setUpdatedAt(java.time.Instant.now());

                    return userService.handleSaveUser(existingUser)
                            .map(savedUser -> {
                                ResResponse<User> res = new ResResponse<>();
                                res.setStatusCode(HttpStatus.OK.value());
                                res.setMessage("User updated successfully");
                                res.setData(savedUser);
                                res.setError(null);
                                return ResponseEntity.ok(res);
                            });
                });
    }

    // Delete user
    @DeleteMapping("/users/{id}")
    public Mono<ResponseEntity<ResResponse<Object>>> deleteUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .switchIfEmpty(Mono.error(new InvalidException("User with ID " + id + " not found")))
                .flatMap(user -> userService.deleteUserById(id)
                        .then(Mono.fromSupplier(() -> {
                            ResResponse<Object> res = new ResResponse<>();
                            res.setStatusCode(HttpStatus.OK.value());
                            res.setMessage("User deleted successfully");
                            res.setData(null);
                            res.setError(null);
                            return ResponseEntity.status(HttpStatus.OK).body(res);
                        })));
    }
}
