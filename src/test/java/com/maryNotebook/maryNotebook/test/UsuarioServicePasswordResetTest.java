package com.maryNotebook.maryNotebook.test;

import com.maryNotebook.maryNotebook.usuario.entity.PasswordResetToken;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import com.maryNotebook.maryNotebook.usuario.repository.PasswordResetTokenRepository;
import com.maryNotebook.maryNotebook.usuario.repository.UsuarioRepository;
import com.maryNotebook.maryNotebook.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServicePasswordResetTest {


    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JavaMailSender mailSender;
    @InjectMocks
    private UsuarioService usuarioService;


    private Usuario usuarioActivo;

    @BeforeEach
    void setUp() {
        usuarioActivo = new Usuario();
        usuarioActivo.setId(1L);
        usuarioActivo.setNombre("Mary Notebook");
        usuarioActivo.setEmail("maria@test.com");
        usuarioActivo.setPassword("hashedPassword");
        usuarioActivo.setActivo(true);
        usuarioActivo.setRol(Usuario.Rol.USER);
    }

    @Nested
    @DisplayName("solicitarRecuperacion()")
    class SolicitarRecuperacion {

        @Test
        @DisplayName("Gebnera token, lo persiste y envia email cuando el usuario existe")
        void deberiaGenerarTokenYEnviarEmail_cuandoUsuarioExiste() {
            // Arrange
            when(usuarioRepository.findByEmail("maria@test.com"))
                    .thenReturn(Optional.of(usuarioActivo));

            // Act
            usuarioService.solicitarRecuperacion("maria@test.com");

            // Assert — se eliminan tokens anteriores
            verify(passwordResetTokenRepository).deleteByUsuario(usuarioActivo);

            // Assert — se guarda un nuevo token
            ArgumentCaptor<PasswordResetToken> tokenCaptor =
                    ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(passwordResetTokenRepository).save(tokenCaptor.capture());

            PasswordResetToken tokenGuardado = tokenCaptor.getValue();
            assertThat(tokenGuardado.getToken()).isNotBlank();
            assertThat(tokenGuardado.getUsuario()).isEqualTo(usuarioActivo);
            assertThat(tokenGuardado.getFechaExpiracion()).isAfter(LocalDateTime.now());

            // Assert — se envía el email
            ArgumentCaptor<SimpleMailMessage> mailCaptor =
                    ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(mailCaptor.capture());

            SimpleMailMessage mail = mailCaptor.getValue();
            assertThat(mail.getTo()).contains("maria@test.com");
            assertThat(mail.getSubject()).containsIgnoringCase("recuperación");
            assertThat(mail.getText()).contains("reset-password?token=");
        }

        @Test
        @DisplayName("Lanza excepción cuando el email no existe")
        void deberiaLanzarExcepcion_cuandoEmailNoExiste() {
            // Arrange
            when(usuarioRepository.findByEmail("noexiste@test.com"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> usuarioService.solicitarRecuperacion("noexiste@test.com"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Email no encontrado");

            // No se debe guardar nada ni enviar email
            verify(passwordResetTokenRepository, never()).save(any());
            verify(mailSender, never()).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Elimina tokens previos antes de generar uno nuevo")
        void deberiaEliminarTokensPrevios_antesDeGenerarUnoNuevo() {
            // Arrange
            when(usuarioRepository.findByEmail("maria@test.com"))
                    .thenReturn(Optional.of(usuarioActivo));

            // Act
            usuarioService.solicitarRecuperacion("maria@test.com");

            // Assert — el delete ocurre ANTES del save (orden de invocación)
            var inOrder = inOrder(passwordResetTokenRepository);
            inOrder.verify(passwordResetTokenRepository).deleteByUsuario(usuarioActivo);
            inOrder.verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        }

        @Test
        @DisplayName("El token generado expira en aproximadamente 30 minutos")
        void tokenDeberiaExpirarEn30Minutos() {
            // Arrange
            when(usuarioRepository.findByEmail("maria@test.com"))
                    .thenReturn(Optional.of(usuarioActivo));

            // Act
            usuarioService.solicitarRecuperacion("maria@test.com");

            // Assert
            ArgumentCaptor<PasswordResetToken> captor =
                    ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(passwordResetTokenRepository).save(captor.capture());

            LocalDateTime expiracion = captor.getValue().getFechaExpiracion();
            LocalDateTime ahora = LocalDateTime.now();

            // Debe expirar entre 29 y 31 minutos desde ahora (margen de 1 min)
            assertThat(expiracion).isBetween(
                    ahora.plusMinutes(29),
                    ahora.plusMinutes(31)
            );
        }
    }

    // =========================================================================
    // resetearPassword()
    // =========================================================================

    @Nested
    @DisplayName("resetearPassword()")
    class ResetearPassword {

        @Test
        @DisplayName("Actualiza la contraseña y elimina el token cuando el token es válido")
        void deberiaActualizarPasswordYEliminarToken_cuandoTokenValido() {
            // Arrange
            PasswordResetToken tokenValido = new PasswordResetToken("token-valido", usuarioActivo);
            // La expiración la pone el constructor (+30min), así que es válido

            when(passwordResetTokenRepository.findByToken("token-valido"))
                    .thenReturn(Optional.of(tokenValido));
            when(passwordEncoder.encode("nuevaPass123"))
                    .thenReturn("hashedNuevaPass");

            // Act
            boolean resultado = usuarioService.resetearPassword("token-valido", "nuevaPass123");

            // Assert
            assertThat(resultado).isTrue();

            // La contraseña se cifra antes de guardar
            verify(passwordEncoder).encode("nuevaPass123");

            ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(usuarioCaptor.capture());
            assertThat(usuarioCaptor.getValue().getPassword()).isEqualTo("hashedNuevaPass");

            // El token se elimina tras el uso
            verify(passwordResetTokenRepository).delete(tokenValido);
        }

        @Test
        @DisplayName("Retorna false cuando el token no existe en BD")
        void deberiaRetornarFalse_cuandoTokenNoExiste() {
            // Arrange
            when(passwordResetTokenRepository.findByToken("token-inexistente"))
                    .thenReturn(Optional.empty());

            // Act
            boolean resultado = usuarioService.resetearPassword("token-inexistente", "pass");

            // Assert
            assertThat(resultado).isFalse();
            verify(usuarioRepository, never()).save(any());
            verify(passwordResetTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Retorna false cuando el token ha expirado")
        void deberiaRetornarFalse_cuandoTokenExpirado() {
            // Arrange — token con fecha de expiración en el pasado
            PasswordResetToken tokenExpirado = new PasswordResetToken("token-expirado", usuarioActivo);
            tokenExpirado.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));

            when(passwordResetTokenRepository.findByToken("token-expirado"))
                    .thenReturn(Optional.of(tokenExpirado));

            // Act
            boolean resultado = usuarioService.resetearPassword("token-expirado", "pass");

            // Assert
            assertThat(resultado).isFalse();
            verify(usuarioRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("No guarda contraseña en texto plano — siempre se cifra")
        void deberiaGuardarPasswordCifrada_nuncarTextPlano() {
            // Arrange
            PasswordResetToken tokenValido = new PasswordResetToken("token-ok", usuarioActivo);
            when(passwordResetTokenRepository.findByToken("token-ok"))
                    .thenReturn(Optional.of(tokenValido));
            when(passwordEncoder.encode("miPasswordNueva"))
                    .thenReturn("$2a$10$hashedvalue");

            // Act
            usuarioService.resetearPassword("token-ok", "miPasswordNueva");

            // Assert — la contraseña guardada NO es el texto plano
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword())
                    .isNotEqualTo("miPasswordNueva")
                    .startsWith("$2a$"); // formato BCrypt
        }

        @Test
        @DisplayName("El token se invalida tras un reset exitoso — no puede usarse dos veces")
        void tokenDeberiaInvalidarseTrasResetExitoso() {
            // Arrange
            PasswordResetToken token = new PasswordResetToken("token-unico", usuarioActivo);
            when(passwordResetTokenRepository.findByToken("token-unico"))
                    .thenReturn(Optional.of(token));
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");

            // Act
            usuarioService.resetearPassword("token-unico", "pass");

            // Assert — se llama a delete exactamente una vez
            verify(passwordResetTokenRepository, times(1)).delete(token);
        }
    }

}
