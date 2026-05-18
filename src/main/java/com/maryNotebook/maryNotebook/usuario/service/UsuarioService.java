package com.maryNotebook.maryNotebook.usuario.service;

import com.maryNotebook.maryNotebook.usuario.entity.PasswordResetToken;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import com.maryNotebook.maryNotebook.usuario.entity.VerificationToken;
import com.maryNotebook.maryNotebook.usuario.repository.PasswordResetTokenRepository;
import com.maryNotebook.maryNotebook.usuario.repository.UsuarioRepository;
import com.maryNotebook.maryNotebook.usuario.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {


    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordResetTokenRepository passwordResetTokenRepository;


    public Usuario registrarUsuario(Usuario usuario) {
        usuario.setActivo(false);
        usuario.setFechaRegistro(LocalDateTime.now());
        //Ciframos contraseña antes de guardar
        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );
        usuario.setRol(Usuario.Rol.USER); // rol por defecto
        Usuario u = usuarioRepository.save(usuario);

        String token = UUID.randomUUID().toString();
        verificationTokenRepository.save(new VerificationToken(token, u));
        enviarEmailActivacion(u.getEmail(), token);

        return u;
    }

    private void enviarEmailActivacion(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("marymemories@marymemories.com");
        message.setTo(email);
        message.setSubject("Activación de cuenta - MaryMemories");

        // IMPORTANTE: Asegúrate de que la URL sea la de tu backend
        // Si estás en local sería: http://localhost:8080/api/activate?token=
        // Si es en producción: https://marymemories.es

        String urlActivacion = "https://api.marymemories.es/api/activate?token=" + token;

        message.setText("Haz clic aquí para activar tu cuenta: " + urlActivacion);
        mailSender.send(message);
    }

    public boolean activarCuenta(String token) {
        return verificationTokenRepository.findByToken(token)
                .filter(vToken -> vToken.getFechaExpiracion().isAfter(LocalDateTime.now()))
                .map(vToken -> {
                    Usuario usuario = vToken.getUsuario();
                    usuario.setActivo(true);
                    usuarioRepository.save(usuario);
                    verificationTokenRepository.delete(vToken);
                    return true;
                }).orElse(false);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }


    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }


    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> toggleEstado(Long id) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setActivo(!usuario.isActivo());
            return usuarioRepository.save(usuario);
        });
    }

    public void solicitarRecuperacion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email no encontrado"));

        // Eliminar tokens anteriores del mismo usuario para evitar duplicados
        passwordResetTokenRepository.deleteByUsuario(usuario);

        String token = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(new PasswordResetToken(token, usuario));

        enviarEmailRecuperacion(email, token);
    }

    private void enviarEmailRecuperacion(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("marymemories@marymemories.com");
        message.setTo(email);
        message.setSubject("Recuperación de contraseña - MaryMemories");

        String urlReset = "https://api.marymemories.es/api/reset-password?token=" + token;
        message.setText("Haz clic aquí para restablecer tu contraseña: " + urlReset +
                "\n\nEste enlace expira en 30 minutos.");
        mailSender.send(message);
    }

    public boolean resetearPassword(String token, String nuevaPassword) {
        return passwordResetTokenRepository.findByToken(token)
                .filter(t -> t.getFechaExpiracion().isAfter(LocalDateTime.now()))
                .map(t -> {
                    Usuario usuario = t.getUsuario();
                    usuario.setPassword(passwordEncoder.encode(nuevaPassword));
                    usuarioRepository.save(usuario);
                    passwordResetTokenRepository.delete(t); // invalidar token usado
                    return true;
                }).orElse(false);
    }

    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    public long contarUsuariosActivos() {
        return usuarioRepository.countByActivoTrue();
    }
}
