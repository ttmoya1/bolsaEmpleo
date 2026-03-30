package org.example.bolsaempleo.logic;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "oferente_habilidad",
        uniqueConstraints = @UniqueConstraint(columnNames = {"oferente_id", "caracteristica_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OferenteHabilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oferente_id", nullable = false)
    private Oferente oferente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "caracteristica_id", nullable = false)
    private Caracteristica caracteristica;


    @Column(nullable = false)
    private int nivel = 1;
}