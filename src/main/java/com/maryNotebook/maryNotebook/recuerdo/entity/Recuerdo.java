package com.maryNotebook.maryNotebook.recuerdo.entity;

import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recuerdos")
public class Recuerdo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String texto;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private String imagen; // puede ser URL o path en storage

    @ElementCollection
    @CollectionTable(name = "recuerdo_etiquetas", joinColumns = @JoinColumn(name = "recuerdo_id"))
    @Column(name = "etiqueta")
    private Set<String> etiquetas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}