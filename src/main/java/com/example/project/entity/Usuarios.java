package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import javax.management.StringValueExp;
import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="Usuarios")
public class Usuarios implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "idUsuarios", nullable = false)
    private Integer idUsuarios;

    @Column(name = "nombres")
    private String nombres;

    @Column(name = "apellidos")
    private String apellidos;


    // --- Validación para DNI (Solo @NotNull para int, el resto manual) ---
    @Column(name = "dni", nullable = false) // DNI sigue siendo int
    @NotNull(message = "{usuario.dni.notblank}") // Para asegurar que no es null (0 no es null)
    @Min(value = 10000000, message = "{usuario.dni.size}") // Minimo 8 dígitos (si es int)
    @Max(value = 99999999, message = "{usuario.dni.size}") // Maximo 8 dígitos (si es int)
    private int dni;

    @Size(max = 100, message = "{usuario.correo.long}") // Mensaje si excede los 100 caracteres
    @Email(message = "{usuario.correo.invalid}")
    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "contrasena", length = 100)
    @NotBlank(message = "{usuario.contrasena.notblank}") // No puede estar en blanco
    @Size(min = 3, max = 100, message = "{usuario.contrasena.size}")
    private String contrasena;

    @Transient // Esta anotación indica a JPA que no se mapee a la base de datos
    @NotBlank(message = "{usuario.confirmContrasena.notblank}")
    private String confirmContrasena;

    @ManyToOne
    @JoinColumn(name = "rol")
    private Rol rol;

    @ManyToOne
    @JoinColumn(name="estado")
    private EstadoUsu estado;

    @Column(name = "telefono")
    private int telefono;

    @Column(name = "fechaCreacion")
    private Timestamp fechaCreacion;

}
