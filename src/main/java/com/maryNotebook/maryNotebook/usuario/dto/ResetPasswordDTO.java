package com.maryNotebook.maryNotebook.usuario.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    private String token;
    private String nuevaPassword;
}
