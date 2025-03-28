package vn.login.loginpage.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import vn.login.loginpage.domain.Role;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.response.ResCreateUserDTO;
import vn.login.loginpage.domain.response.ResResponse;
import vn.login.loginpage.domain.response.ResUpdateUserDTO;
import vn.login.loginpage.domain.response.ResUserDTO;
import vn.login.loginpage.repository.RoleRepository;
import vn.login.loginpage.repository.UserRepository;
import vn.login.loginpage.util.error.InvalidException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    public Mono<User> handleSaveUser(User user) {
        return this.userRepository.save(user);
    }

    public Mono<Boolean> checkExistsByEmail(String email) {
        return this.userRepository.findUserByEmail(email)
                .hasElement();
    }

    public Mono<User> findUserByEmail(String email) {
        return this.userRepository.findUserByEmail(email);
    }

    public Flux<ResUserDTO> findAllUser() {
        return this.userRepository.findAll()
                .flatMap(user -> this.roleService.findRoleById(user.getRoleId())
                        .map(role -> {
                            ResUserDTO dto = new ResUserDTO();
                            dto.setId(user.getId());
                            dto.setEmail(user.getEmail());
                            dto.setName(user.getName());
                            dto.setGender(user.getGender());
                            dto.setAddress(user.getAddress());
                            dto.setAge(user.getAge());
                            dto.setUpdatedAt(user.getUpdatedAt());
                            dto.setCreatedAt(user.getCreatedAt());
                            // Set nested RoleUser
                            ResUserDTO.RoleUser roleDTO = new ResUserDTO.RoleUser(role.getId(), role.getName());
                            dto.setRole(roleDTO);

                            return dto;
                        }

                        ));
    }

    public Mono<ResUserDTO> fetchUserById(Long id) {
        return userRepository.findUserById(id)
                .switchIfEmpty(Mono.error(new InvalidException("User with ID " + id + " not found")))
                .flatMap(user -> roleService.findRoleById(user.getRoleId())
                        .map(role -> {
                            ResUserDTO dto = new ResUserDTO();
                            dto.setId(user.getId());
                            dto.setEmail(user.getEmail());
                            dto.setName(user.getName());
                            dto.setGender(user.getGender());
                            dto.setAddress(user.getAddress());
                            dto.setAge(user.getAge());
                            dto.setUpdatedAt(user.getUpdatedAt());
                            dto.setCreatedAt(user.getCreatedAt());

                            dto.setRole(new ResUserDTO.RoleUser(role.getId(), role.getName()));

                            return dto;
                        }));
    }

    public Mono<User> getUserById(long id) {
        return this.userRepository.findUserById(id);
    }

    public Mono<ResCreateUserDTO> createUser(User user) {
        return checkExistsByEmail(user.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new InvalidException(
                                "Email " + user.getEmail() + " already exists. Please use another email."));
                    }
                    return this.roleService.findRoleById(user.getRoleId())
                            .switchIfEmpty(
                                    (Mono.error(new InvalidException("Role " + user.getRoleId() + " not found"))))
                            .flatMap(role -> {
                                String hashedPassword = passwordEncoder.encode(user.getPassword());
                                user.setPassword(hashedPassword);
                                user.setCreatedAt(Instant.now());

                                return this.handleSaveUser(user)
                                        .map(createUser -> convertUserToUserDTO(createUser, role));
                            });
                });

    }

    // convert createUser
    public ResCreateUserDTO convertUserToUserDTO(User user, Role role) {
        ResCreateUserDTO userdto = new ResCreateUserDTO();

        userdto.setName(user.getName());
        userdto.setId(user.getId());
        userdto.setAge(user.getAge());
        userdto.setCreatedAt(user.getCreatedAt());
        userdto.setGender(user.getGender());
        userdto.setAddress(user.getAddress());
        userdto.setEmail(user.getEmail());
        userdto.setRole(new ResCreateUserDTO.RoleUser(role.getId(), role.getName()));

        return userdto;
    }

    public ResUpdateUserDTO convertUserToUserUpdateDTO(User user, Role role) {
        ResUpdateUserDTO resUpdateUserDTO = new ResUpdateUserDTO();

        resUpdateUserDTO.setName(user.getName());
        resUpdateUserDTO.setId(user.getId());
        resUpdateUserDTO.setAge(user.getAge());
        resUpdateUserDTO.setUpdateAt(user.getUpdatedAt());
        resUpdateUserDTO.setGender(user.getGender());
        resUpdateUserDTO.setAddress(user.getAddress());
        resUpdateUserDTO.setRole(new ResUpdateUserDTO.RoleUser(role.getId(), role.getName()));

        return resUpdateUserDTO;
    }

    public Mono<ResUpdateUserDTO> updateUser(User user) {
        return this.getUserById(user.getId())
                .switchIfEmpty(Mono.error(new InvalidException("User with ID " + user.getId() + " not found")))
                .flatMap(existingUser -> this.roleService.findRoleById(user.getRoleId())
                        .switchIfEmpty(Mono.error(new InvalidException("Role " + user.getRoleId() + " not found")))
                        .flatMap(role -> {
                            if (!existingUser.getEmail().equals(user.getEmail())) {
                                return Mono.error(new InvalidException("Email update is not allowed"));
                            }

                            // Update user fields
                            existingUser.setName(user.getName());
                            existingUser.setAddress(user.getAddress());
                            existingUser.setAge(user.getAge());
                            existingUser.setGender(user.getGender());
                            existingUser.setUpdatedAt(Instant.now());
                            existingUser.setRoleId(user.getRoleId());

                            return this.handleSaveUser(existingUser)
                                    .map(updatedUser -> convertUserToUserUpdateDTO(updatedUser, role));
                        }));
    }

    public Mono<Object> deleteUserById(Long id) {
        return this.userRepository.findById(id)
                .switchIfEmpty(Mono.error(new InvalidException("User with ID " + id + " not found")))
                .flatMap(existingUser -> this.userRepository.deleteById(id));
    }

    public Mono<Void> updateUserToken(String refreshToken, String email) {
        return this.userRepository.findUserByEmail(email)
                .switchIfEmpty(Mono.error(new InvalidException("User with email " + email + " not found")))
                .flatMap(existingUser -> {
                    existingUser.setRefreshToken(refreshToken);
                    return this.handleSaveUser(existingUser);
                })
                .then(); // To return Mono<Void>
    }

    public Mono<User> getUserByRefreshTokenAndEmail(String token, String email) {
        return this.userRepository.findUserByRefreshTokenAndEmail(token, email);
    }

    public Mono<Void> deleteUserByEmail(String email) {
        return this.userRepository.deleteUserByEmail(email);
    }

    public Mono<Tuple2<User, Role>> findUserAndRoleByEmail(String email) {
        return this.userRepository.findUserByEmail(email)
                .flatMap(user -> this.roleService.findRoleById(user.getRoleId())
                        .map(role -> Tuples.of(user, role)));
    }
}
