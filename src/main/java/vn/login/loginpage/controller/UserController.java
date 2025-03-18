package vn.login.loginpage.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.login.loginpage.domain.User;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.error.InvalidException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
// @RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // @PostMapping("/user")
    // public User createNewUser(@RequestBody User user) throws InvalidException {
    // if (this.userService.checkExistsByEmail(user.getEmail())) {
    // throw new InvalidException("Email" + user.getEmail() + "already exists");
    // }

    // return this.userService.handleSaveUser(user);
    // }

    @GetMapping("/")
    public String testAPI() {
        return "createNewUser";
    }

}
