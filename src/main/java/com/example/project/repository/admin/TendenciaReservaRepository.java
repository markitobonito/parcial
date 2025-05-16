package com.example.project.repository.admin;

import com.example.project.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
public interface TendenciaReservaRepository extends JpaRepository<Reserva, Integer> {

    @Query("SELECT " +
            "YEAR(r.fecha) AS anio, " +
            "MONTH(r.fecha) AS mes, " +
            "DAY(r.fecha) AS dia, " +
            "e.tipo AS tipoEspacio, " +
            "COUNT(r) AS totalReservas " +
            "FROM Reserva r JOIN r.espacio e " +
            "GROUP BY YEAR(r.fecha), MONTH(r.fecha), DAY(r.fecha), e.tipo " +
            "ORDER BY YEAR(r.fecha), MONTH(r.fecha), DAY(r.fecha), e.tipo")
    List<TendenciaReservaDTO> obtenerTendenciaReservas();

}
