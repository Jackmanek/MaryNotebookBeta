package com.maryNotebook.maryNotebook.recuerdo.dto;

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
    private String imagenUrl;
}