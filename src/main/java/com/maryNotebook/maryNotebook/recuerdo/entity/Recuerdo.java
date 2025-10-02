package com.maryNotebook.maryNotebook.recuerdo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maryNotebook.maryNotebook.etiqueta.entity.Etiqueta;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recuerdos")
@EqualsAndHashCode(exclude = {"etiquetas", "usuario"})  // ✅ AÑADIR ESTO
@ToString(exclude = {"etiquetas", "usuario"})
public class Recuerdo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    @Size(max = 2000, message = "El texto no puede tener más de 2000 caracteres")
    private String texto;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private String imagen; // puede ser URL o path en storage

    // ✅ NUEVO: Campo de visibilidad
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Visibilidad visibilidad = Visibilidad.PRIVADO;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "recuerdo_etiqueta",
            joinColumns = @JoinColumn(name = "recuerdo_id"),
            inverseJoinColumns = @JoinColumn(name = "etiqueta_id")
    )
    @JsonIgnoreProperties("recuerdos")
    private Set<Etiqueta> etiquetas = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    // ✅ NUEVO: Enum de visibilidad
    public enum Visibilidad {
        PRIVADO,  // Solo el dueño lo ve
        PUBLICO   // Todos lo ven en el home
    }
}