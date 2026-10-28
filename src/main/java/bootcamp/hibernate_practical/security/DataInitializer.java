package bootcamp.hibernate_practical.security;

import bootcamp.hibernate_practical.entity.AppUser;
import bootcamp.hibernate_practical.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createIfMissing("user", "user", "USER");
        createIfMissing("admin", "admin", "ADMIN");
    }

    private void createIfMissing(String username, String rawPassword, String role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(new AppUser(username, passwordEncoder.encode(rawPassword), role));
        }
    }
}