package com.maryNotebook.maryNotebook.etiqueta.repository;

import com.maryNotebook.maryNotebook.etiqueta.entity.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {
    Optional<Etiqueta> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}