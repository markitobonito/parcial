package com.example.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.sql.Time;
import java.sql.Date;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "Reserva")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idReserva", nullable = false)
    private Integer idReserva;

    @Column(name="horaInicio")
    private Time horaInicio;

    @Column(name="horaFin")
    private Time horaFin;

    @Column(name="fecha")
    private Date fecha;

    @JoinColumn(name="coordinador")
    @ManyToOne
    private Usuarios coordinador;

    @Column(name="costo")
    private Double costo;

    @JoinColumn(name="vecino")
    @ManyToOne
    private Usuarios vecino;

    @JoinColumn(name="estado")
    @ManyToOne
    private EstadoReserva estado;

    @JoinColumn(name="espacio")
    @OneToOne
    private Espacio espacio;

    @Lob
    @Column(name="captura")
    private byte[] captura;

    @Column(name="momentoReserva")
    private Timestamp momentoReserva;

    @Column(name="tipoPago")
    private String tipoPago;
}
