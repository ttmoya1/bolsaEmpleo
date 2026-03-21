package org.example.bolsaempleo.logic;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "puesto")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Puesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    // Double es tipo flotante en SQL → NO usar precision/scale (Hibernate 7 lo rechaza)
    private Double salario;

    /**
     * PUB = público (visible para todos)
     * PRI = privado (solo oferentes aprobados)
     */
    @Column(name = "tipo_publicacion", nullable = false, length = 3)
    private String tipoPublicacion = "PUB";

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToMany(mappedBy = "puesto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PuestoCaracteristica> caracteristicas = new ArrayList<>();
}