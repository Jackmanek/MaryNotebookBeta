package com.maryNotebook.maryNotebook.recuerdo.service;

import com.maryNotebook.maryNotebook.recuerdo.entity.Recuerdo;
import com.maryNotebook.maryNotebook.recuerdo.repository.RecuerdoRepository;
import com.maryNotebook.maryNotebook.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecuerdoService {


    private final RecuerdoRepository recuerdoRepository;


    public Recuerdo crearRecuerdo(Recuerdo recuerdo) {
        recuerdo.setFecha(LocalDateTime.now());
        return recuerdoRepository.save(recuerdo);
    }


    public List<Recuerdo> listarRecuerdosPorUsuario(Usuario usuario) {
        return recuerdoRepository.findByUsuarioOrderByFechaDesc(usuario);
    }

    public List<Recuerdo> listarRecuerdosPorEtiqueta(Usuario usuario, String etiqueta) {
        return recuerdoRepository.findByUsuarioAndEtiqueta(usuario, etiqueta);
    }

    public Optional<Recuerdo> obtenerRecuerdoPorId(Long id) {
        return recuerdoRepository.findById(id);
    }


    public void eliminarRecuerdo(Long id) {
        recuerdoRepository.deleteById(id);
    }
}
