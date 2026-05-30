package com.example.springApp.service;

import com.example.springApp.dto.LoginRequest;
import com.example.springApp.dto.RegisterRequest;
import com.example.springApp.exception.BusinessException;
import com.example.springApp.exception.ConflictException;
import com.example.springApp.model.User;
import com.example.springApp.repository.UserRepository;
import com.example.springApp.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AuthService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    /**
     * Cadastra usuario por email/senha garantindo unicidade de email e armazenando senha criptografada.
     */
    @Transactional
    public User register(RegisterRequest request) {
        log.info("Iniciando registro para email: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Tentativa de registro com email ja existente: {}", request.email());
            throw new ConflictException("Email ja cadastrado");
        }

        User user = User.builder()
                .nome(request.nome())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        User savedUser = userRepository.save(user);
        // Dispara boas-vindas depois do commit para nao avisar usuario caso o cadastro falhe no banco.
        sendWelcomeEmailAfterCommit(savedUser);
        log.info("Usuario salvo com sucesso. ID: {}, Email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    /**
     * Valida credenciais locais e devolve o JWT usado pelo cookie HTTP-only.
     */
    public String login(LoginRequest request) {
        log.info("Iniciando validacao de login para: {}", request.email());
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Usuario nao encontrado para login: {}", request.email());
                    return new BusinessException("Email ou senha invalidos");
                });

        if (user.getPassword() == null) {
            log.warn("Usuario tentou login por senha mas possui apenas OAuth2: {}", request.email());
            throw new BusinessException("Esta conta utiliza login pelo Google. Use o botao 'Entrar com Google'.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Senha incorreta para usuario: {}", request.email());
            throw new BusinessException("Email ou senha invalidos");
        }

        log.info("Credenciais validas para usuario: {}. Gerando token...", request.email());
        return jwtService.generateToken(user.getId(), user.getEmail(), user.getNome());
    }

    /**
     * Garante que o email de boas-vindas so saia depois que a conta estiver persistida.
     */
    private void sendWelcomeEmailAfterCommit(User user) {
        Runnable sendEmail = () -> emailService.sendWelcomeEmail(user.getNome(), user.getEmail());
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // Facilita testes unitarios e usos futuros fora de transacao Spring.
            sendEmail.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // O envio real ainda e assincrono dentro do EmailService.
                sendEmail.run();
            }
        });
    }
}
