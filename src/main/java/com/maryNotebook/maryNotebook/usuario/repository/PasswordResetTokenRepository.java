package com.maryNotebook.maryNotebook.usuario.repository;

import com.maryNotebook.maryNotebook.usuario.entity.PasswordResetToken;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUsuario(Usuario usuario); // para limpiar tokens viejos del mismo usuario

}
