package com.maryNotebook.maryNotebook.etiqueta.service;

import com.maryNotebook.maryNotebook.etiqueta.entity.Etiqueta;
import com.maryNotebook.maryNotebook.etiqueta.repository.EtiquetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;

    // Crear nueva etiqueta (si no existe)
    public Etiqueta crearEtiqueta(String nombre) {
        return etiquetaRepository.findByNombre(nombre)
                .orElseGet(() -> etiquetaRepository.save(new Etiqueta(null, nombre, new HashSet<>())));
    }

    // Listar todas las etiquetas
    public List<Etiqueta> listarEtiquetas() {
        return etiquetaRepository.findAll();
    }

    // Listar etiquetas por usuario (solo las que tienen recuerdos de ese usuario)
    public List<Etiqueta> listarEtiquetasPorUsuario(Long usuarioId) {
        return etiquetaRepository.findByUsuarioId(usuarioId);
    }
}