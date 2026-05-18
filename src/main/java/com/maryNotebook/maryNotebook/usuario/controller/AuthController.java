package com.maryNotebook.maryNotebook.usuario.controller;

import com.maryNotebook.maryNotebook.security.JwtUtil;
import com.maryNotebook.maryNotebook.usuario.dto.ForgotPasswordDTO;
import com.maryNotebook.maryNotebook.usuario.dto.RegistroUsuarioDTO;
import com.maryNotebook.maryNotebook.usuario.dto.ResetPasswordDTO;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import com.maryNotebook.maryNotebook.usuario.repository.UsuarioRepository;
import com.maryNotebook.maryNotebook.usuario.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.awt.*;

@RestController
@RequestMapping("/api")
public class AuthController {


    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UsuarioService usuarioService, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegistroUsuarioDTO dto) {
        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setEmail(dto.getEmail());
        u.setPassword(dto.getPassword());
        usuarioService.registrarUsuario(u);
        return "Usuario registrado correctamente";
    }


    @PostMapping("/login")
    public String login(@RequestBody RegistroUsuarioDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        Usuario u = usuarioRepository.findByEmail(dto.getEmail()).orElseThrow();
        return jwtUtil.generarToken(u.getEmail(), u.getRol().name(), u.getNombre());
    }

    @GetMapping(value = "/activate", produces = MediaType.TEXT_HTML_VALUE)
    public String activar(@RequestParam String token) {
        boolean activado = usuarioService.activarCuenta(token);

        String urlActivacion = "https://api.marymemories.es/api/activate?token=" + token;
        String urlLogin = "https://api.marymemories.es/login";

        if(activado) {
            return "<html>" +
                    "<head>" +
                    "  <meta http-equiv='refresh' content='5;url=" + urlLogin + "' />" +
                    "  <style>" +
                    "    body { font-family: sans-serif; text-align: center; padding-top: 50px; }" +
                    "    .card { border: 1px solid #ddd; padding: 20px; display: inline-block; border-radius: 10px; }" +
                    "  </style>" +
                    "</head>" +
                    "<body>" +
                    "  <div class='card'>" +
                    "    <h1 style='color: #28a745;'>¡Cuenta activada con éxito!</h1>" +
                    "    <p>En 5 segundos serás redirigido automáticamente al inicio de sesión...</p>" +
                    "    <p>Si no ocurre nada, <a href='" + urlLogin + "'>haz clic aquí</a>.</p>" +
                    "  </div>" +
                    "</body>" +
                    "</html>";
        }else {
            return "<h1>Error de activación</h1><p>El token es inválido o ya ha caducado.</p>";
        }
    }

    // Paso 1 — el usuario manda su email
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordDTO dto) {
        try {
            usuarioService.solicitarRecuperacion(dto.getEmail());
            // Respuesta genérica por seguridad — no confirmes si el email existe o no
            return ResponseEntity.ok("Si el email existe, recibirás un enlace de recuperación.");
        } catch (Exception e) {
            // Misma respuesta aunque falle — evita enumerar emails válidos
            return ResponseEntity.ok("Si el email existe, recibirás un enlace de recuperación.");
        }
    }

    // Paso 2 — el usuario manda el token + nueva contraseña
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDTO dto) {
        boolean ok = usuarioService.resetearPassword(dto.getToken(), dto.getNuevaPassword());
        if (ok) {
            return ResponseEntity.ok("Contraseña actualizada correctamente.");
        } else {
            return ResponseEntity.badRequest().body("Token inválido o expirado.");
        }
    }
}
