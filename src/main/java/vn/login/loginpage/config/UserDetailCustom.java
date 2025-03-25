package vn.login.loginpage.config;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.repository.UserRepository;

@Component("UserDetailCustom")
public class UserDetailCustom implements ReactiveUserDetailsService {
    private final UserRepository userRepository;

    public UserDetailCustom(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return this.userRepository.findUserByEmail(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("username/password invalid")))
                .map(this::mapToUserDetails);
    }

    private UserDetails mapToUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .build();
    }
}
