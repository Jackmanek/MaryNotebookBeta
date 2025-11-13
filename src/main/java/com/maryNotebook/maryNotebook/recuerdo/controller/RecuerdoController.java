package com.maryNotebook.maryNotebook.recuerdo.controller;

import com.maryNotebook.maryNotebook.etiqueta.entity.Etiqueta;
import com.maryNotebook.maryNotebook.etiqueta.repository.EtiquetaRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recuerdos")
@RequiredArgsConstructor
public class RecuerdoController {


    private final RecuerdoService recuerdoService;
    private final UsuarioRepository usuarioRepository;
    private final FileStorageService fileStorageService;
    private final EtiquetaRepository etiquetaRepository;


    private Set<Etiqueta> procesarEtiquetas(List<String> etiquetas) {
        if (etiquetas == null  || etiquetas.isEmpty()) {
            return new HashSet<>();
        }

        return etiquetas.stream()
                .filter(nombre -> nombre != null && !nombre.trim().isEmpty())
                .map(nombre ->etiquetaRepository.findByNombre(nombre.trim())
                        .orElseGet(()->{
                            Etiqueta nueva = new Etiqueta();
                            nueva.setNombre(nombre.trim());
                            return etiquetaRepository.save(nueva);
                        }))
                .collect(Collectors.toSet());
    }

    @PostMapping
    public ResponseEntity<Recuerdo> crearRecuerdo(@Valid @RequestBody Recuerdo recuerdo, Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        recuerdo.setUsuario(usuario);

        // Procesar etiquetas recibidas
        if (recuerdo.getEtiquetas() != null) {
            Set<Etiqueta> etiquetasFinales = procesarEtiquetas(
                    recuerdo.getEtiquetas().stream().map(Etiqueta::getNombre).toList()
            );
            recuerdo.setEtiquetas(etiquetasFinales);
        }

        Recuerdo nuevo = recuerdoService.crearRecuerdo(recuerdo);
        return ResponseEntity.ok(nuevo);
    }

    // 🔹 Crear recuerdo con imagen
    @PostMapping("/con-imagen")
    public ResponseEntity<Recuerdo> crearRecuerdoConImagen(
            @RequestParam("texto") String texto,
            @RequestParam(value = "etiquetas", required = false) List<String> etiquetas,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam(value = "visibilidad", defaultValue = "PRIVADO") String visibilidad, // ✅ NUEVO
            Authentication auth) throws IOException {

        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        Recuerdo recuerdo = new Recuerdo();
        recuerdo.setTexto(texto);
        recuerdo.setUsuario(usuario);
        recuerdo.setEtiquetas(procesarEtiquetas(etiquetas));

        try {
            recuerdo.setVisibilidad(Recuerdo.Visibilidad.valueOf(visibilidad.toUpperCase()));
        } catch (IllegalArgumentException e) {
            recuerdo.setVisibilidad(Recuerdo.Visibilidad.PRIVADO);
        }

        if (imagen != null && !imagen.isEmpty()) {
            String urlImagen = fileStorageService.guardarArchivo(imagen);
            recuerdo.setImagen(urlImagen);
        }

        Recuerdo nuevo = recuerdoService.crearRecuerdo(recuerdo);
        return ResponseEntity.ok(nuevo);
    }

    // 🔹 Listar recuerdos (todos o filtrados por etiqueta)
    @GetMapping
    public ResponseEntity<List<Recuerdo>> listarRecuerdos(Authentication auth,
                                                          @RequestParam(required = false) String etiqueta) {
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

    // 🔹 Obtener recuerdo por ID
    @GetMapping("/{id}")
    public ResponseEntity<Recuerdo> obtenerRecuerdo(@PathVariable Long id, Authentication auth) {
        Recuerdo r = recuerdoService.obtenerRecuerdoPorId(id).orElseThrow();
        if (!r.getUsuario().getEmail().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(r);
    }

    // 🔹 Timeline simple
    @GetMapping("/timeline/simple")
    public ResponseEntity<List<RecuerdoTimelineDTO>> timelineSimple(
            Authentication auth,
            @RequestParam(value = "etiqueta", required = false) String etiqueta) {

        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        List<RecuerdoTimelineDTO> timeline = recuerdoService.obtenerLineaTiempo(usuario, etiqueta);
        return ResponseEntity.ok(timeline);
    }

    // 🔹 Timeline paginado
    @GetMapping("/timeline")
    public ResponseEntity<Page<RecuerdoTimelineDTO>> timelinePaginado(
            Authentication auth,
            @RequestParam(value = "etiqueta", required = false) String etiqueta,
            @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {

        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        Page<RecuerdoTimelineDTO> timeline = recuerdoService.obtenerLineaTiempo(usuario, etiqueta, pageable);
        return ResponseEntity.ok(timeline);
    }

    // 🔹 Actualizar recuerdo
    @PutMapping("/{id}")
    public ResponseEntity<Recuerdo> actualizarRecuerdo(
            @PathVariable Long id,
            @RequestParam("texto") String texto,
            @RequestParam(value = "etiquetas", required = false) List<String> etiquetas,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam(value = "visibilidad" , required = false) String visibilidad,
            Authentication auth) throws IOException {

        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        Recuerdo recuerdo = recuerdoService.obtenerRecuerdoPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!recuerdo.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes editar este recuerdo");
        }

        recuerdo.setTexto(texto);
        recuerdo.setEtiquetas(procesarEtiquetas(etiquetas));

        // ✅ NUEVO: Actualizar visibilidad si se proporciona
        if (visibilidad != null && !visibilidad.isEmpty()) {
            try {
                recuerdo.setVisibilidad(Recuerdo.Visibilidad.valueOf(visibilidad.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Si el valor no es válido, mantiene la visibilidad actual
            }
        }

        if (imagen != null && !imagen.isEmpty()) {
            String nombreArchivo = fileStorageService.guardarArchivo(imagen);
            recuerdo.setImagen(nombreArchivo);
        }

        Recuerdo actualizado = recuerdoService.crearRecuerdo(recuerdo);
        return ResponseEntity.ok(actualizado);
    }

    // 🔹 Eliminar imagen de un recuerdo
    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<Void> eliminarImagen(@PathVariable Long id, Authentication auth) {
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

    // 🔹 Eliminar recuerdo completo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRecuerdo(@PathVariable Long id, Authentication auth) {
        Recuerdo r = recuerdoService.obtenerRecuerdoPorId(id).orElseThrow();
        if (!r.getUsuario().getEmail().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        recuerdoService.eliminarRecuerdo(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ NUEVO: Feed público (home general) - SIN autenticación requerida
    @GetMapping("/publicos")
    public ResponseEntity<List<Recuerdo>> obtenerRecuerdosPublicos(
            @RequestParam(required = false) String etiqueta) {

        List<Recuerdo> recuerdos;
        if (etiqueta != null && !etiqueta.isEmpty()) {
            recuerdos = recuerdoService.obtenerFeedPublicoPorEtiqueta(etiqueta);
        } else {
            recuerdos = recuerdoService.obtenerFeedPublico();
        }
        return ResponseEntity.ok(recuerdos);
    }

    // ✅ NUEVO: Feed público paginado
    @GetMapping("/publicos/paginado")
    public ResponseEntity<Page<Recuerdo>> obtenerRecuerdosPublicosPaginado(
            @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Recuerdo> recuerdos = recuerdoService.obtenerFeedPublico(pageable);
        return ResponseEntity.ok(recuerdos);
    }
}
