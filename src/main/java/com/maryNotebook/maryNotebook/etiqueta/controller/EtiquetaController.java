package com.maryNotebook.maryNotebook.etiqueta.controller;

import com.maryNotebook.maryNotebook.etiqueta.entity.Etiqueta;
import com.maryNotebook.maryNotebook.etiqueta.service.EtiquetaService;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import com.maryNotebook.maryNotebook.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etiquetas")
@RequiredArgsConstructor
public class EtiquetaController {

    private final EtiquetaService etiquetaService;
    private final UsuarioRepository usuarioRepository;

    // 🔹 Listar todas las etiquetas disponibles (global)
    @GetMapping
    public ResponseEntity<List<Etiqueta>> listarTodas() {
        return ResponseEntity.ok(etiquetaService.listarEtiquetas());
    }

    // 🔹 Listar etiquetas de un usuario (las que tienen recuerdos)
    @GetMapping("/usuario")
    public ResponseEntity<List<Etiqueta>> listarPorUsuario(Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        return ResponseEntity.ok(etiquetaService.listarEtiquetasPorUsuario(usuario.getId()));
    }

    // 🔹 Crear una nueva etiqueta (si no existe)
    @PostMapping
    public ResponseEntity<Etiqueta> crearEtiqueta(@RequestParam String nombre) {
        Etiqueta etiqueta = etiquetaService.crearEtiqueta(nombre);
        return ResponseEntity.ok(etiqueta);
    }
}
