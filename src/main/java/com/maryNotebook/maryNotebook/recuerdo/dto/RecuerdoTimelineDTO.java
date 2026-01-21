package com.maryNotebook.maryNotebook.recuerdo.dto;

import com.maryNotebook.maryNotebook.recuerdo.entity.Recuerdo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
public class RecuerdoTimelineDTO {
    private Long id;
    private String texto;
    private LocalDateTime fecha;
    private Set<String> etiquetas;
    private String imagen;
    private Recuerdo.Visibilidad visibilidad;
    private String nombreUsuario;  // Añadir este campo
    private Long usuarioId;         // Añadir este campo

    public RecuerdoTimelineDTO(Long id, String texto, LocalDateTime fecha, Set<String> etiquetas, String imagen, Recuerdo.Visibilidad visibilidad) {
        this.id = id;
        this.texto = texto;
        this.fecha = fecha;
        this.etiquetas = etiquetas;
        this.imagen = imagen;
        this.visibilidad = visibilidad;
        this.nombreUsuario = null;
        this.usuarioId = null;
    }

    public RecuerdoTimelineDTO() {
    }
}
