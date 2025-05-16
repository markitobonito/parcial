package com.example.project.repository.coordinador;

import com.example.project.entity.Actividad;
import com.example.project.entity.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActividadRepository extends JpaRepository<Actividad, Integer> {
    List<Actividad> findByUsuarioOrderByFechaDesc(Usuarios usuario);
}