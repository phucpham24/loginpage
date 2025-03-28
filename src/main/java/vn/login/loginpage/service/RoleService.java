package vn.login.loginpage.service;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.Role;
import vn.login.loginpage.repository.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Mono<Role> findRoleByName(String name) {
        return this.roleRepository.getRoleByName(name);
    }

    public Mono<Role> findRoleById(Long id) {
        return this.roleRepository.getRoleById(id);
    }

}
