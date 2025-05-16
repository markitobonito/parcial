package com.example.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Actividad")
public class Actividad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idActividad", nullable = false)
    private int idActividad;

    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private Usuarios usuario;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "detalle")
    private String detalle;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
}