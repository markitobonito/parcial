package com.example.project.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Espacio")
public class Espacio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idEspacio", nullable = false)
    private Integer idEspacio;
    @Column(name="nombre")
    private String nombre;

    @ManyToOne
    @JoinColumn(name="idLugar")
    private Lugar idLugar;

    @ManyToOne
    @JoinColumn(name="idEstadoEspacio")
    private EstadoEspacio idEstadoEspacio;

    @Column(name="observaciones", length = 1000)
    private String observaciones;

    @Column(name="tipo")
    private String tipo;

    @Column(name="costo")
    private Double costo;
    @Column(name="foto1")
    private byte[] foto1;
    @Column(name="foto2")
    private byte[] foto2;
    @Column(name="foto3")
    private byte[] foto3;

    @Column(name = "descripcion")
    private String descripcion;


    public List<String> getObservacionesComoLista() {
        if (this.observaciones == null || this.observaciones.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(this.observaciones.split("\\n"));
    }

}
