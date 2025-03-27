package vn.login.loginpage.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.domain.response.ResCreateUserDTO;
import vn.login.loginpage.domain.response.ResResponse;
import vn.login.loginpage.domain.response.ResUpdateUserDTO;
import vn.login.loginpage.domain.response.ResUserDTO;
import vn.login.loginpage.repository.UserRepository;
import vn.login.loginpage.util.error.InvalidException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
                .map(user -> new ResUserDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getGender(),
                        user.getAddress(),
                        user.getAge(),
                        user.getUpdatedAt(),
                        user.getCreatedAt()));
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

                    String hashedPassword = passwordEncoder.encode(user.getPassword());
                    user.setPassword(hashedPassword);
                    user.setCreatedAt(Instant.now());

                    return this.handleSaveUser(user)
                            .map(this::convertUserToUserDTO);
                });
    }

    // convert createUser
    public ResCreateUserDTO convertUserToUserDTO(User user) {
        ResCreateUserDTO userdto = new ResCreateUserDTO();

        userdto.setName(user.getName());
        userdto.setId(user.getId());
        userdto.setAge(user.getAge());
        userdto.setCreatedAt(user.getCreatedAt());
        userdto.setGender(user.getGender());
        userdto.setAddress(user.getAddress());
        userdto.setEmail(user.getEmail());
        return userdto;
    }

    public ResUpdateUserDTO convertUserToUserUpdateDTO(User user) {
        ResUpdateUserDTO resUpdateUserDTO = new ResUpdateUserDTO();

        resUpdateUserDTO.setName(user.getName());
        resUpdateUserDTO.setId(user.getId());
        resUpdateUserDTO.setAge(user.getAge());
        resUpdateUserDTO.setUpdateAt(user.getUpdatedAt());
        resUpdateUserDTO.setGender(user.getGender());
        resUpdateUserDTO.setAddress(user.getAddress());

        return resUpdateUserDTO;
    }

    public Mono<ResUpdateUserDTO> updateUser(User user) {
        return this.getUserById(user.getId())
                .switchIfEmpty(Mono.error(new InvalidException("User with ID " + user.getId() + " not found")))
                .flatMap(existingUser -> {
                    if (!existingUser.getEmail().equals(user.getEmail())) {
                        return Mono.error(new InvalidException("Email update is not allowed"));
                    }
                    existingUser.setName(user.getName());
                    existingUser.setAddress(user.getAddress());
                    existingUser.setAge(user.getAge());
                    // existingUser.setPassword(user.getPassword()); // optionally hash if needed
                    existingUser.setEmail(user.getEmail());
                    existingUser.setGender(user.getGender());
                    existingUser.setUpdatedAt(Instant.now());

                    return this.handleSaveUser(existingUser)
                            .map(this::convertUserToUserUpdateDTO); // map to ResCreateUserDTO
                });
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

}
