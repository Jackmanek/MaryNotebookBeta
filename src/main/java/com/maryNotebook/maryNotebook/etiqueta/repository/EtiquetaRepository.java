package com.maryNotebook.maryNotebook.etiqueta.repository;

import com.maryNotebook.maryNotebook.etiqueta.entity.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {
    // Buscar etiqueta por nombre
    Optional<Etiqueta> findByNombre(String nombre);

    // Listar todas las etiquetas de un usuario
    @Query("SELECT DISTINCT e FROM Etiqueta e JOIN e.recuerdos r WHERE r.usuario.id = :usuarioId")
    List<Etiqueta> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}