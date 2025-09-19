package com.maryNotebook.maryNotebook.etiqueta.service;

import com.maryNotebook.maryNotebook.etiqueta.entity.Etiqueta;
import com.maryNotebook.maryNotebook.etiqueta.repository.EtiquetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;

    public Etiqueta crearEtiqueta(Etiqueta etiqueta) {
        if (etiquetaRepository.existsByNombre(etiqueta.getNombre())) {
            throw new IllegalArgumentException("La etiqueta ya existe");
        }
        return etiquetaRepository.save(etiqueta);
    }

    public Optional<Etiqueta> buscarPorNombre(String nombre) {
        return etiquetaRepository.findByNombre(nombre);
    }
}