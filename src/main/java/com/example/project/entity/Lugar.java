package com.example.project.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
@Table(name = "Lugar")
public class Lugar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idLugar", nullable = false)
    private Integer idLugar;

    @Column(name="lugar", nullable = false)
    private String lugar;
}
