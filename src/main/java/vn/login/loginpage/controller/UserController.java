package vn.login.loginpage.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.login.loginpage.config.SecurityConfig;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.constant.GenderEnum;
import vn.login.loginpage.util.error.InvalidException;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
public class UserController {
    private UserService userService;

    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    public ResponseEntity<User> createNewUser(@RequestBody User user) throws InvalidException {
        if (this.userService.checkExistsByEmail(user.getEmail())) {
            throw new InvalidException("Email: " + user.getEmail() + " already exists");
        }
        String hashPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        User newUser = this.userService.handleSaveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = this.userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@RequestBody User userUpdate) throws InvalidException {
        User user = this.userService.getUsersById(userUpdate.getId());
        if (user == null) {
            throw new InvalidException("User not found");
        }

        String hashPassword = this.passwordEncoder.encode(userUpdate.getPassword());

        user.setAddress(userUpdate.getAddress());
        user.setAge(userUpdate.getAge());
        user.setEmail(userUpdate.getEmail());
        user.setGender(userUpdate.getGender());
        user.setName(userUpdate.getName());
        user.setPassword(hashPassword);
        User userUpdated = this.userService.handleSaveUser(user);
        return ResponseEntity.status(HttpStatus.OK).body(userUpdated);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable("id") long id) throws InvalidException {

        this.userService.deleteUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
