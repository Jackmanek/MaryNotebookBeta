package com.maryNotebook.maryNotebook.usuario.service;

import com.maryNotebook.maryNotebook.usuario.entity.PasswordResetToken;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import com.maryNotebook.maryNotebook.usuario.entity.VerificationToken;
import com.maryNotebook.maryNotebook.usuario.repository.PasswordResetTokenRepository;
import com.maryNotebook.maryNotebook.usuario.repository.UsuarioRepository;
import com.maryNotebook.maryNotebook.usuario.repository.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
        /*
        *


        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("marymemories@marymemories.es");
        //message.setFrom("age002@gmail.com");
        message.setTo(email);
        message.setSubject("Activación de cuenta - MaryMemories");

        // IMPORTANTE: Asegúrate de que la URL sea la de tu backend
        // Si estás en local sería: http://localhost:8080/api/activate?token=
        // Si es en producción: https://marymemories.es

        String urlActivacion = "https://api.marymemories.es/api/activate?token=" + token;
        //String urlActivacion = "http://localhost:8080/api/activate?token=" + token;

        message.setText("Haz clic aquí para activar tu cuenta: " + "<a href='" + urlActivacion + "'>haz clic aquí</a>.</p>");
        mailSender.send(message);
        *
        *
        * */

        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,  "UTF-8");
            helper.setFrom("marymemories@marymemories.es");
            helper.setTo(email);
            helper.setSubject("Activa tu cuenta | MaryMemories");
            String urlActivacion = "https://api.marymemories.es/api/activate?token=" + token;
            //String urlActivacion = "http://localhost:8080/api/activate?token=" + token;
            String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f8f9fa;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f8f9fa; padding: 40px 20px;">
                    <tr>
                        <td align="center">
                            <table width="100%%" style="max-width: 480px; background-color: #ffffff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
                                
                                <!-- Header -->
                                <tr>
                                    <td style="padding: 40px 40px 30px; text-align: center; border-bottom: 1px solid #f0f0f0;">
                                        <h1 style="margin: 0; font-size: 24px; font-weight: 600; color: #1a1a1a; letter-spacing: -0.5px;">
                                            MaryMemories
                                        </h1>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px;">
                                        <p style="margin: 0 0 24px; font-size: 15px; line-height: 1.6; color: #374151;">
                                            Gracias por registrarte. Para completar tu registro y comenzar a crear recuerdos únicos, activa tu cuenta.
                                        </p>
                                        
                                        <!-- Button -->
                                        <table width="100%%" cellpadding="0" cellspacing="0">
                                            <tr>
                                                <td align="center" style="padding: 8px 0 32px;">
                                                    <a href="%s" 
                                                       style="display: inline-block; padding: 14px 32px; background-color: #1a1a1a; color: #ffffff; text-decoration: none; font-size: 14px; font-weight: 500; border-radius: 8px; letter-spacing: 0.3px;">
                                                        Activar mi cuenta
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <p style="margin: 0; font-size: 13px; line-height: 1.6; color: #9ca3af;">
                                            Si no solicitaste esta cuenta, puedes ignorar este correo.
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="padding: 24px 40px; background-color: #fafafa; border-radius: 0 0 12px 12px;">
                                        <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                                            © 2026 MaryMemories · Todos los derechos reservados
                                        </p>
                                    </td>
                                </tr>
                                
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(urlActivacion);

            helper.setText(htmlContent, true); // true = es HTML

            mailSender.send(mimeMessage);
        }catch(MessagingException e){
            throw new RuntimeException("Error al enviar email de activación", e);
        }

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
        /*SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("marymemories@marymemories.es");
        //message.setFrom("age002@gmail.com");
        message.setTo(email);
        message.setSubject("Recuperación de contraseña - MaryMemories");

        String urlReset = "https://api.marymemories.es/api/reset-password?token=" + token;
        //String urlReset = "http://localhost:8080/api/reset-password?token=" + token;
        message.setText("Haz clic aquí para restablecer tu contraseña: " + urlReset +
                "\n\nEste enlace expira en 30 minutos.");
        mailSender.send(message);
        */
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true ,"UTF-8" );
            helper.setFrom("marymemories@marymemories.es");
            helper.setTo(email);
            helper.setSubject("Recuperación de contraseña  | MaryMemories");
            String urlReset = "https://api.marymemories.es/api/reset-password?token=" + token;
            //String urlReset = "http://localhost:8080/api/reset-password?token=" + token;
            String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f8f9fa;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f8f9fa; padding: 40px 20px;">
                    <tr>
                        <td align="center">
                            <table width="100%%" style="max-width: 480px; background-color: #ffffff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
                                
                                <!-- Header -->
                                <tr>
                                    <td style="padding: 40px 40px 30px; text-align: center; border-bottom: 1px solid #f0f0f0;">
                                        <h1 style="margin: 0; font-size: 24px; font-weight: 600; color: #1a1a1a; letter-spacing: -0.5px;">
                                            MaryMemories
                                        </h1>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px;">
                                        <p style="margin: 0 0 24px; font-size: 15px; line-height: 1.6; color: #374151;">
                                            Has recibido este email porque hemos recibido una solicitud para restablecer la contraseña de tu cuenta.
                                        </p>
                                        
                                        <!-- Button -->
                                        <table width="100%%" cellpadding="0" cellspacing="0">
                                            <tr>
                                                <td align="center" style="padding: 8px 0 32px;">
                                                    <a href="%s" 
                                                       style="display: inline-block; padding: 14px 32px; background-color: #1a1a1a; color: #ffffff; text-decoration: none; font-size: 14px; font-weight: 500; border-radius: 8px; letter-spacing: 0.3px;">
                                                        Restablecer contraseña
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <p style="margin: 0; font-size: 13px; line-height: 1.6; color: #9ca3af;">
                                            Este enlace expira en 30 minutos. Si no solicitaste restablecer tu contraseña, puedes ignorar este correo.
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="padding: 24px 40px; background-color: #fafafa; border-radius: 0 0 12px 12px;">
                                        <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                                            © 2026 MaryMemories · Todos los derechos reservados
                                        </p>
                                    </td>
                                </tr>
                                
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
                    """.formatted(urlReset);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);

        }catch(MessagingException e){
            throw new RuntimeException("Error al enviar email de recuperacion", e);
        }


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
