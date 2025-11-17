package com.maryNotebook.maryNotebook.recuerdo.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record RecuerdoPublicoDto(
        Long id,
        String texto,
        LocalDateTime fecha,
        String imagen,
        String visibilidad,
        Set<String> etiquetas,
        String usuario
) {}
