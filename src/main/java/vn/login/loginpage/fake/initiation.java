package vn.login.loginpage.fake;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.login.loginpage.domain.User;
import vn.login.loginpage.repository.UserRepository;
import vn.login.loginpage.util.constant.GenderEnum;

@Component
@RequiredArgsConstructor
public class initiation implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = User.builder()
                .name("admin")
                .email("admin@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .age(45)
                .gender(GenderEnum.MALE) // Or FEMALE / OTHER if needed
                .address("Admin Street, City, Country")
                .refreshToken(null)
                .createdAt(LocalDateTime.now().toInstant(java.time.ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now().toInstant(java.time.ZoneOffset.UTC))
                .roleId(2L)
                .build();

        User user = User.builder()
                .name("user")
                .email("user@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .age(45)
                .gender(GenderEnum.MALE) // You can change this too
                .address("User Avenue, City, Country")
                .refreshToken(null)
                .createdAt(LocalDateTime.now().toInstant(java.time.ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now().toInstant(java.time.ZoneOffset.UTC))
                .roleId(1L)
                .build();

        userRepository.count()
                .filter(count -> count == 0)
                .flatMapMany(ignored -> userRepository.saveAll(java.util.List.of(admin, user)))
                .doOnComplete(() -> System.out.println("✅ Admin and User seeded"))
                .subscribe();
    }
}