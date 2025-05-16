package com.example.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.sql.Date;
import java.sql.Time;

@Getter
@Setter
@Entity
@Table(name = "Geolocalizacion")
public class Geolocalizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idGeolocalizacion", nullable = false)
    private Integer idGeolocalizacion;

    @Column(name="fecha")
    private Date fecha;

    @Column(name="horaInicio")
    private Time horaInicio;

    @Column(name="horaFin")
    private Time horaFin;

    @JoinColumn(name="coordinador")
    @OneToOne
    private Usuarios coordinador;

    @JoinColumn(name="espacio")
    @OneToOne
    private Espacio espacio;

    @Column(name="lugarExacto")
    private String lugarExacto;

    @Column(name="observacion")
    private String observacion;

    @ManyToOne
    @JoinColumn(name="estado")
    private EstadoGeo estado;
}
