package org.example.bolsaempleo.logic;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "oferente")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Oferente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String identificacion;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String nombre;

    @NotBlank
    @Column(name = "primer_apellido", nullable = false, length = 80)
    private String primerApellido;

    @Column(length = 60)
    private String nacionalidad;

    @Column(length = 20)
    private String telefono;

    @Column(name = "lugar_residencia", length = 150)
    private String lugarResidencia;

    @Column(nullable = false)
    private boolean aprobado = false;

    /** Ruta relativa del PDF subido, ej: "curricula/123.pdf" */
    @Column(name = "curriculum_pdf", length = 255)
    private String curriculumPdf;
}