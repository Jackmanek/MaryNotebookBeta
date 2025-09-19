package com.maryNotebook.maryNotebook.recuerdo.entity;

import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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
    @Size(max = 2000, message = "El texto no puede tener más de 2000 caracteres")
    private String texto;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private String imagen; // puede ser URL o path en storage

    @ElementCollection
    @Size(max = 10, message = "No puede tener más de 10 etiquetas")
    @CollectionTable(name = "recuerdo_etiquetas", joinColumns = @JoinColumn(name = "recuerdo_id"))
    @Column(name = "etiqueta")
    private Set<@Size(max = 30, message = "Cada etiqueta no puede tener más de 30 caracteres")String> etiquetas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}