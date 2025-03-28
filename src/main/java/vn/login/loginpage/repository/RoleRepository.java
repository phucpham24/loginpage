package vn.login.loginpage.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.Role;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {
    Mono<Role> getRoleByName(String Name);

    Mono<Role> getRoleById(Long id);

}
