package com.example.project.repository.admin;

import com.example.project.entity.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface EspacioRepository extends JpaRepository<Espacio, Integer> {

    @Query(value ="""
        SELECT
            e.idEspacio AS idEspacio,
            e.nombre AS nombre,
            l.lugar AS nombreLugar,
            es.estado AS estadoEspacio,
            e.tipo AS tipo,
            e.costo AS costo
        FROM
            Espacio e
        LEFT JOIN Lugar l ON e.idLugar = l.idLugar
        LEFT JOIN EstadoEspacio es ON e.idEstadoEspacio = es.idEstadoEspacio
    """,nativeQuery = true)
    List<EspacioDto> findAllEspacioDtos();
}
