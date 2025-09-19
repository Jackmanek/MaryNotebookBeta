package com.maryNotebook.maryNotebook.recuerdo.service;

import com.maryNotebook.maryNotebook.recuerdo.dto.RecuerdoTimelineDTO;
import com.maryNotebook.maryNotebook.recuerdo.entity.Recuerdo;
import com.maryNotebook.maryNotebook.recuerdo.repository.RecuerdoRepository;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecuerdoService {


    private final RecuerdoRepository recuerdoRepository;


    public Recuerdo crearRecuerdo(Recuerdo recuerdo) {
        recuerdo.setFecha(LocalDateTime.now());
        return recuerdoRepository.save(recuerdo);
    }


    public List<Recuerdo> listarRecuerdosPorUsuario(Usuario usuario) {
        return recuerdoRepository.findByUsuarioOrderByFechaDesc(usuario);
    }

    public List<Recuerdo> listarRecuerdosPorEtiqueta(Usuario usuario, String etiqueta) {
        return recuerdoRepository.findByUsuarioAndEtiqueta(usuario, etiqueta);
    }

    public Optional<Recuerdo> obtenerRecuerdoPorId(Long id) {
        return recuerdoRepository.findById(id);
    }

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
                        r.getEtiquetas(),
                        r.getImagen() != null ? r.getImagen() : null
                ))
                .toList();
    }

    public Page<RecuerdoTimelineDTO> obtenerLineaTiempo(Usuario usuario, String etiqueta, Pageable pageable) {
        Page<Recuerdo> recuerdos;

        if (etiqueta != null && !etiqueta.isEmpty()) {
            recuerdos = recuerdoRepository.findByUsuarioAndEtiquetasContainingOrderByFechaDesc(usuario, etiqueta, pageable);
        } else {
            recuerdos = recuerdoRepository.findByUsuarioOrderByFechaDesc(usuario, pageable);
        }

        return recuerdos.map(r -> new RecuerdoTimelineDTO(
                r.getId(),
                r.getTexto(),
                r.getFecha(),
                r.getEtiquetas(),
                r.getImagen()
        ));
    }

    public void eliminarRecuerdo(Long id) {
        recuerdoRepository.deleteById(id);
    }
}
