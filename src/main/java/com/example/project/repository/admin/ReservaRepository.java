package com.example.project.repository.admin;

import com.example.project.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;


public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
    @Query(value = """
        WITH 
        ReservasPorTipo AS (
            SELECT 
                e.tipo,
                COUNT(r.idReserva) AS totalReservas
            FROM Espacio e
            LEFT JOIN Reserva r ON e.idEspacio = r.espacio
            WHERE e.tipo IN (
                'Cancha de fútbol-Grass Sintético',
                'Cancha de fútbol-Loza',
                'Piscina',
                'Gimnasio',
                'Pista de atletismo'
            ) AND e.idEstadoEspacio = 1
            GROUP BY e.tipo
        ),
        EspaciosActivosPorTipo AS (
            SELECT 
                tipo,
                COUNT(*) AS totalEspacios
            FROM Espacio
            WHERE tipo IN (
                'Cancha de fútbol-Grass Sintético',
                'Cancha de fútbol-Loza',
                'Piscina',
                'Gimnasio',
                'Pista de atletismo'
            ) AND idEstadoEspacio = 1
            GROUP BY tipo
        )
        SELECT 
            e.tipo AS tipo,
            COALESCE(r.totalReservas, 0) AS totalReservas,
            e.totalEspacios AS totalEspacios,
            ROUND((COALESCE(r.totalReservas, 0) * 100.0) / e.totalEspacios, 2) AS porcentajeUso
        FROM EspaciosActivosPorTipo e
        LEFT JOIN ReservasPorTipo r ON e.tipo = r.tipo
        ORDER BY porcentajeUso DESC
        """, nativeQuery = true)
    List<ReservaDto> obtenerResumenReservas();
    @Query(value = "select * from reserva where vecino=?1 and fecha>curdate(); ", nativeQuery = true)
    List<Reserva> findByIDUsuario(int idUsuario);
}
