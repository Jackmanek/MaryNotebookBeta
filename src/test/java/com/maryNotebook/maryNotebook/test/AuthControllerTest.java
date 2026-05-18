package com.maryNotebook.maryNotebook.test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maryNotebook.maryNotebook.security.JwtUtil;
import com.maryNotebook.maryNotebook.usuario.controller.AuthController;
import com.maryNotebook.maryNotebook.usuario.dto.ForgotPasswordDTO;
import com.maryNotebook.maryNotebook.usuario.dto.RegistroUsuarioDTO;
import com.maryNotebook.maryNotebook.usuario.dto.ResetPasswordDTO;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import com.maryNotebook.maryNotebook.usuario.repository.UsuarioRepository;
import com.maryNotebook.maryNotebook.usuario.service.CustomUserDetailsService;
import com.maryNotebook.maryNotebook.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {


    // -------------------------------------------------------------------------
    // Infraestructura
    // -------------------------------------------------------------------------

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Mocks — todos los beans que inyecta AuthController
    // -------------------------------------------------------------------------

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private Usuario usuarioFijo;

    @BeforeEach
    void setUp() {
        usuarioFijo = new Usuario();
        usuarioFijo.setId(1L);
        usuarioFijo.setNombre("María");
        usuarioFijo.setEmail("maria@test.com");
        usuarioFijo.setPassword("hashedPassword");
        usuarioFijo.setActivo(true);
        usuarioFijo.setRol(Usuario.Rol.USER);
    }

    // =========================================================================
    // POST /api/forgot-password
    // =========================================================================

    @Nested
    @DisplayName("POST /api/forgot-password")
    class ForgotPassword {

        @Test
        @WithMockUser
        @DisplayName("Retorna 200 con mensaje genérico cuando el email existe")
        void deberiaRetornar200_cuandoEmailExiste() throws Exception {
            // Arrange
            doNothing().when(usuarioService).solicitarRecuperacion("maria@test.com");

            ForgotPasswordDTO dto = new ForgotPasswordDTO();
            dto.setEmail("maria@test.com");

            // Act & Assert
            mockMvc.perform(post("/api/forgot-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(
                            "Si el email existe, recibirás un enlace de recuperación."));

            verify(usuarioService).solicitarRecuperacion("maria@test.com");
        }

        @Test
        @WithMockUser
        @DisplayName("Retorna 200 con el MISMO mensaje genérico cuando el email NO existe (seguridad)")
        void deberiaRetornar200Generico_cuandoEmailNoExiste() throws Exception {
            // Arrange — simula que el service lanza excepción (email no encontrado)
            doThrow(new RuntimeException("Email no encontrado"))
                    .when(usuarioService).solicitarRecuperacion("noexiste@test.com");

            ForgotPasswordDTO dto = new ForgotPasswordDTO();
            dto.setEmail("noexiste@test.com");

            // Act & Assert — la respuesta debe ser IDÉNTICA para no revelar si el email existe
            mockMvc.perform(post("/api/forgot-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(
                            "Si el email existe, recibirás un enlace de recuperación."));
        }

        @Test
        @WithMockUser
        @DisplayName("Nunca expone si un email está o no registrado (anti-enumeración)")
        void nuncaDeberiaExponerSiEmailEstaRegistrado() throws Exception {
            // Este test verifica que ante éxito y fallo la respuesta HTTP es igual
            ForgotPasswordDTO dto = new ForgotPasswordDTO();
            dto.setEmail("cualquiera@test.com");

            // Caso éxito
            doNothing().when(usuarioService).solicitarRecuperacion(anyString());
            String respuestaExito = mockMvc.perform(post("/api/forgot-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andReturn().getResponse().getContentAsString();

            // Caso fallo
            doThrow(new RuntimeException()).when(usuarioService).solicitarRecuperacion(anyString());
            String respuestaFallo = mockMvc.perform(post("/api/forgot-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andReturn().getResponse().getContentAsString();

            // Ambas respuestas deben ser idénticas
            org.assertj.core.api.Assertions.assertThat(respuestaExito).isEqualTo(respuestaFallo);
        }
    }

    // =========================================================================
    // POST /api/reset-password
    // =========================================================================

    @Nested
    @DisplayName("POST /api/reset-password")
    class ResetPassword {

        @Test
        @WithMockUser
        @DisplayName("Retorna 200 cuando el token es válido y la contraseña se actualiza")
        void deberiaRetornar200_cuandoTokenValido() throws Exception {
            // Arrange
            when(usuarioService.resetearPassword("token-valido", "nuevaPass123"))
                    .thenReturn(true);

            ResetPasswordDTO dto = new ResetPasswordDTO();
            dto.setToken("token-valido");
            dto.setNuevaPassword("nuevaPass123");

            // Act & Assert
            mockMvc.perform(post("/api/reset-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Contraseña actualizada correctamente."));
        }

        @Test
        @WithMockUser
        @DisplayName("Retorna 400 cuando el token es inválido o expirado")
        void deberiaRetornar400_cuandoTokenInvalido() throws Exception {
            // Arrange
            when(usuarioService.resetearPassword("token-malo", "pass"))
                    .thenReturn(false);

            ResetPasswordDTO dto = new ResetPasswordDTO();
            dto.setToken("token-malo");
            dto.setNuevaPassword("pass");

            // Act & Assert
            mockMvc.perform(post("/api/reset-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Token inválido o expirado."));
        }

        @Test
        @WithMockUser
        @DisplayName("Retorna 400 cuando el token ha expirado")
        void deberiaRetornar400_cuandoTokenExpirado() throws Exception {
            // Arrange — mismo comportamiento que token inválido desde el controller
            when(usuarioService.resetearPassword("token-expirado", "pass"))
                    .thenReturn(false);

            ResetPasswordDTO dto = new ResetPasswordDTO();
            dto.setToken("token-expirado");
            dto.setNuevaPassword("pass");

            // Act & Assert
            mockMvc.perform(post("/api/reset-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // POST /api/register
    // =========================================================================

    @Nested
    @DisplayName("POST /api/register")
    class Register {

        @Test
        @WithMockUser
        @DisplayName("Retorna 200 y mensaje de éxito al registrar un usuario nuevo")
        void deberiaRetornar200_cuandoRegistroExitoso() throws Exception {
            // Arrange
            when(usuarioService.registrarUsuario(any(Usuario.class)))
                    .thenReturn(usuarioFijo);

            RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
            dto.setNombre("María");
            dto.setEmail("maria@test.com");
            dto.setPassword("password123");

            // Act & Assert
            mockMvc.perform(post("/api/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Usuario registrado correctamente"));
        }

        @Test
        @WithMockUser
        @DisplayName("Llama a registrarUsuario con los datos del DTO correctamente mapeados")
        void deberiaMapearDTOAUsuarioCorrectamente() throws Exception {
            // Arrange
            when(usuarioService.registrarUsuario(any(Usuario.class)))
                    .thenReturn(usuarioFijo);

            RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
            dto.setNombre("María");
            dto.setEmail("maria@test.com");
            dto.setPassword("password123");

            // Act
            mockMvc.perform(post("/api/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert — verificamos que se llama con un Usuario que tiene los campos del DTO
            verify(usuarioService).registrarUsuario(argThat(u ->
                    u.getNombre().equals("María") &&
                            u.getEmail().equals("maria@test.com") &&
                            u.getPassword().equals("password123")
            ));
        }
    }

    // =========================================================================
    // POST /api/login
    // =========================================================================

    @Nested
    @DisplayName("POST /api/login")
    class Login {

        @Test
        @WithMockUser
        @DisplayName("Retorna JWT cuando las credenciales son correctas")
        void deberiaRetornarJwt_cuandoCredencialesCorrectas() throws Exception {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null); // authenticate() no devuelve nada relevante aquí
            when(usuarioRepository.findByEmail("maria@test.com"))
                    .thenReturn(Optional.of(usuarioFijo));
            when(jwtUtil.generarToken("maria@test.com", "USER", "María"))
                    .thenReturn("jwt-token-generado");

            RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
            dto.setEmail("maria@test.com");
            dto.setPassword("password123");

            // Act & Assert
            mockMvc.perform(post("/api/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("jwt-token-generado"));
        }

        @Test
        @WithMockUser
        @DisplayName("Retorna 401 cuando las credenciales son incorrectas")
        void deberiaRetornar401_cuandoCredencialesIncorrectas() throws Exception {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

            RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
            dto.setEmail("maria@test.com");
            dto.setPassword("passwordMala");

            // Act & Assert
            mockMvc.perform(post("/api/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        @DisplayName("Retorna 401 cuando el usuario está desactivado")
        void deberiaRetornar401_cuandoUsuarioDesactivado() throws Exception {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new DisabledException("Usuario desactivado"));

            RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
            dto.setEmail("maria@test.com");
            dto.setPassword("password123");

            // Act & Assert
            mockMvc.perform(post("/api/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /api/activate
    // =========================================================================

    @Nested
    @DisplayName("GET /api/activate")
    class Activate {

        @Test
        @WithMockUser
        @DisplayName("Retorna HTML con éxito cuando el token es válido")
        void deberiaRetornarHtmlExito_cuandoTokenValido() throws Exception {
            // Arrange
            when(usuarioService.activarCuenta("token-valido")).thenReturn(true);

            // Act & Assert
            mockMvc.perform(get("/api/activate")
                            .param("token", "token-valido"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Cuenta activada con éxito")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("localhost:8100/login")));
        }

        @Test
        @WithMockUser
        @DisplayName("Retorna HTML de error cuando el token es inválido o expirado")
        void deberiaRetornarHtmlError_cuandoTokenInvalido() throws Exception {
            // Arrange
            when(usuarioService.activarCuenta("token-malo")).thenReturn(false);

            // Act & Assert
            mockMvc.perform(get("/api/activate")
                            .param("token", "token-malo"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error de activación")));
        }

        @Test
        @WithMockUser
        @DisplayName("La respuesta exitosa incluye meta-refresh apuntando al login")
        void deberiaIncluirMetaRefreshAlLogin_cuandoActivacionExitosa() throws Exception {
            // Arrange
            when(usuarioService.activarCuenta("token-ok")).thenReturn(true);

            // Act & Assert
            mockMvc.perform(get("/api/activate")
                            .param("token", "token-ok"))
                    .andExpect(content().string(
                            org.hamcrest.Matchers.containsString("meta http-equiv='refresh'")))
                    .andExpect(content().string(
                            // La redirección NO debe apuntar al propio /activate
                            org.hamcrest.Matchers.not(
                                    org.hamcrest.Matchers.containsString("/api/activate"))));
        }
    }
}
