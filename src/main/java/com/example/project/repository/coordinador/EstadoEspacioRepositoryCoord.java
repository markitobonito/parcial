package com.example.project.repository.coordinador;

import com.example.project.entity.EstadoEspacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoEspacioRepositoryCoord extends JpaRepository<EstadoEspacio, Integer> {
    Optional<EstadoEspacio> findByEstado(String estado);
}