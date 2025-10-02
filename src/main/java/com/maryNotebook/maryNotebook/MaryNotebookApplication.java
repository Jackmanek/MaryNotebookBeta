package com.maryNotebook.maryNotebook;

import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import com.maryNotebook.maryNotebook.usuario.repository.UsuarioRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.xml.crypto.Data;
import java.time.LocalDateTime;

@SpringBootApplication
public class MaryNotebookApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaryNotebookApplication.class, args);
	}

	@Bean
	public ApplicationRunner configure (UsuarioRepository usuarioRepository, PasswordEncoder encoder){
		return env -> {

			String pass = "quieroser12";

			Usuario u = new Usuario();
			u.setNombre("Mari");
			u.setEmail("mari@gmail.com");
			u.setPassword(encoder.encode(pass));
			u.setFechaRegistro(LocalDateTime.now());
			u.setRol(Usuario.Rol.USER);
			usuarioRepository.save(u);

		};
	}

}
