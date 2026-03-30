package org.example.bolsaempleo.logic;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aplicacion")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Aplicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "puesto_id", nullable = false)
    private Puesto puesto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oferente_id", nullable = false)
    private Oferente oferente;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();


    @Column(nullable = false, length = 10)
    private String estado = "PEN";
}