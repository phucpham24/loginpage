package vn.login.loginpage.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.login.loginpage.domain.User;
import vn.login.loginpage.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleSaveUser(User user) {
        return this.userRepository.save(user);
    }

    public boolean checkExistsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public User getUserByUserName(String email) {
        return this.userRepository.findUserByEmail(email);
    }
}
