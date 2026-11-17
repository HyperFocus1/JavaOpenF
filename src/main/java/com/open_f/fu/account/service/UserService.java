package com.open_f.fu.account.service;

import java.util.Locale;

import com.open_f.fu.account.domain.exception.EmailAlreadyExistsException;
import com.open_f.fu.account.domain.exception.InvalidCredentialsException;
import com.open_f.fu.account.domain.exception.UserNotFoundException;
import com.open_f.fu.account.domain.interfaces.PasswordHasher;
import com.open_f.fu.account.domain.interfaces.UserRepository;
import com.open_f.fu.account.domain.model.User;
import com.open_f.fu.account.service.command.RegisterCommand;
import com.open_f.fu.shared.security.JwtService;

import jakarta.inject.Inject;
import jakarta.enterprise.context.Dependent;
import jakarta.transaction.Transactional;

@Dependent
@Transactional
public class UserService {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;

    @Inject
    public UserService(UserRepository users, PasswordHasher passwordHasher,
                       JwtService jwtService) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
    }

    public User register(RegisterCommand cmd) {
        User.validateRawPassword(cmd.password());
        String email = User.normalizeEmail(cmd.email());

        if (users.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = User.create(email, passwordHasher.hash(cmd.password()), cmd.name(), cmd.lastName());
        return users.save(user);
    }

    // Devuelve el JWT del usuario autenticado
    public String login(String rawEmail, String rawPassword) {
        if (rawEmail == null || rawPassword == null) {
            throw new InvalidCredentialsException();
        }
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);
        User user = users.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return jwtService.generateToken(user.getId(), user.getEmail());
    }

    public User current(Long userId) {
        return users.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    // Lo usan otros módulos (workspaces, chats) en vez de tocar UserRepository
    public boolean exists(Long userId) {
        return users.existsById(userId);
    }
}
