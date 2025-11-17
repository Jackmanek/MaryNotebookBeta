package com.maryNotebook.maryNotebook.recuerdo.service;

import com.maryNotebook.maryNotebook.etiqueta.entity.Etiqueta;
import com.maryNotebook.maryNotebook.etiqueta.repository.EtiquetaRepository;
import com.maryNotebook.maryNotebook.recuerdo.dto.RecuerdoPublicoDto;
import com.maryNotebook.maryNotebook.recuerdo.dto.RecuerdoTimelineDTO;
import com.maryNotebook.maryNotebook.recuerdo.entity.Recuerdo;
import com.maryNotebook.maryNotebook.recuerdo.repository.RecuerdoRepository;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecuerdoService {

    private final RecuerdoRepository recuerdoRepository;
    private final EtiquetaRepository etiquetaRepository;

    // 🔹 Crear o actualizar un recuerdo
    public Recuerdo crearRecuerdo(Recuerdo recuerdo) {
        // Validar etiquetas
        Set<Etiqueta> etiquetasFinales = new HashSet<>();
        if (recuerdo.getEtiquetas() != null) {
            for (Etiqueta etiqueta : recuerdo.getEtiquetas()) {
                Etiqueta e = etiquetaRepository.findByNombre(etiqueta.getNombre())
                        .orElseGet(() -> etiquetaRepository.save(
                                new Etiqueta(null, etiqueta.getNombre(), new HashSet<>())));
                etiquetasFinales.add(e);
            }
        }
        recuerdo.setEtiquetas(etiquetasFinales);

        // Si no tiene fecha, asignamos ahora
        if (recuerdo.getFecha() == null) {
            recuerdo.setFecha(LocalDateTime.now());
        }

        return recuerdoRepository.save(recuerdo);
    }

    // 🔹 Listar recuerdos de un usuario
    public List<Recuerdo> listarRecuerdosPorUsuario(Usuario usuario) {
        return recuerdoRepository.findByUsuarioOrderByFechaDesc(usuario);
    }

    // 🔹 Listar recuerdos de un usuario por etiqueta
    public List<Recuerdo> listarRecuerdosPorEtiqueta(Usuario usuario, String etiqueta) {
        return recuerdoRepository.findByUsuarioAndEtiqueta(usuario.getId(), etiqueta);
    }

    // 🔹 Obtener recuerdo por id
    public Optional<Recuerdo> obtenerRecuerdoPorId(Long id) {
        return recuerdoRepository.findById(id);
    }

    // 🔹 Eliminar recuerdo
    public void eliminarRecuerdo(Long id) {
        recuerdoRepository.deleteById(id);
    }

    // 🔹 Timeline no paginado
    public List<RecuerdoTimelineDTO> obtenerLineaTiempo(Usuario usuario, String etiqueta) {
        List<Recuerdo> recuerdos;

        if (etiqueta != null && !etiqueta.isEmpty()) {
            recuerdos = listarRecuerdosPorEtiqueta(usuario, etiqueta);
        } else {
            recuerdos = listarRecuerdosPorUsuario(usuario);
        }

        return recuerdos.stream()
                .map(r -> new RecuerdoTimelineDTO(
                        r.getId(),
                        r.getTexto(),
                        r.getFecha(),
                        r.getEtiquetas().stream().map(Etiqueta::getNombre).collect(Collectors.toSet()),
                        r.getImagen(),
                        r.getVisibilidad()
                ))
                .toList();
    }

    // 🔹 Timeline paginado
    public Page<RecuerdoTimelineDTO> obtenerLineaTiempo(Usuario usuario, String etiqueta, Pageable pageable) {
        Page<Recuerdo> recuerdos;

        if (etiqueta != null && !etiqueta.isEmpty()) {
            recuerdos = recuerdoRepository.findByUsuarioAndEtiqueta(usuario.getId(), etiqueta, pageable);
        } else {
            recuerdos = recuerdoRepository.findByUsuario(usuario, pageable);
        }

        return recuerdos.map(r -> new RecuerdoTimelineDTO(
                r.getId(),
                r.getTexto(),
                r.getFecha(),
                r.getEtiquetas().stream().map(Etiqueta::getNombre).collect(Collectors.toSet()),
                r.getImagen(),
                r.getVisibilidad()
        ));
    }

    // ✅ NUEVO: Obtener feed público
    public List<Recuerdo> obtenerFeedPublico() {
        return recuerdoRepository.findByVisibilidadOrderByFechaDesc(Recuerdo.Visibilidad.PUBLICO);
    }

    public Page<RecuerdoPublicoDto> obtenerFeedPublico(Pageable pageable) {

        Page<Recuerdo> recuerdos = recuerdoRepository.findByVisibilidadOrderByFechaDesc(
                Recuerdo.Visibilidad.PUBLICO,
                pageable
        );

        return recuerdos.map(r -> new RecuerdoPublicoDto(
                r.getId(),
                r.getTexto(),
                r.getFecha(),
                r.getImagen(),
                r.getVisibilidad().name(),
                r.getEtiquetas().stream().map(Etiqueta::getNombre).collect(Collectors.toSet()),
                r.getUsuario().getNombre()
        ));
    }

    // ✅ NUEVO: Feed público filtrado por etiqueta
    public List<Recuerdo> obtenerFeedPublicoPorEtiqueta(String etiqueta) {
        return recuerdoRepository.findByVisibilidadAndEtiqueta(Recuerdo.Visibilidad.PUBLICO, etiqueta);
    }
}
