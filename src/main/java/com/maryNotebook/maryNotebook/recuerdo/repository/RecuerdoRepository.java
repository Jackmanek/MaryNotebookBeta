package com.maryNotebook.maryNotebook.recuerdo.repository;

import com.maryNotebook.maryNotebook.recuerdo.entity.Recuerdo;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecuerdoRepository extends JpaRepository<Recuerdo, Long> {
    List<Recuerdo> findByUsuario(Usuario usuario);

    // Filtrar por etiqueta
    @Query("SELECT r FROM Recuerdo r JOIN r.etiquetas e WHERE r.usuario = :usuario AND e = :etiqueta ORDER BY r.fecha DESC")
    List<Recuerdo> findByUsuarioAndEtiqueta(@Param("usuario") Usuario usuario, @Param("etiqueta") String etiqueta);


    // Ordenar todos los recuerdos de un usuario por fecha descendente
    List<Recuerdo> findByUsuarioOrderByFechaDesc(Usuario usuario);

    Page<Recuerdo> findByUsuarioOrderByFechaDesc(Usuario usuario, Pageable pageable);
    Page<Recuerdo> findByUsuarioAndEtiquetasContainingOrderByFechaDesc(Usuario usuario, String etiqueta, Pageable pageable);

}