package com.example.project.repository.admin;

import java.util.List;

import com.example.project.entity.Espacio;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface Cant_espacioRepository extends CrudRepository<Espacio, Integer> {

    @Query(value = "SELECT tipo, COUNT(*) FROM Espacio WHERE tipo IN ('Cancha de fútbol-Grass Sintético', 'Gimnasio','Cancha de fútbol-Loza', 'Piscina', 'Pista de atletismo', 'Cancha de fútbol - Grass Sintético') GROUP BY tipo", nativeQuery = true)
    List<Object[]> contarPorTipoEspecifico();
}


