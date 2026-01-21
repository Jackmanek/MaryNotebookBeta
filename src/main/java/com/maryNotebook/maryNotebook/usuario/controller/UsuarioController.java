package com.maryNotebook.maryNotebook.usuario.controller;

import com.maryNotebook.maryNotebook.recuerdo.dto.RecuerdoTimelineDTO;
import com.maryNotebook.maryNotebook.recuerdo.service.RecuerdoService;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import com.maryNotebook.maryNotebook.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {


    private final UsuarioService usuarioService;
    private final RecuerdoService recuerdoService;


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> obtenerPorEmail(@PathVariable String email) {
        return usuarioService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle-estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> toggleEstadoUsuario(@PathVariable Long id) {
        return usuarioService.toggleEstado(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/recuerdos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RecuerdoTimelineDTO>> obtenerRecuerdosUsuario(@PathVariable Long id) {
        List<RecuerdoTimelineDTO> recuerdos = recuerdoService.obtenerRecuerdosPorUsuario(id);
        return ResponseEntity.ok(recuerdos);
    }

    @GetMapping("/admin/estadisticas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsuarios", usuarioService.contarUsuarios());
        stats.put("usuariosActivos", usuarioService.contarUsuariosActivos());
        stats.put("totalRecuerdos", recuerdoService.contarRecuerdos());
        stats.put("recuerdosPublicos", recuerdoService.contarRecuerdosPublicos());
        return ResponseEntity.ok(stats);
    }
}
