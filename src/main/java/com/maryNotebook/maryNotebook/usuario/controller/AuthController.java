package com.maryNotebook.maryNotebook.usuario.controller;

import com.maryNotebook.maryNotebook.security.JwtUtil;
import com.maryNotebook.maryNotebook.usuario.dto.ForgotPasswordDTO;
import com.maryNotebook.maryNotebook.usuario.dto.RegistroUsuarioDTO;
import com.maryNotebook.maryNotebook.usuario.dto.ResetPasswordDTO;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import com.maryNotebook.maryNotebook.usuario.repository.UsuarioRepository;
import com.maryNotebook.maryNotebook.usuario.service.UsuarioService;
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

        String urlActivacion = "https://www.marymemories.es/api/activate?token=" + token;
        String urlLogin = "https://www.marymemories.es/login";
        //String urlLogin = "http://localhost:8100/login";

        if (activado) {
            return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="refresh" content="5;url=%s">
                <title>Cuenta Activada | MaryMemories</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background-color: #f8f9fa;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        padding: 20px;
                    }
                    .card {
                        background: #ffffff;
                        border-radius: 12px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.06);
                        max-width: 420px;
                        width: 100%%;
                        overflow: hidden;
                    }
                    .header {
                        padding: 32px 32px 24px;
                        text-align: center;
                        border-bottom: 1px solid #f0f0f0;
                    }
                    .logo {
                        font-size: 22px;
                        font-weight: 600;
                        color: #1a1a1a;
                        letter-spacing: -0.5px;
                    }
                    .content {
                        padding: 32px;
                        text-align: center;
                    }
                    .icon {
                        width: 56px;
                        height: 56px;
                        background-color: #ecfdf5;
                        border-radius: 50%%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        margin: 0 auto 20px;
                    }
                    .icon svg {
                        width: 28px;
                        height: 28px;
                        color: #10b981;
                    }
                    h1 {
                        font-size: 20px;
                        font-weight: 600;
                        color: #1a1a1a;
                        margin-bottom: 12px;
                    }
                    .message {
                        font-size: 15px;
                        line-height: 1.6;
                        color: #374151;
                        margin-bottom: 24px;
                    }
                    .countdown {
                        font-size: 13px;
                        color: #9ca3af;
                        margin-bottom: 16px;
                    }
                    .link {
                        display: inline-block;
                        padding: 12px 28px;
                        background-color: #1a1a1a;
                        color: #ffffff;
                        text-decoration: none;
                        font-size: 14px;
                        font-weight: 500;
                        border-radius: 8px;
                        transition: background-color 0.2s;
                    }
                    .link:hover {
                        background-color: #333333;
                    }
                    .footer {
                        padding: 20px 32px;
                        background-color: #fafafa;
                        text-align: center;
                    }
                    .footer p {
                        font-size: 12px;
                        color: #9ca3af;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <span class="logo">MaryMemories</span>
                    </div>
                    <div class="content">
                        <div class="icon">
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                            </svg>
                        </div>
                        <h1>Cuenta activada con exito</h1>
                        <p class="message">Tu cuenta ha sido verificada correctamente. Ya puedes acceder a todos los servicios de MaryMemories.</p>
                        <p class="countdown">Redirigiendo en 5 segundos...</p>
                        <a href="%s" class="link">Ir al inicio de sesion</a>
                    </div>
                    <div class="footer">
                        <p>© 2026 MaryMemories · Todos los derechos reservados</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(urlLogin, urlLogin);
        } else {
            return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Error de Activacion | MaryMemories</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background-color: #f8f9fa;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        padding: 20px;
                    }
                    .card {
                        background: #ffffff;
                        border-radius: 12px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.06);
                        max-width: 420px;
                        width: 100%%;
                        overflow: hidden;
                    }
                    .header {
                        padding: 32px 32px 24px;
                        text-align: center;
                        border-bottom: 1px solid #f0f0f0;
                    }
                    .logo {
                        font-size: 22px;
                        font-weight: 600;
                        color: #1a1a1a;
                        letter-spacing: -0.5px;
                    }
                    .content {
                        padding: 32px;
                        text-align: center;
                    }
                    .icon {
                        width: 56px;
                        height: 56px;
                        background-color: #fef2f2;
                        border-radius: 50%%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        margin: 0 auto 20px;
                    }
                    .icon svg {
                        width: 28px;
                        height: 28px;
                        color: #ef4444;
                    }
                    h1 {
                        font-size: 20px;
                        font-weight: 600;
                        color: #1a1a1a;
                        margin-bottom: 12px;
                    }
                    .message {
                        font-size: 15px;
                        line-height: 1.6;
                        color: #374151;
                        margin-bottom: 24px;
                    }
                    .hint {
                        font-size: 13px;
                        color: #9ca3af;
                        margin-bottom: 24px;
                    }
                    .link {
                        display: inline-block;
                        padding: 12px 28px;
                        background-color: #1a1a1a;
                        color: #ffffff;
                        text-decoration: none;
                        font-size: 14px;
                        font-weight: 500;
                        border-radius: 8px;
                        transition: background-color 0.2s;
                    }
                    .link:hover {
                        background-color: #333333;
                    }
                    .footer {
                        padding: 20px 32px;
                        background-color: #fafafa;
                        text-align: center;
                    }
                    .footer p {
                        font-size: 12px;
                        color: #9ca3af;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <span class="logo">MaryMemories</span>
                    </div>
                    <div class="content">
                        <div class="icon">
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </div>
                        <h1>Error de activacion</h1>
                        <p class="message">El enlace de activacion es invalido o ha expirado.</p>
                        <p class="hint">Si crees que esto es un error, solicita un nuevo enlace de activacion.</p>
                        <a href="%s" class="link">Volver al inicio</a>
                    </div>
                    <div class="footer">
                        <p>© 2026 MaryMemories · Todos los derechos reservados</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(urlLogin);
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
