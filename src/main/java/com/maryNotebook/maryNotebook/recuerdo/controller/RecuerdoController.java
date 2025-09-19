package com.maryNotebook.maryNotebook.recuerdo.controller;

import com.maryNotebook.maryNotebook.recuerdo.dto.RecuerdoTimelineDTO;
import com.maryNotebook.maryNotebook.recuerdo.entity.Recuerdo;
import com.maryNotebook.maryNotebook.recuerdo.service.FileStorageService;
import com.maryNotebook.maryNotebook.recuerdo.service.RecuerdoService;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import com.maryNotebook.maryNotebook.usuario.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/recuerdos")
@RequiredArgsConstructor
public class RecuerdoController {


    private final RecuerdoService recuerdoService;
    private final UsuarioRepository usuarioRepository;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<Recuerdo> crearRecuerdo(@Valid @RequestBody Recuerdo recuerdo, Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        recuerdo.setUsuario(usuario);
        Recuerdo nuevo = recuerdoService.crearRecuerdo(recuerdo);
        return ResponseEntity.ok(nuevo);
    }
    @PostMapping("/con-imagen")
    public ResponseEntity<Recuerdo> crearRecuerdoConImagen(
            @RequestParam("texto") String texto,
            @RequestParam(value = "etiquetas", required = false) List<String> etiquetas,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            Authentication auth) throws IOException {

        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        Recuerdo recuerdo = new Recuerdo();
        recuerdo.setTexto(texto);
        recuerdo.setUsuario(usuario);
        if (etiquetas != null) {
            recuerdo.setEtiquetas(Set.copyOf(etiquetas));
        }

        if (imagen != null && !imagen.isEmpty()) {
            String nombreArchivo = fileStorageService.guardarArchivo(imagen);
            recuerdo.setImagen(nombreArchivo);
        }

        Recuerdo nuevo = recuerdoService.crearRecuerdo(recuerdo);
        return ResponseEntity.ok(nuevo);
    }

    @GetMapping
    public ResponseEntity<List<Recuerdo>> listarRecuerdos(Authentication auth, @RequestParam(required = false) String etiqueta) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        List<Recuerdo> recuerdos;
        if (etiqueta != null && !etiqueta.isEmpty()) {
            recuerdos = recuerdoService.listarRecuerdosPorEtiqueta(usuario, etiqueta);
        } else {
            recuerdos = recuerdoService.listarRecuerdosPorUsuario(usuario);
        }
        return ResponseEntity.ok(recuerdos);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Recuerdo> obtenerRecuerdo(@PathVariable Long id, Authentication auth) {
        Recuerdo r = recuerdoService.obtenerRecuerdoPorId(id).orElseThrow();
        if (!r.getUsuario().getEmail().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(r);
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<RecuerdoTimelineDTO>> timeline(
            Authentication auth,
            @RequestParam(value = "etiqueta", required = false) String etiqueta) {

        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        List<RecuerdoTimelineDTO> timeline = recuerdoService.obtenerLineaTiempo(usuario, etiqueta);

        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/timeline")
    public ResponseEntity<Page<RecuerdoTimelineDTO>> timeline(
            Authentication auth,
            @RequestParam(value = "etiqueta", required = false) String etiqueta,
            @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {

        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        Page<RecuerdoTimelineDTO> timeline = recuerdoService.obtenerLineaTiempo(usuario, etiqueta, pageable);
        return ResponseEntity.ok(timeline);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recuerdo> actualizarRecuerdo(
            @PathVariable Long id,
            @RequestParam("texto") String texto,
            @RequestParam(value = "etiquetas", required = false) List<String> etiquetas,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            Authentication auth) throws IOException {

        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        Recuerdo recuerdo = recuerdoService.obtenerRecuerdoPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!recuerdo.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes editar este recuerdo");
        }

        recuerdo.setTexto(texto);

        if (etiquetas != null) {
            recuerdo.setEtiquetas(Set.copyOf(etiquetas));
        }

        if (imagen != null && !imagen.isEmpty()) {
            String nombreArchivo = fileStorageService.guardarArchivo(imagen);
            recuerdo.setImagen(nombreArchivo);
        }

        Recuerdo actualizado = recuerdoService.crearRecuerdo(recuerdo);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<Void> eliminarImagen(
            @PathVariable Long id,
            Authentication auth) {

        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        Recuerdo recuerdo = recuerdoService.obtenerRecuerdoPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!recuerdo.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar este recuerdo");
        }

        fileStorageService.eliminarArchivo(recuerdo.getImagen());
        recuerdo.setImagen(null);
        recuerdoService.crearRecuerdo(recuerdo);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRecuerdo(@PathVariable Long id, Authentication auth) {
        Recuerdo r = recuerdoService.obtenerRecuerdoPorId(id).orElseThrow();
        if (!r.getUsuario().getEmail().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }
        recuerdoService.eliminarRecuerdo(id);
        return ResponseEntity.noContent().build();
    }
}
