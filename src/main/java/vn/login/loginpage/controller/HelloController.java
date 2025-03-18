package vn.login.loginpage.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.login.loginpage.util.error.InvalidException;

@RestController
public class HelloController {
    @GetMapping("/")
    public String getHelloWorld() throws InvalidException {
        if (true)
            throw new InvalidException("check mate hello");

        return "Hello World home page";
    }
}
