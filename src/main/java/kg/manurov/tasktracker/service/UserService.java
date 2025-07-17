package kg.manurov.tasktracker.service;

import kg.manurov.tasktracker.domain.dto.RegistrationDto;
import kg.manurov.tasktracker.domain.models.User;
import kg.manurov.tasktracker.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;


    public User create(RegistrationDto registrationDto) {
        User user = User.builder()
                .name(registrationDto.getName())
                .email(registrationDto.getEmail())
                .passwordHash(registrationDto.getPassword())
                .role("USER")
                .enabled(true)
                .build();
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return repository.save(user);
    }

    public User getByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден с email: " + email));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден с email: " + email));
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

}