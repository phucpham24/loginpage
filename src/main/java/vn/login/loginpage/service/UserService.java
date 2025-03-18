package vn.login.loginpage.service;

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

}
