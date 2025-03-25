package vn.login.loginpage.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.User;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> findUserByEmail(String email);

    // Mono<User> findUserById(long Id);

    Mono<User> findUserByRefreshToken(String refreshToken);

    Mono<User> findUserById(long id);
}
