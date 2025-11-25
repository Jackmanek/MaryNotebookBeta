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

    // 🔹 Listar recuerdos de un usuario ordenados por fecha DESC
    List<Recuerdo> findByUsuarioOrderByFechaDesc(Usuario usuario);

    // 🔹 Paginado: listar recuerdos de un usuario
    Page<Recuerdo> findByUsuario(Usuario usuario, Pageable pageable);

    // 🔹 Buscar recuerdos por etiqueta (sin paginar)
    @Query("SELECT r FROM Recuerdo r JOIN r.etiquetas e " +
            "WHERE r.usuario.id = :usuarioId AND e.nombre = :etiqueta " +
            "ORDER BY r.fecha DESC")
    List<Recuerdo> findByUsuarioAndEtiqueta(@Param("usuarioId") Long usuarioId,
                                            @Param("etiqueta") String etiqueta);

    // 🔹 Buscar recuerdos por etiqueta (paginado)
    @Query("SELECT r FROM Recuerdo r JOIN r.etiquetas e " +
            "WHERE r.usuario.id = :usuarioId AND e.nombre = :etiqueta " +
            "ORDER BY r.fecha DESC")
    Page<Recuerdo> findByUsuarioAndEtiqueta(@Param("usuarioId") Long usuarioId,
                                            @Param("etiqueta") String etiqueta,
                                            Pageable pageable);

    // ✅ NUEVO: Para obtener recuerdos públicos (feed/home)
    List<Recuerdo> findByVisibilidadOrderByFechaDesc(Recuerdo.Visibilidad visibilidad);

    Page<Recuerdo> findByVisibilidadOrderByFechaDesc(
            Recuerdo.Visibilidad visibilidad,
            Pageable pageable
    );

    // ✅ NUEVO: Recuerdos públicos con etiqueta específica
    @Query("SELECT r FROM Recuerdo r JOIN r.etiquetas e WHERE r.visibilidad = :visibilidad AND e.nombre = :etiqueta ORDER BY r.fecha DESC")
    List<Recuerdo> findByVisibilidadAndEtiqueta(
            @Param("visibilidad") Recuerdo.Visibilidad visibilidad,
            @Param("etiqueta") String etiqueta
    );

    @Query("SELECT r FROM Recuerdo r JOIN r.etiquetas e " +
            "WHERE r.visibilidad = :visibilidad AND e.nombre = :etiqueta " +
            "ORDER BY r.fecha DESC")
    Page<Recuerdo> findByVisibilidadAndEtiqueta(
            @Param("visibilidad") Recuerdo.Visibilidad visibilidad,
            @Param("etiqueta") String etiqueta,
            Pageable pageable
    );
}