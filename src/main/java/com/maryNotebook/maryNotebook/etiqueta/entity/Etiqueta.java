package com.maryNotebook.maryNotebook.etiqueta.entity;

import com.maryNotebook.maryNotebook.recuerdo.entity.Recuerdo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la etiqueta no puede estar vacío")
    @Size(max = 30, message = "El nombre de la etiqueta no puede tener más de 30 caracteres")
    @Column(unique = true)
    private String nombre;

    @ManyToMany(mappedBy = "etiquetas")
    private Set<Recuerdo> recuerdos;
}
