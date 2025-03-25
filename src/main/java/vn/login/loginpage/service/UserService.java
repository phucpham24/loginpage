package vn.login.loginpage.service;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.repository.UserRepository;
import vn.login.loginpage.util.error.InvalidException;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<User> handleSaveUser(User user) {
        return this.userRepository.save(user);
    }

    public Mono<Boolean> checkExistsByEmail(String email) {
        return this.userRepository.findUserByEmail(email)
                .hasElement();
    }

    public Flux<User> findAllUser() {
        return this.userRepository.findAll();
    }

    public Mono<User> getUserById(long id) {
        return this.userRepository.findUserById(id);
    }

    public Mono<Void> deleteUserById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new InvalidException("User with ID " + id + " not found")))
                .flatMap(existingUser -> userRepository.deleteById(id));
    }

}
