package com.maryNotebook.maryNotebook.etiqueta.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maryNotebook.maryNotebook.recuerdo.entity.Recuerdo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(exclude = "recuerdos")
@ToString(exclude = "recuerdos")
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la etiqueta no puede estar vacío")
    @Size(max = 30, message = "El nombre de la etiqueta no puede tener más de 30 caracteres")
    @Column(unique = true)
    private String nombre;

    @ManyToMany(mappedBy = "etiquetas")
    @JsonIgnore
    private Set<Recuerdo> recuerdos = new HashSet<>();
}
