// package vn.login.loginpage.fake;

// import java.util.*;
// import java.time.LocalDateTime;
// import java.util.UUID;

// import org.springframework.boot.CommandLineRunner;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Component;

// import com.github.javafaker.Faker;

// import lombok.RequiredArgsConstructor;
// import reactor.core.publisher.Mono;
// import vn.login.loginpage.domain.User;
// import vn.login.loginpage.repository.UserRepository;
// import vn.login.loginpage.util.constant.GenderEnum;

// @Component
// @RequiredArgsConstructor
// public class UserSeeder implements CommandLineRunner {

// private final UserRepository userRepository;
// private final PasswordEncoder passwordEncoder;

// public Mono<Void> deleteAllUser() {
// return this.userRepository.deleteAll();
// }

// @Override
// public void run(String... args) {
// Faker faker = new Faker();
// List<User> fakeUsers = new ArrayList<>();
// Set<String> usedEmails = new HashSet<>();

// while (fakeUsers.size() < 100_000) {
// String email = faker.internet().emailAddress();
// if (usedEmails.contains(email))
// continue;

// usedEmails.add(email);

// fakeUsers.add(User.builder()
// .name(faker.name().username())
// .email(email)
// .password(UUID.randomUUID().toString())
// .age(faker.number().numberBetween(18, 60))
// .gender(GenderEnum.valueOf(faker.options().option("MALE", "FEMALE",
// "OTHER")))
// .address(faker.address().fullAddress())
// .refreshToken(null)
// .createdAt(LocalDateTime.now().toInstant(java.time.ZoneOffset.UTC))
// .updatedAt(LocalDateTime.now().toInstant(java.time.ZoneOffset.UTC))
// .roleId(1L)
// .build());
// }

// userRepository.count()
// .filter(count -> count == 0)
// .flatMapMany(ignored -> userRepository.saveAll(fakeUsers))
// .doOnComplete(() -> System.out.println("✅ All fake users seeded"))
// .subscribe();
// }
// }
