package com.example.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "EstadoUsu")
public class EstadoUsu implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idEstado", nullable = false)
    private Integer idEstado;

    @Column(name="estado")
    private String estado;
}
